/*
 * 根据 TinkersAntique 的多方块控制器接口移植；作者为 Slime Knights 及其贡献者。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryTank;
import net.minecraft.core.BlockPos;

/** 冶炼炉和工匠储罐共用归属检查，排液口只访问仍然有效的控制器。 */
public interface SmelteryMaster {
    BlockPos getBlockPos();

    boolean contains(BlockPos pos);

    void requestCheck();

    SmelteryTank liquids();
}
