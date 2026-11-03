/*
 * Connected glass accompanying the Smeltery port from TinkersAntique.
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Casting glass has connected textures but is not a valid Smeltery wall material. */
public class ClearGlassBlock extends TransparentBlock {
    public static final MapCodec<ClearGlassBlock> CODEC = simpleCodec(ClearGlassBlock::new);
    public ClearGlassBlock(Properties properties) {
        super(properties);
        // 布尔属性默认从 true 开始；独立玻璃必须显式使用无连接状态。
        BlockState state = defaultBlockState();
        for (var connection : SearedGlassBlock.CONNECTIONS) {
            state = state.setValue(connection, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SearedGlassBlock.CONNECTIONS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction side : Direction.values()) {
            state = state.setValue(SearedGlassBlock.CONNECTIONS[side.ordinal()], context.getLevel().getBlockState(context.getClickedPos().relative(side)).is(this));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(SearedGlassBlock.CONNECTIONS[side.ordinal()], neighbor.is(this));
    }
}
