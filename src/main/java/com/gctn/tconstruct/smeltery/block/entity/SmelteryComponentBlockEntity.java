/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.ChannelBlock;
import com.gctn.tconstruct.smeltery.events.SmelteryEvents;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Arrays;

public class SmelteryComponentBlockEntity extends BlockEntity {
    private BlockPos masterPos;
    private boolean dirty;
    private boolean powered;
    public boolean pouring;
    private boolean stopPouring;
    public FluidStack buffered = FluidStack.EMPTY;
    private int timer;
    private SmelteryRecipes.Casting recipe;
    // Connections use NONE=0, OUT=1, IN=2. Up is always input; down is an independent gate.
    private int[] connections = new int[6];
    private int locked;
    private int lastLight;
    public final int[] flow = new int[6];
    public final FluidTank tank;
    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot != 0 || !tank.isEmpty() || !getStackInSlot(1).isEmpty() ? stack : super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot != 1 || !tank.isEmpty() ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            changed();
        }
    };

    public SmelteryComponentBlockEntity(BlockPos pos, BlockState state) {
        super(SmelteryModule.COMPONENT_ENTITY.get(), pos, state);
        tank = new FluidTank(kind() == SmelteryBlock.Kind.CHANNEL ? 36 : kind() == SmelteryBlock.Kind.TANK ? 4000 : 0) {
            @Override
            protected void onContentsChanged() {
                changed();
            }
        };
        connections[Direction.UP.ordinal()] = 2;
    }

    public SmelteryBlock.Kind kind() {
        // 台阶和楼梯也是组成方块，但没有独立的储液或物品交互。
        return getBlockState().getBlock() instanceof SmelteryBlock block ? block.kind : SmelteryBlock.Kind.BRICK;
    }

    @Nullable
    public SmelteryMaster master() {
        if (masterPos == null || level == null || !level.hasChunkAt(masterPos)) {
            return null;
        }
        return level.getBlockEntity(masterPos) instanceof SmelteryMaster controller && controller.contains(worldPosition) ? controller : null;
    }

    public void assign(BlockPos pos) {
        if (!pos.equals(masterPos)) {
            masterPos = pos;
            level.invalidateCapabilities(worldPosition);
            changed();
        }
    }

    public void release(BlockPos pos) {
        if (pos.equals(masterPos)) {
            masterPos = null;
            level.invalidateCapabilities(worldPosition);
            changed();
        }
    }

    @Nullable
    public IFluidHandler fluidHandler(@Nullable Direction side) {
        return switch (kind()) {
            case TANK -> tank;
            case DRAIN -> master() == null ? null : side == null ? extractOnlyHandler : drainHandler;
            case TABLE, BASIN -> castingHandler;
            case CHANNEL -> side == null || connections[side.ordinal()] == 2 ? channelHandler : null;
            default -> null;
        };
    }

    @Nullable
    public IItemHandler itemHandler(@Nullable Direction side) {
        return kind() == SmelteryBlock.Kind.TABLE || kind() == SmelteryBlock.Kind.BASIN ? inventory : null;
    }

    public void tick() {
        updateLight();
        if (kind() == SmelteryBlock.Kind.FAUCET && pouring) {
            if (buffered.isEmpty()) {
                if (stopPouring || !beginTransfer()) {
                    pouring = false;
                    stopPouring = false;
                    level.playSound(null, worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F, 1.0F);
                    changed();
                }
            } else {
                pour();
            }
        } else if (kind() == SmelteryBlock.Kind.TABLE || kind() == SmelteryBlock.Kind.BASIN) {
            cast();
        } else if (kind() == SmelteryBlock.Kind.CHANNEL) {
            channelTick();
        }
        if (dirty) {
            dirty = false;
            if (kind() == SmelteryBlock.Kind.CHANNEL) {
                syncChannelState();
            }
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public void clientTick() {
        updateLight();
        if ((kind() == SmelteryBlock.Kind.TABLE || kind() == SmelteryBlock.Kind.BASIN) && !tank.isEmpty()) {
            if (recipe == null) {
                recipe = SmelteryRecipes.findCasting(kind() == SmelteryBlock.Kind.BASIN, inventory.getStackInSlot(0), tank.getFluid().getFluid());
            }
            if (recipe != null && tank.getFluidAmount() == tank.getCapacity()) {
                timer++;
                if (level.random.nextFloat() > 0.9F) {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, worldPosition.getX() + level.random.nextDouble(),
                            worldPosition.getY() + 1.1, worldPosition.getZ() + level.random.nextDouble(), 0, 0, 0);
                }
            }
        }
    }

    private void updateLight() {
        if (kind() == SmelteryBlock.Kind.TANK) {
            int light = tank.isEmpty() ? 0 : tank.getFluid().getFluidType().getLightLevel(tank.getFluid());
            if (light != lastLight) {
                lastLight = light;
                level.getLightEngine().checkBlock(worldPosition);
            }
        }
    }

    public float progress() {
        return recipe == null || tank.isEmpty() ? 0 : Math.min(1, (float) timer / Math.max(1, recipe.time()));
    }

    public ItemStack castingResult() {
        return recipe == null ? ItemStack.EMPTY : recipe.output().copy();
    }

    public void interact(Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind() == SmelteryBlock.Kind.FAUCET) {
            activate();
        } else if (kind() == SmelteryBlock.Kind.CHANNEL) {
            Direction side = hit.getDirection();
            var local = hit.getLocation().subtract(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
            if (local.x >= 0.3125 && local.x <= 0.6875 && local.z < 0.3125) { side = Direction.NORTH; }
            else if (local.x >= 0.3125 && local.x <= 0.6875 && local.z > 0.6875) { side = Direction.SOUTH; }
            else if (local.z >= 0.3125 && local.z <= 0.6875 && local.x < 0.3125) { side = Direction.WEST; }
            else if (local.z >= 0.3125 && local.z <= 0.6875 && local.x > 0.6875) { side = Direction.EAST; }
            if (side == Direction.UP) {
                side = Direction.DOWN;
            }
            toggleConnection(player, side);
        } else if (tank.isEmpty()) {
            ItemStack output = inventory.getStackInSlot(1);
            ItemStack input = inventory.getStackInSlot(0);
            if (!output.isEmpty() || !input.isEmpty()) {
                int slot = output.isEmpty() ? 0 : 1;
                player.getInventory().placeItemBackInInventory(inventory.getStackInSlot(slot).copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            } else if (!player.getItemInHand(hand).isEmpty()) {
                ItemStack placed = player.getItemInHand(hand).copyWithCount(1);
                player.getItemInHand(hand).shrink(1);
                inventory.setStackInSlot(0, placed);
            }
        }
    }

    public void activate() {
        if (pouring) {
            // A second click stops after the current 144 mB transaction has finished pouring.
            stopPouring = true;
            changed();
        } else if (beginTransfer()) {
            level.playSound(null, worldPosition, SmelteryModule.TRIGGER.get(), SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
    }

    private boolean beginTransfer() {
        Direction side = getBlockState().getValue(SmelteryBlock.FACING);
        IFluidHandler source = handler(worldPosition.relative(side), side.getOpposite());
        IFluidHandler target = handler(worldPosition.below(), Direction.UP);
        if (source == null || target == null) {
            return false;
        }
        FluidStack available = source.drain(144, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty() || !com.gctn.tconstruct.Config.DRAIN_GASEOUS_FLUIDS.get() && available.getFluidType().isLighterThanAir()) {
            return false;
        }
        int accepted = target.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted == 0) {
            return false;
        }
        buffered = source.drain(available.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
        pouring = !buffered.isEmpty();
        pour();
        changed();
        return pouring;
    }

    private void pour() {
        IFluidHandler target = handler(worldPosition.below(), Direction.UP);
        if (target == null) {
            // Matches Antique: removing the receiving block ends the pour and discards its buffer.
            buffered = FluidStack.EMPTY;
            pouring = false;
            stopPouring = false;
            changed();
            return;
        }
        if (!buffered.isEmpty()) {
            int filled = target.fill(buffered.copyWithAmount(Math.min(com.gctn.tconstruct.Config.LIQUID_TRANSFER_RATE.get(), buffered.getAmount())), IFluidHandler.FluidAction.EXECUTE);
            buffered.shrink(filled);
            setChanged();
        }
    }

    private void cast() {
        if (tank.isEmpty()) {
            return;
        }
        if (recipe == null) {
            recipe = SmelteryRecipes.findCasting(kind() == SmelteryBlock.Kind.BASIN, inventory.getStackInSlot(0), tank.getFluid().getFluid());
        }
        if (recipe == null || tank.getFluidAmount() < recipe.amount()) {
            return;
        }
        if (++timer >= recipe.time()) {
            SmelteryEvents.Casted event = NeoForge.EVENT_BUS.post(new SmelteryEvents.Casted(this, recipe));
            ItemStack input = inventory.getStackInSlot(0);
            if (event.consumeCast) {
                input = ItemStack.EMPTY;
            }
            if (event.switchOutputs) {
                inventory.setStackInSlot(1, input);
                inventory.setStackInSlot(0, event.output.copy());
            } else {
                inventory.setStackInSlot(0, input);
                inventory.setStackInSlot(1, event.output.copy());
            }
            tank.setFluid(FluidStack.EMPTY);
            tank.setCapacity(0);
            timer = 0;
            recipe = null;
            level.playSound(null, worldPosition, SmelteryModule.SIZZLE.get(), SoundSource.AMBIENT, 0.5F, 4.0F);
            changed();
        }
        setChanged();
    }

    private void channelTick() {
        int available = Math.max(0, tank.getFluidAmount() - locked);
        if (available > 0) {
            int transferRate = com.gctn.tconstruct.Config.LIQUID_TRANSFER_RATE.get();
            boolean down = connections[Direction.DOWN.ordinal()] == 1 && transferChannel(Direction.DOWN, Math.min(transferRate, available));
            if (!down) {
                int outputs = (int) Direction.Plane.HORIZONTAL.stream().filter(side -> connections[side.ordinal()] == 1).count();
                if (outputs > 0) {
                    int rate = Math.max(1, Math.min(transferRate, available / outputs));
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        if (connections[side.ordinal()] == 1) {
                            transferChannel(side, Math.min(rate, Math.max(0, tank.getFluidAmount() - locked)));
                        }
                    }
                }
            }
        }
        for (int i = 0; i < flow.length; i++) {
            if (flow[i] > 0 && --flow[i] == 0) {
                changed();
            }
        }
        // Newly inserted fluid waits for a full tick; this avoids tick-order-dependent channel chains.
        locked = 0;
    }

    private boolean transferChannel(Direction side, int amount) {
        IFluidHandler target = handler(worldPosition.relative(side), side.getOpposite());
        if (target == null || amount <= 0 || tank.isEmpty()) {
            return false;
        }
        int filled = target.fill(tank.getFluid().copyWithAmount(amount), IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0) {
            tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            flow[side.ordinal()] = 3;
            changed();
        }
        return filled > 0;
    }

    private void toggleConnection(Player player, Direction side) {
        IFluidHandler neighbor = handler(worldPosition.relative(side), side.getOpposite());
        BlockEntity other = level.getBlockEntity(worldPosition.relative(side));
        if (neighbor == null && !(other instanceof SmelteryComponentBlockEntity component && component.kind() == SmelteryBlock.Kind.CHANNEL)) {
            if (side != Direction.DOWN) {
                toggleConnection(player, Direction.DOWN);
            }
            return;
        }
        int next = side == Direction.DOWN ? connections[side.ordinal()] == 1 ? 0 : 1
                : (connections[side.ordinal()] + (player.isShiftKeyDown() ? 2 : 1)) % 3;
        connections[side.ordinal()] = next;
        if (side.getAxis().isHorizontal() && other instanceof SmelteryComponentBlockEntity component && component.kind() == SmelteryBlock.Kind.CHANNEL) {
            component.connections[side.getOpposite().ordinal()] = next == 0 ? 0 : next == 1 ? 2 : 1;
            component.changed();
            level.invalidateCapabilities(component.worldPosition);
        }
        level.invalidateCapabilities(worldPosition);
        changed();
        player.displayClientMessage(Component.translatable(side == Direction.DOWN ? next == 1 ? "channel.down.allow" : "channel.down.disallow"
                : next == 0 ? "channel.none" : next == 1 ? "channel.out" : "channel.in"), true);
    }

    public int connection(Direction side) {
        return connections[side.ordinal()];
    }

    public void placed(LivingEntity placer) {
        if (level == null || level.isClientSide || kind() != SmelteryBlock.Kind.CHANNEL) {
            return;
        }
        Direction side = getBlockState().getValue(SmelteryBlock.FACING);
        BlockEntity other = level.getBlockEntity(worldPosition.relative(side));
        if (other instanceof SmelteryComponentBlockEntity component && component.kind() == SmelteryBlock.Kind.CHANNEL) {
            if (side == Direction.UP) {
                component.connections[Direction.DOWN.ordinal()] = 1;
            } else if (side == Direction.DOWN) {
                connections[Direction.DOWN.ordinal()] = 1;
            } else {
                connections[side.ordinal()] = placer.isShiftKeyDown() ? 1 : 2;
                component.connections[side.getOpposite().ordinal()] = placer.isShiftKeyDown() ? 2 : 1;
            }
            component.changed();
            level.invalidateCapabilities(component.worldPosition);
        } else if (side != Direction.UP && handler(worldPosition.relative(side), side.getOpposite()) != null) {
            connections[side.ordinal()] = 1;
        }
        powered = level.hasNeighborSignal(worldPosition);
        level.invalidateCapabilities(worldPosition);
        changed();
    }

    public void neighborChanged(BlockPos from) {
        SmelteryMaster controller = master();
        if (controller != null) {
            controller.requestCheck();
        }
        boolean signal = level.hasNeighborSignal(worldPosition);
        if (kind() == SmelteryBlock.Kind.CHANNEL) {
            for (Direction side : Direction.values()) {
                if (side == Direction.UP || !worldPosition.relative(side).equals(from)) {
                    continue;
                }
                BlockEntity other = level.getBlockEntity(from);
                boolean channel = other instanceof SmelteryComponentBlockEntity component && component.kind() == SmelteryBlock.Kind.CHANNEL;
                IFluidHandler target = handler(from, side.getOpposite());
                if (target == null && !channel) {
                    connections[side.ordinal()] = 0;
                } else if (!channel && connections[side.ordinal()] == 0) {
                    connections[side.ordinal()] = 1;
                }
            }
            syncChannelState();
            level.invalidateCapabilities(worldPosition);
        }
        if (kind() == SmelteryBlock.Kind.FAUCET && signal && !powered) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 2);
        }
        if (kind() == SmelteryBlock.Kind.CHANNEL && signal != powered) {
            connections[Direction.DOWN.ordinal()] = signal && handler(worldPosition.below(), Direction.UP) != null ? 1 : 0;
            level.invalidateCapabilities(worldPosition);
            changed();
        }
        powered = signal;
        setChanged();
    }

    private void syncChannelState() {
        BlockState state = getBlockState().setValue(ChannelBlock.DOWN, connections[Direction.DOWN.ordinal()] == 1);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            int connection = connections[side.ordinal()];
            BlockState neighbor = level.getBlockState(worldPosition.relative(side));
            if (connection == 0 && (neighbor.getBlock() instanceof net.minecraft.world.level.block.LeverBlock
                    || neighbor.getBlock() instanceof net.minecraft.world.level.block.ButtonBlock)) {
                connection = 3;
            }
            state = state.setValue(ChannelBlock.property(side), connection);
        }
        if (!state.equals(getBlockState())) {
            level.setBlock(worldPosition, state, 2);
        }
    }

    @Nullable
    private IFluidHandler handler(BlockPos pos, Direction side) {
        return level != null && level.hasChunkAt(pos) ? level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side) : null;
    }

    public int comparator() {
        if (kind() == SmelteryBlock.Kind.TABLE || kind() == SmelteryBlock.Kind.BASIN) {
            return inventory.getStackInSlot(1).isEmpty() ? 0 : 15;
        }
        if (kind() == SmelteryBlock.Kind.DRAIN) {
            SmelteryMaster controller = master();
            return controller == null || controller.liquids().amount() == 0 ? 0
                    : Math.min(15, 1 + (int) (14L * controller.liquids().amount() / Math.max(1, controller.liquids().capacity())));
        }
        return tank.isEmpty() ? 0 : 1 + 14 * tank.getFluidAmount() / Math.max(1, tank.getCapacity());
    }

    public void onBroken() {
        SmelteryMaster controller = master();
        if (controller != null) {
            controller.requestCheck();
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inventory.getStackInSlot(i));
        }
    }

    public void changed() {
        setChanged();
        dirty = true;
    }

    private final IFluidHandler drainHandler = new IFluidHandler() {
        private IFluidHandler target() {
            SmelteryMaster controller = master();
            return controller == null ? null : controller.liquids();
        }

        public int getTanks() { return target() == null ? 1 : target().getTanks(); }
        public FluidStack getFluidInTank(int index) { return target() == null ? FluidStack.EMPTY : target().getFluidInTank(index); }
        public int getTankCapacity(int index) { return target() == null ? 0 : target().getTankCapacity(index); }
        public boolean isFluidValid(int index, FluidStack stack) { return target() != null && target().isFluidValid(index, stack); }
        public int fill(FluidStack stack, FluidAction action) { return target() == null ? 0 : target().fill(stack, action); }
        public FluidStack drain(FluidStack stack, FluidAction action) { return target() == null ? FluidStack.EMPTY : target().drain(stack, action); }
        public FluidStack drain(int amount, FluidAction action) { return target() == null ? FluidStack.EMPTY : target().drain(amount, action); }
    };

    private final IFluidHandler castingHandler = new IFluidHandler() {
        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int index) { return tank.getFluid().copy(); }
        public int getTankCapacity(int index) { return tank.getCapacity(); }
        public boolean isFluidValid(int index, FluidStack stack) { return find(stack) != null; }

        private SmelteryRecipes.Casting find(FluidStack stack) {
            return stack.isEmpty() || !inventory.getStackInSlot(1).isEmpty() ? null : SmelteryRecipes.findCasting(
                    kind() == SmelteryBlock.Kind.BASIN, inventory.getStackInSlot(0), stack.getFluid());
        }

        public int fill(FluidStack stack, FluidAction action) {
            if (!tank.isEmpty() && !FluidStack.isSameFluidSameComponents(tank.getFluid(), stack)) {
                return 0;
            }
            SmelteryRecipes.Casting candidate = recipe == null ? find(stack) : recipe;
            if (candidate == null || NeoForge.EVENT_BUS.post(new SmelteryEvents.Casting(SmelteryComponentBlockEntity.this, candidate)).isCanceled()) {
                return 0;
            }
            int accepted = Math.max(0, Math.min(stack.getAmount(), candidate.amount() - tank.getFluidAmount()));
            if (action.execute() && accepted > 0) {
                recipe = candidate;
                tank.setCapacity(candidate.amount());
                tank.fill(stack.copyWithAmount(accepted), action);
            }
            return accepted;
        }

        // Casting is intentionally insert-only, including while a partially filled recipe is cooling.
        public FluidStack drain(FluidStack stack, FluidAction action) { return FluidStack.EMPTY; }
        public FluidStack drain(int amount, FluidAction action) { return FluidStack.EMPTY; }
    };

    private final IFluidHandler extractOnlyHandler = new IFluidHandler() {
        public int getTanks() { return drainHandler.getTanks(); }
        public FluidStack getFluidInTank(int index) { return drainHandler.getFluidInTank(index); }
        public int getTankCapacity(int index) { return drainHandler.getTankCapacity(index); }
        public boolean isFluidValid(int index, FluidStack stack) { return false; }
        public int fill(FluidStack stack, FluidAction action) { return 0; }
        public FluidStack drain(FluidStack stack, FluidAction action) { return drainHandler.drain(stack, action); }
        public FluidStack drain(int amount, FluidAction action) { return drainHandler.drain(amount, action); }
    };

    private final IFluidHandler channelHandler = new IFluidHandler() {
        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int index) { return tank.getFluid().copy(); }
        public int getTankCapacity(int index) { return 36; }
        public boolean isFluidValid(int index, FluidStack stack) { return true; }
        public int fill(FluidStack stack, FluidAction action) {
            int filled = tank.fill(stack, action);
            if (action.execute()) {
                locked += filled;
            }
            return filled;
        }
        public FluidStack drain(FluidStack stack, FluidAction action) { return FluidStack.EMPTY; }
        public FluidStack drain(int amount, FluidAction action) { return FluidStack.EMPTY; }
    };

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (masterPos != null) {
            tag.putLong("Master", masterPos.asLong());
        }
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Capacity", tank.getCapacity());
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("Buffered", buffered.saveOptional(registries));
        tag.putBoolean("Pouring", pouring);
        tag.putBoolean("Stop", stopPouring);
        tag.putBoolean("Powered", powered);
        tag.putInt("Timer", timer);
        tag.putIntArray("Connections", connections);
        tag.putIntArray("Flow", flow);
        tag.putInt("Locked", locked);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        masterPos = tag.contains("Master") ? BlockPos.of(tag.getLong("Master")) : null;
        tank.setCapacity(Math.max(0, tag.getInt("Capacity")));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        buffered = FluidStack.parseOptional(registries, tag.getCompound("Buffered"));
        pouring = tag.getBoolean("Pouring");
        stopPouring = tag.getBoolean("Stop");
        powered = tag.getBoolean("Powered");
        timer = tag.getInt("Timer");
        connections = Arrays.copyOf(tag.getIntArray("Connections"), 6);
        connections[Direction.UP.ordinal()] = 2;
        int[] savedFlow = tag.getIntArray("Flow");
        System.arraycopy(savedFlow, 0, flow, 0, Math.min(6, savedFlow.length));
        locked = Math.clamp(tag.getInt("Locked"), 0, tank.getFluidAmount());
        recipe = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (kind() == SmelteryBlock.Kind.TANK) {
            builder.set(SmelteryModule.TANK_FLUID.get(), net.neoforged.neoforge.fluids.SimpleFluidContent.copyOf(tank.getFluid()));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        if (kind() == SmelteryBlock.Kind.TANK) {
            tank.setFluid(input.getOrDefault(SmelteryModule.TANK_FLUID.get(), net.neoforged.neoforge.fluids.SimpleFluidContent.EMPTY).copy());
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
