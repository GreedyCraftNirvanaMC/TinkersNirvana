package com.gctn.tconstruct.smeltery;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered fluid layers sharing one capacity; the first layer is selected for generic draining.
 * Simulation never mutates storage or notifies listeners, and read APIs return copies of mutable FluidStacks.
 * Reducing capacity preserves excess contents; further filling waits until there is free shared space.
 */
public class MultiFluidTank implements IFluidHandler {
    private final List<FluidStack> fluids = new ArrayList<>();
    private final Runnable changed;
    private int capacity;
    // A long also handles oversized legacy NBT without wrapping the free-space
    // calculation. Normal machines remain bounded by their integer capacity.
    private long totalAmount;

    public MultiFluidTank(Runnable changed) { this.changed = changed; }
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); }
    public int capacity() { return capacity; }
    public int amount() { return (int) Math.min(Integer.MAX_VALUE, totalAmount); }
    /** Snapshot stacks may be inspected/modified without owning the tank's storage. */
    public List<FluidStack> contents() { return fluids.stream().map(FluidStack::copy).toList(); }
    public void select(int index) {
        if (index > 0 && index < fluids.size()) { fluids.addFirst(fluids.remove(index)); changed.run(); }
    }
    @Override public int getTanks() { return fluids.size() + 1; }
    @Override public FluidStack getFluidInTank(int tank) { return tank >= 0 && tank < fluids.size() ? fluids.get(tank).copy() : FluidStack.EMPTY; }
    @Override public int getTankCapacity(int tank) { return tank >= 0 && tank < getTanks() ? capacity : 0; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank >= 0 && tank < getTanks() && !stack.isEmpty(); }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        // Shared capacity is checked once in O(1), rather than summing every
        // layer for every slot in a large smeltery's heating loop.
        int accepted = (int) Math.min(resource.getAmount(), Math.max(0L, capacity - totalAmount));
        if (accepted > 0 && action.execute()) {
            var existing = fluids.stream().filter(f -> FluidStack.isSameFluidSameComponents(f, resource)).findFirst();
            if (existing.isPresent()) existing.get().grow(accepted);
            else fluids.add(resource.copyWithAmount(accepted));
            totalAmount += accepted;
            changed.run();
        }
        return accepted;
    }

    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        // Typed draining selects a matching layer; untyped draining delegates to
        // the first. Only execution changes storage, totals and dirty state.
        if (resource.isEmpty()) return FluidStack.EMPTY;
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack stored = fluids.get(i);
            if (FluidStack.isSameFluidSameComponents(resource, stored)) {
                FluidStack result = stored.copyWithAmount(Math.min(stored.getAmount(), resource.getAmount()));
                if (action.execute()) {
                    stored.shrink(result.getAmount());
                    totalAmount -= result.getAmount();
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
        // Replace saved contents without firing gameplay callbacks. Capacity is
        // restored separately and may legitimately be smaller than stored fluid.
        fluids.clear();
        totalAmount = 0;
        for (var entry : tag.getList("Fluids", CompoundTag.TAG_COMPOUND)) {
            FluidStack.parse(registries, entry).filter(f -> !f.isEmpty()).ifPresent(fluid -> {
                fluids.add(fluid);
                totalAmount += fluid.getAmount();
            });
        }
    }
}
