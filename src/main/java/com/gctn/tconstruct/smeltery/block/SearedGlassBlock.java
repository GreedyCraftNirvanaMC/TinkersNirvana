/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SearedGlassBlock extends SmelteryBlock {
    public static final BooleanProperty[] CONNECTIONS = java.util.Arrays.stream(Direction.values())
            .map(side -> BooleanProperty.create("connected_" + side.getName())).toArray(BooleanProperty[]::new);

    public SearedGlassBlock(Properties properties) {
        super(Kind.GLASS, properties);
        // 命令放置和结构生成同样需要正确的独立玻璃边框，不能依赖玩家放置回调。
        BlockState state = defaultBlockState();
        for (var connection : CONNECTIONS) {
            state = state.setValue(connection, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTIONS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        for (Direction side : Direction.values()) {
            state = state.setValue(CONNECTIONS[side.ordinal()], context.getLevel().getBlockState(context.getClickedPos().relative(side)).is(this));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(CONNECTIONS[side.ordinal()], neighbor.is(this));
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighbor, Direction side) {
        return neighbor.is(this) || super.skipRendering(state, neighbor, side);
    }
}
