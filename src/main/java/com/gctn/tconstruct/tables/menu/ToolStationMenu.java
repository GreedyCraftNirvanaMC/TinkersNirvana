package com.gctn.tconstruct.tables.menu; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity; // 引入 com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity，供本文件使用。
import com.gctn.tconstruct.tables.data.Mode; // 引入 com.gctn.tconstruct.tables.data.Mode，供本文件使用。
import com.gctn.tconstruct.tables.data.ModeAwareInputSlot; // 引入 com.gctn.tconstruct.tables.data.ModeAwareInputSlot，供本文件使用。
import com.gctn.tconstruct.tables.data.ToolStationResultSlot; // 引入 com.gctn.tconstruct.tables.data.ToolStationResultSlot，供本文件使用。
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions; // 引入 com.gctn.tconstruct.tables.data.ToolStationSlotPositions，供本文件使用。
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition; // 引入 com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolAssemblyRecipe; // 引入 com.gctn.tconstruct.tables.recipe.ToolAssemblyRecipe，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes; // 引入 com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes，供本文件使用。
import com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes; // 引入 com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes，供本文件使用。
import net.minecraft.world.Container; // 引入 net.minecraft.world.Container，供本文件使用。
import net.minecraft.world.SimpleContainer; // 引入 net.minecraft.world.SimpleContainer，供本文件使用。
import net.minecraft.world.entity.player.Inventory; // 引入 net.minecraft.world.entity.player.Inventory，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.inventory.AbstractContainerMenu; // 引入 net.minecraft.world.inventory.AbstractContainerMenu，供本文件使用。
import net.minecraft.world.inventory.ContainerData; // 引入 net.minecraft.world.inventory.ContainerData，供本文件使用。
import net.minecraft.world.inventory.SimpleContainerData; // 引入 net.minecraft.world.inventory.SimpleContainerData，供本文件使用。
import net.minecraft.world.inventory.Slot; // 引入 net.minecraft.world.inventory.Slot，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

/**
 * 将工具台状态连接到模式感知槽位和原版物品栏点击逻辑。
 * 每个物理输入槽有多个布局视图，只有当前模式对应的视图允许接收物品转移。
 * 预览生成与原料消耗由方块实体负责，客户端界面不直接处理这些操作。
 */
public class ToolStationMenu extends AbstractContainerMenu { // 定义 ToolStationMenu 的实现类型，具体职责见上方说明。
    public static final int CONTAINER_SIZE = 7; // 设置 CONTAINER_SIZE 为 7。

    public static final int MOVING_SLOT_INDEX = 0; // 设置 MOVING_SLOT_INDEX 为 0。
    public static final int CONDITIONAL_SLOT_INDEX = 1; // 设置 CONDITIONAL_SLOT_INDEX 为 1。
    public static final int RESULT_SLOT_INDEX = 6; // 设置 RESULT_SLOT_INDEX 为 6。
    public static final int INPUT_SLOT_COUNT = ToolStationSlotPositions.INPUT_SLOT_COUNT; // 设置 INPUT_SLOT_COUNT 为 ToolStationSlotPositions.INPUT_SLOT_COUNT。

    public static final int DEFAULT_MODE_BUTTON = Mode.DEFAULT.getButtonId(); // 设置 DEFAULT_MODE_BUTTON 为 Mode.DEFAULT.getButtonId()。
    public static final int DISASSEMBLE_MODE_BUTTON = Mode.DISASSEMBLE.getButtonId(); // 设置 DISASSEMBLE_MODE_BUTTON 为 Mode.DISASSEMBLE.getButtonId()。
    public static final int PICKAXE_MODE_BUTTON = Mode.PICKAXE.getButtonId(); // 设置 PICKAXE_MODE_BUTTON 为 Mode.PICKAXE.getButtonId()。
    public static final int SHOVEL_MODE_BUTTON = Mode.SHOVEL.getButtonId(); // 设置 SHOVEL_MODE_BUTTON 为 Mode.SHOVEL.getButtonId()。
    public static final int AXE_MODE_BUTTON = Mode.AXE.getButtonId(); // 设置 AXE_MODE_BUTTON 为 Mode.AXE.getButtonId()。
    public static final int DEBUG_MODE_BUTTON = Mode.DEBUG.getButtonId(); // 设置 DEBUG_MODE_BUTTON 为 Mode.DEBUG.getButtonId()。

