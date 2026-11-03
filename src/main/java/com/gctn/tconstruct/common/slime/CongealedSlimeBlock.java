/*
 * 根据 TinkersAntique 的 BlockSlimeCongealed 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 凝固史莱姆使用完整外观与较矮的碰撞箱，并增强有效落地的反弹。 */
public final class CongealedSlimeBlock extends Block {
    public static final MapCodec<CongealedSlimeBlock> CODEC = simpleCodec(CongealedSlimeBlock::new);
    private static final VoxelShape COLLISION = Block.box(0, 0, 0, 16, 10, 16);

    public CongealedSlimeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CongealedSlimeBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        // 旧版仍把凝固史莱姆当作完整支撑方块，碰撞高度不应影响火把或铁轨的放置。
        return Shapes.block();
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        var motion = entity.getDeltaMovement();
        // 经验球等实体不能被不断放大速度；潜行不会关闭原版凝固史莱姆的反弹。
        if ((entity instanceof LivingEntity || entity instanceof ItemEntity) && motion.y < -0.25) {
            entity.setDeltaMovement(motion.x, motion.y * -1.2F, motion.z);
            entity.fallDistance = 0;
            if (entity instanceof ItemEntity) {
                entity.setOnGround(false);
            }
        } else {
            super.updateEntityAfterFallOn(level, entity);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0, level.damageSources().fall());
    }
}
