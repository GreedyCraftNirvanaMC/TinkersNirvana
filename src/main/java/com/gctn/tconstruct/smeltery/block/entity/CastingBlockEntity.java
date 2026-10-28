package com.gctn.tconstruct.smeltery.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import com.gctn.tconstruct.smeltery.block.CastingBlock; // 引入 com.gctn.tconstruct.smeltery.block.CastingBlock，供本文件使用。
import com.gctn.tconstruct.smeltery.recipe.*; // 引入 com.gctn.tconstruct.smeltery.recipe.*，供本文件使用。
import net.minecraft.core.*; // 引入 net.minecraft.core.*，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.InteractionHand; // 引入 net.minecraft.world.InteractionHand，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。
import net.neoforged.neoforge.fluids.capability.IFluidHandler; // 引入 net.neoforged.neoforge.fluids.capability.IFluidHandler，供本文件使用。
import net.neoforged.neoforge.fluids.capability.templates.FluidTank; // 引入 net.neoforged.neoforge.fluids.capability.templates.FluidTank，供本文件使用。
import net.neoforged.neoforge.items.ItemStackHandler; // 引入 net.neoforged.neoforge.items.ItemStackHandler，供本文件使用。
import net.neoforged.neoforge.items.IItemHandler; // 引入 net.neoforged.neoforge.items.IItemHandler，供本文件使用。

/**
 * 管理双槽浇铸库存、按配方定容的流体缓冲区和服务端冷却进度。
 * 存在流体时锁定铸模，自动化只能放入铸模并取走成品。
 * 完成时只生成一次输出并清空流体，配方可选择同时消耗模板物品。
 */
