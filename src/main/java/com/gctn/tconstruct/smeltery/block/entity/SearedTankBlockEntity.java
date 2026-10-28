package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SearedTankBlockEntity extends SmelteryComponentBlockEntity {
    public static final int CAPACITY = 4000;
    private final FluidTank tank = new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(Fluids.LAVA)) {
        @Override protected void onContentsChanged() { sync(); if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock()); }
    };
    public SearedTankBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.TANK_ENTITY.get(), pos, state); }
    public FluidTank tank() { return tank; }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); tag.put("Tank", tank.writeToNBT(registries, new CompoundTag())); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); tank.readFromNBT(registries, tag.getCompound("Tank")); }
}
