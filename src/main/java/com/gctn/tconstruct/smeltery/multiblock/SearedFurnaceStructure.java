/*
 * 根据 TinkersAntique 的 MultiblockSearedFurnace、MultiblockCuboid 移植。
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

/** 封闭焦黑熔炉：侧墙使用完整焦黑方块，燃料储罐只能位于外框。 */
public record SearedFurnaceStructure(BlockPos min, BlockPos max, List<BlockPos> blocks, List<BlockPos> tanks) {
    public int slots() {
        return 9 + 3 * (max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1);
    }

    public static SearedFurnaceStructure detect(Level level, BlockPos controller, Direction inward) {
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
        List<BlockPos> tanks = new ArrayList<>();
        if (!plane(level, controller, west, east, north, south, center.getY() - 1, false, blocks, tanks)) {
            return null;
        }
        int y = center.getY();
        for (; y < level.getMaxBuildHeight(); y++) {
            List<BlockPos> layer = new ArrayList<>();
            List<BlockPos> layerTanks = new ArrayList<>();
            boolean valid = true;
            for (int x = west; x <= east; x++) {
                for (int z = north; z <= south; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    boolean sideX = x == west || x == east;
                    boolean sideZ = z == north || z == south;
                    if (sideX || sideZ) {
                        boolean tank = sideX && sideZ && fuelTank(level.getBlockState(pos));
                        valid &= available(level, pos, controller) && (pos.equals(controller) || brick(level.getBlockState(pos)) || tank);
                        layer.add(pos);
                        if (tank) {
                            layerTanks.add(pos);
                        }
                    } else {
                        valid &= inside(level, pos);
                    }
                }
            }
            if (!valid) {
                break;
            }
            blocks.addAll(layer);
            tanks.addAll(layerTanks);
        }
        if (y <= center.getY() || y <= controller.getY()
                || !plane(level, controller, west, east, north, south, y, true, blocks, tanks)
                || tanks.isEmpty() || !blocks.contains(controller)) {
            return null;
        }
        return new SearedFurnaceStructure(new BlockPos(west + 1, center.getY(), north + 1),
                new BlockPos(east - 1, y - 1, south - 1), List.copyOf(blocks), List.copyOf(tanks));
    }

    private static boolean plane(Level level, BlockPos controller, int west, int east, int north, int south, int y,
                                 boolean ceiling, List<BlockPos> blocks, List<BlockPos> tanks) {
        for (int x = west; x <= east; x++) {
            for (int z = north; z <= south; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!available(level, pos, controller)) {
                    return false;
                }
                BlockState state = level.getBlockState(pos);
                boolean frame = x == west || x == east || z == north || z == south;
                // 原版上下外框可留空；只有侧面的角柱必须是焦黑方块或储罐。
                if (!frame && !pos.equals(controller) && !brick(state) && !(ceiling && bottomShape(state))) {
                    return false;
                }
                blocks.add(pos);
                if (frame && fuelTank(state)) {
                    tanks.add(pos);
                }
            }
        }
        return true;
    }

    private static boolean brick(BlockState state) {
        return TinkerTankStructure.fullSearedSlab(state)
                || state.getBlock() instanceof SmelteryBlock block && block.kind == SmelteryBlock.Kind.BRICK;
    }

    private static boolean fuelTank(BlockState state) {
        return state.getBlock() instanceof SmelteryBlock block && block.kind == SmelteryBlock.Kind.TANK;
    }

    private static boolean bottomShape(BlockState state) {
        return state.getBlock() instanceof SearedSlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM
                || state.getBlock() instanceof SearedStairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM;
    }

    private static boolean available(Level level, BlockPos pos, BlockPos controller) {
        return level.isInWorldBounds(pos) && level.hasChunkAt(pos)
                && (!(level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component)
                || component.master() == null || component.master().getBlockPos().equals(controller));
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