    private static final int DATA_MODE = 0; // 设置 DATA_MODE 为 0。
    private static final int DATA_DISASSEMBLY_STATE = 1; // 设置 DATA_DISASSEMBLY_STATE 为 1。
    private static final int DISASSEMBLY_PREVIEW = 1; // 设置 DISASSEMBLY_PREVIEW 为 1。

    private static final int RESULT_MENU_SLOT = 0; // 设置 RESULT_MENU_SLOT 为 0。
    private static final int INPUT_MENU_SLOT_START = 1; // 设置 INPUT_MENU_SLOT_START 为 1。
    private static final int TOOL_STATION_SLOT_COUNT = 1 + Mode.values().length * INPUT_SLOT_COUNT; // 设置 TOOL_STATION_SLOT_COUNT 为 1 + Mode.values().length * INPUT_SLOT_COUNT。

    private static final int PLAYER_INVENTORY_COLUMNS = 9; // 设置 PLAYER_INVENTORY_COLUMNS 为 9。
    private static final int PLAYER_INVENTORY_ROWS = 3; // 设置 PLAYER_INVENTORY_ROWS 为 3。
    private static final int PLAYER_INVENTORY_START = TOOL_STATION_SLOT_COUNT; // 设置 PLAYER_INVENTORY_START 为 TOOL_STATION_SLOT_COUNT。
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + PLAYER_INVENTORY_COLUMNS * PLAYER_INVENTORY_ROWS; // 设置 PLAYER_INVENTORY_END 为 PLAYER_INVENTORY_START + PLAYER_INVENTORY_COLUMNS * PLAYER_INVENTORY_ROWS。
    private static final int HOTBAR_START = PLAYER_INVENTORY_END; // 设置 HOTBAR_START 为 PLAYER_INVENTORY_END。
    private static final int HOTBAR_END = HOTBAR_START + PLAYER_INVENTORY_COLUMNS; // 设置 HOTBAR_END 为 HOTBAR_START + PLAYER_INVENTORY_COLUMNS。

    private static final int MAX_VISIBLE_SPECIAL_MODE_INPUTS = 3; // 设置 MAX_VISIBLE_SPECIAL_MODE_INPUTS 为 3。
    private static final int RESULT_SLOT_X = 124; // 设置 RESULT_SLOT_X 为 124。
    private static final int RESULT_SLOT_Y = 38; // 设置 RESULT_SLOT_Y 为 38。
    private static final int PLAYER_INVENTORY_X = 8; // 设置 PLAYER_INVENTORY_X 为 8。
    private static final int PLAYER_INVENTORY_Y = 92; // 设置 PLAYER_INVENTORY_Y 为 92。
    private static final int HOTBAR_Y = 150; // 设置 HOTBAR_Y 为 150。
    private static final int SLOT_SPACING = 18; // 设置 SLOT_SPACING 为 18。

    private final Container toolStation; // 声明 toolStation，保存后续流程使用的状态或依赖。
    private final ContainerData data; // 声明 data，保存后续流程使用的状态或依赖。
    private final ToolStationBlockEntity blockEntity; // 声明 blockEntity，保存后续流程使用的状态或依赖。

