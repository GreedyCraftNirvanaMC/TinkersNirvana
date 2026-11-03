/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.menu;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SearedFurnaceBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.HeatingContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmelteryMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final Container inventory;
    public final HeatingContainer controller;
    public final boolean furnace;
    private final ContainerData data;
    private final boolean client;
    private final Player player;
    private final int[] heats;
    private final int[] requiredHeats;
    public final int count;
    private final boolean paged;
    private final int inputCount;
    private final Container window;
    private boolean awaitingPage;
    private int requestedRow;
    private int firstRow;
    public final int rows;
    public final int visibleRows;
    public final int sideWidth;

    public SmelteryMenu(int id, Inventory player, RegistryFriendlyByteBuf buffer) {
        this(SmelteryModule.MENU.get(), id, player, buffer.readBlockPos(), buffer.readVarInt());
    }

    public static SmelteryMenu furnace(int id, Inventory player, RegistryFriendlyByteBuf buffer) {
        return new SmelteryMenu(SmelteryModule.FURNACE_MENU.get(), id, player, buffer.readBlockPos(), buffer.readVarInt());
    }

    private SmelteryMenu(MenuType<?> type, int id, Inventory player, BlockPos pos, int count) {
        this(type, id, player, pos, new SimpleContainer(Math.clamp(count, 0, 81 * 4096)),
                player.player.level().getBlockEntity(pos) instanceof HeatingContainer controller ? controller : null, true);
    }

    public SmelteryMenu(int id, Inventory player, SmelteryBlockEntity controller) {
        this(SmelteryModule.MENU.get(), id, player, controller.getBlockPos(), controller, controller, false);
    }

    public SmelteryMenu(int id, Inventory player, SearedFurnaceBlockEntity controller) {
        this(SmelteryModule.FURNACE_MENU.get(), id, player, controller.getBlockPos(), controller, controller, false);
    }

    private SmelteryMenu(MenuType<?> type, int id, Inventory player, BlockPos pos, Container inventory, HeatingContainer controller, boolean client) {
        super(type, id);
        furnace = type == SmelteryModule.FURNACE_MENU.get();
        this.pos = pos;
        this.inventory = inventory;
        this.controller = controller;
        this.client = client;
        this.player = player.player;
        count = inventory.getContainerSize();
        heats = new int[count];
        requiredHeats = new int[count];
        rows = (count + 2) / 3;
        visibleRows = Math.min(7, rows);
        sideWidth = 3 * 22 + 14 + (rows > visibleRows ? 12 : 0);
        // 原版槽位协议使用有符号 short；超大结构只给当前七行分配网络槽位。
        paged = count + 36 > Short.MAX_VALUE;
        inputCount = paged ? visibleRows * 3 : count;
        window = !paged ? inventory : client ? new SimpleContainer(inputCount) : new PageContainer();
        data = client ? new SimpleContainerData(3) : new ContainerData() {
            public int get(int index) {
                return index == 0 ? controller.fuel() : index == 1 ? controller.temperature() : controller.fuelQuality();
            }
            public void set(int index, int value) { }
            public int getCount() { return 3; }
        };
        for (int slot = 0; slot < inputCount; slot++) {
            addSlot(inputSlot(slot));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(player, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(player, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    private Slot inputSlot(int slot) {
        int row = slot / 3 - (paged ? 0 : firstRow);
        return new Slot(window, slot, -sideWidth + 18 + slot % 3 * 22, 8 + row * 18) {
            @Override
            public int getMaxStackSize() {
                return furnace ? 16 : 1;
            }
            @Override
            public boolean mayPlace(ItemStack stack) {
                return window.canPlaceItem(slot, stack);
            }
            @Override
            public boolean isActive() {
                return !client || !awaitingPage && row >= 0 && row < visibleRows
                        && (!paged || firstRow * 3 + slot < count);
            }
        };
    }

    public Slot visibleSlot(int backingSlot) {
        return slots.get(paged ? backingSlot - firstRow * 3 : backingSlot);
    }

    public int firstRow() {
        return firstRow;
    }

    public void scrollTo(int row) {
        row = Math.clamp(row, 0, Math.max(0, rows - visibleRows));
        if (paged && client) {
            requestedRow = row;
            if (!awaitingPage && row != firstRow) {
                // 收到新页物品和确认包之前禁用输入槽，避免把旧页点击发给新页。
                awaitingPage = true;
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.gctn.tconstruct.smeltery.network.SmelteryNetwork.Scroll(containerId, row));
            }
            return;
        }
        firstRow = row;
        // 现代槽位坐标不可变；普通结构仅替换显示对象，保持网络编号不变。
        if (paged) {
            return;
        }
        for (int i = 0; i < count; i++) {
            Slot slot = inputSlot(i);
            slot.index = i;
            slots.set(i, slot);
        }
    }

    public void changePage(int row) {
        if (client || !paged) {
            return;
        }
        resetQuickCraft();
        scrollTo(row);
        java.util.Arrays.fill(heats, firstRow * 3, Math.min(count, (firstRow + visibleRows) * 3), Integer.MIN_VALUE);
        // 同一连接依次同步物品、热量和确认，客户端确认后才允许操作当前页。
        broadcastFullState();
        broadcastChanges();
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                (net.minecraft.server.level.ServerPlayer) player,
                new com.gctn.tconstruct.smeltery.network.SmelteryNetwork.Page(containerId, firstRow));
    }

    public void confirmPage(int row) {
        if (!client || !paged) {
            return;
        }
        firstRow = Math.clamp(row, 0, Math.max(0, rows - visibleRows));
        awaitingPage = false;
        scrollTo(requestedRow);
    }

    public int fuel() {
        return data.get(0);
    }

    public int temperature() {
        return data.get(1);
    }

    public int fuelQuality() {
        return data.get(2);
    }

    public int heat(int slot) {
        return client ? heats[slot] : controller.heat(slot);
    }

    public int required(int slot) {
        return client ? requiredHeats[slot] : controller.required(slot);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (client || controller.getContainerSize() != count || !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        java.util.List<Integer> changes = new java.util.ArrayList<>();
        int begin = paged ? firstRow * 3 : 0;
        int end = paged ? Math.min(count, begin + inputCount) : count;
        for (int slot = begin; slot < end; slot++) {
            int heat = controller.heat(slot);
            int required = controller.required(slot);
            if (heats[slot] != heat || requiredHeats[slot] != required) {
                heats[slot] = heat;
                requiredHeats[slot] = required;
                changes.add(slot);
                changes.add(heat);
                changes.add(required);
            }
            if (changes.size() >= 3072) {
                sendHeats(serverPlayer, changes);
                changes.clear();
            }
        }
        if (!changes.isEmpty()) {
            sendHeats(serverPlayer, changes);
        }
    }

    private void sendHeats(net.minecraft.server.level.ServerPlayer player, java.util.List<Integer> values) {
        // 热量用整数三元组批量同步，避免 short 编号溢出及逐槽发包。
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new com.gctn.tconstruct.smeltery.network.SmelteryNetwork.Heats(containerId, values.stream().mapToInt(Integer::intValue).toArray()));
    }

    public void applyHeats(int[] values) {
        for (int i = 0; i + 2 < values.length; i += 3) {
            int slot = values[i];
            if (slot >= 0 && slot < count) {
                heats[slot] = values[i + 1];
                requiredHeats[slot] = values[i + 2];
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return controller != null && !controller.isRemoved() && controller.active()
                && player.level().getBlockEntity(pos) == controller && controller.getContainerSize() == count
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (client && paged) {
            // 隐藏槽的内容只保留在服务器，客户端等待权威结果，不预测整库转移。
            return ItemStack.EMPTY;
        }
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < inputCount ? moveItemStackTo(stack, inputCount, slots.size(), true)
                : paged || furnace ? moveIntoInventory(stack) : moveItemStackTo(stack, 0, inputCount, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        boolean collect = paged && !client && type == ClickType.PICKUP_ALL && slot >= 0 && slot < slots.size()
                && !getCarried().isEmpty() && (!slots.get(slot).hasItem() || !slots.get(slot).mayPickup(player));
        super.clicked(slot, button, type, player);
        if (collect) {
            // 双击收集仍覆盖隐藏槽；原版先收取不满堆叠，再收取整组。
            ItemStack carried = getCarried();
            for (int pass = 0; pass < 2 && carried.getCount() < carried.getMaxStackSize(); pass++) {
                for (int i = button == 0 ? 0 : count - 1; i >= 0 && i < count && carried.getCount() < carried.getMaxStackSize(); i += button == 0 ? 1 : -1) {
                    ItemStack current = inventory.getItem(i);
                    if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, carried)
                            && (pass != 0 || current.getCount() != current.getMaxStackSize())) {
                        ItemStack removed = inventory.removeItem(i, carried.getMaxStackSize() - carried.getCount());
                        carried.grow(removed.getCount());
                    }
                }
            }
        }
    }

    private boolean moveIntoInventory(ItemStack stack) {
        // Shift 点击仍遍历整个结构，而不是只向当前显示的七行插入。
        int original = stack.getCount();
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
            for (int slot = 0; slot < count && !stack.isEmpty(); slot++) {
                ItemStack current = inventory.getItem(slot);
                if (!inventory.canPlaceItem(slot, stack) || (pass == 0 ? current.isEmpty() : !current.isEmpty())
                        || !current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) {
                    continue;
                }
                int limit = Math.min(furnace ? 16 : 1, stack.getMaxStackSize());
                int moved = Math.min(stack.getCount(), limit - current.getCount());
                if (moved > 0) {
                    inventory.setItem(slot, stack.copyWithCount(current.getCount() + moved));
                    stack.shrink(moved);
                }
            }
        }
        return stack.getCount() != original;
    }

    private final class PageContainer implements Container {
        private int backing(int slot) { return firstRow * 3 + slot; }
        public int getContainerSize() { return inputCount; }
        public boolean isEmpty() {
            for (int slot = 0; slot < inputCount; slot++) {
                if (!getItem(slot).isEmpty()) { return false; }
            }
            return true;
        }
        public ItemStack getItem(int slot) { return backing(slot) < count ? inventory.getItem(backing(slot)) : ItemStack.EMPTY; }
        public ItemStack removeItem(int slot, int amount) { return backing(slot) < count ? inventory.removeItem(backing(slot), amount) : ItemStack.EMPTY; }
        public ItemStack removeItemNoUpdate(int slot) { return backing(slot) < count ? inventory.removeItemNoUpdate(backing(slot)) : ItemStack.EMPTY; }
        public void setItem(int slot, ItemStack stack) { if (backing(slot) < count) { inventory.setItem(backing(slot), stack); } }
        public int getMaxStackSize() { return furnace ? 16 : 1; }
        public void setChanged() { inventory.setChanged(); }
        public boolean stillValid(Player player) { return SmelteryMenu.this.stillValid(player); }
        public boolean canPlaceItem(int slot, ItemStack stack) { return backing(slot) < count && inventory.canPlaceItem(backing(slot), stack); }
        public void clearContent() {
            for (int slot = 0; slot < inputCount; slot++) { setItem(slot, ItemStack.EMPTY); }
        }
    }
}
