/*
 * 根据 TinkersAntique 的 BlockSoil 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.soil;

import com.gctn.tconstruct.TinkersNirvana;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.util.TriState;

/** 土壤共用原版物理参数，仅在对应种类上附加行走效果。 */
public final class AntiqueSoilBlock extends Block {
    public static final TagKey<Block> SLIME_PLANTS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "slime_plants"));
    public final Kind kind;

    public AntiqueSoilBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public static BlockBehaviour.Properties soilProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(3).friction(0.8F).sound(SoundType.SAND);
    }

    @Override
    protected MapCodec<AntiqueSoilBlock> codec() {
        return simpleCodec(properties -> new AntiqueSoilBlock(kind, properties));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (kind == Kind.SLIMY_MUD_GREEN || kind == Kind.SLIMY_MUD_BLUE) {
            // 只减慢水平速度，保持跳跃和坠落分量；熔岩泥泞在原版没有这项效果。
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.4, 1, 0.4));
            if (!level.isClientSide && entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1));
            }
        } else if (!level.isClientSide && entity instanceof Mob living && living.getType().is(EntityTypeTags.UNDEAD)) {
            // 原版只影响生物，不能把玩家或普通非亡灵实体当作亡灵处理。
            if (kind == Kind.GRAVEYARD) {
                living.heal(1);
            } else if (kind == Kind.CONSECRATED) {
                living.hurt(level.damageSources().magic(), 1);
                living.igniteForSeconds(1);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockState plant) {
        if (kind == Kind.SLIMY_MUD_GREEN || kind == Kind.SLIMY_MUD_BLUE) {
            // 世界模块可通过此标签声明史莱姆植物，普通花草不能借此在泥泞上生长。
            return plant.is(SLIME_PLANTS) ? TriState.TRUE : TriState.FALSE;
        }
        return super.canSustainPlant(state, level, pos, direction, plant);
    }

    public enum Kind {
        GROUT, SLIMY_MUD_GREEN, SLIMY_MUD_BLUE, GRAVEYARD, CONSECRATED, SLIMY_MUD_MAGMA
    }
}
