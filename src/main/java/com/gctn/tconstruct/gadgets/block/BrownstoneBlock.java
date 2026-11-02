/*
 * 根据 TinkersAntique 的 BlockBrownstone 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** 褐砂石只放大行走时的水平速度，保持跳跃和坠落速度不变。 */
public class BrownstoneBlock extends Block {
    public BrownstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        boost(entity);
        super.stepOn(level, pos, state, entity);
    }

    public static void boost(Entity entity) {
        // 原版在水中使用较小倍率；客户端和服务端沿用同一行走回调。
        double factor = entity.isInWater() ? 1.20 : 1.25;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
    }
}
