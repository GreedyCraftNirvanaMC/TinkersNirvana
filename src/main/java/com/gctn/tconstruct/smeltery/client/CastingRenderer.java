package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.entity.CastingBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Renders a recipe-sized fluid surface plus the mold or cooled output item.
 * Table and basin use different local heights to keep visuals inside their block models.
 */
public class CastingRenderer implements BlockEntityRenderer<CastingBlockEntity> {
    public CastingRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(CastingBlockEntity casting, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        var tank = casting.fluidHandler();
        float bottom = casting.isBasin() ? 0.251F : 0.939F;
        float height = casting.isBasin() ? 0.68F : 0.045F;
        FluidRenderer.box(poses, buffers, tank.getFluid(), 0.126F, bottom, 0.126F, 0.874F,
                bottom + height * tank.getFluidAmount() / tank.getCapacity(), 0.874F, LightTexture.FULL_BRIGHT);
        var items = casting.itemHandler();
        var stack = items.getStackInSlot(1).isEmpty() ? items.getStackInSlot(0) : items.getStackInSlot(1);
        if (!stack.isEmpty()) {
            poses.pushPose();
            poses.translate(0.5, casting.isBasin() ? 0.6 : 0.957, 0.5);
            poses.mulPose(Axis.XP.rotationDegrees(90));
            poses.scale(0.65F, 0.65F, 0.65F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poses, buffers, casting.getLevel(), 0);
            poses.popPose();
        }
    }
}
