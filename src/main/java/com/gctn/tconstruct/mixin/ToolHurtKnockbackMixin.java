/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class ToolHurtKnockbackMixin {
    @WrapOperation(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void tconstruct$scaleMeleeKnockback(LivingEntity target, double strength, double x, double z,
                                                Operation<Void> original, @Local(argsOnly = true) DamageSource source) {
        float scale = ToolCombat.hurtKnockbackScale(source);
        Vec3 before = target.getDeltaMovement();
        original.call(target, strength, x, z);
        if (scale > 0) {
            // 只缩放本次受伤所造成的速度变化。原版仍检查抗性和保护事件，额外击退由攻击者另外结算。
            Vec3 change = target.getDeltaMovement().subtract(before);
            target.setDeltaMovement(before.add(change.multiply(scale, scale / 3D, scale)));
        }
    }
}
