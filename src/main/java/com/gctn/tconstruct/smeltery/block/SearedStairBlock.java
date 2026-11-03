/*
 * 根据 TinkersAntique 的 BlockSearedStairs 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 使用原生楼梯朝向、转角和含水状态。 */
public class SearedStairBlock extends StairBlock implements SearedBuildingBlock {
    public SearedStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        SearedBuildingBlock.neighborChanged(level, pos, from);
        super.neighborChanged(state, level, pos, neighbor, from, moving);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            SearedBuildingBlock.removed(level, pos);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
