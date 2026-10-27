package com.gctn.tconstruct.tables.menu;

import com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity;
import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tables.data.ModeAwareInputSlot;
import com.gctn.tconstruct.tables.data.ToolStationResultSlot;
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions;
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition;
import com.gctn.tconstruct.tables.recipe.ToolAssemblyRecipe;
import com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes;
import com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ToolStationMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 7;

    public static final int MOVING_SLOT_INDEX = 0;
    public static final int CONDITIONAL_SLOT_INDEX = 1;
    public static final int RESULT_SLOT_INDEX = 6;
    public static final int INPUT_SLOT_COUNT = ToolStationSlotPositions.INPUT_SLOT_COUNT;

    public static final int DEFAULT_MODE_BUTTON = Mode.DEFAULT.getButtonId();
    public static final int DISASSEMBLE_MODE_BUTTON = Mode.DISASSEMBLE.getButtonId();
    public static final int PICKAXE_MODE_BUTTON = Mode.PICKAXE.getButtonId();
    public static final int SHOVEL_MODE_BUTTON = Mode.SHOVEL.getButtonId();
    public static final int AXE_MODE_BUTTON = Mode.AXE.getButtonId();
    public static final int DEBUG_MODE_BUTTON = Mode.DEBUG.getButtonId();

    private static final int DATA_MODE = 0;
    private static final int DATA_DISASSEMBLY_STATE = 1;
    private static final int DISASSEMBLY_PREVIEW = 1;

    private static final int RESULT_MENU_SLOT = 0;
    private static final int INPUT_MENU_SLOT_START = 1;
    private static final int TOOL_STATION_SLOT_COUNT = 1 + Mode.values().length * INPUT_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_START = TOOL_STATION_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + PLAYER_INVENTORY_COLUMNS * PLAYER_INVENTORY_ROWS;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + PLAYER_INVENTORY_COLUMNS;

    private static final int MAX_VISIBLE_SPECIAL_MODE_INPUTS = 3;
    private static final int RESULT_SLOT_X = 124;
    private static final int RESULT_SLOT_Y = 38;
    private static final int PLAYER_INVENTORY_X = 8;
    private static final int PLAYER_INVENTORY_Y = 92;
    private static final int HOTBAR_Y = 150;
    private static final int SLOT_SPACING = 18;

    private final Container toolStation;
    private final ContainerData data;
    private final ToolStationBlockEntity blockEntity;

    public ToolStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(CONTAINER_SIZE),
                new SimpleContainerData(ToolStationBlockEntity.DATA_COUNT), null);
    }

    public ToolStationMenu(int containerId, Inventory playerInventory, ToolStationBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getMenuData(), blockEntity);
    }

    private ToolStationMenu(int containerId, Inventory playerInventory, Container container,
                            ContainerData data, ToolStationBlockEntity blockEntity) {
        super(TableMenuRegistries.TOOL_STATION_MENU.get(), containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        checkContainerDataCount(data, ToolStationBlockEntity.DATA_COUNT);
        this.toolStation = container;
        this.data = data;
        this.blockEntity = blockEntity;
        container.startOpen(playerInventory.player);

        this.addToolStationSlots(container);
        this.addPlayerInventorySlots(playerInventory);
        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        if (quickMovedSlotIndex < 0 || quickMovedSlotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);
        if (quickMovedSlot == null || !quickMovedSlot.hasItem() || !quickMovedSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack rawStack = quickMovedSlot.getItem();
        ItemStack quickMovedStack = rawStack.copy();
        if (!this.moveQuickMovedStack(quickMovedSlotIndex, rawStack)) {
            return ItemStack.EMPTY;
        }

        if (quickMovedSlotIndex == RESULT_MENU_SLOT) {
            quickMovedSlot.onQuickCraft(rawStack, quickMovedStack);
        }

        if (rawStack.isEmpty()) {
            quickMovedSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            quickMovedSlot.setChanged();
        }

        if (rawStack.getCount() == quickMovedStack.getCount()) {
            return ItemStack.EMPTY;
        }

        quickMovedSlot.onTake(player, rawStack);
        return quickMovedStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.toolStation.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.toolStation.stillValid(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        Mode selectedMode = Mode.byButtonId(id);
        if (selectedMode == null || !this.isModeButtonVisible(selectedMode) || this.blockEntity == null) {
            return false;
        }
        return this.blockEntity.trySwitchMode(selectedMode, player);
    }

    public void inputsChanged() {
        if (this.blockEntity != null) {
            this.blockEntity.inputsChanged();
        }
    }

    public Mode getMode() {
        Mode currentMode = Mode.byButtonId(this.data.get(DATA_MODE));
        return currentMode == null ? Mode.DEFAULT : currentMode;
    }

    public boolean isMode(Mode mode) {
        return this.getMode() == mode;
    }

    public boolean isModeButtonVisible(Mode mode) {
        return mode == Mode.DEFAULT
                || mode == Mode.DISASSEMBLE
                || ToolStationSlotPositions.getActiveInputSlotCount(mode) <= MAX_VISIBLE_SPECIAL_MODE_INPUTS;
    }

    public boolean isInputSlotVisible(int inputSlot) {
        return this.isInputSlotInsertable(inputSlot) || !this.toolStation.getItem(inputSlot).isEmpty();
    }

    public boolean isInputSlotAvailable(Mode slotMode, int inputSlot) {
        return this.isMode(slotMode)
                && (ToolStationSlotPositions.isInputSlotActive(slotMode, inputSlot)
                || !this.toolStation.getItem(inputSlot).isEmpty());
    }

    public boolean isInputSlotInsertable(int inputSlot) {
        return ToolStationSlotPositions.isInputSlotActive(this.getMode(), inputSlot);
    }

    public SlotPosition getInputSlotPosition(int inputSlot) {
        return ToolStationSlotPositions.getDisplayInputSlotPosition(this.getMode(), inputSlot);
    }

    public ItemStack getInputSlotIcon(int inputSlot) {
        if (!this.isInputSlotInsertable(inputSlot)) {
            return ItemStack.EMPTY;
        }
        Item requiredItem = this.getRequiredInputItem(this.getMode(), inputSlot);
        return requiredItem == null ? ItemStack.EMPTY : new ItemStack(requiredItem);
    }

    public Item getRequiredInputItem(Mode targetMode, int inputSlot) {
        if (!ToolStationSlotPositions.isInputSlotActive(targetMode, inputSlot)) {
            return null;
        }
        ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(targetMode);
        return recipe == null ? null : recipe.getRequiredItem(inputSlot);
    }

    public boolean canPlaceInput(Mode slotMode, int inputSlot, ItemStack stack) {
        if (!this.isMode(slotMode) || !ToolStationSlotPositions.isInputSlotActive(slotMode, inputSlot)) {
            return false;
        }
        if (slotMode == Mode.DISASSEMBLE) {
            return false;
        }
        Item requiredItem = this.getRequiredInputItem(slotMode, inputSlot);
        return requiredItem == null || stack.is(requiredItem);
    }

    public boolean canPlaceResultSlot(ItemStack stack) {
        return this.blockEntity != null
                ? this.blockEntity.canPlaceDisassemblySource(stack)
                : this.isMode(Mode.DISASSEMBLE)
                && this.data.get(DATA_DISASSEMBLY_STATE) == 0
                && this.toolStation.getItem(RESULT_SLOT_INDEX).isEmpty()
                && this.areInputSlotsEmpty()
                && ToolStationDisassemblyRecipes.canDisassemble(stack);
    }

    public boolean canTakeResultSlot() {
        if (this.blockEntity != null) {
            return this.blockEntity.canTakeResult();
        }
        return this.isMode(Mode.DISASSEMBLE)
                ? this.data.get(DATA_DISASSEMBLY_STATE) == DISASSEMBLY_PREVIEW
                && !this.toolStation.getItem(RESULT_SLOT_INDEX).isEmpty()
                : !this.toolStation.getItem(RESULT_SLOT_INDEX).isEmpty();
    }

    public void resultSlotChanged(ItemStack newStack) {
        if (this.blockEntity != null) {
            this.blockEntity.resultSlotChanged(newStack);
        }
    }

    public void resultSlotTaken(Mode actionMode) {
        if (this.blockEntity != null) {
            this.blockEntity.resultTaken(actionMode);
        }
    }

    public void disassemblyPartTaken() {
        if (this.blockEntity != null) {
            this.blockEntity.disassemblyPartTaken();
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot != this.slots.get(RESULT_MENU_SLOT) && super.canTakeItemForPickAll(stack, slot);
    }

    private void addToolStationSlots(Container container) {
        this.addSlot(new ToolStationResultSlot(this, container, RESULT_SLOT_INDEX, RESULT_SLOT_X, RESULT_SLOT_Y));

        for (Mode slotMode : Mode.values()) {
            for (int slotIndex = 0; slotIndex < INPUT_SLOT_COUNT; slotIndex++) {
                SlotPosition position = ToolStationSlotPositions.getDisplayInputSlotPosition(slotMode, slotIndex);
                this.addSlot(new ModeAwareInputSlot(this, container, slotIndex,
                        position.slotX(), position.slotY(), slotMode, slotIndex));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INVENTORY_ROWS; row++) {
            for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) {
                int inventorySlot = column + row * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS;
                int x = PLAYER_INVENTORY_X + column * SLOT_SPACING;
                int y = PLAYER_INVENTORY_Y + row * SLOT_SPACING;
                this.addSlot(new Slot(playerInventory, inventorySlot, x, y));
            }
        }

        for (int column = 0; column < PLAYER_INVENTORY_COLUMNS; column++) {
            int x = PLAYER_INVENTORY_X + column * SLOT_SPACING;
            this.addSlot(new Slot(playerInventory, column, x, HOTBAR_Y));
        }
    }

    private boolean moveQuickMovedStack(int quickMovedSlotIndex, ItemStack stack) {
        if (quickMovedSlotIndex == RESULT_MENU_SLOT) {
            return this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true);
        }
        if (quickMovedSlotIndex >= PLAYER_INVENTORY_START && quickMovedSlotIndex < HOTBAR_END) {
            return this.movePlayerStack(quickMovedSlotIndex, stack);
        }
        return this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false);
    }

    private boolean movePlayerStack(int quickMovedSlotIndex, ItemStack stack) {
        if (this.isMode(Mode.DISASSEMBLE)
                && this.moveItemStackTo(stack, RESULT_MENU_SLOT, RESULT_MENU_SLOT + 1, false)) {
            return true;
        }
        if (this.moveToActiveInputs(stack)) {
            return true;
        }
        return quickMovedSlotIndex < PLAYER_INVENTORY_END
                ? this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)
                : this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false);
    }

    private boolean moveToActiveInputs(ItemStack stack) {
        boolean moved = false;
        // Vanilla's merge pass skips mayPlace/isActive. Check both before using it
        // because each mode has duplicate Slot views over the same physical inputs.
        for (int pass = 0; pass < 2; pass++) {
            for (int index = INPUT_MENU_SLOT_START; index < TOOL_STATION_SLOT_COUNT; index++) {
                Slot slot = this.slots.get(index);
                if (!slot.isActive() || !slot.mayPlace(stack) || slot.hasItem() != (pass == 0)) {
                    continue;
                }
                if (this.moveItemStackTo(stack, index, index + 1, false)) {
                    moved = true;
                    if (stack.isEmpty() || pass == 1) {
                        return true;
                    }
                }
            }
        }
        return moved;
    }

    private boolean areInputSlotsEmpty() {
        for (int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++) {
            if (!this.toolStation.getItem(inputSlot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean areSlotsEmpty(int slotId) {
        return this.toolStation.getItem(slotId).isEmpty();
    }
}
