package com.gctn.tconstruct.tables.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.data.Mode; // 引入 com.gctn.tconstruct.tables.data.Mode，供本文件使用。
import com.gctn.tconstruct.tables.menu.ToolStationMenu; // 引入 com.gctn.tconstruct.tables.menu.ToolStationMenu，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolAssemblyRecipe; // 引入 com.gctn.tconstruct.tables.recipe.ToolAssemblyRecipe，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes; // 引入 com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolStationDefaultRecipes; // 引入 com.gctn.tconstruct.tables.recipe.ToolStationDefaultRecipes，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes; // 引入 com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes.PartList; // 引入 com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes.PartList，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.core.NonNullList; // 引入 net.minecraft.core.NonNullList，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.world.Container; // 引入 net.minecraft.world.Container，供本文件使用。
import net.minecraft.world.ContainerHelper; // 引入 net.minecraft.world.ContainerHelper，供本文件使用。
import net.minecraft.world.entity.player.Inventory; // 引入 net.minecraft.world.entity.player.Inventory，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.inventory.AbstractContainerMenu; // 引入 net.minecraft.world.inventory.AbstractContainerMenu，供本文件使用。
import net.minecraft.world.inventory.ContainerData; // 引入 net.minecraft.world.inventory.ContainerData，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity; // 引入 net.minecraft.world.level.block.entity.BaseContainerBlockEntity，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。

/**
 * 管理工具台的权威库存，以及合成、拆解状态机。
 * 普通合成结果只是派生预览；拆解时则在取走首个部件前始终保留真实源工具。
 * 所有玩家与自动化共用此状态，因此输入变化必须刷新预览，领取结果时必须重新校验。
 */
public class ToolStationBlockEntity extends BaseContainerBlockEntity { // 定义 ToolStationBlockEntity 的实现类型，具体职责见上方说明。
    private static final int CONTAINER_SIZE = ToolStationMenu.CONTAINER_SIZE; // 设置 CONTAINER_SIZE 为 ToolStationMenu.CONTAINER_SIZE。
    private static final int INPUT_SLOT_COUNT = ToolStationMenu.INPUT_SLOT_COUNT; // 设置 INPUT_SLOT_COUNT 为 ToolStationMenu.INPUT_SLOT_COUNT。
    private static final int RESULT_SLOT = ToolStationMenu.RESULT_SLOT_INDEX; // 设置 RESULT_SLOT 为 ToolStationMenu.RESULT_SLOT_INDEX。

    private static final int DATA_MODE = 0; // 设置 DATA_MODE 为 0。
    private static final int DATA_DISASSEMBLY_STATE = 1; // 设置 DATA_DISASSEMBLY_STATE 为 1。
    public static final int DATA_COUNT = 2; // 设置 DATA_COUNT 为 2。

    private static final int DISASSEMBLY_NONE = 0; // 设置 DISASSEMBLY_NONE 为 0。
    private static final int DISASSEMBLY_PREVIEW = 1; // 设置 DISASSEMBLY_PREVIEW 为 1。
    private static final int DISASSEMBLY_COMMITTED = 2; // 设置 DISASSEMBLY_COMMITTED 为 2。