public class CastingBlockEntity extends SyncedBlockEntity { // 定义 CastingBlockEntity 的实现类型，具体职责见上方说明。
    private int cooling; // 声明 cooling，保存后续流程使用的状态或依赖。
    private SmelteryRecipe coolingRecipe; // 声明 coolingRecipe，保存后续流程使用的状态或依赖。
    // 槽位 0 保存铸模，槽位 1 保存成品；分开管理两者，
    // 使自动化能够只提取成品而不取走可重复使用的铸模。
    private final ItemStackHandler items = new ItemStackHandler(2) { // 设置 items 为 new ItemStackHandler(2) {。
        @Override public int getSlotLimit(int slot) { return slot == 0 ? 1 : 64; } // 实现父类或接口约定；定义 getSlotLimit 的调用入口；返回 slot == 0 ? 1 : 64，将当前分支的结果交给调用者。
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && tank.isEmpty() && getStackInSlot(1).isEmpty(); } // 实现父类或接口约定；定义 isItemValid 的调用入口；返回 slot == 0 && tank.isEmpty() && getStackInSlot(1).isEmpty()，将当前分支的结果交给调用者。
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { // 实现父类或接口约定；定义 extractItem 的调用入口，参数和具体处理见下方实现。
            return slot == 0 && !tank.isEmpty() ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate); // 返回 slot == 0 && !tank.isEmpty() ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        @Override protected void onContentsChanged(int slot) { sync(); } // 实现父类或接口约定；定义 onContentsChanged 的调用入口；同步方块实体的显示状态。
    }; // 结束当前作用域并完成本次表达式或声明。
    private final FluidTank tank = new FluidTank(1) { // 设置 tank 为 new FluidTank(1) {。
        @Override public boolean isFluidValid(FluidStack resource) { // 实现父类或接口约定；定义 isFluidValid 的调用入口，参数和具体处理见下方实现。
            return level != null && !resource.isEmpty() && items.getStackInSlot(1).isEmpty() // 返回 level != null && !resource.isEmpty() && items.getStackInSlot(1).isEmpty()，将当前分支的结果交给调用者。
                    && SmelteryRecipes.casting(level, isBasin(), items.getStackInSlot(0), resource) != null; // 调用 casting，执行当前流程所需的处理。
        } // 结束当前作用域。
        @Override public int fill(FluidStack resource, FluidAction action) { // 实现父类或接口约定；定义 fill 的调用入口，参数和具体处理见下方实现。
            if (level == null || resource.isEmpty() || !items.getStackInSlot(1).isEmpty() // 检查 level == null || resource.isEmpty() || !items.getStackInSlot(1).isEmpty()；按条件决定是否执行本分支。
                    || !isEmpty() && !FluidStack.isSameFluidSameComponents(getFluid(), resource)) return 0; // 调用 isEmpty，执行当前流程所需的处理。
            var recipe = SmelteryRecipes.casting(level, isBasin(), items.getStackInSlot(0), resource); // 设置 recipe 为 SmelteryRecipes.casting(level, isBasin(), items.getStackInSlot(0), resource)。
            if (recipe == null) return 0; // 检查 recipe == null；按条件决定是否执行本分支。
            // 容量由配方决定，SIMULATE 不能改变容量、
            // 冷却进度或需要同步的方块实体状态。
            int accepted = Math.min(resource.getAmount(), Math.max(0, recipe.fluids().getFirst().getAmount() - getFluidAmount())); // 设置 accepted 为 Math.min(resource.getAmount(), Math.max(0, recipe.fluids().getFirst().getAmount() - getFluidAmount()))。
            if (action.simulate() || accepted <= 0) return accepted; // 检查 action.simulate() || accepted <= 0；按条件决定是否执行本分支。
            setCapacity(recipe.fluids().getFirst().getAmount()); // 更新容量限制。
            return super.fill(resource, action); // 返回 super.fill(resource, action)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        @Override protected void onContentsChanged() { cooling = 0; coolingRecipe = null; sync(); } // 实现父类或接口约定；定义 onContentsChanged 的调用入口；设置 cooling 为 0; coolingRecipe = null; sync()。
    }; // 结束当前作用域并完成本次表达式或声明。
    private final IItemHandler automation = new IItemHandler() { // 设置 automation 为 new IItemHandler() {。
        @Override public int getSlots() { return 2; } // 实现父类或接口约定；定义 getSlots 的调用入口；返回 2，将当前分支的结果交给调用者。
        @Override public ItemStack getStackInSlot(int slot) { return items.getStackInSlot(slot).copy(); } // 实现父类或接口约定；定义 getStackInSlot 的调用入口；返回 items.getStackInSlot(slot).copy()，将当前分支的结果交给调用者。
        @Override public int getSlotLimit(int slot) { return items.getSlotLimit(slot); } // 实现父类或接口约定；定义 getSlotLimit 的调用入口；返回 items.getSlotLimit(slot)，将当前分支的结果交给调用者。
        @Override public boolean isItemValid(int slot, ItemStack stack) { return items.isItemValid(slot, stack); } // 实现父类或接口约定；定义 isItemValid 的调用入口；返回 items.isItemValid(slot, stack)，将当前分支的结果交给调用者。
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return items.insertItem(slot, stack, simulate); } // 实现父类或接口约定；定义 insertItem 的调用入口；返回 items.insertItem(slot, stack, simulate)，将当前分支的结果交给调用者。
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { // 实现父类或接口约定；定义 extractItem 的调用入口，参数和具体处理见下方实现。
            return slot == 1 ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY; // 返回 slot == 1 ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    }; // 结束当前作用域并完成本次表达式或声明。

    public CastingBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.CASTING_ENTITY.get(), pos, state); } // 定义 CastingBlockEntity 的调用入口；读取指定条目或延迟注册对象。
    public boolean isBasin() { return ((CastingBlock) getBlockState().getBlock()).isBasin(); } // 定义 isBasin 的调用入口；返回 ((CastingBlock) getBlockState().getBlock()).isBasin()，将当前分支的结果交给调用者。
    public ItemStackHandler itemHandler() { return items; } // 定义 itemHandler 的调用入口；返回 items，将当前分支的结果交给调用者。
    public IItemHandler automation() { return automation; } // 定义 automation 的调用入口；返回 automation，将当前分支的结果交给调用者。
    public FluidTank fluidHandler() { return tank; } // 定义 fluidHandler 的调用入口；返回 tank，将当前分支的结果交给调用者。
    public void interact(Player player, InteractionHand hand) { // 定义 interact 的调用入口，参数和具体处理见下方实现。
        ItemStack held = player.getItemInHand(hand); // 设置 held 为 player.getItemInHand(hand)。
        if (!items.getStackInSlot(1).isEmpty()) { // 检查 !items.getStackInSlot(1).isEmpty()；按条件决定是否执行本分支。
            player.getInventory().placeItemBackInInventory(items.extractItem(1, 64, false)); // 将真实物品交还玩家背包。
        } else if (tank.isEmpty()) { // 前一条件不成立时继续检查本分支条件。
            if (held.isEmpty()) player.getInventory().placeItemBackInInventory(items.extractItem(0, 1, false)); // 检查 held.isEmpty()；按条件决定是否执行本分支。
            else if (items.getStackInSlot(0).isEmpty()) { // 前一条件不成立时继续检查本分支条件。
                ItemStack remainder = items.insertItem(0, held.copyWithCount(1), false); // 设置 remainder 为 items.insertItem(0, held.copyWithCount(1), false)。
                if (remainder.isEmpty() && !player.getAbilities().instabuild) held.shrink(1); // 检查 remainder.isEmpty() && !player.getAbilities().instabuild；按条件决定是否执行本分支。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。
    public static void tick(Level level, BlockPos pos, BlockState state, CastingBlockEntity casting) { // 定义 tick 的调用入口，参数和具体处理见下方实现。
        if (casting.tank.isEmpty() || !casting.items.getStackInSlot(1).isEmpty()) return; // 检查 casting.tank.isEmpty() || !casting.items.getStackInSlot(1).isEmpty()；按条件决定是否执行本分支。
        var recipe = SmelteryRecipes.casting(level, casting.isBasin(), casting.items.getStackInSlot(0), casting.tank.getFluid()); // 设置 recipe 为 SmelteryRecipes.casting(level, casting.isBasin(), casting.items.getStackInSlot(0), casting.tank.getFluid())。
        if (recipe == null || casting.tank.getFluidAmount() != recipe.fluids().getFirst().getAmount()) { casting.cooling = 0; return; } // 检查 recipe == null || casting.tank.getFluidAmount() != recipe.fluids().getFirst().getAmount()；按条件决定是否执行本分支。
        // 首次查询保留已保存的进度，但如果数据包重载在保留流体与铸模的情况下
        // 替换了配方，就重新开始冷却。
        if (casting.coolingRecipe != null && casting.coolingRecipe != recipe) casting.cooling = 0; // 检查 casting.coolingRecipe != null && casting.coolingRecipe != recipe；按条件决定是否执行本分支。
        casting.coolingRecipe = recipe; // 设置 casting.coolingRecipe 为 recipe。
        if (++casting.cooling >= recipe.time()) { // 检查 ++casting.cooling >= recipe.time()；按条件决定是否执行本分支。
            // 在服务端线程中完成时只生成一次成品，随后消耗
            // 流体，并按一次性铸模配方的要求消耗模板。
            if (recipe.consumeInput()) casting.items.setStackInSlot(0, ItemStack.EMPTY); // 检查 recipe.consumeInput()；按条件决定是否执行本分支。
            casting.items.setStackInSlot(1, recipe.result().copy()); // 更新内部物品处理器的指定槽位。
            casting.tank.setFluid(FluidStack.EMPTY); // 替换储存的流体内容。
            casting.cooling = 0; // 设置 casting.cooling 为 0。
            casting.sync(); // 同步方块实体的显示状态。
        } else if (casting.cooling % 20 == 0) casting.setChanged(); // 前一条件不成立时继续检查本分支条件。
    } // 结束当前作用域。
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 saveAdditional 的调用入口，参数和具体处理见下方实现。
        super.saveAdditional(tag, registries); // 调用 saveAdditional，执行当前流程所需的处理。
        tag.put("Items", items.serializeNBT(registries)); // 按键保存对应数据。
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag())); // 按键保存对应数据。
        tag.putInt("Capacity", tank.getCapacity()); tag.putInt("Cooling", cooling); // 写入整型存档字段。
    } // 结束当前作用域。
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 loadAdditional 的调用入口，参数和具体处理见下方实现。
        super.loadAdditional(tag, registries); // 调用 loadAdditional，执行当前流程所需的处理。
        items.deserializeNBT(registries, tag.getCompound("Items")); // 读取嵌套的 NBT 数据。
        tank.setCapacity(Math.max(1, tag.getInt("Capacity"))); // 更新容量限制。
        tank.readFromNBT(registries, tag.getCompound("Tank")); // 读取嵌套的 NBT 数据。
        cooling = Math.clamp(tag.getInt("Cooling"), 0, 72000); // 设置 cooling 为 Math.clamp(tag.getInt("Cooling"), 0, 72000)。
        coolingRecipe = null; // 设置 coolingRecipe 为 null。
    } // 结束当前作用域。
} // 结束当前作用域。
