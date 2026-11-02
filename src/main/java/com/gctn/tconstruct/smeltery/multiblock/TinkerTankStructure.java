/*
 * 根据 TinkersAntique 的 MultiblockTinkerTank、MultiblockCuboid 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.multiblock;

import com.gctn.tconstruct.smeltery.block.SearedSlabBlock;
import com.gctn.tconstruct.smeltery.block.SearedStairBlock;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.List;

/** 封闭长方体：内宽、内深为 1～9，底板和顶板都必须有完整边框。 */
public record TinkerTankStructure(BlockPos min, BlockPos max, List<BlockPos> blocks) {
    public int capacity() {
        // 原版按包含外壳的体积计算；最小 3×3×3 储罐为 108 桶。
        return (max.getX() - min.getX() + 3) * (max.getY() - min.getY() + 3)
                * (max.getZ() - min.getZ() + 3) * 4000;
    }

    public static TinkerTankStructure detect(Level level, BlockPos controller, Direction inward) {
        BlockPos center = controller.relative(inward);
        for (int i = 0; i < 64 && inside(level, center); i++) {
            center = center.below();
        }
        center = center.above();
        if (!inside(level, center)) {
            return null;
        }
        int west = edge(level, center, Direction.WEST).getX();
        int east = edge(level, center, Direction.EAST).getX();
        int north = edge(level, center, Direction.NORTH).getZ();
        int south = edge(level, center, Direction.SOUTH).getZ();
        if (east - west < 2 || east - west > 10 || south - north < 2 || south - north > 10) {
            return null;
        }
        List<BlockPos> blocks = new ArrayList<>();
        if (!plane(level, controller, west, east, north, south, center.getY() - 1, false, blocks)) {
            return null;
        }
        int y = center.getY();
        for (; y < level.getMaxBuildHeight(); y++) {
            List<BlockPos> layer = new ArrayList<>();
            boolean valid = true;
            for (int x = west; x <= east; x++) {
                for (int z = north; z <= south; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (x == west || x == east || z == north || z == south) {
                        valid &= wall(level, pos, controller);
                        layer.add(pos);
                    } else {
                        valid &= inside(level, pos);
                    }
                }
            }
            if (!valid) {
                break;
            }
            blocks.addAll(layer);
        }
        // 控制器不能位于顶板，也不能用实心结构冒充没有内部空间的储罐。
        if (y <= center.getY() || y <= controller.getY()
                || !plane(level, controller, west, east, north, south, y, true, blocks)
                || !blocks.contains(controller)) {
            return null;
        }
        return new TinkerTankStructure(new BlockPos(west + 1, center.getY(), north + 1),
                new BlockPos(east - 1, y - 1, south - 1), List.copyOf(blocks));
    }

    private static boolean plane(Level level, BlockPos controller, int west, int east, int north, int south,
                                 int y, boolean ceiling, List<BlockPos> blocks) {
        for (int x = west; x <= east; x++) {
            for (int z = north; z <= south; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!available(level, pos, controller)) {
                    return false;
                }
                BlockState state = level.getBlockState(pos);
                boolean frame = x == west || x == east || z == north || z == south;
                boolean valid = frame ? pos.equals(controller) || brickOrDrain(state)
                        : ceiling ? wall(level, pos, controller) : brickOrDrain(state) || isGlass(state);
                // 与原版条件一致：楼梯、台阶仅能作为底半部顶板，不能放在侧墙。
                if (ceiling && bottomShape(state)) {
                    valid = true;
                }
                if (!valid) {
                    return false;
                }
                blocks.add(pos);
            }
        }
        return true;
    }

    public static boolean fullSearedSlab(BlockState state) {
        return state.getBlock() instanceof SearedSlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
    }

    private static boolean brickOrDrain(BlockState state) {
        return fullSearedSlab(state) || state.getBlock() instanceof SmelteryBlock block
                && (block.kind == SmelteryBlock.Kind.BRICK || block.kind == SmelteryBlock.Kind.DRAIN);
    }

    private static boolean isGlass(BlockState state) {
        return state.getBlock() instanceof SmelteryBlock block && block.kind == SmelteryBlock.Kind.GLASS;
    }

    private static boolean bottomShape(BlockState state) {
        return state.getBlock() instanceof SearedSlabBlock && state.getValue(SlabBlock.TYPE) != SlabType.TOP
                || state.getBlock() instanceof SearedStairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM;
    }

    private static boolean wall(Level level, BlockPos pos, BlockPos controller) {
        return available(level, pos, controller) && (pos.equals(controller) || fullSearedSlab(level.getBlockState(pos))
                || level.getBlockState(pos).getBlock() instanceof SmelteryBlock block && block.kind.structure);
    }

    private static boolean available(Level level, BlockPos pos, BlockPos controller) {
        if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) {
            return false;
        }
        // 已属于另一个有效结构的方块不能被抢占，包括冶炼炉的组成方块。
        return !(level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component)
                || component.master() == null || component.master().getBlockPos().equals(controller);
    }

    private static boolean inside(Level level, BlockPos pos) {
        return level.isInWorldBounds(pos) && level.hasChunkAt(pos) && level.isEmptyBlock(pos);
    }

    private static BlockPos edge(Level level, BlockPos pos, Direction side) {
        for (int i = 0; i < 9 && inside(level, pos); i++) {
            pos = pos.relative(side);
        }
        return pos;
    }
}
