package com.gctn.tconstruct.smeltery.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。

/**
 * 同步浇注口正在流动的流体种类，供客户端绘制流体束。
 * 本实体不持有传输中的实际体积，源端和目标端始终保存权威流体数量。
 */
public class FaucetBlockEntity extends SyncedBlockEntity { // 定义 FaucetBlockEntity 的实现类型，具体职责见上方说明。
    private FluidStack flowing = FluidStack.EMPTY; // 设置 flowing 为 FluidStack.EMPTY。
    public FaucetBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.FAUCET_ENTITY.get(), pos, state); } // 定义 FaucetBlockEntity 的调用入口；读取指定条目或延迟注册对象。
    public FluidStack flowing() { return flowing; } // 定义 flowing 的调用入口；返回 flowing，将当前分支的结果交给调用者。
    public void setFlowing(FluidStack fluid) { // 定义 setFlowing 的调用入口，参数和具体处理见下方实现。
        if (flowing.isEmpty() && fluid.isEmpty() || !flowing.isEmpty() && !fluid.isEmpty() && FluidStack.isSameFluidSameComponents(flowing, fluid)) return; // 检查 flowing.isEmpty() && fluid.isEmpty() || !flowing.isEmpty() && !fluid.isEmpty() && FluidStack.isSameFluidSam…；按条件决定是否执行本分支。
        flowing = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(1); // 设置 flowing 为 fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(1)。
        sync(); // 同步方块实体的显示状态。
    } // 结束当前作用域。
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 saveAdditional 的调用入口，参数和具体处理见下方实现。
        super.saveAdditional(tag, registries); // 调用 saveAdditional，执行当前流程所需的处理。
        tag.put("Flowing", flowing.saveOptional(registries)); // 按键保存对应数据。
    } // 结束当前作用域。
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 loadAdditional 的调用入口，参数和具体处理见下方实现。
        super.loadAdditional(tag, registries); // 调用 loadAdditional，执行当前流程所需的处理。
        flowing = FluidStack.parseOptional(registries, tag.getCompound("Flowing")); // 设置 flowing 为 FluidStack.parseOptional(registries, tag.getCompound("Flowing"))。
    } // 结束当前作用域。
} // 结束当前作用域。
