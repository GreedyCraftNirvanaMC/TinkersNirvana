/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.SmelteryTank;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.events.SmelteryEvents;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.smeltery.multiblock.SmelteryStructure;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.Arrays;
import java.util.List;

public class SmelteryBlockEntity extends BaseContainerBlockEntity implements HeatingContainer, FluidSmelteryMaster {
    public static final int CAPACITY_PER_BLOCK = 144 * 8;
    private NonNullList<ItemStack> items = NonNullList.create();
    private int[] heat = new int[0];
    private int[] required = new int[0];
    private SmelteryStructure structure;
    private BlockPos min;
    private BlockPos max;
    private boolean active;
    private boolean heating;
    private boolean needsCheck = true;
    private boolean dirty;
    private int tick;
    private int fullCheck;
    private int insideCheck;
    private int fuel;
    private int temperature;
    private int fuelQuality;
    private boolean needsFuel;
    private FluidStack currentFuel = FluidStack.EMPTY;
    private BlockPos currentTank;
    public final SmelteryTank liquids = new SmelteryTank(this::changed);
    private final IItemHandler itemHandler = new InvWrapper(this);

    public SmelteryBlockEntity(BlockPos pos, BlockState state) {
        super(SmelteryModule.CONTROLLER_ENTITY.get(), pos, state);
    }

