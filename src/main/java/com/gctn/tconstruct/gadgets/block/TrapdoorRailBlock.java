/*
 * 根据 TinkersAntique 的 BlockWoodRailDropper 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Consumer;

/** 漏斗矿车每次经过时向下方库存卸载一个物品。 */
public class TrapdoorRailBlock extends WoodenRailBlock {
    public TrapdoorRailBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<RailBlock> codec() {
        return simpleCodec(TrapdoorRailBlock::new);
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (level.isClientSide || !(cart instanceof Hopper)
                || cart.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, Direction.DOWN) == null
                || level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.DOWN) == null) {
            return;
        }
        var source = cart.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, Direction.UP);
        var target = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.UP);
        if (source == null || target == null) {
            return;
        }
        transferOne(source, target, stack -> level.addFreshEntity(new ItemEntity(level, cart.getX(), cart.getY(), cart.getZ(), stack)));
    }

    /** 模拟成功后卸载一个物品；插入状态改变时退回原槽，无法退回的余量交由调用方保存。 */
    public static void transferOne(IItemHandler source, IItemHandler target, Consumer<ItemStack> remainderHandler) {
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack candidate = source.extractItem(slot, 1, true);
            if (candidate.isEmpty() || !ItemHandlerHelper.insertItem(target, candidate, true).isEmpty()) {
                continue;
            }
            ItemStack removed = source.extractItem(slot, 1, false);
            if (removed.isEmpty()) {
                continue;
            }
            ItemStack remainder = ItemHandlerHelper.insertItem(target, removed, false);
            if (!remainder.isEmpty()) {
                // 模组库存可能在模拟后拒绝真实插入；先退回原槽，仍有余量时掉落以免吞物品。
                remainder = source.insertItem(slot, remainder, false);
                if (!remainder.isEmpty()) {
                    remainderHandler.accept(remainder);
                }
            }
            return;
        }
    }
}
