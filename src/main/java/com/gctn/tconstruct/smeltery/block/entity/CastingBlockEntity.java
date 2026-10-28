package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.gctn.tconstruct.smeltery.block.CastingBlock;
import com.gctn.tconstruct.smeltery.recipe.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class CastingBlockEntity extends SyncedBlockEntity {
    private int cooling;
    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override public int getSlotLimit(int slot) { return slot == 0 ? 1 : 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && tank.isEmpty() && getStackInSlot(1).isEmpty(); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 && !tank.isEmpty() ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
        }
        @Override protected void onContentsChanged(int slot) { sync(); }
    };
    private final FluidTank tank = new FluidTank(1) {
        @Override public boolean isFluidValid(FluidStack resource) {
            return level != null && !resource.isEmpty() && items.getStackInSlot(1).isEmpty()
                    && SmelteryRecipes.casting(level, isBasin(), items.getStackInSlot(0), resource) != null;
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (level == null || resource.isEmpty() || !items.getStackInSlot(1).isEmpty()
                    || !isEmpty() && !FluidStack.isSameFluidSameComponents(getFluid(), resource)) return 0;
            var recipe = SmelteryRecipes.casting(level, isBasin(), items.getStackInSlot(0), resource);
            if (recipe == null) return 0;
            int accepted = Math.min(resource.getAmount(), Math.max(0, recipe.fluids().getFirst().getAmount() - getFluidAmount()));
            if (action.simulate() || accepted <= 0) return accepted;
            setCapacity(recipe.fluids().getFirst().getAmount());
            return super.fill(resource, action);
        }
        @Override protected void onContentsChanged() { cooling = 0; sync(); }
    };
    private final IItemHandler automation = new IItemHandler() {
        @Override public int getSlots() { return 2; }
        @Override public ItemStack getStackInSlot(int slot) { return items.getStackInSlot(slot).copy(); }
        @Override public int getSlotLimit(int slot) { return items.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return items.isItemValid(slot, stack); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return items.insertItem(slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
    };

    public CastingBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.CASTING_ENTITY.get(), pos, state); }
    public boolean isBasin() { return ((CastingBlock) getBlockState().getBlock()).isBasin(); }
    public ItemStackHandler itemHandler() { return items; }
    public IItemHandler automation() { return automation; }
    public FluidTank fluidHandler() { return tank; }
    public void interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!items.getStackInSlot(1).isEmpty()) {
            player.getInventory().placeItemBackInInventory(items.extractItem(1, 64, false));
        } else if (tank.isEmpty()) {
            if (held.isEmpty()) player.getInventory().placeItemBackInInventory(items.extractItem(0, 1, false));
            else if (items.getStackInSlot(0).isEmpty()) {
                ItemStack remainder = items.insertItem(0, held.copyWithCount(1), false);
                if (remainder.isEmpty() && !player.getAbilities().instabuild) held.shrink(1);
            }
        }
    }
    public static void tick(Level level, BlockPos pos, BlockState state, CastingBlockEntity casting) {
        if (casting.tank.isEmpty() || !casting.items.getStackInSlot(1).isEmpty()) return;
        var recipe = SmelteryRecipes.casting(level, casting.isBasin(), casting.items.getStackInSlot(0), casting.tank.getFluid());
        if (recipe == null || casting.tank.getFluidAmount() != recipe.fluids().getFirst().getAmount()) { casting.cooling = 0; return; }
        if (++casting.cooling >= recipe.time()) {
            if (recipe.consumeInput()) casting.items.setStackInSlot(0, ItemStack.EMPTY);
            casting.items.setStackInSlot(1, recipe.result().copy());
            casting.tank.setFluid(FluidStack.EMPTY);
            casting.cooling = 0;
            casting.sync();
        } else if (casting.cooling % 20 == 0) casting.setChanged();
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Capacity", tank.getCapacity()); tag.putInt("Cooling", cooling);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        tank.setCapacity(Math.max(1, tag.getInt("Capacity")));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        cooling = Math.max(0, tag.getInt("Cooling"));
    }
}
