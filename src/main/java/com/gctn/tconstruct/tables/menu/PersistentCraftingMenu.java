/* SPDX-License-Identifier: LGPL-3.0-only; Antique's persistent grid using Minecraft's native crafting lifecycle. */
package com.gctn.tconstruct.tables.menu;

import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

public class PersistentCraftingMenu extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    private final Inventory inventory;
    private final WorkshopBlockEntity entity;
    private final BlockPos pos;
    private final CraftingContainer grid;
    private final ResultContainer result = new ResultContainer();
    private final NearbyInventory nearby;
    private boolean placingRecipe;
    public PersistentCraftingMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) { this(id, inventory, buf.readBlockPos(), null, NearbyInventory.read(buf)); }
    public PersistentCraftingMenu(int id, Inventory inventory, WorkshopBlockEntity entity) {
        this(id, inventory, entity.getBlockPos(), entity, NearbyInventory.find(inventory.player, entity.getBlockPos(), WorkshopModule.Kind.CRAFTING));
    }
    private PersistentCraftingMenu(int id, Inventory inventory, BlockPos pos, WorkshopBlockEntity entity, NearbyInventory.Source source) {
        super(WorkshopModule.CRAFTING_MENU.get(), id);
        this.inventory = inventory; this.pos = pos; this.entity = entity;
        nearby = new NearbyInventory(inventory.player, source, -94);
        grid = new PersistentGrid(entity == null ? new net.minecraft.world.SimpleContainer(9) : entity);
        addSlot(new ResultSlot(inventory.player, grid, result, 0, 124, 35) {
            @Override public boolean mayPickup(Player player) {
                ItemStack fresh = recipeResult();
                return !fresh.isEmpty() && ItemStack.isSameItemSameComponents(fresh, getItem()) && fresh.getCount() == getItem().getCount();
            }
        });
        for (int row = 0; row < 3; row++) { for (int col = 0; col < 3; col++) { addSlot(new Slot(grid, row * 3 + col, 30 + col * 18, 17 + row * 18)); } }
        for (int row = 0; row < 3; row++) { for (int col = 0; col < 9; col++) { addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18)); } }
        for (int col = 0; col < 9; col++) { addSlot(new Slot(inventory, col, 8 + col * 18, 142)); }
        nearby.addSlots(this::addSlot); addDataSlots(nearby.data());
        slotsChanged(grid);
    }
    public BlockPos position() { return pos; }
    public NearbyInventory nearby() { return nearby; }
    private ItemStack recipeResult() {
        var level = inventory.player.level();
        if (level.isClientSide) { return result.getItem(0); }
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid.asCraftInput(), level);
        if (recipe.isEmpty() || !result.setRecipeUsed(level, (ServerPlayer) inventory.player, recipe.get())) { return ItemStack.EMPTY; }
        ItemStack output = recipe.get().value().assemble(grid.asCraftInput(), level.registryAccess());
        return output.isItemEnabled(level.enabledFeatures()) ? output : ItemStack.EMPTY;
    }
    @Override public void slotsChanged(Container container) {
        updateResult();
    }
    private void updateResult() {
        if (!placingRecipe && !inventory.player.level().isClientSide) {
            ItemStack output = recipeResult();
            if (ItemStack.matches(result.getItem(0), output)) { return; }
            result.setItem(0, output);
            setRemoteSlot(0, output);
            ((ServerPlayer) inventory.player).connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, output));
        }
    }
    @Override public void broadcastChanges() { if (!placingRecipe) { updateResult(); } super.broadcastChanges(); }
    @Override public boolean stillValid(Player player) { return nearby.valid() && WorkshopModule.isTable(player.level(), pos) && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64; }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player)) { return false; }
        if (nearby.click(id)) { broadcastFullState(); return true; }
        return WorkshopModule.navigate(player, pos, id);
    }
    @Override public void beginPlacingRecipe() { placingRecipe = true; }
    @Override public void finishPlacingRecipe(RecipeHolder<CraftingRecipe> recipe) { placingRecipe = false; slotsChanged(grid); }
    @Override public void fillCraftSlotsStackedContents(StackedContents contents) { grid.fillStackedContents(contents); }
    @Override public void clearCraftingContent() { grid.clearContent(); result.clearContent(); slotsChanged(grid); }
    @Override public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe) { return recipe.value().matches(grid.asCraftInput(), inventory.player.level()); }
    @Override public int getResultSlotIndex() { return 0; }
    @Override public int getGridWidth() { return 3; }
    @Override public int getGridHeight() { return 3; }
    @Override public int getSize() { return 10; }
    @Override public RecipeBookType getRecipeBookType() { return RecipeBookType.CRAFTING; }
    @Override public boolean shouldMoveToInventory(int index) { return index != 0; }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return slot.container != result && super.canTakeItemForPickAll(stack, slot); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) { return ItemStack.EMPTY; }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) { return ItemStack.EMPTY; }
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index == 0) {
            stack.getItem().onCraftedBy(stack, player.level(), player);
            if (!moveItemStackTo(stack, 10, 46, true)) { return ItemStack.EMPTY; }
            slot.onQuickCraft(stack, original);
        } else if (index >= 46) {
            if (!moveItemStackTo(stack, 10, 46, false)) { return ItemStack.EMPTY; }
        } else if (index >= 10) {
            if (!moveItemStackTo(stack, 1, 10, false) && !moveItemStackTo(stack, 46, slots.size(), false)
                    && !moveItemStackTo(stack, index < 37 ? 37 : 10, index < 37 ? 46 : 37, false)) { return ItemStack.EMPTY; }
        } else if (!moveItemStackTo(stack, 10, 46, false)) { return ItemStack.EMPTY; }
        if (stack.getCount() == original.getCount()) { return ItemStack.EMPTY; }
        if (stack.isEmpty()) { slot.setByPlayer(ItemStack.EMPTY); } else { slot.setChanged(); }
        slot.onTake(player, stack);
        if (index == 0 && !stack.isEmpty()) { player.drop(stack, false); }
        return original;
    }
    private class PersistentGrid implements CraftingContainer {
        private final Container backing;
        PersistentGrid(Container backing) { this.backing = backing; }
        @Override public int getWidth() { return 3; }
        @Override public int getHeight() { return 3; }
        @Override public int getContainerSize() { return 9; }
        @Override public boolean isEmpty() { return backing.isEmpty(); }
        @Override public java.util.List<ItemStack> getItems() { return java.util.stream.IntStream.range(0, 9).mapToObj(backing::getItem).toList(); }
        @Override public ItemStack getItem(int slot) { return backing.getItem(slot); }
        @Override public ItemStack removeItem(int slot, int count) { ItemStack stack = backing.removeItem(slot, count); slotsChanged(this); return stack; }
        @Override public ItemStack removeItemNoUpdate(int slot) { return backing.removeItemNoUpdate(slot); }
        @Override public void setItem(int slot, ItemStack stack) { backing.setItem(slot, stack); slotsChanged(this); }
        @Override public void setChanged() { backing.setChanged(); slotsChanged(this); }
        @Override public boolean stillValid(Player player) { return PersistentCraftingMenu.this.stillValid(player); }
        @Override public void clearContent() { backing.clearContent(); setChanged(); }
        @Override public void fillStackedContents(StackedContents contents) { for (ItemStack stack : getItems()) { contents.accountSimpleStack(stack); } }
    }
}
