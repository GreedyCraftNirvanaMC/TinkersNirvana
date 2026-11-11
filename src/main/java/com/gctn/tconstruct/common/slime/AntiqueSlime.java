/*
 * 根据 TinkersAntique 的 EntityBlueSlime 与 EntityPurpleSlime 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.world.IslandBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidType;

/** 两种彩色史莱姆共用原生攻击、体型属性和分裂流程，行为差异由实体类型决定。 */
public final class AntiqueSlime extends Slime {
    public static final TagKey<Block> BLUE_SPAWNABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("tconstruct", "slime_spawnable/blue"));
    public static final TagKey<Block> PURPLE_SPAWNABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("tconstruct", "slime_spawnable/purple"));

    private int outsideIslandTicks;

    public AntiqueSlime(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    private static boolean isSlimeFluid(FluidType type) {
        return type == SmelteryFluids.get("greenslime").getFluidType()
                || type == SmelteryFluids.get("blueslime").getFluidType()
                || type == SmelteryFluids.get("purpleslime").getFluidType();
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return !isSlimeFluid(type) && super.canDrownInFluidType(type);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Slime's native floating goal only recognizes vanilla water and lava.
        goalSelector.addGoal(0, new FloatGoal(this) {
            @Override
            public boolean canUse() {
                return isInFluidType((type, height) -> isSlimeFluid(type) && height > getFluidJumpThreshold());
            }
        });
    }

    @Override
    public void tick() {
        if (isSlimeFluid(getEyeInFluidType())) {
            setAirSupply(getMaxAirSupply());
        }
        super.tick();
        if (level() instanceof ServerLevel level && isAlive()) {
            if (IslandBounds.containsNormalIsland(level, blockPosition())) {
                outsideIslandTicks = 0;
            } else if (++outsideIslandTicks >= Config.SLIME_OUTSIDE_ISLAND_SECONDS.get() * 20) {
                // Discard while alive to bypass death loot, XP and Slime's death-splitting path.
                discard();
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TconstructOutsideIslandTicks", outsideIslandTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        outsideIslandTicks = Math.max(0, tag.getInt("TconstructOutsideIslandTicks"));
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
