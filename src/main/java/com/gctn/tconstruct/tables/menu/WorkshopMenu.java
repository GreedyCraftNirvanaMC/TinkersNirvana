/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's table and dynamic chest menus. */
package com.gctn.tconstruct.tables.menu;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.WorkshopModule.Kind;
import com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity;
import com.gctn.tconstruct.tables.recipe.PartBuilderRecipes;
import com.gctn.tconstruct.tools.PatternItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/** Stable physical slots; paged views never resize the network menu. */
public class WorkshopMenu extends AbstractContainerMenu {
    public static final int PAGE_SIZE = 45;
    private final Kind kind;
    private final BlockPos pos;
    private final Container backing;
    private final ResultContainer result = new ResultContainer();
    private final SimpleContainerData data = new SimpleContainerData(2);
    private final Inventory inventory;
    private final int playerStart;
    private final int playerEnd;
    private final NearbyInventory nearby;
    private boolean consuming;

    public WorkshopMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos(), buf.readEnum(Kind.class), NearbyInventory.read(buf));
    }
    private WorkshopMenu(int id, Inventory inventory, BlockPos pos, Kind kind, NearbyInventory.Source source) {
        this(id, inventory, kind, pos, new SimpleContainer(kind.size), source);
    }
    public WorkshopMenu(int id, Inventory inventory, WorkshopBlockEntity entity) {
        this(id, inventory, entity.kind(), entity.getBlockPos(), entity, NearbyInventory.find(inventory.player, entity.getBlockPos(), entity.kind()));
    }
    private WorkshopMenu(int id, Inventory inventory, Kind kind, BlockPos pos, Container backing, NearbyInventory.Source source) {
        super(WorkshopModule.MENU.get(), id);
        this.kind = kind; this.pos = pos; this.backing = backing; this.inventory = inventory;
        nearby = new NearbyInventory(inventory.player, source, kind == Kind.STENCIL ? 182 : -94);
        data.set(1, -1);
        if (kind.storage()) {
            Container page = new PageView();
            for (int i = 0; i < PAGE_SIZE; i++) { addSlot(new Slot(page, i, 8 + i % 9 * 18, 38 + i / 9 * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return page.canPlaceItem(getContainerSlot(), stack); }
                @Override public boolean isActive() { return page() * PAGE_SIZE + getContainerSlot() < kind.size; }
            }); }
        } else {
            addSlot(new Slot(result, 0, 124, 42) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) {
                    ItemStack current = preview();
                    return !current.isEmpty() && ItemStack.isSameItemSameComponents(current, getItem()) && current.getCount() == getItem().getCount();
                }
                @Override public ItemStack remove(int count) { return super.remove(getItem().getCount()); }
                @Override public void onTake(Player player, ItemStack stack) { consume(); super.onTake(player, stack); }
            });
            if (kind == Kind.STENCIL) {
                addSlot(new Slot(backing, 0, 48, 42) {
                    @Override public boolean mayPlace(ItemStack stack) {
                        return stack.getItem() instanceof PatternItem && (Config.REUSE_STENCIL.get() || !TinkerDataComponents.getMaterialTag(stack).contains("Pattern"));
                    }
                    @Override public void setChanged() { super.setChanged(); slotsChanged(backing); }
                });
            } else {
                for (int i = 0; i < 6; i++) {
                    var position = com.gctn.tconstruct.tables.data.ToolStationSlotPositions.getDisplayInputSlotPosition(com.gctn.tconstruct.tables.data.Mode.DEFAULT, i);
                    addSlot(new Slot(backing, i, position.slotX(), position.slotY()) {
                        @Override public boolean mayPlace(ItemStack stack) {
                            return getContainerSlot() == 0 ? stack.getItem() instanceof PatternItem
                                    : com.gctn.tconstruct.library.materials.MaterialInputs.match(stack) != null;
                        }
                        @Override public int getMaxStackSize() { return getContainerSlot() == 0 ? 1 : super.getMaxStackSize(); }
                        @Override public void setChanged() { super.setChanged(); slotsChanged(backing); }
                    });
                }
                addSlot(new Slot(backing, 6, 151, 63) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
            }
        }
        playerStart = slots.size();
        int playerY = kind.storage() ? 142 : 94;
        for (int row = 0; row < 3; row++) { for (int col = 0; col < 9; col++) { addSlot(new Slot(inventory, 9 + row * 9 + col, 8 + col * 18, playerY + row * 18)); } }
        for (int col = 0; col < 9; col++) { addSlot(new Slot(inventory, col, 8 + col * 18, playerY + 58)); }
        playerEnd = slots.size();
        nearby.addSlots(this::addSlot);
        addDataSlots(data);
        addDataSlots(nearby.data());
        slotsChanged(backing);
    }
    public Kind kind() { return kind; }
    public BlockPos position() { return pos; }
    public NearbyInventory nearby() { return nearby; }
    @Override public void clicked(int slot, int button, net.minecraft.world.inventory.ClickType type, Player player) {
        if (kind == Kind.PART_BUILDER && type == net.minecraft.world.inventory.ClickType.PICKUP && button == 0 && getCarried().isEmpty()
                && slot >= playerEnd && slot < slots.size() && slots.get(slot).isActive() && slots.get(slot).getItem().getItem() instanceof PatternItem) {
            if (player.level().isClientSide) { return; }
            if (stillValid(player) && nearby.swapPattern(slot - playerEnd, backing)) { slotsChanged(backing); broadcastFullState(); return; }
        }
        super.clicked(slot, button, type, player);
    }
    public int page() { return data.get(0); }
    public int pages() { return (kind.size + PAGE_SIZE - 1) / PAGE_SIZE; }
    public int selection() { return data.get(1); }
    public static List<AntiqueParts.Definition> patterns() { return AntiqueParts.entries().values().stream().filter(part -> !part.id().equals("bolt_core")).toList(); }
    @Override public boolean stillValid(Player player) {
        return backing.stillValid(player) && nearby.valid() && WorkshopModule.isTable(player.level(), pos)
                && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64;
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player)) { return false; }
        if (id >= 100) { return WorkshopModule.navigate(player, pos, id); }
        if (id == NearbyInventory.PREVIOUS || id == NearbyInventory.NEXT) {
            if (!nearby.click(id)) { return false; } broadcastFullState(); return true;
        }
        if (kind.storage() && id >= 0 && id < pages()) { data.set(0, id); broadcastFullState(); return true; }
        if (kind == Kind.STENCIL && id >= 0 && id < patterns().size()) { data.set(1, id); slotsChanged(backing); return true; }
        return false;
    }
    private ItemStack preview() {
        if (kind == Kind.PART_BUILDER) { var plan = PartBuilderRecipes.planWithRemainder(backing); return plan == null ? ItemStack.EMPTY : plan.result(); }
        if (kind != Kind.STENCIL || selection() < 0 || selection() >= patterns().size() || !(backing.getItem(0).getItem() instanceof PatternItem)) { return ItemStack.EMPTY; }
        if (!Config.REUSE_STENCIL.get() && TinkerDataComponents.getMaterialTag(backing.getItem(0)).contains("Pattern")) { return ItemStack.EMPTY; }
        ItemStack output = new ItemStack(PatternItem.PATTERN.get());
        CompoundTag tag = new CompoundTag(); tag.putString("Pattern", patterns().get(selection()).id());
        output.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(tag));
        return output;
    }
    private void consume() {
        if (inventory.player.level().isClientSide || preview().isEmpty()) { return; }
        consuming = true;
        try {
            if (kind == Kind.STENCIL) { backing.removeItem(0, 1); }
            else { var plan = PartBuilderRecipes.planWithRemainder(backing); if (plan != null) { plan.consume(backing); } }
            backing.setChanged();
        } finally { consuming = false; }
        slotsChanged(backing);
    }
    @Override public void slotsChanged(Container container) {
        if (!consuming && !kind.storage() && !inventory.player.level().isClientSide) { result.setItem(0, preview()); }
    }
    @Override public void broadcastChanges() { if (!kind.storage()) { slotsChanged(backing); } super.broadcastChanges(); }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return slot.container != result && super.canTakeItemForPickAll(stack, slot); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) { return ItemStack.EMPTY; }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) { return ItemStack.EMPTY; }
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index < playerStart) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, !kind.storage() && index == 0)) { return ItemStack.EMPTY; }
        } else if (index >= playerEnd) {
            if (!moveItemStackTo(stack, playerStart, playerEnd, false)) { return ItemStack.EMPTY; }
        } else if (!moveItemStackTo(stack, kind.storage() ? 0 : 1, kind == Kind.PART_BUILDER ? 7 : playerStart, false)
                && !moveItemStackTo(stack, playerEnd, slots.size(), false)) { return ItemStack.EMPTY; }
        if (stack.getCount() == original.getCount()) { return ItemStack.EMPTY; }
        if (stack.isEmpty()) { slot.setByPlayer(ItemStack.EMPTY); } else { slot.setChanged(); }
        slot.onTake(player, stack);
        if (!kind.storage() && index == 0 && !stack.isEmpty()) { player.drop(stack, false); }
        return original;
    }
    private class PageView implements Container {
        private int index(int slot) { return inventory.player.level().isClientSide ? slot : page() * PAGE_SIZE + slot; }
        @Override public int getContainerSize() { return PAGE_SIZE; }
        @Override public boolean isEmpty() { for (int i = 0; i < PAGE_SIZE; i++) { if (!getItem(i).isEmpty()) { return false; } } return true; }
        @Override public ItemStack getItem(int slot) { return index(slot) < backing.getContainerSize() ? backing.getItem(index(slot)) : ItemStack.EMPTY; }
        @Override public ItemStack removeItem(int slot, int amount) { return index(slot) < backing.getContainerSize() ? backing.removeItem(index(slot), amount) : ItemStack.EMPTY; }
        @Override public ItemStack removeItemNoUpdate(int slot) { return index(slot) < backing.getContainerSize() ? backing.removeItemNoUpdate(index(slot)) : ItemStack.EMPTY; }
        @Override public void setItem(int slot, ItemStack stack) { if (index(slot) < backing.getContainerSize()) { backing.setItem(index(slot), stack); } }
        @Override public int getMaxStackSize() { return kind == Kind.PATTERN_CHEST ? 1 : backing.getMaxStackSize(); }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) { return index(slot) < backing.getContainerSize() && WorkshopBlockEntity.validInput(kind, backing, index(slot), stack); }
        @Override public void setChanged() { backing.setChanged(); }
        @Override public boolean stillValid(Player player) { return WorkshopMenu.this.stillValid(player); }
        @Override public void clearContent() { for (int i = 0; i < PAGE_SIZE; i++) { setItem(i, ItemStack.EMPTY); } }
    }
}
