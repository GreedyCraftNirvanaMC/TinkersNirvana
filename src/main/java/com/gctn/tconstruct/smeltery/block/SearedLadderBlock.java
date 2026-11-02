/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SearedLadderBlock extends SmelteryBlock {
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

    public SearedLadderBlock(Properties properties) {
        super(Kind.LADDER, properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                ? context.getHorizontalDirection() : context.getHorizontalDirection().getOpposite();
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        return defaultBlockState().setValue(FACING, facing).setValue(BOTTOM, !below.is(this) || below.getValue(FACING) != facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return side == Direction.DOWN ? state.setValue(BOTTOM, !neighbor.is(this) || neighbor.getValue(FACING) != state.getValue(FACING)) : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // The recessed face needs a real cavity so the player can climb it, as in Antique.
        VoxelShape shape = switch (state.getValue(FACING)) {
            case NORTH -> Shapes.or(box(0,0,2,16,16,16), box(0,0,0,2,16,2), box(14,0,0,16,16,2));
            case SOUTH -> Shapes.or(box(0,0,0,16,16,14), box(0,0,14,2,16,16), box(14,0,14,16,16,16));
            case WEST -> Shapes.or(box(2,0,0,16,16,16), box(0,0,0,2,16,2), box(0,0,14,2,16,16));
            default -> Shapes.or(box(0,0,0,14,16,16), box(14,0,0,16,16,2), box(14,0,14,16,16,16));
        };
        return state.getValue(BOTTOM) ? Shapes.or(shape, box(0,0,0,16,2,16)) : shape;
    }
}
