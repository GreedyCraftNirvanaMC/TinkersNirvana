/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.menu;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmelteryMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final Container inventory;
    public final SmelteryBlockEntity controller;
    private final ContainerData data;
    private final boolean client;
    private final Player player;
    private final int[] heats;
    private final int[] requiredHeats;
    public final int count;
    private int firstRow;
    public final int rows;
    public final int visibleRows;
    public final int sideWidth;

    public SmelteryMenu(int id, Inventory player, RegistryFriendlyByteBuf buffer) {
        this(id, player, buffer.readBlockPos(), buffer.readVarInt());
    }

    private SmelteryMenu(int id, Inventory player, BlockPos pos, int count) {
        this(id, player, pos, new SimpleContainer(Math.clamp(count, 0, 81 * 4096)),
                player.player.level().getBlockEntity(pos) instanceof SmelteryBlockEntity controller ? controller : null, true);
    }

    public SmelteryMenu(int id, Inventory player, SmelteryBlockEntity controller) {
        this(id, player, controller.getBlockPos(), controller, controller, false);
    }

    private SmelteryMenu(int id, Inventory player, BlockPos pos, Container inventory, SmelteryBlockEntity controller, boolean client) {
        super(SmelteryModule.MENU.get(), id);
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
        data = client ? new SimpleContainerData(2) : new ContainerData() {
            public int get(int index) {
                return index == 0 ? controller.fuel() : controller.temperature();
            }
            public void set(int index, int value) { }
            public int getCount() { return 2; }
        };
        for (int slot = 0; slot < count; slot++) {
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
        int row = slot / 3 - firstRow;
        return new Slot(inventory, slot, -sideWidth + 18 + slot % 3 * 22, 8 + row * 18) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
            @Override
            public boolean isActive() {
                return !client || row >= 0 && row < visibleRows;
            }
        };
    }

    public int firstRow() {
        return firstRow;
    }

    public void scrollTo(int row) {
        firstRow = Math.clamp(row, 0, Math.max(0, rows - visibleRows));
        // Slot coordinates became final in modern Minecraft. Replace only their presentation objects;
        // their network IDs and backing inventory indices remain unchanged while scrolling.
        for (int i = 0; i < count; i++) {
            Slot slot = inputSlot(i);
            slot.index = i;
            slots.set(i, slot);
        }
    }

    public int fuel() {
        return data.get(0);
    }

    public int temperature() {
        return data.get(1);
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
        for (int slot = 0; slot < count; slot++) {
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
        // Vanilla menu properties have signed-short IDs. Batched integer triples support tall
        // 9x9 structures without overflowing those IDs or emitting one packet per heat value.
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
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < count ? moveItemStackTo(stack, count, slots.size(), true) : moveItemStackTo(stack, 0, count, false);
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
}
