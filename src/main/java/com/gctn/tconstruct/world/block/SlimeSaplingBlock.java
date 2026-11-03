/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.world.block;

import com.gctn.tconstruct.world.WorldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

public class SlimeSaplingBlock extends SaplingBlock {
    public SlimeSaplingBlock(TreeGrower grower, Properties properties) { super(grower, properties); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return WorldModule.soil(state); }
    @Override protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) { return WorldModule.soil(level.getBlockState(pos.below())); }
}
