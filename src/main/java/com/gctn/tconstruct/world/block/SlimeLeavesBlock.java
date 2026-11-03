/* SPDX-License-Identifier: LGPL-3.0-only; Antique leaves supported by congealed slime. */
package com.gctn.tconstruct.world.block;

import com.gctn.tconstruct.common.slime.CongealedSlimeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SlimeLeavesBlock extends LeavesBlock {
    public SlimeLeavesBlock(Properties properties) { super(properties); }
    @Override protected boolean isRandomlyTicking(BlockState state) { return !state.getValue(PERSISTENT) && state.getValue(DISTANCE) > 4; }
    @Override protected boolean decaying(BlockState state) { return !state.getValue(PERSISTENT) && state.getValue(DISTANCE) > 4; }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int distance = 7;
        for (Direction direction : Direction.values()) {
            BlockState adjacent = level.getBlockState(pos.relative(direction));
            int value = adjacent.getBlock() instanceof CongealedSlimeBlock ? 0 : getOptionalDistanceAt(adjacent).orElse(7);
            distance = Math.min(distance, value + 1);
        }
        level.setBlock(pos, state.setValue(DISTANCE, distance), 3);
    }
}
