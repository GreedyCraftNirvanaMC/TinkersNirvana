/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TinkerEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, TinkersNirvana.MODID);
    public static final Holder<MobEffect> MOMENTUM = effect("momentum", false, 0x2882d4);
    public static final Holder<MobEffect> INSATIABLE = effect("insatiable", false, 0xa15cf8);
    public static final Holder<MobEffect> SPLINTER = effect("splinter", true, 0xede6bf);
    public static final Holder<MobEffect> ENDERFERENCE = effect("enderference", true, 0x21985f);
    public static final Holder<MobEffect> BLEED = EFFECTS.register("bleed", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xa61b1b) {
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration % 15 == 0; }
        @Override public boolean applyEffectTick(LivingEntity target, int amplifier) {
            if (!target.level().isClientSide) { TraitDamage.hurt(target, target.getLastHurtByMob(), "bleed", (amplifier + 1F) / 3F); }
            return true;
        }
    });
    public static final Holder<MobEffect> MAGNETIC = EFFECTS.register("magnetic", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xdddddd) {
        @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return duration % 2 == 0; }
        @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity.level().isClientSide) { return true; }
            int pulled = 0;
            double range = 1.8 + amplifier * .3;
            var area = new net.minecraft.world.phys.AABB(entity.position(), entity.position()).inflate(range);
            for (ItemEntity item : entity.level().getEntitiesOfClass(ItemEntity.class, area)) {
                if (pulled > 200) { break; }
                if (!item.isAlive() || item.getItem().isEmpty()) { continue; }
                Vec3 direction = entity.position().subtract(item.position());
                if (direction.lengthSqr() > .05) {
                    // 拾取延迟由原版处理，怀古会在这段时间内照常吸引掉落物。
                    item.setDeltaMovement(item.getDeltaMovement().add(direction.normalize().scale(.07)));
                    pulled++;
                }
            }
            return true;
        }
    });
    private TinkerEffects() {}
    private static Holder<MobEffect> effect(String id, boolean harmful, int color) {
        return EFFECTS.register(id, () -> new MobEffect(harmful ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color) {});
    }
    public static void register(IEventBus bus) { EFFECTS.register(bus); }
    public static int level(LivingEntity entity, Holder<MobEffect> effect) {
        var instance = entity.getEffect(effect);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }
}
