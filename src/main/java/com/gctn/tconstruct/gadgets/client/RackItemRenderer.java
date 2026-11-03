/* SPDX-License-Identifier: LGPL-3.0-only; Antique's hanging rack contents. */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.gadgets.block.entity.RackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RackItemRenderer implements BlockEntityRenderer<RackBlockEntity> {
    @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(RackBlockEntity rack) { return new net.minecraft.world.phys.AABB(rack.getBlockPos()).expandTowards(0, -1, 0); }
    @Override public void render(RackBlockEntity rack, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var stack = rack.displayed(); if (stack.isEmpty()) { return; }
        var renderer = Minecraft.getInstance().getItemRenderer();
        boolean block = renderer.getModel(stack, rack.getLevel(), null, 0).isGui3d();
        AttachFace face = rack.getBlockState().getValue(BlockStateProperties.ATTACH_FACE);
        pose.pushPose(); pose.translate(.5, (face == AttachFace.FLOOR ? -.375 : .375) + (block ? .0625 : 0), .5);
        pose.mulPose(Axis.YP.rotationDegrees(-rack.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()));
        if (face == AttachFace.WALL) { pose.translate(0, 0, -.375); }
        float scale = block ? .5f : .875f; pose.scale(scale, scale, scale);
        renderer.renderStatic(stack, ItemDisplayContext.NONE, light, overlay, pose, buffers, rack.getLevel(), 0);
        pose.popPose();
    }
}
