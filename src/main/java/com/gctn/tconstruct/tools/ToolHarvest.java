/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** 每个额外方块都走原版玩家破坏流程，包括领地保护、掉落、事件和耐久。 */
public final class ToolHarvest {
    private static final ThreadLocal<Boolean> HARVESTING = ThreadLocal.withInitial(() -> false);
    private static final int TREE_LIMIT = 256;
    private static final ThreadLocal<net.minecraft.core.BlockPos> REPLANTING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SEED_RESERVED = ThreadLocal.withInitial(() -> false);
    private ToolHarvest() {}
    public static void afterBreak(AntiqueTool item, ItemStack tool, Level level, BlockState original, BlockPos pos, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || player.isShiftKeyDown() || HARVESTING.get() || TinkerTools.isBroken(tool)) { return; }
        var action = item.definition().action();
        if (action != ToolDefinition.Action.HAMMER && action != ToolDefinition.Action.EXCAVATOR
                && action != ToolDefinition.Action.LUMBERAXE && action != ToolDefinition.Action.SCYTHE) { return; }
        HARVESTING.set(true);
        try {
            if (action == ToolDefinition.Action.LUMBERAXE && original.is(BlockTags.LOGS)) { tree(player, tool, pos, original); }
            else {
                Direction.Axis axis = Direction.getNearest(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z).getAxis();
                int width = 1 + Math.min(1, ToolModifiers.level(tool, "harvestwidth"));
                int height = 1 + Math.min(1, ToolModifiers.level(tool, "harvestheight"));
                for (int u = -width; u <= width; u++) {
                    for (int v = -height; v <= height; v++) {
                        if (u == 0 && v == 0) { continue; }
                        BlockPos next = switch (axis) { case X -> pos.offset(0, v, u); case Y -> pos.offset(u, 0, v); case Z -> pos.offset(u, v, 0); };
                        harvest(player, tool, next, original);
                    }
                }
            }
        } finally { HARVESTING.set(false); }
    }

    public static boolean harvestCrops(AntiqueTool item, ItemStack tool, net.minecraft.world.item.context.UseOnContext context) {
        Level level = context.getLevel();
        BlockState center = level.getBlockState(context.getClickedPos());
        if (!(center.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop) || !crop.isMaxAge(center)) { return false; }
        if (level.isClientSide) { return true; }
        if (!(context.getPlayer() instanceof ServerPlayer player)) { return false; }
        int radius = item.definition().action() == ToolDefinition.Action.SCYTHE && !player.isShiftKeyDown() ? 1 : 0;
        boolean harvested = false;
        for (BlockPos candidate : BlockPos.betweenClosed(context.getClickedPos().offset(-radius, 0, -radius), context.getClickedPos().offset(radius, 0, radius))) {
            BlockPos pos = candidate.immutable();
            BlockState state = level.getBlockState(pos);
            if (TinkerTools.isBroken(tool) || !(state.getBlock() instanceof net.minecraft.world.level.block.CropBlock plant)
                    || !plant.isMaxAge(state) || !player.mayInteract(level, pos)) { continue; }
            REPLANTING.set(pos);
            SEED_RESERVED.set(false);
            try {
                if (player.gameMode.destroyBlock(pos)) {
                    if (state.getDestroySpeed(level, pos) == 0) { item.damage(tool, 1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND); }
                    if (SEED_RESERVED.get() && level.isEmptyBlock(pos)) { level.setBlockAndUpdate(pos, plant.getStateForAge(0)); }
                    harvested = true;
                }
            } finally { REPLANTING.remove(); SEED_RESERVED.remove(); }
        }
        return harvested;
    }

    public static void reserveSeed(net.neoforged.neoforge.event.level.BlockDropsEvent event) {
        if (!event.getPos().equals(REPLANTING.get()) || !(event.getState().getBlock() instanceof net.minecraft.world.level.block.CropBlock crop)) { return; }
        ItemStack seed = crop.getCloneItemStack(event.getLevel(), event.getPos(), event.getState());
        for (var iterator = event.getDrops().iterator(); iterator.hasNext();) {
            ItemStack drop = iterator.next().getItem();
            if (drop.is(seed.getItem()) && !drop.isEmpty()) {
                drop.shrink(1);
                if (drop.isEmpty()) { iterator.remove(); }
                SEED_RESERVED.set(true);
                return;
            }
        }
    }
    private static void tree(ServerPlayer player, ItemStack tool, BlockPos origin, BlockState original) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);
        while (!queue.isEmpty() && visited.size() <= TREE_LIMIT && !TinkerTools.isBroken(tool)) {
            BlockPos current = queue.removeFirst();
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos next = current.offset(x, y, z);
                        if (!player.level().hasChunkAt(next) || next.distSqr(origin) > 32 * 32
                                || !player.level().getBlockState(next).is(BlockTags.LOGS) || !visited.add(next)) { continue; }
                        if (harvest(player, tool, next, original)) { queue.addLast(next); }
                        if (visited.size() >= TREE_LIMIT) { return; }
                    }
                }
            }
        }
    }
    private static boolean harvest(ServerPlayer player, ItemStack tool, BlockPos pos, BlockState original) {
        if (TinkerTools.isBroken(tool) || player.getMainHandItem() != tool || !player.level().hasChunkAt(pos)) { return false; }
        BlockState state = player.level().getBlockState(pos);
        float hardness = state.getDestroySpeed(player.level(), pos);
        if (state.isAir() || state.hasBlockEntity() || hardness < 0 || hardness > Math.max(1, original.getDestroySpeed(player.level(), pos)) * 3
                || tool.getDestroySpeed(state) <= 1 || (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state)) || !player.mayInteract(player.level(), pos)) { return false; }
        return player.gameMode.destroyBlock(pos);
    }
}