    public ToolStationMenu(int containerId, Inventory playerInventory) { // 定义 ToolStationMenu 的调用入口，参数和具体处理见下方实现。
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SIZE), // 调用 this，执行当前流程所需的处理。
                new SimpleContainerData(ToolStationBlockEntity.DATA_COUNT), null); // 创建 new SimpleContainerData(ToolStationBlockEntity.DATA_COUNT), null)，作为当前对象或调用的组成部分。
    } // 结束当前作用域。

    public ToolStationMenu(int containerId, Inventory playerInventory, ToolStationBlockEntity blockEntity) { // 定义 ToolStationMenu 的调用入口，参数和具体处理见下方实现。
        this(containerId, playerInventory, blockEntity, blockEntity.getMenuData(), blockEntity); // 调用 this，执行当前流程所需的处理。
    } // 结束当前作用域。

    private ToolStationMenu(int containerId, Inventory playerInventory, Container container, // 定义 ToolStationMenu 的调用入口，参数和具体处理见下方实现。
                            ContainerData data, ToolStationBlockEntity blockEntity) { // 在 ToolStationMenu 中补充当前声明或表达式的 ContainerData data, ToolStationBlockEntity blockEntity) { 部分。
        super(TableMenuRegistries.TOOL_STATION_MENU.get(), containerId); // 读取指定条目或延迟注册对象。
        checkContainerSize(container, CONTAINER_SIZE); // 调用 checkContainerSize，执行当前流程所需的处理。
        checkContainerDataCount(data, ToolStationBlockEntity.DATA_COUNT); // 调用 checkContainerDataCount，执行当前流程所需的处理。
        this.toolStation = container; // 设置 this.toolStation 为 container。
        this.data = data; // 设置 this.data 为 data。
        this.blockEntity = blockEntity; // 设置 this.blockEntity 为 blockEntity。
        container.startOpen(playerInventory.player); // 调用 startOpen，执行当前流程所需的处理。

        this.addToolStationSlots(container); // 调用 addToolStationSlots，执行当前流程所需的处理。
        this.addPlayerInventorySlots(playerInventory); // 调用 addPlayerInventorySlots，执行当前流程所需的处理。
        this.addDataSlots(data); // 挂接需要在客户端与服务端之间同步的菜单数据。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) { // 定义 quickMoveStack 的调用入口，参数和具体处理见下方实现。
        if (quickMovedSlotIndex < 0 || quickMovedSlotIndex >= this.slots.size()) { // 检查 quickMovedSlotIndex < 0 || quickMovedSlotIndex >= this.slots.size()；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex); // 设置 quickMovedSlot 为 this.slots.get(quickMovedSlotIndex)。
        if (quickMovedSlot == null || !quickMovedSlot.hasItem() || !quickMovedSlot.mayPickup(player)) { // 检查 quickMovedSlot == null || !quickMovedSlot.hasItem() || !quickMovedSlot.mayPickup(player)；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        ItemStack rawStack = quickMovedSlot.getItem(); // 设置 rawStack 为 quickMovedSlot.getItem()。
        ItemStack quickMovedStack = rawStack.copy(); // 设置 quickMovedStack 为 rawStack.copy()。
        if (!this.moveQuickMovedStack(quickMovedSlotIndex, rawStack)) { // 检查 !this.moveQuickMovedStack(quickMovedSlotIndex, rawStack)；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        if (quickMovedSlotIndex == RESULT_MENU_SLOT) { // 检查 quickMovedSlotIndex == RESULT_MENU_SLOT；按条件决定是否执行本分支。
            quickMovedSlot.onQuickCraft(rawStack, quickMovedStack); // 调用 onQuickCraft，执行当前流程所需的处理。
        } // 结束当前作用域。

        if (rawStack.isEmpty()) { // 检查 rawStack.isEmpty()；按条件决定是否执行本分支。
            quickMovedSlot.setByPlayer(ItemStack.EMPTY); // 调用 setByPlayer，执行当前流程所需的处理。
        } else { // 处理前述条件均不满足时的替代分支。
            quickMovedSlot.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
        } // 结束当前作用域。

        if (rawStack.getCount() == quickMovedStack.getCount()) { // 检查 rawStack.getCount() == quickMovedStack.getCount()；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        quickMovedSlot.onTake(player, rawStack); // 调用 onTake，执行当前流程所需的处理。
        return quickMovedStack; // 返回 quickMovedStack，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void removed(Player player) { // 定义 removed 的调用入口，参数和具体处理见下方实现。
        super.removed(player); // 调用 removed，执行当前流程所需的处理。
        this.toolStation.stopOpen(player); // 调用 stopOpen，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean stillValid(Player player) { // 定义 stillValid 的调用入口，参数和具体处理见下方实现。
        return this.toolStation.stillValid(player); // 返回 this.toolStation.stillValid(player)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean clickMenuButton(Player player, int id) { // 定义 clickMenuButton 的调用入口，参数和具体处理见下方实现。
        Mode selectedMode = Mode.byButtonId(id); // 设置 selectedMode 为 Mode.byButtonId(id)。
        if (selectedMode == null || !this.isModeButtonVisible(selectedMode) || this.blockEntity == null) { // 检查 selectedMode == null || !this.isModeButtonVisible(selectedMode) || this.blockEntity == null；按条件决定是否执行本分支。
            return false; // 返回 false，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return this.blockEntity.trySwitchMode(selectedMode, player); // 返回 this.blockEntity.trySwitchMode(selectedMode, player)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public void inputsChanged() { // 定义 inputsChanged 的调用入口，参数和具体处理见下方实现。
        if (this.blockEntity != null) { // 检查 this.blockEntity != null；按条件决定是否执行本分支。
            this.blockEntity.inputsChanged(); // 通知输入变化并刷新相关状态。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public Mode getMode() { // 定义 getMode 的调用入口，参数和具体处理见下方实现。
        Mode currentMode = Mode.byButtonId(this.data.get(DATA_MODE)); // 设置 currentMode 为 Mode.byButtonId(this.data.get(DATA_MODE))。
        return currentMode == null ? Mode.DEFAULT : currentMode; // 返回 currentMode == null ? Mode.DEFAULT : currentMode，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isMode(Mode mode) { // 定义 isMode 的调用入口，参数和具体处理见下方实现。
        return this.getMode() == mode; // 返回 this.getMode() == mode，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isModeButtonVisible(Mode mode) { // 定义 isModeButtonVisible 的调用入口，参数和具体处理见下方实现。
        return mode == Mode.DEFAULT // 返回 mode == Mode.DEFAULT，将当前分支的结果交给调用者。
                || mode == Mode.DISASSEMBLE // 在 ToolStationMenu 中补充当前声明或表达式的 || mode == Mode.DISASSEMBLE 部分。
                || ToolStationSlotPositions.getActiveInputSlotCount(mode) <= MAX_VISIBLE_SPECIAL_MODE_INPUTS; // 调用 getActiveInputSlotCount，执行当前流程所需的处理。
    } // 结束当前作用域。

    public boolean isInputSlotVisible(int inputSlot) { // 定义 isInputSlotVisible 的调用入口，参数和具体处理见下方实现。
        return this.isInputSlotInsertable(inputSlot) || !this.toolStation.getItem(inputSlot).isEmpty(); // 返回 this.isInputSlotInsertable(inputSlot) || !this.toolStation.getItem(inputSlot).isEmpty()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isInputSlotAvailable(Mode slotMode, int inputSlot) { // 定义 isInputSlotAvailable 的调用入口，参数和具体处理见下方实现。
        return this.isMode(slotMode) // 返回 this.isMode(slotMode)，将当前分支的结果交给调用者。
                && (ToolStationSlotPositions.isInputSlotActive(slotMode, inputSlot) // 调用 isInputSlotActive，执行当前流程所需的处理。
                || !this.toolStation.getItem(inputSlot).isEmpty()); // 调用 getItem，执行当前流程所需的处理。
    } // 结束当前作用域。

    public boolean isInputSlotInsertable(int inputSlot) { // 定义 isInputSlotInsertable 的调用入口，参数和具体处理见下方实现。
        return ToolStationSlotPositions.isInputSlotActive(this.getMode(), inputSlot); // 返回 ToolStationSlotPositions.isInputSlotActive(this.getMode(), inputSlot)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public SlotPosition getInputSlotPosition(int inputSlot) { // 定义 getInputSlotPosition 的调用入口，参数和具体处理见下方实现。
        return ToolStationSlotPositions.getDisplayInputSlotPosition(this.getMode(), inputSlot); // 返回 ToolStationSlotPositions.getDisplayInputSlotPosition(this.getMode(), inputSlot)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public ItemStack getInputSlotIcon(int inputSlot) { // 定义 getInputSlotIcon 的调用入口，参数和具体处理见下方实现。
        if (!this.isInputSlotInsertable(inputSlot)) { // 检查 !this.isInputSlotInsertable(inputSlot)；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        Item requiredItem = this.getRequiredInputItem(this.getMode(), inputSlot); // 设置 requiredItem 为 this.getRequiredInputItem(this.getMode(), inputSlot)。
        return requiredItem == null ? ItemStack.EMPTY : new ItemStack(requiredItem); // 返回 requiredItem == null ? ItemStack.EMPTY : new ItemStack(requiredItem)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public Item getRequiredInputItem(Mode targetMode, int inputSlot) { // 定义 getRequiredInputItem 的调用入口，参数和具体处理见下方实现。
        if (!ToolStationSlotPositions.isInputSlotActive(targetMode, inputSlot)) { // 检查 !ToolStationSlotPositions.isInputSlotActive(targetMode, inputSlot)；按条件决定是否执行本分支。
            return null; // 返回 null，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(targetMode); // 设置 recipe 为 ToolStationAssemblyRecipes.get(targetMode)。
        return recipe == null ? null : recipe.getRequiredItem(inputSlot); // 返回 recipe == null ? null : recipe.getRequiredItem(inputSlot)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean canPlaceInput(Mode slotMode, int inputSlot, ItemStack stack) { // 定义 canPlaceInput 的调用入口，参数和具体处理见下方实现。
        if (!this.isMode(slotMode) || !ToolStationSlotPositions.isInputSlotActive(slotMode, inputSlot)) { // 检查 !this.isMode(slotMode) || !ToolStationSlotPositions.isInputSlotActive(slotMode, inputSlot)；按条件决定是否执行本分支。
            return false; // 返回 false，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (slotMode == Mode.DISASSEMBLE) { // 检查 slotMode == Mode.DISASSEMBLE；按条件决定是否执行本分支。
            return false; // 返回 false，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        Item requiredItem = this.getRequiredInputItem(slotMode, inputSlot); // 设置 requiredItem 为 this.getRequiredInputItem(slotMode, inputSlot)。
        return requiredItem == null || stack.is(requiredItem); // 返回 requiredItem == null || stack.is(requiredItem)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean canPlaceResultSlot(ItemStack stack) { // 定义 canPlaceResultSlot 的调用入口，参数和具体处理见下方实现。
        return this.blockEntity != null // 返回 this.blockEntity != null，将当前分支的结果交给调用者。
                ? this.blockEntity.canPlaceDisassemblySource(stack) // 调用 canPlaceDisassemblySource，执行当前流程所需的处理。
                : this.isMode(Mode.DISASSEMBLE) // 调用 isMode，执行当前流程所需的处理。
                && this.data.get(DATA_DISASSEMBLY_STATE) == 0 // 读取指定条目或延迟注册对象。
                && this.toolStation.getItem(RESULT_SLOT_INDEX).isEmpty() // 调用 getItem，执行当前流程所需的处理。
                && this.areInputSlotsEmpty() // 调用 areInputSlotsEmpty，执行当前流程所需的处理。
                && ToolStationDisassemblyRecipes.canDisassemble(stack); // 调用 canDisassemble，执行当前流程所需的处理。
    } // 结束当前作用域。

    public boolean canTakeResultSlot() { // 定义 canTakeResultSlot 的调用入口，参数和具体处理见下方实现。
        if (this.blockEntity != null) { // 检查 this.blockEntity != null；按条件决定是否执行本分支。
            return this.blockEntity.canTakeResult(); // 返回 this.blockEntity.canTakeResult()，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return this.isMode(Mode.DISASSEMBLE) // 返回 this.isMode(Mode.DISASSEMBLE)，将当前分支的结果交给调用者。
                ? this.data.get(DATA_DISASSEMBLY_STATE) == DISASSEMBLY_PREVIEW // 读取指定条目或延迟注册对象。
                && !this.toolStation.getItem(RESULT_SLOT_INDEX).isEmpty() // 调用 getItem，执行当前流程所需的处理。
                : !this.toolStation.getItem(RESULT_SLOT_INDEX).isEmpty(); // 调用 getItem，执行当前流程所需的处理。
    } // 结束当前作用域。

    public void resultSlotChanged(ItemStack newStack) { // 定义 resultSlotChanged 的调用入口，参数和具体处理见下方实现。
        if (this.blockEntity != null) { // 检查 this.blockEntity != null；按条件决定是否执行本分支。
            this.blockEntity.resultSlotChanged(newStack); // 调用 resultSlotChanged，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public void resultSlotTaken(Mode actionMode) { // 定义 resultSlotTaken 的调用入口，参数和具体处理见下方实现。
        if (this.blockEntity != null) { // 检查 this.blockEntity != null；按条件决定是否执行本分支。
            this.blockEntity.resultTaken(actionMode); // 调用 resultTaken，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public void disassemblyPartTaken() { // 定义 disassemblyPartTaken 的调用入口，参数和具体处理见下方实现。
        if (this.blockEntity != null) { // 检查 this.blockEntity != null；按条件决定是否执行本分支。
            this.blockEntity.disassemblyPartTaken(); // 调用 disassemblyPartTaken，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { // 定义 canTakeItemForPickAll 的调用入口，参数和具体处理见下方实现。
        return slot != this.slots.get(RESULT_MENU_SLOT) && super.canTakeItemForPickAll(stack, slot); // 返回 slot != this.slots.get(RESULT_MENU_SLOT) && super.canTakeItemForPickAll(stack, slot)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private void addToolStationSlots(Container container) { // 定义 addToolStationSlots 的调用入口，参数和具体处理见下方实现。
        this.addSlot(new ToolStationResultSlot(this, container, RESULT_SLOT_INDEX, RESULT_SLOT_X, RESULT_SLOT_Y)); // 向菜单登记物品槽位及其交互规则。

        for (Mode slotMode : Mode.values()) { // 遍历 Mode slotMode : Mode.values()，逐项执行当前处理。
            for (int slotIndex = 0; slotIndex < INPUT_SLOT_COUNT; slotIndex++) { // 遍历 int slotIndex = 0; slotIndex < INPUT_SLOT_COUNT; slotIndex++，逐项执行当前处理。
                SlotPosition position = ToolStationSlotPositions.getDisplayInputSlotPosition(slotMode, slotIndex); // 设置 position 为 ToolStationSlotPositions.getDisplayInputSlotPosition(slotMode, slotIndex)。
                this.addSlot(new ModeAwareInputSlot(this, container, slotIndex, // 向菜单登记物品槽位及其交互规则。
                        position.slotX(), position.slotY(), slotMode, slotIndex)); // 调用 slotX，执行当前流程所需的处理。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。

    private void addPlayerInventorySlots(Inventory playerInventory) { // 定义 addPlayerInventorySlots 的调用入口，参数和具体处理见下方实现。
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) { // 遍历 int row = 0; row < PLAYER_INVENTORY_ROWS; row++，逐项执行当前处理。
            for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) { // 遍历 int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++，逐项执行当前处理。
                int inventorySlot = column + row * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS; // 设置 inventorySlot 为 column + row * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS。
                int x = PLAYER_INVENTORY_X + column * SLOT_SPACING; // 设置 x 为 PLAYER_INVENTORY_X + column * SLOT_SPACING。
                int y = PLAYER_INVENTORY_Y + row * SLOT_SPACING; // 设置 y 为 PLAYER_INVENTORY_Y + row * SLOT_SPACING。
                this.addSlot(new Slot(playerInventory, inventorySlot, x, y)); // 向菜单登记物品槽位及其交互规则。
            } // 结束当前作用域。
        } // 结束当前作用域。

        for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) { // 遍历 int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++，逐项执行当前处理。
            int x = PLAYER_INVENTORY_X + column * SLOT_SPACING; // 设置 x 为 PLAYER_INVENTORY_X + column * SLOT_SPACING。
            this.addSlot(new Slot(playerInventory, column, x, HOTBAR_Y)); // 向菜单登记物品槽位及其交互规则。
        } // 结束当前作用域。
    } // 结束当前作用域。

    private boolean moveQuickMovedStack(int quickMovedSlotIndex, ItemStack stack) { // 定义 moveQuickMovedStack 的调用入口，参数和具体处理见下方实现。
        if (quickMovedSlotIndex == RESULT_MENU_SLOT) { // 检查 quickMovedSlotIndex == RESULT_MENU_SLOT；按条件决定是否执行本分支。
            return this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true); // 返回 this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (quickMovedSlotIndex >= PLAYER_INVENTORY_START && quickMovedSlotIndex < HOTBAR_END) { // 检查 quickMovedSlotIndex >= PLAYER_INVENTORY_START && quickMovedSlotIndex < HOTBAR_END；按条件决定是否执行本分支。
            return this.movePlayerStack(quickMovedSlotIndex, stack); // 返回 this.movePlayerStack(quickMovedSlotIndex, stack)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false); // 返回 this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private boolean movePlayerStack(int quickMovedSlotIndex, ItemStack stack) { // 定义 movePlayerStack 的调用入口，参数和具体处理见下方实现。
        if (this.isMode(Mode.DISASSEMBLE) // 检查 this.isMode(Mode.DISASSEMBLE)；按条件决定是否执行本分支。
                && this.moveItemStackTo(stack, RESULT_MENU_SLOT, RESULT_MENU_SLOT + 1, false)) { // 调用 moveItemStackTo，执行当前流程所需的处理。
            return true; // 返回 true，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (this.moveToActiveInputs(stack)) { // 检查 this.moveToActiveInputs(stack)；按条件决定是否执行本分支。
            return true; // 返回 true，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return quickMovedSlotIndex < PLAYER_INVENTORY_END // 返回 quickMovedSlotIndex < PLAYER_INVENTORY_END，将当前分支的结果交给调用者。
                ? this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false) // 调用 moveItemStackTo，执行当前流程所需的处理。
                : this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false); // 调用 moveItemStackTo，执行当前流程所需的处理。
    } // 结束当前作用域。

    private boolean moveToActiveInputs(ItemStack stack) { // 定义 moveToActiveInputs 的调用入口，参数和具体处理见下方实现。
        boolean moved = false; // 设置 moved 为 false。
        // 原版合并阶段不会检查 mayPlace 和 isActive，调用前需要补充这两项校验，
        // 因为各模式的 Slot 视图会指向同一组物理输入槽。
        for (int pass = 0; pass < 2; pass++) { // 遍历 int pass = 0; pass < 2; pass++，逐项执行当前处理。
            for (int index = INPUT_MENU_SLOT_START; index < TOOL_STATION_SLOT_COUNT; index++) { // 遍历 int index = INPUT_MENU_SLOT_START; index < TOOL_STATION_SLOT_COUNT; index++，逐项执行当前处理。
                Slot slot = this.slots.get(index); // 设置 slot 为 this.slots.get(index)。
                if (!slot.isActive() || !slot.mayPlace(stack) || slot.hasItem() != (pass == 0)) { // 检查 !slot.isActive() || !slot.mayPlace(stack) || slot.hasItem() != (pass == 0)；按条件决定是否执行本分支。
                    continue; // 跳过当前条目，继续处理下一条。
                } // 结束当前作用域。
                if (this.moveItemStackTo(stack, index, index + 1, false)) { // 检查 this.moveItemStackTo(stack, index, index + 1, false)；按条件决定是否执行本分支。
                    moved = true; // 设置 moved 为 true。
                    if (stack.isEmpty() || pass == 1) { // 检查 stack.isEmpty() || pass == 1；按条件决定是否执行本分支。
                        return true; // 返回 true，将当前分支的结果交给调用者。
                    } // 结束当前作用域。
                } // 结束当前作用域。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return moved; // 返回 moved，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private boolean areInputSlotsEmpty() { // 定义 areInputSlotsEmpty 的调用入口，参数和具体处理见下方实现。
        for (int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++) { // 遍历 int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++，逐项执行当前处理。
            if (!this.toolStation.getItem(inputSlot).isEmpty()) { // 检查 !this.toolStation.getItem(inputSlot).isEmpty()；按条件决定是否执行本分支。
                return false; // 返回 false，将当前分支的结果交给调用者。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return true; // 返回 true，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean areSlotsEmpty(int slotId) { // 定义 areSlotsEmpty 的调用入口，参数和具体处理见下方实现。
        return this.toolStation.getItem(slotId).isEmpty(); // 返回 this.toolStation.getItem(slotId).isEmpty()，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
