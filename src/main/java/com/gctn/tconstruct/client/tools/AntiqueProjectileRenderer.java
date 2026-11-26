/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.client.tools;

import com.gctn.tconstruct.tools.AntiqueProjectile;
import com.gctn.tconstruct.tools.ToolDefinition;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

final class AntiqueProjectileRenderer extends EntityRenderer<AntiqueProjectile> {
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private final ItemRenderer items;
    private final ArrowRenderer<AntiqueProjectile> uninitialized;

    AntiqueProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        uninitialized = new ArrowRenderer<>(context) {
            @Override public ResourceLocation getTextureLocation(AntiqueProjectile entity) { return ARROW_TEXTURE; }
        };
    }

    @Override public ResourceLocation getTextureLocation(AntiqueProjectile entity) { return ARROW_TEXTURE; }

    @Override
    public void render(AntiqueProjectile entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        var stack = entity.traitTool();
        if (!(stack.getItem() instanceof AntiqueTool ammo) || !ammo.isAmmo()) {
            // 实体生成包到达时可能还没有收到弹药数据。
            uninitialized.render(entity, yaw, partial, pose, buffers, light);
            return;
        }
        var action = ammo.definition().action();
        boolean shuriken = action == ToolDefinition.Action.SHURIKEN;
        int seed = entity.getUUID().hashCode();
        var impact = entity.impactRotation();
        boolean hasImpact = impact.getZ() != 0;
        float modelYaw = hasImpact ? impact.getY() : Mth.rotLerp(partial, entity.yRotO, entity.getYRot());
        float modelPitch = hasImpact ? impact.getX() : Mth.rotLerp(partial, entity.xRotO, entity.getXRot());
        pose.pushPose();
        float scale = shuriken ? .3F : .5F;
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.YP.rotationDegrees(modelYaw));
        pose.mulPose(Axis.XP.rotationDegrees(-modelPitch));
        if (shuriken) {
            pose.mulPose(Axis.ZP.rotationDegrees(7 - Math.floorMod(seed, 14)));
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.mulPose(Axis.ZP.rotationDegrees(Math.floorMod(seed, 360) + entity.visualSpin(partial)));
        } else {
            if (entity.isEmbedded()) { pose.translate(0, 0, -.4F); }
            if (action == ToolDefinition.Action.ARROW) {
                pose.mulPose(Axis.ZP.rotationDegrees((seed & 1) == 0 ? entity.visualSpin(partial) : -entity.visualSpin(partial)));
            }
            pose.mulPose(Axis.YP.rotationDegrees(-90));
            pose.mulPose(Axis.ZP.rotationDegrees(-45));
        }
        float shake = entity.shakeTime - partial;
        if (shake > 0) { pose.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3) * shake)); }
        items.renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffers, light);
    }
}
