package com.gctn.tconstruct.tables.block.entity;

import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import com.gctn.tconstruct.tables.recipe.ToolAssemblyRecipe;
import com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes;
import com.gctn.tconstruct.tables.recipe.ToolStationDefaultRecipes;
import com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes;
import com.gctn.tconstruct.tables.recipe.ToolStationDisassemblyRecipes.PartList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 管理工具台的权威库存，以及合成、拆解状态机。
 * 普通合成结果只是派生预览；拆解时则在取走首个部件前始终保留真实源工具。
 * 所有玩家与自动化共用此状态，因此输入变化必须刷新预览，领取结果时必须重新校验。
 */
public class ToolStationBlockEntity extends BaseContainerBlockEntity {
    private static final int CONTAINER_SIZE = ToolStationMenu.CONTAINER_SIZE;
    private static final int INPUT_SLOT_COUNT = ToolStationMenu.INPUT_SLOT_COUNT;
    private static final int RESULT_SLOT = ToolStationMenu.RESULT_SLOT_INDEX;

    private static final int DATA_MODE = 0;
    private static final int DATA_DISASSEMBLY_STATE = 1;
    public static final int DATA_COUNT = 2;

    private static final int DISASSEMBLY_NONE = 0;
    private static final int DISASSEMBLY_PREVIEW = 1;
    private static final int DISASSEMBLY_COMMITTED = 2;

    private static final String TAG_MODE = "Mode";
    private static final String TAG_DISASSEMBLY_STATE = "DisassemblyState";

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private Mode mode = Mode.DEFAULT;
    private int disassemblyState = DISASSEMBLY_NONE;
    private boolean consumingInputs;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_MODE -> ToolStationBlockEntity.this.mode.getButtonId();
                case DATA_DISASSEMBLY_STATE -> ToolStationBlockEntity.this.disassemblyState;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 只有方块实体可以修改权威状态，客户端数据槽使用 SimpleContainerData。
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ToolStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TableEntityRegistries.TOOL_STATION_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public ContainerData getMenuData() {
        return this.menuData;
    }

    public Mode getMode() {
        return this.mode;
    }

    public boolean isDisassemblyPreview() {
        return this.disassemblyState == DISASSEMBLY_PREVIEW;
    }

    public boolean trySwitchMode(Mode targetMode, Player player) {
        if (targetMode == null) {
            return false;
        }
        if (targetMode == this.mode) {
            return true;
        }

        if (this.isDisassemblyPreview()) {
            if (this.mode != Mode.DISASSEMBLE || this.getItem(RESULT_SLOT).isEmpty()) {
                return false;
            }
            this.cancelDisassemblyPreview(player, true);
        } else if (this.disassemblyState == DISASSEMBLY_COMMITTED) {
            this.disassemblyState = DISASSEMBLY_NONE;
        }

        this.mode = targetMode;
        this.updateResult();
        return true;
    }

    public void inputsChanged() {
        // 一次确认操作可能修改多个输入，等待全部修改完成后再重建
        // 预览并执行确定性的修复回调。
        if (this.consumingInputs) return;
        if (this.disassemblyState == DISASSEMBLY_COMMITTED && this.areInputSlotsEmpty()) {
            this.disassemblyState = DISASSEMBLY_NONE;
        }
        this.updateResult();
    }

    public boolean canPlaceDisassemblySource(ItemStack stack) {
        return this.mode == Mode.DISASSEMBLE
                && this.disassemblyState == DISASSEMBLY_NONE
                && this.getItem(RESULT_SLOT).isEmpty()
                && this.areInputSlotsEmpty()
                && ToolStationDisassemblyRecipes.canDisassemble(stack);
    }

    public boolean canTakeResult() {
        if (this.mode == Mode.DISASSEMBLE) {
            return this.isDisassemblyPreview() && !this.getItem(RESULT_SLOT).isEmpty();
        }
        // 预览发送后，其他玩家或自动化可能已改变输入。
        // 将结果交给玩家前，必须在服务端重新校验。
        ItemStack expected = this.createCraftingResult();
        if (!ItemStack.matches(expected, this.getItem(RESULT_SLOT))) {
            this.updateResult();
            return false;
        }
        return !expected.isEmpty();
    }

    public void resultSlotChanged(ItemStack newStack) {
        if (this.mode != Mode.DISASSEMBLE) {
            return;
        }
        if (newStack.isEmpty()) {
            this.cancelDisassemblyPreview(null, false);
        } else if (this.disassemblyState == DISASSEMBLY_NONE) {
            this.startDisassembly(newStack);
        }
    }

    public void resultTaken(Mode actionMode) {
        if (actionMode != this.mode) {
            return;
        }
        if (actionMode == Mode.DISASSEMBLE) {
            this.cancelDisassemblyPreview(null, false);
        } else {
            this.takeCraftingResult();
        }
    }

