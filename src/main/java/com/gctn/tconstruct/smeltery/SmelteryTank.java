/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Antique's ordered, shared-capacity tank. The first fluid is the drain's default output. */
public final class SmelteryTank implements IFluidHandler {
    private final List<FluidStack> fluids = new ArrayList<>();
    private final Runnable changed;
    private int capacity;

    public SmelteryTank(Runnable changed) {
        this.changed = changed;
    }

    public List<FluidStack> fluids() {
        return Collections.unmodifiableList(fluids);
    }

    public int amount() {
        return fluids.stream().mapToInt(FluidStack::getAmount).sum();
    }

    public int capacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        // A smaller structure retains its liquids, even when over capacity; only new filling stops.
        this.capacity = Math.max(0, capacity);
    }

    public void moveToBottom(int index) {
        if (index >= 0 && index < fluids.size()) {
            fluids.add(0, fluids.remove(index));
            changed.run();
        }
    }

    @Override
    public int getTanks() {
        return Math.max(1, fluids.size());
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tank >= 0 && tank < fluids.size() ? fluids.get(tank).copy() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? Math.max(0, capacity - amount()) + getFluidInTank(0).getAmount() : getFluidInTank(tank).getAmount();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        int accepted = Math.max(0, Math.min(capacity - amount(), resource.getAmount()));
        if (accepted == 0 || action.simulate()) {
            return accepted;
        }
        for (FluidStack fluid : fluids) {
            if (FluidStack.isSameFluidSameComponents(fluid, resource)) {
                fluid.grow(accepted);
                changed.run();
                return accepted;
            }
        }
        fluids.add(resource.copyWithAmount(accepted));
        changed.run();
        return accepted;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (int i = 0; i < fluids.size(); i++) {
            FluidStack fluid = fluids.get(i);
            if (FluidStack.isSameFluidSameComponents(fluid, resource)) {
                FluidStack result = fluid.copyWithAmount(Math.min(resource.getAmount(), fluid.getAmount()));
                if (action.execute()) {
                    fluid.shrink(result.getAmount());
                    if (fluid.isEmpty()) {
                        fluids.remove(i);
                    }
                    changed.run();
                }
                return result;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int amount, FluidAction action) {
        return fluids.isEmpty() || amount <= 0 ? FluidStack.EMPTY : drain(fluids.getFirst().copyWithAmount(amount), action);
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        fluids.forEach(fluid -> list.add(fluid.save(registries)));
        tag.put("Liquids", list);
        tag.putInt("LiquidCapacity", capacity);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        fluids.clear();
        for (var value : tag.getList("Liquids", 10)) {
            FluidStack fluid = FluidStack.parseOptional(registries, (CompoundTag) value);
            if (!fluid.isEmpty()) {
                fluids.add(fluid);
            }
        }
        capacity = Math.max(0, tag.getInt("LiquidCapacity"));
    }
}
