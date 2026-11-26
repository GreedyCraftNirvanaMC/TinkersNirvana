/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.DualToolHarvest;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
abstract class OffhandHarvestSwingMixin {
    @Shadow public LocalPlayer player;
    @Shadow public ClientLevel level;
    @Shadow public HitResult hitResult;

    @WrapOperation(method = {"startAttack", "continueAttack"}, at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/client/ClientHooks;onClickInput(ILnet/minecraft/client/KeyMapping;Lnet/minecraft/world/InteractionHand;)Lnet/neoforged/neoforge/client/event/InputEvent$InteractionKeyMappingTriggered;"))
    private InputEvent.InteractionKeyMappingTriggered tconstruct$rememberSwingHand(int button, KeyMapping key, InteractionHand hand,
            Operation<InputEvent.InteractionKeyMappingTriggered> original, @Share("offhandHarvest") LocalBooleanRef offhand) {
        var event = original.call(button, key, hand);
        if (!event.isCanceled() && player != null && level != null
                && hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            // 在方块被即时破坏前记录用手，保留原版粒子和取消挥手的判定。
            offhand.set(DualToolHarvest.shouldUseOffhand(player, level.getBlockState(hit.getBlockPos())));
        }
        return event;
    }

    @WrapOperation(method = {"startAttack", "continueAttack"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void tconstruct$swingMiningHand(LocalPlayer player, InteractionHand hand, Operation<Void> original,
                                           @Share("offhandHarvest") LocalBooleanRef offhand) {
        original.call(player, offhand.get() ? InteractionHand.OFF_HAND : hand);
    }
}
