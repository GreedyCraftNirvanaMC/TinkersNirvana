/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChannelBlock extends SmelteryBlock {
    public static final IntegerProperty NORTH = IntegerProperty.create("north", 0, 3);
    public static final IntegerProperty SOUTH = IntegerProperty.create("south", 0, 3);
    public static final IntegerProperty EAST = IntegerProperty.create("east", 0, 3);
    public static final IntegerProperty WEST = IntegerProperty.create("west", 0, 3);
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public ChannelBlock(Properties properties) {
        super(Kind.CHANNEL, properties);
    }

    public static IntegerProperty property(Direction side) {
        return switch (side) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("Channel arm must be horizontal");
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, EAST, WEST, DOWN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = box(5, state.getValue(DOWN) ? 2 : 4, 5, 11, 8, 11);
        if (state.getValue(NORTH) > 0) { shape = Shapes.or(shape, box(5, 4, 0, 11, 8, 5)); }
        if (state.getValue(SOUTH) > 0) { shape = Shapes.or(shape, box(5, 4, 11, 11, 8, 16)); }
        if (state.getValue(WEST) > 0) { shape = Shapes.or(shape, box(0, 4, 5, 5, 8, 11)); }
        if (state.getValue(EAST) > 0) { shape = Shapes.or(shape, box(11, 4, 5, 16, 8, 11)); }
        return shape;
    }
}
