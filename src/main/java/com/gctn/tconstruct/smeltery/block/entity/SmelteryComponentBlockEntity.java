package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Stores membership and forwards drain operations to the currently valid controller.
 * Every call rechecks ownership and loaded chunks, so cached capabilities cannot access an old owner.
 * An unloaded owner cannot be stolen by another controller during structure detection.
 */
public class SmelteryComponentBlockEntity extends SyncedBlockEntity {
    private BlockPos controller;
    // Providers remain valid when the structure changes; every operation resolves the current owner.
    private final IFluidHandler drain = new IFluidHandler() {
        private IFluidHandler target() { var owner = controller(); return owner != null && owner.isFormed() ? owner.tank() : null; }
        @Override public int getTanks() { var t = target(); return t == null ? 0 : t.getTanks(); }
        @Override public FluidStack getFluidInTank(int tank) { var t = target(); return t == null ? FluidStack.EMPTY : t.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { var t = target(); return t == null ? 0 : t.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { var t = target(); return t != null && t.isFluidValid(tank, stack); }
        @Override public int fill(FluidStack stack, FluidAction action) { var t = target(); return t == null ? 0 : t.fill(stack, action); }
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { var t = target(); return t == null ? FluidStack.EMPTY : t.drain(stack, action); }
        @Override public FluidStack drain(int amount, FluidAction action) { var t = target(); return t == null ? FluidStack.EMPTY : t.drain(amount, action); }
    };

    public SmelteryComponentBlockEntity(BlockPos pos, BlockState state) { this(SmelteryRegistration.COMPONENT_ENTITY.get(), pos, state); }
    protected SmelteryComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    public IFluidHandler drainHandler() { return drain; }
    public boolean canJoin(BlockPos owner) {
        if (controller == null || controller.equals(owner)) return true;
        if (level == null || !level.hasChunkAt(controller)) return false;
        return !(level.getBlockEntity(controller) instanceof SmelteryControllerBlockEntity previous) || !previous.contains(worldPosition);
    }
    public void setController(BlockPos owner) {
        if (!java.util.Objects.equals(controller, owner)) { controller = owner; setChanged(); }
    }
    public void clearController(BlockPos owner) { if (owner.equals(controller)) setController(null); }
    public SmelteryControllerBlockEntity controller() {
        if (controller == null || level == null || !level.hasChunkAt(controller)) return null;
        return level.getBlockEntity(controller) instanceof SmelteryControllerBlockEntity owner && owner.contains(worldPosition) ? owner : null;
    }
    public void notifyController() { var owner = controller(); if (owner != null) owner.requestCheck(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controller != null) tag.putLong("Controller", controller.asLong());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controller = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null;
    }
}
