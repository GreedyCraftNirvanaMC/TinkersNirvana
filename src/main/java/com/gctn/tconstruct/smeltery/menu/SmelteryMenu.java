package com.gctn.tconstruct.smeltery.menu;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Each viewer has its own page. Only the 27 visible item slots are sent over the network. */
public class SmelteryMenu extends AbstractContainerMenu {
    public static final int VISIBLE_SLOTS = 27, DATA_COUNT = 32;
    public static final int PREVIOUS = 10000, NEXT = 10001;
    private final SmelteryControllerBlockEntity controller;
    private final ContainerData data;
    private final Container view;
    private int page;

    public SmelteryMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof SmelteryControllerBlockEntity c ? c : null, false);
    }
    public SmelteryMenu(int id, Inventory inventory, SmelteryControllerBlockEntity controller) { this(id, inventory, controller, true); }
    private SmelteryMenu(int id, Inventory inventory, SmelteryControllerBlockEntity controller, boolean server) {
        super(SmelteryRegistration.MENU.get(), id);
        this.controller = controller;
        this.view = server ? new PageContainer() : new SimpleContainer(VISIBLE_SLOTS);
        this.data = server ? new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> controller.getContainerSize() & 65535;
                    case 1 -> controller.getContainerSize() >>> 16;
                    case 2 -> controller.fuel();
                    case 3 -> page;
                    case 4 -> controller.isFormed() ? 1 : 0;
                    default -> controller.progress(page * VISIBLE_SLOTS + index - 5);
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return DATA_COUNT; }
        } : new SimpleContainerData(DATA_COUNT);
        for (int i = 0; i < VISIBLE_SLOTS; i++) {
            final int index = i;
            addSlot(new Slot(view, i, 8 + i % 9 * 18, 28 + i / 9 * 18) {
                @Override public boolean isActive() { return currentPage() * VISIBLE_SLOTS + index < totalSlots(); }
                @Override public boolean mayPlace(ItemStack stack) { return isActive() && SmelteryRecipes.melting(inventory.player.level(), stack) != null; }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        addDataSlots(data);
    }
    public SmelteryControllerBlockEntity controller() { return controller; }
    public int totalSlots() { return (data.get(0) & 65535) | data.get(1) << 16; }
    public int currentPage() { return data.get(3); }
    public int pages() { return Math.max(1, (totalSlots() + VISIBLE_SLOTS - 1) / VISIBLE_SLOTS); }
    public int fuel() { return data.get(2); }
    public int progress(int visibleSlot) { return data.get(5 + visibleSlot); }
    @Override public void broadcastChanges() {
        if (controller != null && !controller.getLevel().isClientSide) page = Math.min(page, Math.max(0, (controller.getContainerSize() - 1) / VISIBLE_SLOTS));
        super.broadcastChanges();
    }
    @Override public boolean stillValid(Player player) { return controller != null && controller.isFormed() && controller.stillValid(player); }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player) || player.level().isClientSide) return false;
        if (button == PREVIOUS && page > 0) page--;
        else if (button == NEXT && page + 1 < pages()) page++;
        else if (button >= 0 && button < controller.tank().contents().size()) controller.tank().select(button);
        else return false;
        broadcastChanges();
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < VISIBLE_SLOTS) { if (!moveItemStackTo(stack, VISIBLE_SLOTS, slots.size(), true)) return ItemStack.EMPTY; }
        else if (!moveItemStackTo(stack, 0, VISIBLE_SLOTS, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
    private class PageContainer implements Container {
        private int real(int index) { return page * VISIBLE_SLOTS + index; }
        private boolean available(int index) { return index >= 0 && index < VISIBLE_SLOTS && real(index) < controller.getContainerSize(); }
        @Override public int getContainerSize() { return VISIBLE_SLOTS; }
        @Override public int getMaxStackSize() { return 1; }
        @Override public boolean isEmpty() { for (int i = 0; i < VISIBLE_SLOTS; i++) if (!getItem(i).isEmpty()) return false; return true; }
        @Override public ItemStack getItem(int index) { return available(index) ? controller.getItem(real(index)) : ItemStack.EMPTY; }
        @Override public ItemStack removeItem(int index, int count) { return available(index) ? controller.removeItem(real(index), count) : ItemStack.EMPTY; }
        @Override public ItemStack removeItemNoUpdate(int index) { return available(index) ? controller.removeItemNoUpdate(real(index)) : ItemStack.EMPTY; }
        @Override public void setItem(int index, ItemStack stack) { if (available(index)) controller.setItem(real(index), stack); }
        @Override public void setChanged() { controller.setChanged(); }
        @Override public boolean stillValid(Player player) { return SmelteryMenu.this.stillValid(player); }
        @Override public boolean canPlaceItem(int index, ItemStack stack) { return available(index) && controller.canPlaceItem(real(index), stack); }
        @Override public void clearContent() { for (int i = 0; i < VISIBLE_SLOTS; i++) setItem(i, ItemStack.EMPTY); }
    }
}