    public void tick() {
        if (needsCheck || tick == 0 && (!active || ++fullCheck >= 15)) {
            fullCheck = 0;
            needsCheck = false;
            checkStructure();
        }
        if (active()) {
            if (tick == 0) {
                checkInterior();
                if (active()) {
                    interactWithEntities();
                }
            }
            if (active() && tick % com.gctn.tconstruct.Config.SMELTERY_HEAT_TICK_RATE.get() == 0) {
                heatItems();
                alloy();
            }
            if (active() && needsFuel) {
                consumeFuel();
            }
        } else if (heating) {
            heating = false;
            changed();
        }
        if (dirty) {
            dirty = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
        tick = (tick + 1) % 20;
    }

    private void checkStructure() {
        // Never force-load neighbors or destroy stored contents merely because a chunk unloaded.
        if (min != null && max != null && !loaded(min.offset(-1, -1, -1), max.offset(1, 0, 1))) {
            return;
        }
        SmelteryStructure found = SmelteryStructure.detect(level, worldPosition, getBlockState().getValue(SmelteryBlock.FACING).getOpposite());
        if (found == null) {
            invalidateStructure();
            return;
        }
        if (found.equals(structure) && active) {
            assignComponents(level, found.blocks());
            return;
        }
        releaseComponents();
        structure = found;
        min = found.min();
        max = found.max();
        active = true;
        resize(found.size());
        liquids.setCapacity(found.size() * CAPACITY_PER_BLOCK);
        assignComponents(level, found.blocks());
        level.setBlock(worldPosition, getBlockState().setValue(SmelteryBlock.ACTIVE, true), 3);
        level.invalidateCapabilities(worldPosition);
        changed();
    }

    private boolean loaded(BlockPos from, BlockPos to) {
        for (int x = from.getX() >> 4; x <= to.getX() >> 4; x++) {
            for (int z = from.getZ() >> 4; z <= to.getZ() >> 4; z++) {
                if (!level.hasChunk(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkInterior() {
        int width = max.getX() - min.getX() + 1;
        int depth = max.getZ() - min.getZ() + 1;
        insideCheck %= Math.max(1, items.size());
        BlockPos pos = min.offset(insideCheck % width, insideCheck / (width * depth), insideCheck / width % depth);
        if (level.hasChunkAt(pos) && !level.isEmptyBlock(pos)) {
            invalidateStructure();
        }
        insideCheck++;
    }

    private void invalidateStructure() {
        if (!active && structure == null && items.isEmpty()) {
            return;
        }
        releaseComponents();
        active = false;
        heating = false;
        structure = null;
        resize(0);
        // Antique retains molten contents across invalidation and rejects further filling at zero capacity.
        liquids.setCapacity(0);
        level.setBlock(worldPosition, getBlockState().setValue(SmelteryBlock.ACTIVE, false), 3);
        level.invalidateCapabilities(worldPosition);
        changed();
    }

    private void resize(int size) {
        NonNullList<ItemStack> resized = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            if (i < size) {
                resized.set(i, items.get(i));
            } else if (!items.get(i).isEmpty()) {
                BlockPos outside = worldPosition.relative(getBlockState().getValue(SmelteryBlock.FACING));
                Containers.dropItemStack(level, outside.getX(), outside.getY(), outside.getZ(), items.get(i));
            }
        }
        items = resized;
        heat = Arrays.copyOf(heat, size);
        required = Arrays.copyOf(required, size);
        for (int i = 0; i < size; i++) {
            updateRequired(i);
        }
    }

    public void releaseComponents() {
        if (structure == null || level == null) {
            return;
        }
        for (BlockPos pos : structure.blocks()) {
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                component.release(worldPosition);
            }
        }
    }

    public void requestCheck() {
        needsCheck = true;
    }

    @Override
    public SmelteryTank liquids() {
        return liquids;
    }

    public boolean contains(BlockPos pos) {
        return active() && structure != null && structure.blocks().contains(pos);
    }

    public boolean active() {
        return active && (level == null || level.isClientSide || structure != null);
    }

    public boolean heating() {
        return heating && active();
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }

    public IItemHandler itemHandler() {
        return itemHandler;
    }

    private void heatItems() {
        boolean heated = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                heat[i] = 0;
                continue;
            }
            updateRequired(i);
            if (required[i] == 0) {
                continue;
            }
            if (fuel <= 0) {
                needsFuel = true;
                break;
            }
            if (temperature >= required[i] / 8) {
                if (heat[i] >= required[i]) {
                    melt(i);
                } else {
                    heat[i] += temperature / 100;
                    heated = true;
                }
            }
        }
        // Antique consumes one unit per heating pass, regardless of how many slots were heated.
        if (heated) {
            fuel--;
            setChanged();
        }
        if (heated != heating) {
            heating = heated;
            changed();
        }
    }

    private void melt(int slot) {
        SmelteryRecipes.Melting recipe = SmelteryRecipes.findMelting(items.get(slot));
        if (recipe == null) {
            return;
        }
        SmelteryEvents.Melting event = NeoForge.EVENT_BUS.post(new SmelteryEvents.Melting(this, items.get(slot), recipe.output()));
        if (event.isCanceled()) {
            return;
        }
        if (event.result != null && !event.result.isEmpty() && liquids.fill(event.result, FluidAction.SIMULATE) == event.result.getAmount()) {
            liquids.fill(event.result, FluidAction.EXECUTE);
            setItem(slot, ItemStack.EMPTY);
        } else {
            heat[slot] = required[slot] * 2 + 1;
        }
    }

    private void consumeFuel() {
        if (fuel > 0 || structure == null) {
            return;
        }
        // Keep using the last fuel type before considering another tank/type.
        for (int pass = 0; pass < 2; pass++) {
            List<BlockPos> tanks = new java.util.ArrayList<>(structure.tanks());
            if (currentTank != null && tanks.remove(currentTank)) {
                tanks.addFirst(currentTank);
            }
            for (BlockPos pos : tanks) {
                if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                    FluidStack fluid = component.tank.getFluid();
                    SmelteryRecipes.Fuel recipe = SmelteryRecipes.fuel(fluid);
                    if (recipe == null || pass == 0 && !currentFuel.isEmpty() && !FluidStack.isSameFluidSameComponents(currentFuel, fluid)) {
                        continue;
                    }
                    currentFuel = component.tank.drain(recipe.amount(), FluidAction.EXECUTE);
                    currentTank = pos;
                    fuelQuality = recipe.duration();
                    fuel = fuelQuality;
                    needsFuel = false;
                    temperature = currentFuel.getFluidType().getTemperature(currentFuel) - 300;
                    changed();
                    return;
                }
            }
        }
    }

    private void alloy() {
        if (liquids.amount() > liquids.capacity()) {
            return;
        }
        for (SmelteryRecipes.Alloy recipe : SmelteryRecipes.ALLOYS) {
            if (recipe.output().is(SmelteryFluids.get("obsidian")) && !com.gctn.tconstruct.Config.OBSIDIAN_ALLOY.get()) {
                continue;
            }
            int matched = Math.min(10, recipe.matches(liquids));
            while (matched > 0) {
                int removed = recipe.inputs().stream().mapToInt(FluidStack::getAmount).sum();
                if ((long) liquids.amount() - removed + recipe.output().getAmount() > liquids.capacity()) {
                    break;
                }
                // Simulate the whole transaction before removing any ingredient.
                if (recipe.inputs().stream().anyMatch(input -> liquids.drain(input, FluidAction.SIMULATE).getAmount() != input.getAmount())) {
                    break;
                }
                recipe.inputs().forEach(input -> liquids.drain(input, FluidAction.EXECUTE));
                liquids.fill(recipe.output(), FluidAction.EXECUTE);
                matched -= recipe.output().getAmount();
            }
        }
    }

    private void interactWithEntities() {
        AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        for (Entity entity : level.getEntities((Entity) null, bounds)) {
            if (entity instanceof ItemEntity item && SmelteryRecipes.findMelting(item.getItem()) != null) {
                ItemStack stack = item.getItem();
                for (int slot = 0; slot < items.size() && !stack.isEmpty(); slot++) {
                    if (items.get(slot).isEmpty()) {
                        setItem(slot, stack.split(1));
                    }
                }
                if (stack.isEmpty()) {
                    item.discard();
                }
            } else if (entity instanceof LivingEntity living && living.isAlive()) {
                if (fuel <= 0) {
                    needsFuel = true;
                } else if (living.hurt(level.damageSources().source(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DAMAGE_TYPE, SmelteryFluids.id("smeltery"))), 2.0F)) {
                    liquids.fill(SmelteryRecipes.entityMelting(living), FluidAction.EXECUTE);
                }
            }
        }
    }

