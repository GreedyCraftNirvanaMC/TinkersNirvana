/*
 * 根据 TinkersAntique 的褐砂石楼梯移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 原生楼梯处理朝向和转角，行走时补充褐砂石加速。 */
public class BrownstoneStairBlock extends StairBlock {
    public BrownstoneStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        BrownstoneBlock.boost(entity);
        super.stepOn(level, pos, state, entity);
    }
}
