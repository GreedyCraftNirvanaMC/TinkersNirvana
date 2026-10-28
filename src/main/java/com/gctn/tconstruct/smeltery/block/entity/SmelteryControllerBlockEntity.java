package com.gctn.tconstruct.smeltery.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.*; // 引入 com.gctn.tconstruct.smeltery.*，供本文件使用。
import com.gctn.tconstruct.smeltery.block.SmelteryControllerBlock; // 引入 com.gctn.tconstruct.smeltery.block.SmelteryControllerBlock，供本文件使用。
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu; // 引入 com.gctn.tconstruct.smeltery.menu.SmelteryMenu，供本文件使用。
import com.gctn.tconstruct.library.materials.MaterialValue; // 引入 com.gctn.tconstruct.library.materials.MaterialValue，供本文件使用。
import com.gctn.tconstruct.smeltery.recipe.*; // 引入 com.gctn.tconstruct.smeltery.recipe.*，供本文件使用。
import net.minecraft.core.*; // 引入 net.minecraft.core.*，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.nbt.ListTag; // 引入 net.minecraft.nbt.ListTag，供本文件使用。
import net.minecraft.nbt.Tag; // 引入 net.minecraft.nbt.Tag，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.network.Connection; // 引入 net.minecraft.network.Connection，供本文件使用。
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket; // 引入 net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket，供本文件使用。
import net.minecraft.world.*; // 引入 net.minecraft.world.*，供本文件使用。
import net.minecraft.world.entity.item.ItemEntity; // 引入 net.minecraft.world.entity.item.ItemEntity，供本文件使用。
import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.entity.player.*; // 引入 net.minecraft.world.entity.player.*，供本文件使用。
import net.minecraft.world.inventory.AbstractContainerMenu; // 引入 net.minecraft.world.inventory.AbstractContainerMenu，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity; // 引入 net.minecraft.world.level.block.entity.BaseContainerBlockEntity，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.minecraft.world.phys.AABB; // 引入 net.minecraft.world.phys.AABB，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction; // 引入 net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction，供本文件使用。
import net.neoforged.neoforge.items.IItemHandler; // 引入 net.neoforged.neoforge.items.IItemHandler，供本文件使用。

import java.util.*; // 引入 java.util.*，供本文件使用。

/**
 * 管理多方块结构归属、可变大小物品库存、加热、燃料和分层流体。
 * 结构变化时保留流体，只弹出不再容纳的物品；所需区块未加载时暂停访问。
 * 服务端菜单为每位玩家同步 27 个槽位，方块更新只发送流体和结构的可视状态。
 */
public class SmelteryControllerBlockEntity extends BaseContainerBlockEntity { // 定义 SmelteryControllerBlockEntity 的实现类型，具体职责见上方说明。
    public static final int INGOT = MaterialValue.VALUE_Ingot; // 设置 INGOT 为 MaterialValue.VALUE_Ingot。
    public static final int CAPACITY_PER_BLOCK = INGOT * 8; // 设置 CAPACITY_PER_BLOCK 为 INGOT * 8。
    private NonNullList<ItemStack> items = NonNullList.create(); // 设置 items 为 NonNullList.create()。
    private int[] heat = new int[0]; // 设置 heat 为 new int[0]。
    private SmelteryRecipe[] recipes = new SmelteryRecipe[0]; // 设置 recipes 为 new SmelteryRecipe[0]。
    private List<net.minecraft.world.item.crafting.RecipeHolder<SmelteryRecipe>> recipeSnapshot; // 声明 recipeSnapshot，保存后续流程使用的状态或依赖。
    private final MultiFluidTank tank = new MultiFluidTank(() -> { fluidsDirty = true; setChanged(); }); // 设置 tank 为 new MultiFluidTank(() -> { fluidsDirty = true; setChanged(); })。
    private SmelteryStructure structure; // 声明 structure，保存后续流程使用的状态或依赖。
    private final Set<BlockPos> members = new HashSet<>(); // 设置 members 为 new HashSet<>()。
    // 所需区块缺失时暂停访问，直到所有构件都可检查前不调整库存或弹出物品。
    // 这种情况需要与结构实际损坏区分处理。
    private boolean suspended, needsCheck = true, fluidsDirty, clientFormed; // 在 SmelteryControllerBlockEntity 中补充当前声明或表达式的 private boolean suspended, needsCheck = true, fluidsDirty, clientFormed 部分。
    private int ticks, fuel; // 在 SmelteryControllerBlockEntity 中补充当前声明或表达式的 private int ticks, fuel 部分。
    private BlockPos min = BlockPos.ZERO, max = BlockPos.ZERO; // 设置 min 为 BlockPos.ZERO, max = BlockPos.ZERO。

