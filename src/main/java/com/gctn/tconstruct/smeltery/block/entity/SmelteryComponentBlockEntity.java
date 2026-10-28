package com.gctn.tconstruct.smeltery.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntityType; // 引入 net.minecraft.world.level.block.entity.BlockEntityType，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。
import net.neoforged.neoforge.fluids.capability.IFluidHandler; // 引入 net.neoforged.neoforge.fluids.capability.IFluidHandler，供本文件使用。

/**
 * 保存结构归属，并将排液操作转发给当前有效的控制器。
 * 每次调用都重新检查归属和区块加载状态，防止缓存的能力访问旧控制器。
 * 结构检测时，不能将属于未加载控制器的构件抢占给另一控制器。
 */
public class SmelteryComponentBlockEntity extends SyncedBlockEntity { // 定义 SmelteryComponentBlockEntity 的实现类型，具体职责见上方说明。
    private BlockPos controller; // 声明 controller，保存后续流程使用的状态或依赖。
    // 结构改变后能力提供者仍可保留，每次操作都重新解析当前控制器。
    private final IFluidHandler drain = new IFluidHandler() { // 设置 drain 为 new IFluidHandler() {。
        private IFluidHandler target() { var owner = controller(); return owner != null && owner.isFormed() ? owner.tank() : null; } // 定义 target 的调用入口；设置 owner 为 controller(); return owner != null && owner.isFormed() ? owner.tank() : null。
        @Override public int getTanks() { var t = target(); return t == null ? 0 : t.getTanks(); } // 实现父类或接口约定；定义 getTanks 的调用入口；设置 t 为 target(); return t == null ? 0 : t.getTanks()。
        @Override public FluidStack getFluidInTank(int tank) { var t = target(); return t == null ? FluidStack.EMPTY : t.getFluidInTank(tank); } // 实现父类或接口约定；定义 getFluidInTank 的调用入口；设置 t 为 target(); return t == null ? FluidStack.EMPTY : t.getFluidInTank(tank)。
        @Override public int getTankCapacity(int tank) { var t = target(); return t == null ? 0 : t.getTankCapacity(tank); } // 实现父类或接口约定；定义 getTankCapacity 的调用入口；设置 t 为 target(); return t == null ? 0 : t.getTankCapacity(tank)。
        @Override public boolean isFluidValid(int tank, FluidStack stack) { var t = target(); return t != null && t.isFluidValid(tank, stack); } // 实现父类或接口约定；定义 isFluidValid 的调用入口；设置 t 为 target(); return t != null && t.isFluidValid(tank, stack)。
        @Override public int fill(FluidStack stack, FluidAction action) { var t = target(); return t == null ? 0 : t.fill(stack, action); } // 实现父类或接口约定；定义 fill 的调用入口；设置 t 为 target(); return t == null ? 0 : t.fill(stack, action)。
        @Override public FluidStack drain(FluidStack stack, FluidAction action) { var t = target(); return t == null ? FluidStack.EMPTY : t.drain(stack, action); } // 实现父类或接口约定；定义 drain 的调用入口；设置 t 为 target(); return t == null ? FluidStack.EMPTY : t.drain(stack, action)。
        @Override public FluidStack drain(int amount, FluidAction action) { var t = target(); return t == null ? FluidStack.EMPTY : t.drain(amount, action); } // 实现父类或接口约定；定义 drain 的调用入口；设置 t 为 target(); return t == null ? FluidStack.EMPTY : t.drain(amount, action)。
    }; // 结束当前作用域并完成本次表达式或声明。

    public SmelteryComponentBlockEntity(BlockPos pos, BlockState state) { this(SmelteryRegistration.COMPONENT_ENTITY.get(), pos, state); } // 定义 SmelteryComponentBlockEntity 的调用入口；读取指定条目或延迟注册对象。
    protected SmelteryComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); } // 定义 SmelteryComponentBlockEntity 的调用入口；向当前声明或调用传入 super(type, pos, state)。
    public IFluidHandler drainHandler() { return drain; } // 定义 drainHandler 的调用入口；返回 drain，将当前分支的结果交给调用者。
    public boolean canJoin(BlockPos owner) { // 定义 canJoin 的调用入口，参数和具体处理见下方实现。
        if (controller == null || controller.equals(owner)) return true; // 检查 controller == null || controller.equals(owner)；按条件决定是否执行本分支。
        if (level == null || !level.hasChunkAt(controller)) return false; // 检查 level == null || !level.hasChunkAt(controller)；按条件决定是否执行本分支。
        return !(level.getBlockEntity(controller) instanceof SmelteryControllerBlockEntity previous) || !previous.contains(worldPosition); // 返回 !(level.getBlockEntity(controller) instanceof SmelteryControllerBlockEntity previous) || !previous.contains…，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    public void setController(BlockPos owner) { // 定义 setController 的调用入口，参数和具体处理见下方实现。
        if (!java.util.Objects.equals(controller, owner)) { controller = owner; setChanged(); } // 检查 !java.util.Objects.equals(controller, owner)；按条件决定是否执行本分支。
    } // 结束当前作用域。
    public void clearController(BlockPos owner) { if (owner.equals(controller)) setController(null); } // 定义 clearController 的调用入口；检查 owner.equals(controller)；按条件决定是否执行本分支。
    public SmelteryControllerBlockEntity controller() { // 定义 controller 的调用入口，参数和具体处理见下方实现。
        if (controller == null || level == null || !level.hasChunkAt(controller)) return null; // 检查 controller == null || level == null || !level.hasChunkAt(controller)；按条件决定是否执行本分支。
        return level.getBlockEntity(controller) instanceof SmelteryControllerBlockEntity owner && owner.contains(worldPosition) ? owner : null; // 返回 level.getBlockEntity(controller) instanceof SmelteryControllerBlockEntity owner && owner.contains(worldPosi…，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    public void notifyController() { var owner = controller(); if (owner != null) owner.requestCheck(); } // 定义 notifyController 的调用入口；设置 owner 为 controller(); if (owner != null) owner.requestCheck()。
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 saveAdditional 的调用入口，参数和具体处理见下方实现。
        super.saveAdditional(tag, registries); // 调用 saveAdditional，执行当前流程所需的处理。
        if (controller != null) tag.putLong("Controller", controller.asLong()); // 检查 controller != null；按条件决定是否执行本分支。
    } // 结束当前作用域。
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 loadAdditional 的调用入口，参数和具体处理见下方实现。
        super.loadAdditional(tag, registries); // 调用 loadAdditional，执行当前流程所需的处理。
        controller = tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null; // 设置 controller 为 tag.contains("Controller") ? BlockPos.of(tag.getLong("Controller")) : null。
    } // 结束当前作用域。
} // 结束当前作用域。
