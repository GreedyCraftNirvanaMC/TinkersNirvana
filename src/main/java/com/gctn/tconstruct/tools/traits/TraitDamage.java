/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools.traits;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** 次级词条伤害仍经过原版伤害事件；单次命中结束后恢复原来的受击冷却。 */
public final class TraitDamage {
    private TraitDamage() {}
    public static boolean hurt(LivingEntity target, LivingEntity attacker, String id, float amount) {
        if (target.level().isClientSide || amount <= 0 || !target.isAlive()) { return false; }
        var key = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("tconstruct", id));
        DamageSource source = new DamageSource(target.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), attacker);
        int invulnerability = target.invulnerableTime;
        target.invulnerableTime = 0;
        try { return target.hurt(source, amount); }
        finally { target.invulnerableTime = invulnerability; }
    }
}
