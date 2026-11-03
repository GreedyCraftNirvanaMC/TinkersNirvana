/*
 * 根据 TinkersAntique 的 BlockBrownstoneSlab、BlockBrownstoneSlab2 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 褐砂石台阶保留完整方块的行走加速效果。 */
public class BrownstoneSlabBlock extends SlabBlock {
    public BrownstoneSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        BrownstoneBlock.boost(entity);
        super.stepOn(level, pos, state, entity);
    }
}
