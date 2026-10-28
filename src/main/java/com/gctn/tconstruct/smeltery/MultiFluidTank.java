package com.gctn.tconstruct.smeltery;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/** All layers share one capacity. Shrinking a structure never deletes excess fluid. */
public class MultiFluidTank implements IFluidHandler {
    private final List<FluidStack> fluids = new ArrayList<>();
    private final Runnable changed;
    private int capacity;

    public MultiFluidTank(Runnable changed) { this.changed = changed; }
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); }
    public int capacity() { return capacity; }
    public int amount() { return fluids.stream().mapToInt(FluidStack::getAmount).sum(); }
    public List<FluidStack> contents() { return fluids.stream().map(FluidStack::copy).toList(); }
    public void select(int index) {
        if (index > 0 && index < fluids.size()) { fluids.addFirst(fluids.remove(index)); changed.run(); }
    }
    @Override public int getTanks() { return fluids.size() + 1; }
    @Override public FluidStack getFluidInTank(int tank) { return tank >= 0 && tank < fluids.size() ? fluids.get(tank).copy() : FluidStack.EMPTY; }
    @Override public int getTankCapacity(int tank) { return capacity; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return !stack.isEmpty(); }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        int accepted = Math.min(resource.getAmount(), Math.max(0, capacity - amount()));
        if (accepted > 0 && action.execute()) {
            var existing = fluids.stream().filter(f -> FluidStack.isSameFluidSameComponents(f, resource)).findFirst();
            if (existing.isPresent()) existing.get().grow(accepted);
            else fluids.add(resource.copyWithAmount(accepted));
            changed.run();
        }
        return accepted;
    }

    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return FluidStack.EMPTY;
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack stored = fluids.get(i);
            if (FluidStack.isSameFluidSameComponents(resource, stored)) {
                FluidStack result = stored.copyWithAmount(Math.min(stored.getAmount(), resource.getAmount()));
                if (action.execute()) {
                    stored.shrink(result.getAmount());
                    if (stored.isEmpty()) fluids.remove(i);
                    changed.run();
                }
                return result;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        return fluids.isEmpty() || maxDrain <= 0 ? FluidStack.EMPTY : drain(fluids.getFirst().copyWithAmount(maxDrain), action);
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        fluids.forEach(f -> list.add(f.save(registries)));
        tag.put("Fluids", list);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        fluids.clear();
        for (var entry : tag.getList("Fluids", CompoundTag.TAG_COMPOUND)) {
            FluidStack.parse(registries, entry).filter(f -> !f.isEmpty()).ifPresent(fluids::add);
        }
    }
}
