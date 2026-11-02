/*
 * 根据 TinkersAntique 的 BlockSearedSlab 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 使用原生台阶合并和含水状态，双台阶按完整焦黑方块参与结构判定。 */
public class SearedSlabBlock extends SlabBlock implements SearedBuildingBlock {
    public SearedSlabBlock(Properties properties) {
        super(properties);
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
