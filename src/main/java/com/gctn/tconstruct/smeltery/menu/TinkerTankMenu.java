/*
 * 根据 TinkersAntique 的 ContainerTinkerTank 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.menu;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.block.entity.TinkerTankBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 储罐没有物品槽，也不显示玩家物品栏。 */
public class TinkerTankMenu extends AbstractContainerMenu {
    public final TinkerTankBlockEntity controller;

    public TinkerTankMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof TinkerTankBlockEntity tank ? tank : null);
    }

    public TinkerTankMenu(int id, Inventory inventory, TinkerTankBlockEntity controller) {
        super(SmelteryModule.TINKER_TANK_MENU.get(), id);
        this.controller = controller;
    }

    @Override
    public boolean stillValid(Player player) {
        return controller != null && controller.active()
                && player.level().getBlockEntity(controller.getBlockPos()) == controller
                && player.distanceToSqr(controller.getBlockPos().getCenter()) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
