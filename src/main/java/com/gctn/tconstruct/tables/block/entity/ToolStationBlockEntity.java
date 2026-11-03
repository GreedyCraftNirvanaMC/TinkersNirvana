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

public class ToolStationBlockEntity extends BaseContainerBlockEntity {
    public final com.gctn.tconstruct.tables.TableMaterial material;
    private static final int CONTAINER_SIZE = ToolStationMenu.CONTAINER_SIZE;
    private static final int INPUT_SLOT_COUNT = ToolStationMenu.INPUT_SLOT_COUNT;
    private static final int RESULT_SLOT = ToolStationMenu.RESULT_SLOT_INDEX;

    private static final int DATA_MODE = 0;
    private static final int DATA_DISASSEMBLY_STATE = 1;
    public static final int DATA_COUNT = 3;

    private static final int DISASSEMBLY_NONE = 0;
    private static final int DISASSEMBLY_PREVIEW = 1;
    private static final int DISASSEMBLY_COMMITTED = 2;

    private static final String TAG_MODE = "Mode";
    private static final String TAG_DISASSEMBLY_STATE = "DisassemblyState";

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private Mode mode = Mode.DEFAULT;
    private int disassemblyState = DISASSEMBLY_NONE;
    private String pendingName = "";
    public String pendingName() { return pendingName; }
    public void rename(String name) {
        pendingName = net.minecraft.util.StringUtil.filterText(name).strip();
        if (pendingName.length() > 50) { pendingName = pendingName.substring(0, 50); }
        updateResult();
    }

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_MODE -> ToolStationBlockEntity.this.mode.getButtonId();
                case DATA_DISASSEMBLY_STATE -> ToolStationBlockEntity.this.disassemblyState;
                case 2 -> ToolStationBlockEntity.this.isForge() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Only the block entity changes authoritative state; client data slots use SimpleContainerData.
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ToolStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TableEntityRegistries.TOOL_STATION_BLOCK_ENTITY.get(), blockPos, blockState);
        material = new com.gctn.tconstruct.tables.TableMaterial((isForge() ? net.minecraft.world.level.block.Blocks.IRON_BLOCK : net.minecraft.world.level.block.Blocks.OAK_PLANKS).defaultBlockState());
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
        if (targetMode == null || (targetMode.requiresForge() && !isForge())) {
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
        return !this.getItem(RESULT_SLOT).isEmpty();
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
        this.disassemblyState = DISASSEMBLY_COMMITTED;
        this.items.set(RESULT_SLOT, ItemStack.EMPTY);
        this.setChanged();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        this.commitDisassemblyAfterPartRemoval(slot, removed);
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        this.commitDisassemblyAfterPartRemoval(slot, removed);
        return removed;
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
        // The result slot has crafting semantics and may only be taken through ToolStationResultSlot.
        return slot >= 0 && slot < INPUT_SLOT_COUNT;
    }

    private void commitDisassemblyAfterPartRemoval(int slot, ItemStack removed) {
        if (slot >= 0 && slot < INPUT_SLOT_COUNT && !removed.isEmpty()) {
            this.disassemblyPartTaken();
        }
    }

    /** Removes generated preview parts before the block drops its real contents. */
    public void prepareForRemoval() {
        if (this.isDisassemblyPreview()) {
            this.clearInputSlots();
            this.disassemblyState = DISASSEMBLY_NONE;
        } else if (this.mode != Mode.DISASSEMBLE) {
            // Crafting output is derived from the inputs and must not be dropped as an extra item.
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
        if (this.mode.requiresForge() && !isForge()) { return ItemStack.EMPTY; }
        ItemStack result;
        if (this.mode == Mode.DEFAULT) {
            result = ToolStationDefaultRecipes.createResult(this);
        } else {
            ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode);
            result = recipe == null ? ItemStack.EMPTY : recipe.createResult(this);
        }
        if (!result.isEmpty() && result.getItem() instanceof com.gctn.tconstruct.tools.TinkerTools && level != null) {
            com.gctn.tconstruct.tools.ToolModifiers.applyEnchantments(result, level.registryAccess());
        }
        if (result.isEmpty() && canRenameAlone()) { result = getItem(0).copyWithCount(1); }
        if (!pendingName.isEmpty() && canRenameItem(result)) {
            result.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(pendingName));
        }
        return result;
    }

    private boolean canRenameAlone() {
        if (mode != Mode.DEFAULT || pendingName.isEmpty() || !canRenameItem(getItem(0))) { return false; }
        Component current = getItem(0).get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        if (current != null && current.getString().equals(pendingName)) { return false; }
        for (int i = 1; i < INPUT_SLOT_COUNT; i++) { if (!getItem(i).isEmpty()) { return false; } }
        return true;
    }

    private static boolean canRenameItem(ItemStack stack) { return stack.getItem() instanceof com.gctn.tconstruct.tools.TinkerTools || com.gctn.tconstruct.tables.recipe.StationProcessingRecipes.supports(stack); }

    private void takeCraftingResult() {
        if (this.createCraftingResult().isEmpty()) {
            this.updateResult();
            return;
        }

        if (this.mode == Mode.DEFAULT) {
            if (canRenameAlone()) { setItem(0, ItemStack.EMPTY); }
            else { ToolStationDefaultRecipes.consumeInputs(this); }
        } else {
            ToolAssemblyRecipe recipe = ToolStationAssemblyRecipes.get(this.mode);
            if (recipe == null) {
                this.updateResult();
                return;
            }
            recipe.consumeInputs(this);
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
        material.load(tag, registries);
        requestModelDataUpdate();
        if (level != null && level.isClientSide) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }

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
        material.save(tag);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        this.updateResult();
        return new ToolStationMenu(containerId, inventory, this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(isForge() ? "block.tconstruct.toolforge" : "container.tconstruct.toolstation.name");
    }

    public boolean isForge() { return getBlockState().is(com.gctn.tconstruct.tables.block.TableBlocks.TOOL_FORGE.get()); }
    @Override public net.neoforged.neoforge.client.model.data.ModelData getModelData() { return material.modelData(); }
    @Override protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        material.set(input.getOrDefault(com.gctn.tconstruct.init.TinkerDataComponents.TABLE_MATERIAL.get(), material.state()));
    }
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder output) { super.collectImplicitComponents(output); material.collect(output); }
    @Override public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide && !isRemoved()) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() { return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this); }
}
