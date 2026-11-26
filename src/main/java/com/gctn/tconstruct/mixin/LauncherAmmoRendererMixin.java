/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolDefinition;
import com.gctn.tconstruct.tools.ToolRanged;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
abstract class LauncherAmmoRendererMixin {
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private void tconstruct$renderNockedAmmo(LivingEntity entity, ItemStack stack, ItemDisplayContext context,
                                           boolean leftHand, PoseStack pose, MultiBufferSource buffers,
                                           Level level, int light, int overlay, int seed, CallbackInfo ci,
                                           @Local BakedModel launcherModel) {
        if (!(entity instanceof Player player) || !(stack.getItem() instanceof AntiqueTool launcher)
                || !launcher.isLauncher() || launcher.isBroken(stack)
                || !(context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            return;
        }
        boolean crossbow = launcher.definition().action() == ToolDefinition.Action.CROSSBOW;
        boolean loaded = crossbow && ToolRanged.isLoaded(stack);
        if (!loaded && (!player.isUsingItem() || player.getUseItem() != stack)) {
            return;
        }
        ItemStack ammo = ToolRanged.ammoForDisplay(launcher, stack, player);
        if (ammo.isEmpty()) {
            return;
        }
        pose.pushPose();
        ClientHooks.handleCameraTransforms(pose, launcherModel, context, leftHand);
        float draw = ToolRanged.drawProgress(launcher, stack, player.getTicksUsingItem());
        if (crossbow) {
            // 弩弦从弩臂中线拉向弩托。三个挂点与模型的三个弦位一一对应。
            float offset = loaded || draw >= .999F ? .0625F : draw >= .5F ? -.125F : -.25F;
            pose.translate(offset, -offset, .0625F);
            pose.mulPose(Axis.ZP.rotationDegrees(90));
        } else {
            // 怀古的三个拉弓模型各有箭尾挂点，箭跟随弦位移动并保留自身的部件颜色。
            float offset = draw >= .9F ? .063F : draw >= .65F ? .1255F : .188F;
            pose.translate(-offset, offset, .01F);
            pose.mulPose(Axis.YP.rotationDegrees(180));
        }
        ((ItemRenderer) (Object) this).renderStatic(entity, ammo, ItemDisplayContext.NONE, false, pose, buffers, level, light, overlay, seed);
        pose.popPose();
    }
}