    private static final String TAG_MODE = "Mode"; // 设置 TAG_MODE 为 "Mode"。
    private static final String TAG_DISASSEMBLY_STATE = "DisassemblyState"; // 设置 TAG_DISASSEMBLY_STATE 为 "DisassemblyState"。

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY); // 设置 items 为 NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY)。
    private Mode mode = Mode.DEFAULT; // 设置 mode 为 Mode.DEFAULT。
    private int disassemblyState = DISASSEMBLY_NONE; // 设置 disassemblyState 为 DISASSEMBLY_NONE。
    private boolean consumingInputs; // 声明 consumingInputs，保存后续流程使用的状态或依赖。

    private final ContainerData menuData = new ContainerData() { // 设置 menuData 为 new ContainerData() {。
        @Override // 覆盖父类或接口的方法，保持原有调用契约。
        public int get(int index) { // 定义 get 的调用入口，参数和具体处理见下方实现。
            return switch (index) { // 返回 switch (index) {，将当前分支的结果交给调用者。
                case DATA_MODE -> ToolStationBlockEntity.this.mode.getButtonId(); // 定义 case DATA_MODE 分支，调用 getButtonId，执行当前流程所需的处理。
                case DATA_DISASSEMBLY_STATE -> ToolStationBlockEntity.this.disassemblyState; // 定义 case DATA_DISASSEMBLY_STATE 分支，产生此情况对应的结果。
                default -> 0; // 定义 default 分支，产生此情况对应的结果。
            }; // 结束当前作用域并完成本次表达式或声明。
        } // 结束当前作用域。

        @Override // 覆盖父类或接口的方法，保持原有调用契约。
        public void set(int index, int value) { // 定义 set 的调用入口，参数和具体处理见下方实现。
            // 只有方块实体可以修改权威状态，客户端数据槽使用 SimpleContainerData。
        } // 结束当前作用域。

        @Override // 覆盖父类或接口的方法，保持原有调用契约。
        public int getCount() { // 定义 getCount 的调用入口，参数和具体处理见下方实现。
            return DATA_COUNT; // 返回 DATA_COUNT，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    }; // 结束当前作用域并完成本次表达式或声明。

    public ToolStationBlockEntity(BlockPos blockPos, BlockState blockState) { // 定义 ToolStationBlockEntity 的调用入口，参数和具体处理见下方实现。
        super(TableEntityRegistries.TOOL_STATION_BLOCK_ENTITY.get(), blockPos, blockState); // 读取指定条目或延迟注册对象。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int getContainerSize() { // 定义 getContainerSize 的调用入口，参数和具体处理见下方实现。
        return CONTAINER_SIZE; // 返回 CONTAINER_SIZE，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public NonNullList<ItemStack> getItems() { // 定义 getItems 的调用入口，参数和具体处理见下方实现。
        return this.items; // 返回 this.items，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void setItems(NonNullList<ItemStack> items) { // 定义 setItems 的调用入口，参数和具体处理见下方实现。
        this.items = items; // 设置 this.items 为 items。
    } // 结束当前作用域。

    public ContainerData getMenuData() { // 定义 getMenuData 的调用入口，参数和具体处理见下方实现。
        return this.menuData; // 返回 this.menuData，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public Mode getMode() { // 定义 getMode 的调用入口，参数和具体处理见下方实现。
        return this.mode; // 返回 this.mode，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isDisassemblyPreview() { // 定义 isDisassemblyPreview 的调用入口，参数和具体处理见下方实现。
        return this.disassemblyState == DISASSEMBLY_PREVIEW; // 返回 this.disassemblyState == DISASSEMBLY_PREVIEW，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean trySwitchMode(Mode targetMode, Player player) { // 定义 trySwitchMode 的调用入口，参数和具体处理见下方实现。
        if (targetMode == null) { // 检查 targetMode == null；按条件决定是否执行本分支。
            return false; // 返回 false，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (targetMode == this.mode) { // 检查 targetMode == this.mode；按条件决定是否执行本分支。
            return true; // 返回 true，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        if (this.isDisassemblyPreview()) { // 检查 this.isDisassemblyPreview()；按条件决定是否执行本分支。
            if (this.mode != Mode.DISASSEMBLE || this.getItem(RESULT_SLOT).isEmpty()) { // 检查 this.mode != Mode.DISASSEMBLE || this.getItem(RESULT_SLOT).isEmpty()；按条件决定是否执行本分支。
                return false; // 返回 false，将当前分支的结果交给调用者。
            } // 结束当前作用域。
            this.cancelDisassemblyPreview(player, true); // 调用 cancelDisassemblyPreview，执行当前流程所需的处理。
        } else if (this.disassemblyState == DISASSEMBLY_COMMITTED) { // 前一条件不成立时继续检查本分支条件。
            this.disassemblyState = DISASSEMBLY_NONE; // 设置 this.disassemblyState 为 DISASSEMBLY_NONE。
        } // 结束当前作用域。

        this.mode = targetMode; // 设置 this.mode 为 targetMode。
        this.updateResult(); // 根据当前输入重新生成结果预览。
        return true; // 返回 true，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public void inputsChanged() { // 定义 inputsChanged 的调用入口，参数和具体处理见下方实现。
        // 一次确认操作可能修改多个输入，等待全部修改完成后再重建
        // 预览并执行确定性的修复回调。
        if (this.consumingInputs) return; // 检查 this.consumingInputs；按条件决定是否执行本分支。
        if (this.disassemblyState == DISASSEMBLY_COMMITTED && this.areInputSlotsEmpty()) { // 检查 this.disassemblyState == DISASSEMBLY_COMMITTED && this.areInputSlotsEmpty()；按条件决定是否执行本分支。
            this.disassemblyState = DISASSEMBLY_NONE; // 设置 this.disassemblyState 为 DISASSEMBLY_NONE。
        } // 结束当前作用域。
        this.updateResult(); // 根据当前输入重新生成结果预览。
    } // 结束当前作用域。

    public boolean canPlaceDisassemblySource(ItemStack stack) { // 定义 canPlaceDisassemblySource 的调用入口，参数和具体处理见下方实现。
        return this.mode == Mode.DISASSEMBLE // 返回 this.mode == Mode.DISASSEMBLE，将当前分支的结果交给调用者。
                && this.disassemblyState == DISASSEMBLY_NONE // 在 ToolStationBlockEntity 中补充当前声明或表达式的 && this.disassemblyState == DISASSEMBLY_NONE 部分。
                && this.getItem(RESULT_SLOT).isEmpty() // 调用 getItem，执行当前流程所需的处理。
                && this.areInputSlotsEmpty() // 调用 areInputSlotsEmpty，执行当前流程所需的处理。
                && ToolStationDisassemblyRecipes.canDisassemble(stack); // 调用 canDisassemble，执行当前流程所需的处理。
    } // 结束当前作用域。

    public boolean canTakeResult() { // 定义 canTakeResult 的调用入口，参数和具体处理见下方实现。
        if (this.mode == Mode.DISASSEMBLE) { // 检查 this.mode == Mode.DISASSEMBLE；按条件决定是否执行本分支。
            return this.isDisassemblyPreview() && !this.getItem(RESULT_SLOT).isEmpty(); // 返回 this.isDisassemblyPreview() && !this.getItem(RESULT_SLOT).isEmpty()，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        // 预览发送后，其他玩家或自动化可能已改变输入。
        // 将结果交给玩家前，必须在服务端重新校验。
        ItemStack expected = this.createCraftingResult(); // 设置 expected 为 this.createCraftingResult()。
        if (!ItemStack.matches(expected, this.getItem(RESULT_SLOT))) { // 检查 !ItemStack.matches(expected, this.getItem(RESULT_SLOT))；按条件决定是否执行本分支。
            this.updateResult(); // 根据当前输入重新生成结果预览。
            return false; // 返回 false，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return !expected.isEmpty(); // 返回 !expected.isEmpty()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public void resultSlotChanged(ItemStack newStack) { // 定义 resultSlotChanged 的调用入口，参数和具体处理见下方实现。
        if (this.mode != Mode.DISASSEMBLE) { // 检查 this.mode != Mode.DISASSEMBLE；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        if (newStack.isEmpty()) { // 检查 newStack.isEmpty()；按条件决定是否执行本分支。
            this.cancelDisassemblyPreview(null, false); // 调用 cancelDisassemblyPreview，执行当前流程所需的处理。
        } else if (this.disassemblyState == DISASSEMBLY_NONE) { // 前一条件不成立时继续检查本分支条件。
            this.startDisassembly(newStack); // 根据源工具建立拆解预览。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public void resultTaken(Mode actionMode) { // 定义 resultTaken 的调用入口，参数和具体处理见下方实现。
        if (actionMode != this.mode) { // 检查 actionMode != this.mode；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        if (actionMode == Mode.DISASSEMBLE) { // 检查 actionMode == Mode.DISASSEMBLE；按条件决定是否执行本分支。
            this.cancelDisassemblyPreview(null, false); // 调用 cancelDisassemblyPreview，执行当前流程所需的处理。
        } else { // 处理前述条件均不满足时的替代分支。
            this.takeCraftingResult(); // 调用 takeCraftingResult，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public void disassemblyPartTaken() { // 定义 disassemblyPartTaken 的调用入口，参数和具体处理见下方实现。
        if (!this.isDisassemblyPreview()) { // 检查 !this.isDisassemblyPreview()；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        // 取走第一个部件时，将预览部件确认为真实物品，并且只消耗
        // 一次源工具；之后取走其他部件按普通库存移除处理。
        this.disassemblyState = DISASSEMBLY_COMMITTED; // 设置 this.disassemblyState 为 DISASSEMBLY_COMMITTED。
        this.items.set(RESULT_SLOT, ItemStack.EMPTY); // 设置目标槽位、组件或属性的值。
        this.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public ItemStack removeItem(int slot, int amount) { // 定义 removeItem 的调用入口，参数和具体处理见下方实现。
        ItemStack removed = super.removeItem(slot, amount); // 设置 removed 为 super.removeItem(slot, amount)。
        this.commitDisassemblyAfterPartRemoval(slot, removed); // 调用 commitDisassemblyAfterPartRemoval，执行当前流程所需的处理。
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) { // 检查 slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()；按条件决定是否执行本分支。
            this.inputsChanged(); // 通知输入变化并刷新相关状态。
        } // 结束当前作用域。
        return removed; // 返回 removed，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public ItemStack removeItemNoUpdate(int slot) { // 定义 removeItemNoUpdate 的调用入口，参数和具体处理见下方实现。
        ItemStack removed = super.removeItemNoUpdate(slot); // 设置 removed 为 super.removeItemNoUpdate(slot)。
        this.commitDisassemblyAfterPartRemoval(slot, removed); // 调用 commitDisassemblyAfterPartRemoval，执行当前流程所需的处理。
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) { // 检查 slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()；按条件决定是否执行本分支。
            this.inputsChanged(); // 通知输入变化并刷新相关状态。
        } // 结束当前作用域。
        return removed; // 返回 removed，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void setItem(int slot, ItemStack stack) { // 定义 setItem 的调用入口，参数和具体处理见下方实现。
        super.setItem(slot, stack); // 更新指定物品槽位。
        if (slot >= 0 && slot < INPUT_SLOT_COUNT) { // 检查 slot >= 0 && slot < INPUT_SLOT_COUNT；按条件决定是否执行本分支。
            this.inputsChanged(); // 通知输入变化并刷新相关状态。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void clearContent() { // 定义 clearContent 的调用入口，参数和具体处理见下方实现。
        super.clearContent(); // 调用 clearContent，执行当前流程所需的处理。
        this.disassemblyState = DISASSEMBLY_NONE; // 设置 this.disassemblyState 为 DISASSEMBLY_NONE。
        this.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean canPlaceItem(int slot, ItemStack stack) { // 定义 canPlaceItem 的调用入口，参数和具体处理见下方实现。
        if (slot < 0 || slot >= INPUT_SLOT_COUNT || this.mode == Mode.DISASSEMBLE // 检查 slot < 0 || slot >= INPUT_SLOT_COUNT || this.mode == Mode.DISASSEMBLE；按条件决定是否执行本分支。
                || !com.gctn.tconstruct.tables.data.ToolStationSlotPositions.isInputSlotActive(this.mode, slot)) { // 调用 isInputSlotActive，执行当前流程所需的处理。
            return false; // 返回 false，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode); // 设置 recipe 为 ToolStationAssemblyRecipes.get(this.mode)。
        return recipe == null || recipe.getRequiredItem(slot) == null || stack.is(recipe.getRequiredItem(slot)); // 返回 recipe == null || recipe.getRequiredItem(slot) == null || stack.is(recipe.getRequiredItem(slot))，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean canTakeItem(Container target, int slot, ItemStack stack) { // 定义 canTakeItem 的调用入口，参数和具体处理见下方实现。
        // 结果槽具有合成事务语义，只允许通过 ToolStationResultSlot 领取。
        return slot >= 0 && slot < INPUT_SLOT_COUNT; // 返回 slot >= 0 && slot < INPUT_SLOT_COUNT，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private void commitDisassemblyAfterPartRemoval(int slot, ItemStack removed) { // 定义 commitDisassemblyAfterPartRemoval 的调用入口，参数和具体处理见下方实现。
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) { // 检查 slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()；按条件决定是否执行本分支。
            this.disassemblyPartTaken(); // 调用 disassemblyPartTaken，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    /** 方块掉落真实库存前，先移除生成的部件预览。 */
    public void prepareForRemoval() { // 定义 prepareForRemoval 的调用入口，参数和具体处理见下方实现。
        if (this.isDisassemblyPreview()) { // 检查 this.isDisassemblyPreview()；按条件决定是否执行本分支。
            this.clearInputSlots(); // 调用 clearInputSlots，执行当前流程所需的处理。
            this.disassemblyState = DISASSEMBLY_NONE; // 设置 this.disassemblyState 为 DISASSEMBLY_NONE。
        } else if (this.mode != Mode.DISASSEMBLE) { // 前一条件不成立时继续检查本分支条件。
            // 合成结果由输入派生，不能作为额外物品一并掉落。
            this.items.set(RESULT_SLOT, ItemStack.EMPTY); // 设置目标槽位、组件或属性的值。
        } // 结束当前作用域。
        this.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。

    private void updateResult() { // 定义 updateResult 的调用入口，参数和具体处理见下方实现。
        if (this.mode == Mode.DISASSEMBLE) { // 检查 this.mode == Mode.DISASSEMBLE；按条件决定是否执行本分支。
            if (!this.isDisassemblyPreview()) { // 检查 !this.isDisassemblyPreview()；按条件决定是否执行本分支。
                this.items.set(RESULT_SLOT, ItemStack.EMPTY); // 设置目标槽位、组件或属性的值。
            } // 结束当前作用域。
        } else { // 处理前述条件均不满足时的替代分支。
            this.items.set(RESULT_SLOT, this.createCraftingResult()); // 设置目标槽位、组件或属性的值。
        } // 结束当前作用域。
        this.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。

    private ItemStack createCraftingResult() { // 定义 createCraftingResult 的调用入口，参数和具体处理见下方实现。
        if (this.mode == Mode.DEFAULT) { // 检查 this.mode == Mode.DEFAULT；按条件决定是否执行本分支。
            return ToolStationDefaultRecipes.createResult(this); // 返回 ToolStationDefaultRecipes.createResult(this)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode); // 设置 recipe 为 ToolStationAssemblyRecipes.get(this.mode)。
        return recipe == null ? ItemStack.EMPTY : recipe.createResult(this); // 返回 recipe == null ? ItemStack.EMPTY : recipe.createResult(this)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private void takeCraftingResult() { // 定义 takeCraftingResult 的调用入口，参数和具体处理见下方实现。
        if (this.createCraftingResult().isEmpty()) { // 检查 this.createCraftingResult().isEmpty()；按条件决定是否执行本分支。
            this.updateResult(); // 根据当前输入重新生成结果预览。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        this.consumingInputs = true; // 设置 this.consumingInputs 为 true。
        try { // 执行需要保证清理或资源关闭的操作。
            if (this.mode == Mode.DEFAULT) { // 检查 this.mode == Mode.DEFAULT；按条件决定是否执行本分支。
                ToolStationDefaultRecipes.consumeInputs(this); // 在确认结果后消耗对应原料。
            } else { // 处理前述条件均不满足时的替代分支。
                ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode); // 设置 recipe 为 ToolStationAssemblyRecipes.get(this.mode)。
                if (recipe != null) recipe.consumeInputs(this); // 检查 recipe != null；按条件决定是否执行本分支。
            } // 结束当前作用域。
        } finally { // 无论前面操作是否成功，都执行状态恢复或清理。
            this.consumingInputs = false; // 设置 this.consumingInputs 为 false。
        } // 结束当前作用域。
        this.updateResult(); // 根据当前输入重新生成结果预览。
    } // 结束当前作用域。

    private void startDisassembly(ItemStack sourceTool) { // 定义 startDisassembly 的调用入口，参数和具体处理见下方实现。
        PartList parts = ToolStationDisassemblyRecipes.createParts(sourceTool); // 设置 parts 为 ToolStationDisassemblyRecipes.createParts(sourceTool)。
        if (parts.isEmpty() || !this.areInputSlotsEmpty()) { // 检查 parts.isEmpty() || !this.areInputSlotsEmpty()；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        this.disassemblyState = DISASSEMBLY_PREVIEW; // 设置 this.disassemblyState 为 DISASSEMBLY_PREVIEW。
        ItemStack[] partStacks = parts.parts(); // 设置 partStacks 为 parts.parts()。
        for (int inputSlot = 0; inputSlot < partStacks.length; inputSlot++) { // 遍历 int inputSlot = 0; inputSlot < partStacks.length; inputSlot++，逐项执行当前处理。
            this.items.set(inputSlot, partStacks[inputSlot].copy()); // 设置目标槽位、组件或属性的值。
        } // 结束当前作用域。
        this.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。

    private void cancelDisassemblyPreview(Player player, boolean returnSource) { // 定义 cancelDisassemblyPreview 的调用入口，参数和具体处理见下方实现。
        if (!this.isDisassemblyPreview()) { // 检查 !this.isDisassemblyPreview()；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        ItemStack sourceTool = this.items.get(RESULT_SLOT); // 设置 sourceTool 为 this.items.get(RESULT_SLOT)。
        this.items.set(RESULT_SLOT, ItemStack.EMPTY); // 设置目标槽位、组件或属性的值。
        this.clearInputSlots(); // 调用 clearInputSlots，执行当前流程所需的处理。
        this.disassemblyState = DISASSEMBLY_NONE; // 设置 this.disassemblyState 为 DISASSEMBLY_NONE。

        if (returnSource && player != null && !sourceTool.isEmpty()) { // 检查 returnSource && player != null && !sourceTool.isEmpty()；按条件决定是否执行本分支。
            player.getInventory().placeItemBackInInventory(sourceTool); // 将真实物品交还玩家背包。
        } // 结束当前作用域。
        this.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
    } // 结束当前作用域。

    private void clearInputSlots() { // 定义 clearInputSlots 的调用入口，参数和具体处理见下方实现。
        for (int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++) { // 遍历 int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++，逐项执行当前处理。
            this.items.set(inputSlot, ItemStack.EMPTY); // 设置目标槽位、组件或属性的值。
        } // 结束当前作用域。
    } // 结束当前作用域。

    private boolean areInputSlotsEmpty() { // 定义 areInputSlotsEmpty 的调用入口，参数和具体处理见下方实现。
        for (int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++) { // 遍历 int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++，逐项执行当前处理。
            if (!this.getItem(inputSlot).isEmpty()) { // 检查 !this.getItem(inputSlot).isEmpty()；按条件决定是否执行本分支。
                return false; // 返回 false，将当前分支的结果交给调用者。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return true; // 返回 true，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 定义 loadAdditional 的调用入口，参数和具体处理见下方实现。
        super.loadAdditional(tag, registries); // 调用 loadAdditional，执行当前流程所需的处理。
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY); // 设置 this.items 为 NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY)。
        ContainerHelper.loadAllItems(tag, this.items, registries); // 调用 loadAllItems，执行当前流程所需的处理。

        Mode loadedMode = Mode.byButtonId(tag.getInt(TAG_MODE)); // 设置 loadedMode 为 Mode.byButtonId(tag.getInt(TAG_MODE))。
        this.mode = loadedMode == null ? Mode.DEFAULT : loadedMode; // 设置 this.mode 为 loadedMode == null ? Mode.DEFAULT : loadedMode。
        int loadedState = tag.getInt(TAG_DISASSEMBLY_STATE); // 设置 loadedState 为 tag.getInt(TAG_DISASSEMBLY_STATE)。
        this.disassemblyState = loadedState >= DISASSEMBLY_NONE && loadedState <= DISASSEMBLY_COMMITTED // 设置 this.disassemblyState 为 loadedState >= DISASSEMBLY_NONE && loadedState <= DISASSEMBLY_COMMITTED。
                ? loadedState // 在 ToolStationBlockEntity 中补充当前声明或表达式的 ? loadedState 部分。
                : DISASSEMBLY_NONE; // 在 ToolStationBlockEntity 中补充当前声明或表达式的 : DISASSEMBLY_NONE 部分。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { // 定义 saveAdditional 的调用入口，参数和具体处理见下方实现。
        super.saveAdditional(tag, registries); // 调用 saveAdditional，执行当前流程所需的处理。
        ContainerHelper.saveAllItems(tag, this.items, registries); // 调用 saveAllItems，执行当前流程所需的处理。
        tag.putInt(TAG_MODE, this.mode.getButtonId()); // 写入整型存档字段。
        tag.putInt(TAG_DISASSEMBLY_STATE, this.disassemblyState); // 写入整型存档字段。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) { // 定义 createMenu 的调用入口，参数和具体处理见下方实现。
        this.updateResult(); // 根据当前输入重新生成结果预览。
        return new ToolStationMenu(containerId, inventory, this); // 返回 new ToolStationMenu(containerId, inventory, this)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected Component getDefaultName() { // 定义 getDefaultName 的调用入口，参数和具体处理见下方实现。
        return Component.translatable("container.tconstruct.toolstation.name"); // 返回 Component.translatable("container.tconstruct.toolstation.name")，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
