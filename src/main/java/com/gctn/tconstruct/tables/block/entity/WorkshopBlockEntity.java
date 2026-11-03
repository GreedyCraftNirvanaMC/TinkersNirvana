/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's persistent tool tables and chests. */
package com.gctn.tconstruct.tables.block.entity;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.WorkshopModule.Kind;
import com.gctn.tconstruct.tables.block.WorkshopBlock;
import com.gctn.tconstruct.tables.menu.PersistentCraftingMenu;
import com.gctn.tconstruct.tables.menu.WorkshopMenu;
import com.gctn.tconstruct.tools.PatternItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class WorkshopBlockEntity extends net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity {
    public final com.gctn.tconstruct.tables.TableMaterial material;
    private NonNullList<ItemStack> items;
    private final IItemHandler handler = new InvWrapper(this) {
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return kind().storage() || kind() == Kind.CRAFTING ? super.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return kind().storage() ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
    };
    public WorkshopBlockEntity(BlockPos pos, BlockState state) {
        super(WorkshopModule.ENTITY.get(), pos, state);
        items = NonNullList.withSize(kind().size, ItemStack.EMPTY);
        material = new com.gctn.tconstruct.tables.TableMaterial((kind() == Kind.PART_BUILDER ? net.minecraft.world.level.block.Blocks.OAK_LOG : net.minecraft.world.level.block.Blocks.OAK_PLANKS).defaultBlockState());
    }
    public Kind kind() { return ((WorkshopBlock) getBlockState().getBlock()).kind(); }
    @Override public int getContainerSize() { return items.size(); }
    @Override public NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    public IItemHandler itemHandler() { return handler; }
    public static boolean isCast(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("tconstruct") && (id.getPath().startsWith("cast_") || id.getPath().startsWith("clay_cast_"));
    }
    public static String patternKey(ItemStack stack) {
        if (stack.getItem() instanceof PatternItem) { return "pattern:" + (PatternItem.isBlank(stack) ? "blank" : PatternItem.selected(stack).id()); }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return id.replace("tconstruct:clay_cast_", "tconstruct:cast_");
    }
    @Override public int getMaxStackSize() { return kind() == Kind.PATTERN_CHEST ? 1 : 64; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return (kind().storage() || kind() == Kind.CRAFTING) && validInput(kind(), this, slot, stack);
    }
    @Override public boolean canTakeItem(net.minecraft.world.Container target, int slot, ItemStack stack) { return kind().storage(); }
    public static boolean validInput(Kind kind, net.minecraft.world.Container container, int slot, ItemStack stack) {
        if (kind == Kind.STENCIL) { return stack.getItem() instanceof PatternItem; }
        if (kind == Kind.PART_CHEST) {
            if (AntiqueParts.get(stack) == null) { return false; }
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (i != slot && ItemStack.isSameItemSameComponents(container.getItem(i), stack)) { return false; }
            }
        } else if (kind == Kind.PATTERN_CHEST) {
            boolean cast = isCast(stack);
            if (!cast && !(stack.getItem() instanceof PatternItem)) { return false; }
            for (int i = 0; i < container.getContainerSize(); i++) {
                var existing = container.getItem(i);
                if (existing.isEmpty()) { continue; }
                if (isCast(existing) != cast || (i != slot && patternKey(existing).equals(patternKey(stack)))) { return false; }
            }
        }
        return true;
    }
    @Override protected Component getDefaultName() {
        return Component.translatable("block.tconstruct." + (kind() == Kind.PATTERN_CHEST && getBlockState().getValue(WorkshopBlock.CASTS) ? "cast_chest" : kind().id));
    }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return kind() == Kind.CRAFTING ? new PersistentCraftingMenu(id, inventory, this) : new WorkshopMenu(id, inventory, this);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(kind().size, ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) { lootTable = null; ContainerHelper.loadAllItems(tag, items, registries); }
        material.load(tag, registries);
        requestModelDataUpdate();
        if (level != null && level.isClientSide) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) { ContainerHelper.saveAllItems(tag, items, registries); }
        material.save(tag);
    }
    @Override public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide && !isRemoved()) {
            if (kind() == Kind.PATTERN_CHEST) {
                boolean cast = items.stream().anyMatch(WorkshopBlockEntity::isCast);
                if (getBlockState().getValue(WorkshopBlock.CASTS) != cast) { level.setBlock(worldPosition, getBlockState().setValue(WorkshopBlock.CASTS, cast), 3); }
            }
            if (!kind().storage()) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public net.neoforged.neoforge.client.model.data.ModelData getModelData() { return material.modelData(); }
    @Override protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        material.set(input.getOrDefault(TinkerDataComponents.TABLE_MATERIAL.get(), material.state()));
    }
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder output) { super.collectImplicitComponents(output); material.collect(output); }
}
