/*
 * 根据 TinkersAntique 的 BlockSlime 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 彩色史莱姆共用原生弹跳、潜行和减速逻辑。 */
public final class ColoredSlimeBlock extends SlimeBlock {
    public static final MapCodec<SlimeBlock> CODEC = simpleCodec(ColoredSlimeBlock::new);

    public ColoredSlimeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SlimeBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isSlimeBlock(BlockState state) {
        // 活塞推动实体时使用此钩子，不能只声明相邻方块的黏连性。
        return true;
    }

    @Override
    public boolean isStickyBlock(BlockState state) {
        return true;
    }

    @Override
    public boolean canStickTo(BlockState state, BlockState other) {
        // 蜂蜜是新版加入的方块，沿用新版原生史莱姆与蜂蜜互不黏连的规则。
        return !other.is(Blocks.HONEY_BLOCK) && super.canStickTo(state, other);
    }
}
