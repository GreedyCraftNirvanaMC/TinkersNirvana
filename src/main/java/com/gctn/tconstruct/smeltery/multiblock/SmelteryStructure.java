/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.multiblock;

import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/** Open cuboid detection, following Antique's MultiblockCuboid/MultiblockSmeltery rules. */
public record SmelteryStructure(BlockPos min, BlockPos max, List<BlockPos> blocks, List<BlockPos> tanks) {
    public int size() {
        return (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
    }

    public static SmelteryStructure detect(Level level, BlockPos controller, Direction inward) {
        BlockPos center = controller.relative(inward);
        int controllerY = center.getY();
        if (!inside(level, center)) {
            return null;
        }
        int down = 0;
        while (down++ < 64 && inside(level, center)) {
            center = center.below();
        }
        center = center.above();
        int west = edge(level, center, Direction.WEST).getX();
        int east = edge(level, center, Direction.EAST).getX();
        int north = edge(level, center, Direction.NORTH).getZ();
        int south = edge(level, center, Direction.SOUTH).getZ();
        if (controllerY < center.getY() || east - west < 2 || south - north < 2
                || east - west > 10 || south - north > 10) {
            return null;
        }
        List<BlockPos> blocks = new ArrayList<>();
        List<BlockPos> tanks = new ArrayList<>();
        // Corners and the outside rim of the floor are deliberately optional in Antique.
        for (int x = west + 1; x < east; x++) {
            for (int z = north + 1; z < south; z++) {
                BlockPos pos = new BlockPos(x, center.getY() - 1, z);
                if (!valid(level, pos, controller) || !TinkerTankStructure.fullSearedSlab(level.getBlockState(pos))
                        && ((SmelteryBlock) level.getBlockState(pos).getBlock()).kind != SmelteryBlock.Kind.BRICK) {
                    return null;
                }
                blocks.add(pos);
            }
        }
        int y = center.getY();
        for (; y < level.getMaxBuildHeight(); y++) {
            List<BlockPos> layer = new ArrayList<>();
            boolean complete = true;
            for (int x = west + 1; x < east; x++) {
                for (int z = north + 1; z < south; z++) {
                    complete &= inside(level, new BlockPos(x, y, z));
                }
                layer.add(new BlockPos(x, y, north));
                layer.add(new BlockPos(x, y, south));
            }
            for (int z = north + 1; z < south; z++) {
                layer.add(new BlockPos(west, y, z));
                layer.add(new BlockPos(east, y, z));
            }
            for (BlockPos pos : layer) {
                complete &= valid(level, pos, controller);
            }
            if (!complete) {
                break;
            }
            blocks.addAll(layer);
            for (BlockPos pos : layer) {
                if (level.getBlockState(pos).getBlock() instanceof SmelteryBlock block && block.kind == SmelteryBlock.Kind.TANK) {
                    tanks.add(pos);
                }
            }
        }
        if (y <= controllerY || tanks.isEmpty() || !blocks.contains(controller)) {
            return null;
        }
        return new SmelteryStructure(new BlockPos(west + 1, center.getY(), north + 1),
                new BlockPos(east - 1, y - 1, south - 1), List.copyOf(blocks), List.copyOf(tanks));
    }

    private static BlockPos edge(Level level, BlockPos center, Direction side) {
        for (int i = 0; i < 9 && inside(level, center); i++) {
            center = center.relative(side);
        }
        return center;
    }

    private static boolean inside(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.isInWorldBounds(pos) && level.isEmptyBlock(pos);
    }

    private static boolean valid(Level level, BlockPos pos, BlockPos controller) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        if (pos.equals(controller)) {
            return true;
        }
        // 原版合并台阶会变成完整焦黑方块；现代双台阶沿用同一结构规则。
        if (!TinkerTankStructure.fullSearedSlab(level.getBlockState(pos))
                && (!(level.getBlockState(pos).getBlock() instanceof SmelteryBlock block) || !block.kind.structure)) {
            return false;
        }
        // A valid servant cannot belong to two controllers, including two touching structures.
        return !(level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component)
                || component.master() == null || component.master().getBlockPos().equals(controller);
    }
}
