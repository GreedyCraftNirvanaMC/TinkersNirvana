/*
 * 根据 TinkersAntique 的 EntityBlueSlime 与 EntityPurpleSlime 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.smeltery.SmelteryFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;

/** 两种彩色史莱姆共用原生攻击、体型属性和分裂流程，行为差异由实体类型决定。 */
public final class AntiqueSlime extends Slime {
    public static final TagKey<Block> BLUE_SPAWNABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("tconstruct", "slime_spawnable/blue"));
    public static final TagKey<Block> PURPLE_SPAWNABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("tconstruct", "slime_spawnable/purple"));

    public AntiqueSlime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    public boolean isPurple() {
        return getType() == SlimeMobModule.PURPLE.get();
    }

    @Override
    protected ParticleOptions getParticleType() {
        // 原版直接使用对应史莱姆球的碎片，不使用绿色史莱姆粒子再叠加颜色。
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(SlimeModule.ball(isPurple() ? "purple" : "blue")));
    }

    @Override
    protected void decreaseSquish() {
        targetSquish *= 0.8F;
    }

    @Override
    protected int getJumpDelay() {
        return random.nextInt(10) + 10;
    }

    @Override
    protected float getJumpPower() {
        // 固定跳跃力度也保留原版忽略跳跃药水加成的行为。
        return 0.63F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    public static boolean checkSpawnRules(EntityType<AntiqueSlime> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        boolean purple = type == SlimeMobModule.PURPLE.get();
        var fluid = SmelteryFluids.get(purple ? "purpleslime" : "blueslime");
        // 流动与源头使用同一流体家族；草地标签由后续世界模块填充。
        return level.getFluidState(pos).getType().isSame(fluid)
                || level.getBlockState(pos.below()).is(purple ? PURPLE_SPAWNABLE : BLUE_SPAWNABLE);
    }
}
