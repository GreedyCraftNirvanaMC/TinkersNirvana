/*
 * 根据 TinkersAntique 的 TileSearedFurnace、TileHeatingStructureFuelTank 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.SmelteryInventory;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.smeltery.multiblock.SearedFurnaceStructure;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 每槽最多十六个物品，整批烧制后将成品留在原槽，避免自动连续加工。 */
public class SearedFurnaceBlockEntity extends BaseContainerBlockEntity implements HeatingContainer {
    private NonNullList<ItemStack> items = NonNullList.create();
    private int[] heat = new int[0];
    private int[] required = new int[0];
    private SearedFurnaceStructure structure;
    private Set<BlockPos> members = Set.of();
    private BlockPos min;
    private BlockPos max;
    private boolean active;
    private boolean heating;
    private boolean needsCheck = true;
    private boolean needsFuel;
    private boolean dirty;
    private int tick;
    private int fullCheck;
    private int fuel;
    private int fuelQuality;
    private int temperature;
    private FluidStack currentFuel = FluidStack.EMPTY;
    private BlockPos currentTank;
    private List<BlockPos> clientTanks = List.of();
    private final IItemHandler itemHandler = new InvWrapper(this);

    public SearedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(SmelteryModule.FURNACE_ENTITY.get(), pos, state);
    }

    public void tick() {
        if (needsCheck || tick == 0 && (!active() || ++fullCheck >= 15)) {
            needsCheck = false;
            fullCheck = 0;
            checkStructure();
        }
        if (active()) {
            if (tick % Config.SEARED_FURNACE_HEAT_TICK_RATE.get() == 0) {
                heatItems();
            }
            if (needsFuel) {
                consumeFuel();
            }
            if (tick % 20 == 0) {
                // 与原版 EntityMob 对应，只清除熔炉内部的 Monster，不产生战利品。
                AABB bounds = new AABB(min.getX(), min.getY() + 0.5, min.getZ(),
                        max.getX() + 1, max.getY() + 2, max.getZ() + 1);
                level.getEntitiesOfClass(Monster.class, bounds).forEach(entity -> {
                    if (entity.isAlive()) {
                        entity.discard();
                    }
                });
            }
        } else if (heating) {
            heating = false;
            changed();
        }
        tick = (tick + 1) % 20;
        if (dirty) {
            dirty = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private void checkStructure() {
        if (min != null && max != null) {
            for (int x = (min.getX() - 1) >> 4; x <= (max.getX() + 1) >> 4; x++) {
                for (int z = (min.getZ() - 1) >> 4; z <= (max.getZ() + 1) >> 4; z++) {
                    if (!level.hasChunk(x, z)) {
                        return;
                    }
                }
            }
        }
        SearedFurnaceStructure found = SearedFurnaceStructure.detect(level, worldPosition,
                getBlockState().getValue(SmelteryBlock.FACING).getOpposite());
        if (found != null && active() && found.equals(structure)) {
            assignComponents(level, found.blocks());
            return;
        }
        if (found == null && !active && structure == null && items.size() == 9) {
            return;
        }
        releaseComponents();
        structure = found;
        active = found != null;
        members = found == null ? Set.of() : new HashSet<>(found.blocks());
        min = found == null ? null : found.min();
        max = found == null ? null : found.max();
        // 原版失效时仍保留九个基础槽，超出的物品掉落到控制器外侧。
        resize(found == null ? 9 : found.slots());
        if (found != null) {
            assignComponents(level, found.blocks());
        }
        level.setBlock(worldPosition, getBlockState().setValue(SmelteryBlock.ACTIVE, active), 3);
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
            if (heat[i] != 1 || required[i] != 0) {
                updateRequired(i);
            }
        }
    }

    public void releaseComponents() {
        if (structure != null && level != null && !level.isClientSide) {
            for (BlockPos pos : structure.blocks()) {
                if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                    component.release(worldPosition);
                }
            }
        }
    }

    private ItemStack result(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
    }

    private void updateRequired(int slot) {
        ItemStack stack = items.get(slot);
        ItemStack output = result(stack);
        required[slot] = 0;
        if (output.isEmpty()) {
            return;
        }
        if ((long) output.getCount() * stack.getCount() > Math.min(16, output.getMaxStackSize())) {
            heat[slot] = -1;
            return;
        }
        // 原版以 200 刻为基准，整批时间按数量缩放；食物另外加快 20%。
        int time = (int) (200 * stack.getCount() / 4.0F * (output.has(DataComponents.FOOD) ? 0.8F : 1.0F));
        required[slot] = time * 8;
    }

    private void heatItems() {
        boolean heated = false;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                heat[slot] = 0;
                required[slot] = 0;
            } else if (required[slot] > 0) {
                if (fuel <= 0) {
                    needsFuel = true;
                    break;
                }
                if (temperature < required[slot] / 8) {
                    continue;
                }
                if (heat[slot] >= required[slot]) {
                    ItemStack output = result(stack);
                    // 数据包重载可能改变产量，必须在替换输入前再次校验整个批次。
                    if (!output.isEmpty() && (long) output.getCount() * stack.getCount() <= Math.min(16, output.getMaxStackSize())) {
                        items.set(slot, output.copyWithCount(output.getCount() * stack.getCount()));
                        heat[slot] = 1;
                        required[slot] = 0;
                        changed();
                    } else {
                        heat[slot] = output.isEmpty() ? 0 : -1;
                        required[slot] = 0;
                        changed();
                    }
                } else {
                    heat[slot] += temperature / 100;
                    heated = true;
                    setChanged();
                }
            }
        }
        if (heated) {
            fuel--;
        }
        if (heating != heated) {
            heating = heated;
            changed();
        }
    }

    private void consumeFuel() {
        if (fuel > 0 || structure == null) {
            return;
        }
        // 优先继续使用当前燃料，再寻找其他储罐和燃料种类。
        for (int pass = 0; pass < 2; pass++) {
            List<BlockPos> tanks = new ArrayList<>(structure.tanks());
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

    public IItemHandler itemHandler() {
        return itemHandler;
    }

    @Override
    public boolean active() {
        return active && !isRemoved() && (level == null || level.isClientSide || structure != null);
    }

    @Override
    public boolean heating() {
        return heating && active();
    }

    @Override
    public boolean contains(BlockPos pos) {
        return active() && members.contains(pos);
    }

    @Override
    public void requestCheck() {
        needsCheck = true;
    }

    @Override
    public int heat(int slot) {
        return heat[slot];
    }

    @Override
    public int required(int slot) {
        return required[slot];
    }

    @Override
    public int fuel() {
        return fuel;
    }

    @Override
    public int fuelQuality() {
        return fuelQuality;
    }

    @Override
    public int temperature() {
        return temperature;
    }

    @Override
    public FuelDisplay fuelDisplay() {
        return FuelDisplay.of(level, structure == null ? clientTanks : structure.tanks(), currentFuel, fuel);
    }

    private void changed() {
        setChanged();
        dirty = true;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public int getMaxStackSize() {
        return 16;
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
        heat[slot] = 0;
        super.setItem(slot, stack);
        updateRequired(slot);
        if (required[slot] > 0 && level != null && !level.isClientSide) {
            consumeFuel();
        }
        changed();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        boolean completed = heat[slot] == 1 && required[slot] == 0;
        ItemStack output = super.removeItem(slot, amount);
        if (!completed || items.get(slot).isEmpty()) {
            heat[slot] = 0;
            updateRequired(slot);
        }
        changed();
        return output;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.searedfurnace.name");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SmelteryMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        SmelteryInventory.save(tag, items, registries);
        tag.putInt("Size", items.size());
        tag.putIntArray("Heat", heat);
        tag.putIntArray("Required", required);
        saveDisplay(tag, registries);
    }

    private void saveDisplay(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Active", active);
        tag.putBoolean("Heating", heating);
        tag.putInt("Fuel", fuel);
        tag.putInt("FuelQuality", fuelQuality);
        tag.putInt("Temperature", temperature);
        tag.putBoolean("NeedsFuel", needsFuel);
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
        int size = Math.clamp(tag.getInt(tag.contains("RenderSize") ? "RenderSize" : "Size"), 0, 81 * 4096);
        items = NonNullList.withSize(size, ItemStack.EMPTY);
        SmelteryInventory.load(tag, items, registries);
        heat = Arrays.copyOf(tag.getIntArray("Heat"), size);
        required = Arrays.copyOf(tag.getIntArray("Required"), size);
        active = tag.getBoolean("Active");
        heating = tag.getBoolean("Heating");
        fuel = Math.max(0, tag.getInt("Fuel"));
        fuelQuality = tag.getInt("FuelQuality");
        temperature = tag.getInt("Temperature");
        needsFuel = tag.getBoolean("NeedsFuel");
        currentFuel = FluidStack.parseOptional(registries, tag.getCompound("CurrentFuel"));
        currentTank = tag.contains("CurrentTank") ? BlockPos.of(tag.getLong("CurrentTank")) : null;
        min = tag.contains("Min") ? BlockPos.of(tag.getLong("Min")) : null;
        max = tag.contains("Max") ? BlockPos.of(tag.getLong("Max")) : null;
        clientTanks = Arrays.stream(tag.getLongArray("Tanks")).mapToObj(BlockPos::of).toList();
        structure = null;
        members = Set.of();
        needsCheck = true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 世界渲染不显示物品，仅同步尺寸和燃料；打开的菜单单独同步物品及热量。
        CompoundTag tag = new CompoundTag();
        saveDisplay(tag, registries);
        tag.putInt("RenderSize", items.size());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
