/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolCombat;
import com.gctn.tconstruct.tools.ToolModifiers;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @WrapMethod(method = "attack")
    private void tconstruct$scytheAttack(Entity target, Operation<Void> original) {
        ToolCombat.attack((Player) (Object) this, target, () -> original.call(target));
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean tconstruct$beforeToolHit(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        ToolCombat.beforeHit((Player) (Object) this, target);
        return original.call(target, source, amount);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
    private float tconstruct$toolKnockback(Player attacker, Entity target, DamageSource source, Operation<Float> original) {
        float strength = original.call(attacker, target, source);
        // 让原版把强化和冲刺合并计算，再同步被击中的玩家。
        if (!attacker.level().isClientSide && target instanceof LivingEntity) {
            strength += ToolModifiers.knockbackBonus(attacker.getMainHandItem());
        }
        return strength;
    }
}
