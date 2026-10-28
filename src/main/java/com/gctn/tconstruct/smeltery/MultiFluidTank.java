package com.gctn.tconstruct.smeltery; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.nbt.ListTag; // 引入 net.minecraft.nbt.ListTag，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。
import net.neoforged.neoforge.fluids.capability.IFluidHandler; // 引入 net.neoforged.neoforge.fluids.capability.IFluidHandler，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 多个有序流体层共用一个容量，通用排液默认选择第一层。
 * 模拟操作不修改库存或通知监听器，读取接口返回可变 FluidStack 的副本。
 * 容量缩小时保留超出的流体，只有共享空间足够时才允许继续注入。
 */
public class MultiFluidTank implements IFluidHandler { // 定义 MultiFluidTank 的实现类型，具体职责见上方说明。
    private final List<FluidStack> fluids = new ArrayList<>(); // 设置 fluids 为 new ArrayList<>()。
    private final Runnable changed; // 声明 changed，保存后续流程使用的状态或依赖。
    private int capacity; // 声明 capacity，保存后续流程使用的状态或依赖。
    // 使用 long 统计总量，避免旧存档中超大的 NBT 数值使剩余空间
    // 计算发生溢出；正常机器仍受整型容量限制。
    private long totalAmount; // 声明 totalAmount，保存后续流程使用的状态或依赖。

