/*
 * 根据 TinkersAntique 的 BlockWoodRail 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.RailBlock;

/** 保留原生轨道的连接、斜坡、水含量和矿车行为，仅调整材质与硬度。 */
public class WoodenRailBlock extends RailBlock {
    public WoodenRailBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<RailBlock> codec() {
        return simpleCodec(WoodenRailBlock::new);
    }
}
