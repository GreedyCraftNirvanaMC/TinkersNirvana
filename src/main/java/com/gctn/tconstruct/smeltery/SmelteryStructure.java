// SPDX-License-Identifier: LGPL-3.0-only
// Adapted from TinkersAntique's Smeltery detector; see THIRD_PARTY_NOTICES.md.
package com.gctn.tconstruct.smeltery;

import com.gctn.tconstruct.smeltery.block.SmelteryControllerBlock;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects Antique-style floors and unframed walls around a rectangular air interior.
 * The first invalid wall layer ends the usable height; corners and a roof are not required.
 * Unloaded chunks suspend detection instead of being treated as missing blocks or force-loaded.
 */
public record SmelteryStructure(BlockPos min, BlockPos max, List<BlockPos> members, List<BlockPos> tanks) {
    public static final int MAX_INNER_SIZE = 9;
    public int width() { return max.getX() - min.getX() + 1; }
    public int height() { return max.getY() - min.getY() + 1; }
    public int depth() { return max.getZ() - min.getZ() + 1; }
    public int volume() { return width() * height() * depth(); }
    public boolean loaded(Level level) { return level.hasChunksAt(min.offset(-1, -1, -1), max.offset(1, 0, 1)); }

    public record Detection(SmelteryStructure structure, boolean unloaded) {}

    public static Detection detect(Level level, BlockPos controller) {
        var scan = new Scan(level, controller);
        SmelteryStructure result = scan.detect();
        return new Detection(result, scan.unloaded);
    }

    private static class Scan {
        private final Level level;
        private final BlockPos controller;
        private boolean unloaded;
        Scan(Level level, BlockPos controller) { this.level = level; this.controller = controller; }
        private boolean loaded(BlockPos pos) {
            if (level.isOutsideBuildHeight(pos)) return false;
            if (!level.hasChunkAt(pos)) { unloaded = true; return false; }
            return true;
        }
        private boolean air(BlockPos pos) { return loaded(pos) && level.getBlockState(pos).isAir(); }
        private boolean member(BlockPos pos, boolean floor) {
            if (!loaded(pos)) return false;
            if (pos.equals(controller)) return !floor;
            return level.getBlockState(pos).is(floor ? SmelteryRegistration.FLOOR : SmelteryRegistration.WALL)
                    && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part && part.canJoin(controller);
        }
        private BlockPos edge(BlockPos start, Direction direction) {
            BlockPos cursor = start;
            for (int i = 0; i <= MAX_INNER_SIZE && air(cursor); i++) cursor = cursor.relative(direction);
            return cursor;
        }
        SmelteryStructure detect() {
            var state = level.getBlockState(controller);
            BlockPos inside = controller.relative(state.getValue(SmelteryControllerBlock.FACING).getOpposite());
            if (!air(inside)) return null;
            // The world bounds and loaded() bound the scan. An arbitrary 64-block
            // descent cap rejects tall structures that upward scanning permits.
            while (air(inside.below())) inside = inside.below();
            int minX = edge(inside, Direction.WEST).getX() + 1;
            int maxX = edge(inside, Direction.EAST).getX() - 1;
            int minZ = edge(inside, Direction.NORTH).getZ() + 1;
            int maxZ = edge(inside, Direction.SOUTH).getZ() - 1;
            int width = maxX - minX + 1, depth = maxZ - minZ + 1;
            if (width < 1 || width > MAX_INNER_SIZE || depth < 1 || depth > MAX_INNER_SIZE) return null;
            // The controller must be on a side, rather than an ignored corner or the floor.
            boolean onX = (controller.getX() == minX - 1 || controller.getX() == maxX + 1) && controller.getZ() >= minZ && controller.getZ() <= maxZ;
            boolean onZ = (controller.getZ() == minZ - 1 || controller.getZ() == maxZ + 1) && controller.getX() >= minX && controller.getX() <= maxX;
            if (!onX && !onZ) return null;
            List<BlockPos> members = new ArrayList<>(), tanks = new ArrayList<>();
            int bottom = inside.getY();
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                BlockPos pos = new BlockPos(x, bottom - 1, z);
                if (!member(pos, true)) return null;
                members.add(pos);
            }
            int top = bottom - 1;
            for (int y = bottom; y < level.getMaxBuildHeight(); y++) {
                List<BlockPos> layer = new ArrayList<>();
                boolean valid = true;
                for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) valid &= air(new BlockPos(x, y, z));
                for (int x = minX; x <= maxX; x++) { layer.add(new BlockPos(x, y, minZ - 1)); layer.add(new BlockPos(x, y, maxZ + 1)); }
                for (int z = minZ; z <= maxZ; z++) { layer.add(new BlockPos(minX - 1, y, z)); layer.add(new BlockPos(maxX + 1, y, z)); }
                for (BlockPos pos : layer) valid &= member(pos, false);
                if (!valid) break;
                members.addAll(layer);
                for (BlockPos pos : layer) if (level.getBlockState(pos).is(SmelteryRegistration.SEARED_TANK.get())) tanks.add(pos);
                top = y;
            }
            if (top < controller.getY() || tanks.isEmpty() || unloaded) return null;
            return new SmelteryStructure(new BlockPos(minX, bottom, minZ), new BlockPos(maxX, top, maxZ), List.copyOf(members), List.copyOf(tanks));
        }
    }
}
