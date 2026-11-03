/*
 * 根据 TinkersAntique 的多方块控制器接口移植；作者为 Slime Knights 及其贡献者。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

/** 焦黑多方块共用归属检查，组成方块只通知仍然有效的控制器。 */
public interface SmelteryMaster {
    BlockPos getBlockPos();

    boolean contains(BlockPos pos);

    void requestCheck();

    default void assignComponents(Level level, List<BlockPos> blocks) {
        // 尺寸不变也可能更换了排液口或顶板，必须为新创建的组成方块恢复归属。
        for (BlockPos pos : blocks) {
            if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                component.assign(getBlockPos());
            }
        }
    }
}
