/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.AntiqueProjectile;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class AntiqueProjectileDamageMixin {
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean tconsnirvana$applyEnderference(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        if ((Object) this instanceof AntiqueProjectile projectile && !projectile.level().isClientSide
                && target instanceof EnderMan enderman) {
            return projectile.hurtTarget(enderman, source, actual -> original.call(target, actual, amount));
        }
        return original.call(target, source, amount);
    }
}