    private void updateRequired(int slot) {
        SmelteryRecipes.Melting recipe = items.get(slot).isEmpty() ? null : SmelteryRecipes.findMelting(items.get(slot));
        required[slot] = recipe == null ? 0 : recipe.heat() * 8;
    }

    public int heat(int slot) {
        return heat[slot];
    }

    public int required(int slot) {
        return required[slot];
    }

    public int fuel() {
        return fuel;
    }

    @Override
    public int fuelQuality() {
        return fuelQuality;
    }

    public int temperature() {
        return temperature;
    }

    public FuelDisplay fuelDisplay() {
        return FuelDisplay.of(level, structure == null ? clientTanks : structure.tanks(), currentFuel, fuel);
    }

    public void changed() {
        setChanged();
        dirty = true;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!ItemStack.isSameItemSameComponents(items.get(slot), stack)) {
            heat[slot] = 0;
        }
        super.setItem(slot, stack);
        updateRequired(slot);
        if (!stack.isEmpty() && required[slot] > 0 && level != null && !level.isClientSide) {
            consumeFuel();
        }
        changed();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = super.removeItem(slot, amount);
        if (items.get(slot).isEmpty()) {
            heat[slot] = 0;
            required[slot] = 0;
        }
        changed();
        return result;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.smeltery.name");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SmelteryMenu(id, inventory, this);
    }

    private List<BlockPos> clientTanks = List.of();

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        com.gctn.tconstruct.smeltery.SmelteryInventory.save(tag, items, registries);
        tag.putInt("Size", items.size());
        tag.putIntArray("Heat", heat);
        tag.putIntArray("Required", required);
        saveDisplay(tag, registries);
    }

    private void saveDisplay(CompoundTag tag, HolderLookup.Provider registries) {
        liquids.save(tag, registries);
        tag.putBoolean("Active", active);
        tag.putBoolean("Heating", heating);
        tag.putInt("Fuel", fuel);
        tag.putInt("FuelQuality", fuelQuality);
        tag.putBoolean("NeedsFuel", needsFuel);
        tag.putInt("Temperature", temperature);
        tag.put("CurrentFuel", currentFuel.saveOptional(registries));
        if (currentTank != null) {
            tag.putLong("CurrentTank", currentTank.asLong());
        }
        if (min != null && max != null) {
            tag.putLong("Min", min.asLong());
            tag.putLong("Max", max.asLong());
        }
        List<BlockPos> tanks = structure == null ? clientTanks : structure.tanks();
        tag.putLongArray("Tanks", tanks.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Size")) {
            int size = Math.clamp(tag.getInt("Size"), 0, 81 * 4096);
            items = NonNullList.withSize(size, ItemStack.EMPTY);
            com.gctn.tconstruct.smeltery.SmelteryInventory.load(tag, items, registries);
            heat = Arrays.copyOf(tag.getIntArray("Heat"), size);
            required = Arrays.copyOf(tag.getIntArray("Required"), size);
        }
        if (tag.contains("RenderSize")) {
            items = NonNullList.withSize(Math.clamp(tag.getInt("RenderSize"), 0, 81 * 4096), ItemStack.EMPTY);
            for (var entry : tag.getList("RenderItems", 10)) {
                CompoundTag item = (CompoundTag) entry;
                int slot = item.getInt("Slot");
                if (slot >= 0 && slot < items.size()) {
                    items.set(slot, ItemStack.parseOptional(registries, item.getCompound("Stack")));
                }
            }
            heat = new int[items.size()];
            required = new int[items.size()];
        }
        liquids.load(tag, registries);
        active = tag.getBoolean("Active");
        heating = tag.getBoolean("Heating");
        fuel = Math.max(0, tag.getInt("Fuel"));
        fuelQuality = tag.getInt("FuelQuality");
        needsFuel = tag.getBoolean("NeedsFuel");
        temperature = tag.getInt("Temperature");
        currentFuel = FluidStack.parseOptional(registries, tag.getCompound("CurrentFuel"));
        currentTank = tag.contains("CurrentTank") ? BlockPos.of(tag.getLong("CurrentTank")) : null;
        min = tag.contains("Min") ? BlockPos.of(tag.getLong("Min")) : null;
        max = tag.contains("Max") ? BlockPos.of(tag.getLong("Max")) : null;
        clientTanks = Arrays.stream(tag.getLongArray("Tanks")).mapToObj(BlockPos::of).toList();
        needsCheck = true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        // Only occupied slots are sent to world renderers. Heat remains in the open menu's data slots.
        saveDisplay(tag, registries);
        tag.putInt("RenderSize", items.size());
        net.minecraft.nbt.ListTag renderItems = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", i);
                entry.put("Stack", items.get(i).save(registries));
                renderItems.add(entry);
            }
        }
        tag.put("RenderItems", renderItems);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