    public MultiFluidTank(Runnable changed) { this.changed = changed; } // 定义 MultiFluidTank 的调用入口；设置 this.changed 为 changed。
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); } // 定义 setCapacity 的调用入口；设置 this.capacity 为 Math.max(0, capacity)。
    public int capacity() { return capacity; } // 定义 capacity 的调用入口；返回 capacity，将当前分支的结果交给调用者。
    public int amount() { return (int) Math.min(Integer.MAX_VALUE, totalAmount); } // 定义 amount 的调用入口；返回 (int) Math.min(Integer.MAX_VALUE, totalAmount)，将当前分支的结果交给调用者。
    /** 返回快照，调用者检查或修改这些流体栈不会改变储罐本身。 */
    public List<FluidStack> contents() { return fluids.stream().map(FluidStack::copy).toList(); } // 定义 contents 的调用入口；返回 fluids.stream().map(FluidStack::copy).toList()，将当前分支的结果交给调用者。
    public void select(int index) { // 定义 select 的调用入口，参数和具体处理见下方实现。
        if (index > 0 && index < fluids.size()) { fluids.addFirst(fluids.remove(index)); changed.run(); } // 检查 index > 0 && index < fluids.size()；按条件决定是否执行本分支。
    } // 结束当前作用域。
    @Override public int getTanks() { return fluids.size() + 1; } // 实现父类或接口约定；定义 getTanks 的调用入口；返回 fluids.size() + 1，将当前分支的结果交给调用者。
    @Override public FluidStack getFluidInTank(int tank) { return tank >= 0 && tank < fluids.size() ? fluids.get(tank).copy() : FluidStack.EMPTY; } // 实现父类或接口约定；定义 getFluidInTank 的调用入口；返回 tank >= 0 && tank < fluids.size() ? fluids.get(tank).copy() : FluidStack.EMPTY，将当前分支的结果交给调用者。
    @Override public int getTankCapacity(int tank) { return tank >= 0 && tank < getTanks() ? capacity : 0; } // 实现父类或接口约定；定义 getTankCapacity 的调用入口；返回 tank >= 0 && tank < getTanks() ? capacity : 0，将当前分支的结果交给调用者。
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return tank >= 0 && tank < getTanks() && !stack.isEmpty(); } // 实现父类或接口约定；定义 isFluidValid 的调用入口；返回 tank >= 0 && tank < getTanks() && !stack.isEmpty()，将当前分支的结果交给调用者。

    @Override public int fill(FluidStack resource, FluidAction action) { // 实现父类或接口约定；定义 fill 的调用入口，参数和具体处理见下方实现。
        if (resource.isEmpty()) return 0; // 检查 resource.isEmpty()；按条件决定是否执行本分支。
        // 以 O(1) 检查共享剩余容量，避免大型冶炼炉加热时
        // 对每个槽位都重新累加所有流体层。
        int accepted = (int) Math.min(resource.getAmount(), Math.max(0L, capacity - totalAmount)); // 设置 accepted 为 (int) Math.min(resource.getAmount(), Math.max(0L, capacity - totalAmount))。
        if (accepted > 0 && action.execute()) { // 检查 accepted > 0 && action.execute()；按条件决定是否执行本分支。
            var existing = fluids.stream().filter(f -> FluidStack.isSameFluidSameComponents(f, resource)).findFirst(); // 设置 existing 为 fluids.stream().filter(f -> FluidStack.isSameFluidSameComponents(f, resource)).findFirst()。
            if (existing.isPresent()) existing.get().grow(accepted); // 检查 existing.isPresent()；按条件决定是否执行本分支。
            else fluids.add(resource.copyWithAmount(accepted)); // 处理前述条件均不满足时的替代分支。
            totalAmount += accepted; // 将当前计算结果累积到 totalAmount，保持本轮状态一致。
            changed.run(); // 调用 run，执行当前流程所需的处理。
        } // 结束当前作用域。
        return accepted; // 返回 accepted，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override public FluidStack drain(FluidStack resource, FluidAction action) { // 实现父类或接口约定；定义 drain 的调用入口，参数和具体处理见下方实现。
        // 按流体排液时选择匹配层，未指定流体的排液操作使用
        // 第一层；只有实际执行才修改库存、总量和脏标记。
        if (resource.isEmpty()) return FluidStack.EMPTY; // 检查 resource.isEmpty()；按条件决定是否执行本分支。
        for (int i = 0; i < fluids.size(); i++) { // 遍历 int i = 0; i < fluids.size(); i++，逐项执行当前处理。
            FluidStack stored = fluids.get(i); // 设置 stored 为 fluids.get(i)。
            if (FluidStack.isSameFluidSameComponents(resource, stored)) { // 检查 FluidStack.isSameFluidSameComponents(resource, stored)；按条件决定是否执行本分支。
                FluidStack result = stored.copyWithAmount(Math.min(stored.getAmount(), resource.getAmount())); // 设置 result 为 stored.copyWithAmount(Math.min(stored.getAmount(), resource.getAmount()))。
                if (action.execute()) { // 检查 action.execute()；按条件决定是否执行本分支。
                    stored.shrink(result.getAmount()); // 减少已经确认消耗的数量。
                    totalAmount -= result.getAmount(); // 将当前计算结果累积到 totalAmount，保持本轮状态一致。
                    if (stored.isEmpty()) fluids.remove(i); // 检查 stored.isEmpty()；按条件决定是否执行本分支。
                    changed.run(); // 调用 run，执行当前流程所需的处理。
                } // 结束当前作用域。
                return result; // 返回 result，将当前分支的结果交给调用者。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return FluidStack.EMPTY; // 返回 FluidStack.EMPTY，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override public FluidStack drain(int maxDrain, FluidAction action) { // 实现父类或接口约定；定义 drain 的调用入口，参数和具体处理见下方实现。
        return fluids.isEmpty() || maxDrain <= 0 ? FluidStack.EMPTY : drain(fluids.getFirst().copyWithAmount(maxDrain), action); // 返回 fluids.isEmpty() || maxDrain <= 0 ? FluidStack.EMPTY : drain(fluids.getFirst().copyWithAmount(maxDrain), ac…，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public void save(CompoundTag tag, HolderLookup.Provider registries) { // 定义 save 的调用入口，参数和具体处理见下方实现。
        ListTag list = new ListTag(); // 设置 list 为 new ListTag()。
        fluids.forEach(f -> list.add(f.save(registries))); // 定义回调或映射规则，在对应条目被处理时执行。
        tag.put("Fluids", list); // 按键保存对应数据。
    } // 结束当前作用域。

    public void load(CompoundTag tag, HolderLookup.Provider registries) { // 定义 load 的调用入口，参数和具体处理见下方实现。
        // 加载时直接替换存储内容，不触发游戏逻辑回调。容量单独
        // 恢复，允许其小于已经保存的流体总量。
        fluids.clear(); // 清空当前集合中的内容。
        totalAmount = 0; // 设置 totalAmount 为 0。
        for (var entry : tag.getList("Fluids", CompoundTag.TAG_COMPOUND)) { // 遍历 var entry : tag.getList("Fluids", CompoundTag.TAG_COMPOUND)，逐项执行当前处理。
            FluidStack.parse(registries, entry).filter(f -> !f.isEmpty()).ifPresent(fluid -> { // 定义回调或映射规则，在对应条目被处理时执行。
                fluids.add(fluid); // 向集合或构建器追加条目。
                totalAmount += fluid.getAmount(); // 将当前计算结果累积到 totalAmount，保持本轮状态一致。
            }); // 结束当前作用域并完成本次表达式或声明。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
