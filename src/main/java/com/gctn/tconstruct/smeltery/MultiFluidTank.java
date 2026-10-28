package com.gctn.tconstruct.smeltery;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 多个有序流体层共用一个容量，通用排液默认选择第一层。
 * 模拟操作不修改库存或通知监听器，读取接口返回可变 FluidStack 的副本。
 * 容量缩小时保留超出的流体，只有共享空间足够时才允许继续注入。
 */
public class MultiFluidTank implements IFluidHandler {
    private final List<FluidStack> fluids = new ArrayList<>();
    private final Runnable changed;
    private int capacity;
    // 使用 long 统计总量，避免旧存档中超大的 NBT 数值使剩余空间
    // 计算发生溢出；正常机器仍受整型容量限制。
    private long totalAmount;

    public MultiFluidTank(Runnable changed) { this.changed = changed; }
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); }
    public int capacity() { return capacity; }
    public int amount() { return (int) Math.min(Integer.MAX_VALUE, totalAmount); }
    /** 返回快照，调用者检查或修改这些流体栈不会改变储罐本身。 */
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
        // 以 O(1) 检查共享剩余容量，避免大型冶炼炉加热时
        // 对每个槽位都重新累加所有流体层。
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
        // 按流体排液时选择匹配层，未指定流体的排液操作使用
        // 第一层；只有实际执行才修改库存、总量和脏标记。
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
        // 加载时直接替换存储内容，不触发游戏逻辑回调。容量单独
        // 恢复，允许其小于已经保存的流体总量。
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
