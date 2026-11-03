/* SPDX-License-Identifier: LGPL-3.0-only; Antique frame models with native item/map rendering. */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.gadgets.FancyFrames;
import com.gctn.tconstruct.gadgets.SlimeGear;
import com.gctn.tconstruct.gadgets.entity.FancyItemFrame;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;

public class FancyFrameRenderer extends ItemFrameRenderer<FancyItemFrame> {
    public FancyFrameRenderer(EntityRendererProvider.Context context) { super(context); }
    public static ModelResourceLocation model(FancyFrames.Kind kind, boolean map) { return ModelResourceLocation.standalone(SlimeGear.id("block/fancy_frame/" + kind.id() + (map ? "_map" : ""))); }
    @Override protected ModelResourceLocation getFrameModelResourceLoc(FancyItemFrame frame, ItemStack item) {
        if (frame.kind() == FancyFrames.Kind.CLEAR && !item.isEmpty()) { return ModelResourceLocation.standalone(SlimeGear.id("block/fancy_frame/empty")); }
        return model(frame.kind(), item.getItem() instanceof MapItem);
    }
    @Override public void render(FancyItemFrame frame, float yaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        if (frame.kind() == FancyFrames.Kind.CLEAR && frame.getItem().isEmpty()) {
            var original = buffers;
            buffers = type -> original.getBuffer(type == net.minecraft.client.renderer.Sheets.solidBlockSheet() ? net.minecraft.client.renderer.Sheets.translucentCullBlockSheet() : type);
        }
        super.render(frame, yaw, partialTick, pose, buffers, light);
    }
}