    private final IItemHandler itemHandler = new IItemHandler() { // 设置 itemHandler 为 new IItemHandler() {。
        @Override public int getSlots() { return isFormed() ? items.size() : 0; } // 实现父类或接口约定；定义 getSlots 的调用入口；返回 isFormed() ? items.size() : 0，将当前分支的结果交给调用者。
        @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < getSlots() ? getItem(slot).copy() : ItemStack.EMPTY; } // 实现父类或接口约定；定义 getStackInSlot 的调用入口；返回 slot >= 0 && slot < getSlots() ? getItem(slot).copy() : ItemStack.EMPTY，将当前分支的结果交给调用者。
        @Override public int getSlotLimit(int slot) { return 1; } // 实现父类或接口约定；定义 getSlotLimit 的调用入口；返回 1，将当前分支的结果交给调用者。
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot >= 0 && slot < getSlots() && canPlaceItem(slot, stack); } // 实现父类或接口约定；定义 isItemValid 的调用入口；返回 slot >= 0 && slot < getSlots() && canPlaceItem(slot, stack)，将当前分支的结果交给调用者。
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { // 实现父类或接口约定；定义 insertItem 的调用入口，参数和具体处理见下方实现。
            if (!isItemValid(slot, stack) || !getItem(slot).isEmpty()) return stack; // 检查 !isItemValid(slot, stack) || !getItem(slot).isEmpty()；按条件决定是否执行本分支。
            if (!simulate) setItem(slot, stack.copyWithCount(1)); // 检查 !simulate；按条件决定是否执行本分支。
            return stack.copyWithCount(stack.getCount() - 1); // 返回 stack.copyWithCount(stack.getCount() - 1)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { // 实现父类或接口约定；定义 extractItem 的调用入口，参数和具体处理见下方实现。
            if (slot < 0 || slot >= getSlots() || amount <= 0) return ItemStack.EMPTY; // 检查 slot < 0 || slot >= getSlots() || amount <= 0；按条件决定是否执行本分支。
            return simulate ? getItem(slot).copy() : removeItem(slot, amount); // 返回 simulate ? getItem(slot).copy() : removeItem(slot, amount)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    }; // 结束当前作用域并完成本次表达式或声明。

    public SmelteryControllerBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.CONTROLLER_ENTITY.get(), pos, state); } // 定义 SmelteryControllerBlockEntity 的调用入口；读取指定条目或延迟注册对象。
    public MultiFluidTank tank() { return tank; } // 定义 tank 的调用入口；返回 tank，将当前分支的结果交给调用者。
    public IItemHandler itemHandler() { return itemHandler; } // 定义 itemHandler 的调用入口；返回 itemHandler，将当前分支的结果交给调用者。
    public boolean isFormed() { return level != null && level.isClientSide ? clientFormed : structure != null && !suspended && structure.loaded(level); } // 定义 isFormed 的调用入口；返回 level != null && level.isClientSide ? clientFormed : structure != null && !suspended && structure.loaded(le…，将当前分支的结果交给调用者。
    public boolean contains(BlockPos pos) { return members.contains(pos); } // 定义 contains 的调用入口；返回 members.contains(pos)，将当前分支的结果交给调用者。
    public BlockPos min() { return min; } // 定义 min 的调用入口；返回 min，将当前分支的结果交给调用者。
    public BlockPos max() { return max; } // 定义 max 的调用入口；返回 max，将当前分支的结果交给调用者。
    public int fuel() { return fuel; } // 定义 fuel 的调用入口；返回 fuel，将当前分支的结果交给调用者。
    public int progress(int slot) { // 定义 progress 的调用入口，参数和具体处理见下方实现。
        if (slot < 0 || slot >= heat.length || recipes[slot] == null) return 0; // 检查 slot < 0 || slot >= heat.length || recipes[slot] == null；按条件决定是否执行本分支。
        return (int) Math.clamp((long) heat[slot] * 1000 / recipes[slot].time(), 0L, 1000L); // 返回 (int) Math.clamp((long) heat[slot] * 1000 / recipes[slot].time(), 0L, 1000L)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    public void requestCheck() { needsCheck = true; } // 定义 requestCheck 的调用入口；设置 needsCheck 为 true。

    public static void tick(Level level, BlockPos pos, BlockState state, SmelteryControllerBlockEntity controller) { // 定义 tick 的调用入口，参数和具体处理见下方实现。
        controller.serverTick(); // 调用 serverTick，执行当前流程所需的处理。
    } // 结束当前作用域。
    private void serverTick() { // 定义 serverTick 的调用入口，参数和具体处理见下方实现。
        ticks++; // 更新 ticks++ 对应的计数，供下一步判定使用。
        if (needsCheck || ticks % 20 == 0) checkStructure(); // 检查 needsCheck || ticks % 20 == 0；按条件决定是否执行本分支。
        if (!isFormed()) return; // 检查 !isFormed()；按条件决定是否执行本分支。
        // RecipeManager 在重载时替换不可变的配方映射，此前一直复用
        // 已有索引列表；旧配方进度不能直接用于完成新配方。
        var currentRecipes = SmelteryRecipes.all(level); // 设置 currentRecipes 为 SmelteryRecipes.all(level)。
        if (recipeSnapshot != currentRecipes) { // 检查 recipeSnapshot != currentRecipes；按条件决定是否执行本分支。
            if (recipeSnapshot != null) Arrays.fill(heat, 0); // 检查 recipeSnapshot != null；按条件决定是否执行本分支。
            Arrays.fill(recipes, null); // 按给定动作模拟或执行流体注入。
            recipeSnapshot = currentRecipes; // 设置 recipeSnapshot 为 currentRecipes。
        } // 结束当前作用域。
        // 消耗燃料或推进进度前，先模拟是否能完整接收输出，
        // 避免满罐时仍消耗熔炼输入；每个槽位只容纳一个物品。
        boolean heating = false; // 设置 heating 为 false。
        for (int slot = 0; slot < items.size(); slot++) { // 遍历 int slot = 0; slot < items.size(); slot++，逐项执行当前处理。
            ItemStack input = items.get(slot); // 设置 input 为 items.get(slot)。
            if (input.isEmpty()) continue; // 检查 input.isEmpty()；按条件决定是否执行本分支。
            var recipe = recipes[slot]; // 设置 recipe 为 recipes[slot]。
            if (recipe == null) recipes[slot] = recipe = SmelteryRecipes.melting(level, input); // 检查 recipe == null；按条件决定是否执行本分支。
            if (recipe == null || recipe.temperature() > 1000 || tank.fill(recipe.fluidResult(), FluidAction.SIMULATE) != recipe.fluidResult().getAmount()) continue; // 检查 recipe == null || recipe.temperature() > 1000 || tank.fill(recipe.fluidResult(), FluidAction.SIMULATE) != r…；按条件决定是否执行本分支。
            if (!ensureFuel()) break; // 检查 !ensureFuel()；按条件决定是否执行本分支。
            heating = true; // 设置 heating 为 true。
            if (++heat[slot] >= recipe.time()) { // 检查 ++heat[slot] >= recipe.time()；按条件决定是否执行本分支。
                // 本轮已在消耗输入前完成容量检查。
                tank.fill(recipe.fluidResult(), FluidAction.EXECUTE); // 按给定动作模拟或执行流体注入。
                items.set(slot, ItemStack.EMPTY); // 设置目标槽位、组件或属性的值。
                heat[slot] = 0; // 设置 heat[slot] 为 0。
                recipes[slot] = null; // 设置 recipes[slot] 为 null。
                setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
            } // 结束当前作用域。
        } // 结束当前作用域。
        if (ticks % 5 == 0) heating |= alloy(); // 检查 ticks % 5 == 0；按条件决定是否执行本分支。
        if (ticks % 20 == 0) heating |= interactWithEntities(); // 检查 ticks % 20 == 0；按条件决定是否执行本分支。
        if (heating && fuel > 0) { fuel--; setChanged(); } // 检查 heating && fuel > 0；按条件决定是否执行本分支。
        if (ticks % 5 == 0 && fluidsDirty) { fluidsDirty = false; sync(); } // 检查 ticks % 5 == 0 && fluidsDirty；按条件决定是否执行本分支。
        if (ticks % 20 == 0) setChanged(); // 检查 ticks % 20 == 0；按条件决定是否执行本分支。
    } // 结束当前作用域。

    private boolean ensureFuel() { // 定义 ensureFuel 的调用入口，参数和具体处理见下方实现。
        // 未用完的工作刻数保留到后续操作，机器空闲时不消耗燃料。
        // 结构中的任意燃料储罐都可以供应下一小批熔岩。
        if (fuel > 0) return true; // 检查 fuel > 0；按条件决定是否执行本分支。
        for (BlockPos pos : structure.tanks()) { // 遍历 BlockPos pos : structure.tanks()，逐项执行当前处理。
            if (level.getBlockEntity(pos) instanceof SearedTankBlockEntity fuelTank && fuelTank.tank().getFluidAmount() >= 50) { // 检查 level.getBlockEntity(pos) instanceof SearedTankBlockEntity fuelTank && fuelTank.tank().getFluidAmount() >= 50；按条件决定是否执行本分支。
                fuelTank.tank().drain(50, FluidAction.EXECUTE); // 按给定动作模拟或执行流体排出。
                fuel = 100; // 设置 fuel 为 100。
                setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
                return true; // 返回 true，将当前分支的结果交给调用者。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return false; // 返回 false，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private boolean alloy() { // 定义 alloy 的调用入口，参数和具体处理见下方实现。
        boolean worked = false; // 设置 worked 为 false。
        if (tank.amount() > tank.capacity()) return false; // 检查 tank.amount() > tank.capacity()；按条件决定是否执行本分支。
        for (var holder : SmelteryRecipes.all(level)) { // 遍历 var holder : SmelteryRecipes.all(level)，逐项执行当前处理。
            var recipe = holder.value(); // 设置 recipe 为 holder.value()。
            if (recipe.operation() != SmelteryRecipe.Operation.ALLOY || recipe.temperature() > 1000) continue; // 检查 recipe.operation() != SmelteryRecipe.Operation.ALLOY || recipe.temperature() > 1000；按条件决定是否执行本分支。
            // 先在副本上模拟整个事务，同时正确处理多次要求同一种流体的配方。
            MultiFluidTank trial = new MultiFluidTank(() -> {}); // 设置 trial 为 new MultiFluidTank(() -> {})。
            trial.setCapacity(tank.capacity()); // 更新容量限制。
            tank.contents().forEach(f -> trial.fill(f, FluidAction.EXECUTE)); // 定义回调或映射规则，在对应条目被处理时执行。
            boolean matches = true; // 设置 matches 为 true。
            for (var input : recipe.fluids()) if (trial.drain(input, FluidAction.EXECUTE).getAmount() != input.getAmount()) { matches = false; break; } // 遍历 var input : recipe.fluids()) if (trial.drain(input, FluidAction.EXECUTE).getAmount() != input.getAmount()，逐项执行当前处理。
            if (!matches || trial.fill(recipe.fluidResult(), FluidAction.SIMULATE) != recipe.fluidResult().getAmount() || !ensureFuel()) continue; // 检查 !matches || trial.fill(recipe.fluidResult(), FluidAction.SIMULATE) != recipe.fluidResult().getAmount() || !…；按条件决定是否执行本分支。
            for (var input : recipe.fluids()) tank.drain(input, FluidAction.EXECUTE); // 遍历 var input : recipe.fluids()) tank.drain(input, FluidAction.EXECUTE，逐项执行当前处理。
            tank.fill(recipe.fluidResult(), FluidAction.EXECUTE); // 按给定动作模拟或执行流体注入。
            worked = true; // 设置 worked 为 true。
        } // 结束当前作用域。
        return worked; // 返回 worked，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private boolean interactWithEntities() { // 定义 interactWithEntities 的调用入口，参数和具体处理见下方实现。
        AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1); // 设置 bounds 为 new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1)。
        for (var entity : level.getEntitiesOfClass(ItemEntity.class, bounds, e -> e.isAlive() && !e.hasPickUpDelay())) { // 遍历 var entity : level.getEntitiesOfClass(ItemEntity.class, bounds, e -> e.isAlive() && !e.hasPickUpDelay())，逐项执行当前处理。
            ItemStack input = entity.getItem(); // 设置 input 为 entity.getItem()。
            if (SmelteryRecipes.melting(level, input) == null) continue; // 检查 SmelteryRecipes.melting(level, input) == null；按条件决定是否执行本分支。
            for (int slot = 0; slot < items.size() && !input.isEmpty(); slot++) { // 遍历 int slot = 0; slot < items.size() && !input.isEmpty(); slot++，逐项执行当前处理。
                if (items.get(slot).isEmpty()) { setItem(slot, input.split(1)); } // 检查 items.get(slot).isEmpty()；按条件决定是否执行本分支。
            } // 结束当前作用域。
            if (input.isEmpty()) entity.discard(); // 检查 input.isEmpty()；按条件决定是否执行本分支。
            else entity.setItem(input); // 处理前述条件均不满足时的替代分支。
        } // 结束当前作用域。
        boolean worked = false; // 设置 worked 为 false。
        for (var entity : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) { // 遍历 var entity : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)，逐项执行当前处理。
            FluidStack blood = new FluidStack(SmelteryFluids.BLOOD.source().get(), 20); // 设置 blood 为 new FluidStack(SmelteryFluids.BLOOD.source().get(), 20)。
            if (tank.fill(blood, FluidAction.SIMULATE) == 20 && ensureFuel() // 检查 tank.fill(blood, FluidAction.SIMULATE) == 20 && ensureFuel()；按条件决定是否执行本分支。
                    && entity.hurt(level.damageSources().inFire(), 2)) { tank.fill(blood, FluidAction.EXECUTE); worked = true; } // 按给定动作模拟或执行流体注入。
        } // 结束当前作用域。
        return worked; // 返回 worked，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public void checkStructure() { // 定义 checkStructure 的调用入口，参数和具体处理见下方实现。
        if (level == null || level.isClientSide) return; // 检查 level == null || level.isClientSide；按条件决定是否执行本分支。
        needsCheck = false; // 设置 needsCheck 为 false。
        if (structure != null && !structure.loaded(level)) { suspended = true; updateActive(); return; } // 检查 structure != null && !structure.loaded(level)；按条件决定是否执行本分支。
        var detection = SmelteryStructure.detect(level, worldPosition); // 设置 detection 为 SmelteryStructure.detect(level, worldPosition)。
        if (detection.unloaded()) { suspended = true; updateActive(); return; } // 检查 detection.unloaded()；按条件决定是否执行本分支。
        suspended = false; // 设置 suspended 为 false。
        SmelteryStructure next = detection.structure(); // 设置 next 为 detection.structure()。
        if (Objects.equals(structure, next) && (next != null || members.isEmpty() && tank.capacity() == 0)) { updateActive(); return; } // 检查 Objects.equals(structure, next) && (next != null || members.isEmpty() && tank.capacity() == 0)；按条件决定是否执行本分支。
        // 认领新结构范围前先解除旧归属，已加载的构件
        // 会先检查是否仍属于该控制器，再暴露其能力。
        releaseMembers(); // 调用 releaseMembers，执行当前流程所需的处理。
        structure = next; // 设置 structure 为 next。
        if (next != null) { // 检查 next != null；按条件决定是否执行本分支。
            members.addAll(next.members()); // 合并当前批次的全部条目。
            min = next.min(); max = next.max(); // 设置 min 为 next.min(); max = next.max()。
            for (BlockPos pos : members) if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.setController(worldPosition); // 遍历 BlockPos pos : members) if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.se…，逐项执行当前处理。
            resize(next.volume()); // 调用 resize，执行当前流程所需的处理。
            tank.setCapacity(next.volume() * CAPACITY_PER_BLOCK); // 更新容量限制。
        } else { resize(0); tank.setCapacity(0); } // 处理前述条件均不满足时的替代分支。
        updateActive(); // 调用 updateActive，执行当前流程所需的处理。
        sync(); // 同步方块实体的显示状态。
    } // 结束当前作用域。

    private void updateActive() { // 定义 updateActive 的调用入口，参数和具体处理见下方实现。
        BlockState state = getBlockState(); // 设置 state 为 getBlockState()。
        if (state.getValue(SmelteryControllerBlock.ACTIVE) != isFormed()) { // 检查 state.getValue(SmelteryControllerBlock.ACTIVE) != isFormed()；按条件决定是否执行本分支。
            level.setBlock(worldPosition, state.setValue(SmelteryControllerBlock.ACTIVE, isFormed()), 3); // 更新世界中的方块状态。
            sync(); // 同步方块实体的显示状态。
        } // 结束当前作用域。
    } // 结束当前作用域。
    private void releaseMembers() { // 定义 releaseMembers 的调用入口，参数和具体处理见下方实现。
        if (level != null) for (BlockPos pos : members) { // 检查 level != null；按条件决定是否执行本分支。
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.clearController(worldPosition); // 检查 level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part；按条件决定是否执行本分支。
        } // 结束当前作用域。
        members.clear(); // 清空当前集合中的内容。
    } // 结束当前作用域。
    public void releaseStructure() { releaseMembers(); structure = null; tank.setCapacity(0); } // 定义 releaseStructure 的调用入口；更新容量限制。
    private void resize(int size) { // 定义 resize 的调用入口，参数和具体处理见下方实现。
        if (size == items.size()) return; // 检查 size == items.size()；按条件决定是否执行本分支。
        // 保留仍可容纳输入的槽位顺序和进度，只弹出超出的
        // 物品；流体容量缩小时不截断已有流体。
        var resized = NonNullList.withSize(size, ItemStack.EMPTY); // 设置 resized 为 NonNullList.withSize(size, ItemStack.EMPTY)。
        for (int i = 0; i < items.size(); i++) { // 遍历 int i = 0; i < items.size(); i++，逐项执行当前处理。
            if (i < size) resized.set(i, items.get(i)); // 检查 i < size；按条件决定是否执行本分支。
            else if (!items.get(i).isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, items.get(i)); // 前一条件不成立时继续检查本分支条件。
        } // 结束当前作用域。
        items = resized; // 设置 items 为 resized。
        heat = Arrays.copyOf(heat, size); // 设置 heat 为 Arrays.copyOf(heat, size)。
        recipes = Arrays.copyOf(recipes, size); // 设置 recipes 为 Arrays.copyOf(recipes, size)。
        setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。
    private void sync() { setChanged(); if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); } // 定义 sync 的调用入口；标记数据已变化，供区块保存或菜单同步使用。

    @Override public int getContainerSize() { return items.size(); } // 实现父类或接口约定；定义 getContainerSize 的调用入口；返回 items.size()，将当前分支的结果交给调用者。
    @Override protected NonNullList<ItemStack> getItems() { return items; } // 实现父类或接口约定；定义 getItems 的调用入口；返回 items，将当前分支的结果交给调用者。
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; heat = new int[items.size()]; recipes = new SmelteryRecipe[items.size()]; } // 实现父类或接口约定；定义 setItems 的调用入口；设置 this.items 为 items; heat = new int[items.size()]; recipes = new SmelteryRecipe[items.size()]。
    @Override public int getMaxStackSize() { return 1; } // 实现父类或接口约定；定义 getMaxStackSize 的调用入口；返回 1，将当前分支的结果交给调用者。
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return isFormed() && level != null && SmelteryRecipes.melting(level, stack) != null; } // 实现父类或接口约定；定义 canPlaceItem 的调用入口；返回 isFormed() && level != null && SmelteryRecipes.melting(level, stack) != null，将当前分支的结果交给调用者。
    @Override public void setItem(int slot, ItemStack stack) { super.setItem(slot, stack); heat[slot] = 0; recipes[slot] = null; } // 实现父类或接口约定；定义 setItem 的调用入口；更新指定物品槽位。
    @Override public ItemStack removeItem(int slot, int count) { ItemStack result = super.removeItem(slot, count); if (!result.isEmpty()) { heat[slot] = 0; recipes[slot] = null; } return result; } // 实现父类或接口约定；定义 removeItem 的调用入口；设置 result 为 super.removeItem(slot, count); if (!result.isEmpty()) { heat[slot] = 0; recipes[slot] = null; } return result。
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack result = super.removeItemNoUpdate(slot); if (slot >= 0 && slot < heat.length) { heat[slot] = 0; recipes[slot] = null; } return result; } // 实现父类或接口约定；定义 removeItemNoUpdate 的调用入口；设置 result 为 super.removeItemNoUpdate(slot); if (slot >= 0 && slot < heat.length) { heat[slot] = 0; recipes[slot] = null…。
    @Override protected Component getDefaultName() { return Component.translatable("container.tconstruct.smeltery"); } // 实现父类或接口约定；定义 getDefaultName 的调用入口；返回 Component.translatable("container.tconstruct.smeltery")，将当前分支的结果交给调用者。
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new SmelteryMenu(id, inventory, this); } // 实现父类或接口约定；定义 createMenu 的调用入口；返回 new SmelteryMenu(id, inventory, this)，将当前分支的结果交给调用者。

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 saveAdditional 的调用入口，参数和具体处理见下方实现。
        super.saveAdditional(tag, registries); // 调用 saveAdditional，执行当前流程所需的处理。
        tag.putInt("InventorySize", items.size()); // 写入整型存档字段。
        // 原版 ContainerHelper 使用字节保存 Slot，而大型冶炼炉可能超过 256 个槽位。
        ListTag inventoryTag = new ListTag(); // 设置 inventoryTag 为 new ListTag()。
        for (int slot = 0; slot < items.size(); slot++) { // 遍历 int slot = 0; slot < items.size(); slot++，逐项执行当前处理。
            if (!items.get(slot).isEmpty()) { // 检查 !items.get(slot).isEmpty()；按条件决定是否执行本分支。
                CompoundTag entry = new CompoundTag(); // 设置 entry 为 new CompoundTag()。
                entry.putInt("Slot", slot); // 写入整型存档字段。
                inventoryTag.add(items.get(slot).save(registries, entry)); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } // 结束当前作用域。
        tag.put("Items", inventoryTag); // 按键保存对应数据。
        tag.putIntArray("Heat", heat); // 写入整型数组，保留各槽位数据。
        tag.putLongArray("Members", members.stream().mapToLong(BlockPos::asLong).toArray()); // 写入长整型数组，保存位置等批量数据。
        saveVisuals(tag, registries); // 调用 saveVisuals，执行当前流程所需的处理。
    } // 结束当前作用域。
    private void saveVisuals(CompoundTag tag, HolderLookup.Provider registries) { // 定义 saveVisuals 的调用入口，参数和具体处理见下方实现。
        // 附近客户端只需要这些可视数据，无需接收整个可变大小库存。
        // 物品内容仅通过分页菜单同步。
        tank.save(tag, registries); // 调用 save，执行当前流程所需的处理。
        tag.putInt("Capacity", tank.capacity()); // 写入整型存档字段。
        tag.putInt("Fuel", fuel); // 写入整型存档字段。
        tag.putBoolean("Formed", isFormed()); // 写入布尔存档字段。
        tag.putLong("Min", min.asLong()); tag.putLong("Max", max.asLong()); // 写入长整型存档字段。
    } // 结束当前作用域。
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 实现父类或接口约定；定义 loadAdditional 的调用入口，参数和具体处理见下方实现。
        super.loadAdditional(tag, registries); // 调用 loadAdditional，执行当前流程所需的处理。
        // 限制读取存档 NBT 时的内存分配：有效结构每层最多有 9×9 个物品
        // 槽位；配方引用在加载后重新构建。
        int limit = level == null ? 81 * 4096 : 81 * level.getHeight(); // 设置 limit 为 level == null ? 81 * 4096 : 81 * level.getHeight()。
        items = NonNullList.withSize(Math.clamp(tag.getInt("InventorySize"), 0, limit), ItemStack.EMPTY); // 设置 items 为 NonNullList.withSize(Math.clamp(tag.getInt("InventorySize"), 0, limit), ItemStack.EMPTY)。
        ListTag inventoryTag = tag.getList("Items", Tag.TAG_COMPOUND); // 设置 inventoryTag 为 tag.getList("Items", Tag.TAG_COMPOUND)。
        for (int i = 0; i < inventoryTag.size(); i++) { // 遍历 int i = 0; i < inventoryTag.size(); i++，逐项执行当前处理。
            CompoundTag entry = inventoryTag.getCompound(i); // 设置 entry 为 inventoryTag.getCompound(i)。
            int slot = entry.contains("Slot", Tag.TAG_BYTE) ? entry.getByte("Slot") & 255 : entry.getInt("Slot"); // 设置 slot 为 entry.contains("Slot", Tag.TAG_BYTE) ? entry.getByte("Slot") & 255 : entry.getInt("Slot")。
            if (slot >= 0 && slot < items.size()) items.set(slot, ItemStack.parse(registries, entry).orElse(ItemStack.EMPTY)); // 检查 slot >= 0 && slot < items.size()；按条件决定是否执行本分支。
        } // 结束当前作用域。
        heat = Arrays.copyOf(tag.getIntArray("Heat"), items.size()); // 设置 heat 为 Arrays.copyOf(tag.getIntArray("Heat"), items.size())。
        for (int i = 0; i < heat.length; i++) heat[i] = Math.clamp(heat[i], 0, 72000); // 遍历 int i = 0; i < heat.length; i++) heat[i] = Math.clamp(heat[i], 0, 72000，逐项执行当前处理。
        recipes = new SmelteryRecipe[items.size()]; // 设置 recipes 为 new SmelteryRecipe[items.size()]。
        recipeSnapshot = null; // 设置 recipeSnapshot 为 null。
        members.clear(); // 清空当前集合中的内容。
        for (long pos : tag.getLongArray("Members")) members.add(BlockPos.of(pos)); // 遍历 long pos : tag.getLongArray("Members")) members.add(BlockPos.of(pos)，逐项执行当前处理。
        loadVisuals(tag, registries); // 调用 loadVisuals，执行当前流程所需的处理。
        structure = null; // 设置 structure 为 null。
        needsCheck = true; // 设置 needsCheck 为 true。
    } // 结束当前作用域。
    private void loadVisuals(CompoundTag tag, HolderLookup.Provider registries) { // 定义 loadVisuals 的调用入口，参数和具体处理见下方实现。
        tank.load(tag, registries); // 调用 load，执行当前流程所需的处理。
        tank.setCapacity(Math.max(0, tag.getInt("Capacity"))); // 更新容量限制。
        fuel = Math.clamp(tag.getInt("Fuel"), 0, 100); // 设置 fuel 为 Math.clamp(tag.getInt("Fuel"), 0, 100)。
        min = BlockPos.of(tag.getLong("Min")); max = BlockPos.of(tag.getLong("Max")); // 设置 min 为 BlockPos.of(tag.getLong("Min")); max = BlockPos.of(tag.getLong("Max"))。
        clientFormed = tag.getBoolean("Formed"); // 设置 clientFormed 为 tag.getBoolean("Formed")。
    } // 结束当前作用域。
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); } // 实现父类或接口约定；定义 getUpdatePacket 的调用入口；返回 ClientboundBlockEntityDataPacket.create(this)，将当前分支的结果交给调用者。
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); saveVisuals(tag, registries); return tag; } // 实现父类或接口约定；定义 getUpdateTag 的调用入口；设置 tag 为 new CompoundTag(); saveVisuals(tag, registries); return tag。
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) { loadVisuals(tag, registries); } // 实现父类或接口约定；定义 handleUpdateTag 的调用入口；调用 loadVisuals，执行当前流程所需的处理。
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) { loadVisuals(packet.getTag(), registries); } // 实现父类或接口约定；定义 onDataPacket 的调用入口；调用 loadVisuals，执行当前流程所需的处理。
} // 结束当前作用域。
