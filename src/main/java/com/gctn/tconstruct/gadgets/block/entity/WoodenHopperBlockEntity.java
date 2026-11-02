/*
 * 根据 TinkersAntique 的 TileWoodenHopper 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block.entity;

import com.gctn.tconstruct.gadgets.GadgetModule;
import com.gctn.tconstruct.gadgets.menu.WoodenHopperMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** 通过原生漏斗继承能力保留实体吸取、侧面库存和存档行为。 */
public class WoodenHopperBlockEntity extends HopperBlockEntity {
    private final IItemHandler itemHandler = new InvWrapper(this);

    public WoodenHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        setItems(NonNullList.withSize(1, ItemStack.EMPTY));
    }

    @Override
    public BlockEntityType<?> getType() {
        // NeoForge 明确通过此方法校验和保存模组子类，不能沿用原生漏斗类型。
        return GadgetModule.WOODEN_HOPPER_ENTITY.get();
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public void setCooldown(int ticks) {
        super.setCooldown(ticks * 2);
    }

    public IItemHandler itemHandler() {
        return itemHandler;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new WoodenHopperMenu(id, inventory, this);
    }
}
