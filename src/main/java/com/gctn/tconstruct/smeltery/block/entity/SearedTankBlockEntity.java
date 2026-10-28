package com.gctn.tconstruct.smeltery.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.minecraft.world.level.material.Fluids; // 引入 net.minecraft.world.level.material.Fluids，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。
import net.neoforged.neoforge.fluids.capability.templates.FluidTank; // 引入 net.neoforged.neoforge.fluids.capability.templates.FluidTank，供本文件使用。

/**
 * 独立保存四桶熔岩，供冶炼炉作为燃料使用。
 * 流体变化时同步渲染状态并更新比较器输出，排液口不会暴露此燃料储罐。
 */
public class SearedTankBlockEntity extends SmelteryComponentBlockEntity { // 定义 SearedTankBlockEntity 的实现类型，具体职责见上方说明。
    public static final int CAPACITY = 4000; // 设置 CAPACITY 为 4000。
    private final FluidTank tank = new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(Fluids.LAVA)) { // 设置 tank 为 new FluidTank(CAPACITY, stack -> stack.getFluid().isSame(Fluids.LAVA)) {。
        @Override protected void onContentsChanged() { sync(); if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock()); } // 实现父类或接口约定；定义 onContentsChanged 的调用入口；同步方块实体的显示状态。
    }; // 结束当前作用域并完成本次表达式或声明。
    public SearedTankBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.TANK_ENTITY.get(), pos, state); } // 定义 SearedTankBlockEntity 的调用入口；读取指定条目或延迟注册对象。
    public FluidTank tank() { return tank; } // 定义 tank 的调用入口；返回 tank，将当前分支的结果交给调用者。
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); tag.put("Tank", tank.writeToNBT(registries, new CompoundTag())); } // 实现父类或接口约定；定义 saveAdditional 的调用入口；按键保存对应数据。
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); tank.readFromNBT(registries, tag.getCompound("Tank")); } // 实现父类或接口约定；定义 loadAdditional 的调用入口；读取嵌套的 NBT 数据。
} // 结束当前作用域。