    public void disassemblyPartTaken() {
        if (!this.isDisassemblyPreview()) {
            return;
        }
        // 取走第一个部件时，将预览部件确认为真实物品，并且只消耗
        // 一次源工具；之后取走其他部件按普通库存移除处理。
        this.disassemblyState = DISASSEMBLY_COMMITTED;
        this.items.set(RESULT_SLOT, ItemStack.EMPTY);
        this.setChanged();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        this.commitDisassemblyAfterPartRemoval(slot, removed);
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) {
            this.inputsChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        this.commitDisassemblyAfterPartRemoval(slot, removed);
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) {
            this.inputsChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot >= 0 && slot < INPUT_SLOT_COUNT) {
            this.inputsChanged();
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.disassemblyState = DISASSEMBLY_NONE;
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INPUT_SLOT_COUNT || this.mode == Mode.DISASSEMBLE
                || !com.gctn.tconstruct.tables.data.ToolStationSlotPositions.isInputSlotActive(this.mode, slot)) {
            return false;
        }
        ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode);
        return recipe == null || recipe.getRequiredItem(slot) == null || stack.is(recipe.getRequiredItem(slot));
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        // 结果槽具有合成事务语义，只允许通过 ToolStationResultSlot 领取。
        return slot >= 0 && slot < INPUT_SLOT_COUNT;
    }

    private void commitDisassemblyAfterPartRemoval(int slot, ItemStack removed) {
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) {
            this.disassemblyPartTaken();
        }
    }

    /** 方块掉落真实库存前，先移除生成的部件预览。 */
    public void prepareForRemoval() {
        if (this.isDisassemblyPreview()) {
            this.clearInputSlots();
            this.disassemblyState = DISASSEMBLY_NONE;
        } else if (this.mode != Mode.DISASSEMBLE) {
            // 合成结果由输入派生，不能作为额外物品一并掉落。
            this.items.set(RESULT_SLOT, ItemStack.EMPTY);
        }
        this.setChanged();
    }

    private void updateResult() {
        if (this.mode == Mode.DISASSEMBLE) {
            if (!this.isDisassemblyPreview()) {
                this.items.set(RESULT_SLOT, ItemStack.EMPTY);
            }
        } else {
            this.items.set(RESULT_SLOT, this.createCraftingResult());
        }
        this.setChanged();
    }

    private ItemStack createCraftingResult() {
        if (this.mode == Mode.DEFAULT) {
            return ToolStationDefaultRecipes.createResult(this);
        }
        ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode);
        return recipe == null ? ItemStack.EMPTY : recipe.createResult(this);
    }

    private void takeCraftingResult() {
        if (this.createCraftingResult().isEmpty()) {
            this.updateResult();
            return;
        }

        this.consumingInputs = true;
        try {
            if (this.mode == Mode.DEFAULT) {
                ToolStationDefaultRecipes.consumeInputs(this);
            } else {
                ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode);
                if (recipe != null) recipe.consumeInputs(this);
            }
        } finally {
            this.consumingInputs = false;
        }
        this.updateResult();
    }

    private void startDisassembly(ItemStack sourceTool) {
        PartList parts = ToolStationDisassemblyRecipes.createParts(sourceTool);
        if (parts.isEmpty() || !this.areInputSlotsEmpty()) {
            return;
        }

        this.disassemblyState = DISASSEMBLY_PREVIEW;
        ItemStack[] partStacks = parts.parts();
        for (int inputSlot = 0; inputSlot < partStacks.length; inputSlot++) {
            this.items.set(inputSlot, partStacks[inputSlot].copy());
        }
        this.setChanged();
    }

    private void cancelDisassemblyPreview(Player player, boolean returnSource) {
        if (!this.isDisassemblyPreview()) {
            return;
        }

        ItemStack sourceTool = this.items.get(RESULT_SLOT);
        this.items.set(RESULT_SLOT, ItemStack.EMPTY);
        this.clearInputSlots();
        this.disassemblyState = DISASSEMBLY_NONE;

        if (returnSource && player != null && !sourceTool.isEmpty()) {
            player.getInventory().placeItemBackInInventory(sourceTool);
        }
        this.setChanged();
    }

    private void clearInputSlots() {
        for (int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++) {
            this.items.set(inputSlot, ItemStack.EMPTY);
        }
    }

    private boolean areInputSlotsEmpty() {
        for (int inputSlot = 0; inputSlot < INPUT_SLOT_COUNT; inputSlot++) {
            if (!this.getItem(inputSlot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);

        Mode loadedMode = Mode.byButtonId(tag.getInt(TAG_MODE));
        this.mode = loadedMode == null ? Mode.DEFAULT : loadedMode;
        int loadedState = tag.getInt(TAG_DISASSEMBLY_STATE);
        this.disassemblyState = loadedState >= DISASSEMBLY_NONE && loadedState <= DISASSEMBLY_COMMITTED
                ? loadedState
                : DISASSEMBLY_NONE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt(TAG_MODE, this.mode.getButtonId());
        tag.putInt(TAG_DISASSEMBLY_STATE, this.disassemblyState);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        this.updateResult();
        return new ToolStationMenu(containerId, inventory, this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.tconstruct.toolstation.name");
    }
}
