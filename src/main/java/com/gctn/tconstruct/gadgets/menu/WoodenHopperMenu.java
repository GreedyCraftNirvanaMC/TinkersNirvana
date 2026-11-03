/*
 * 根据 TinkersAntique 的单槽木漏斗界面及原生漏斗菜单移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.menu;

import com.gctn.tconstruct.gadgets.GadgetModule;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 原版木漏斗将唯一的槽位居中，其余坐标与原生漏斗相同。 */
public class WoodenHopperMenu extends AbstractContainerMenu {
    private final Container hopper;

    public WoodenHopperMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1));
    }

    public WoodenHopperMenu(int id, Inventory inventory, Container hopper) {
        super(GadgetModule.WOODEN_HOPPER_MENU.get(), id);
        checkContainerSize(hopper, 1);
        this.hopper = hopper;
        hopper.startOpen(inventory.player);
        addSlot(new Slot(hopper, 0, 80, 20));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return hopper.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem();
        ItemStack previous = stack.copy();
        if (!(index == 0 ? moveItemStackTo(stack, 1, slots.size(), true) : moveItemStackTo(stack, 0, 1, false))) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return previous;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        hopper.stopOpen(player);
    }
}
