/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's table inventory display. */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.tables.WorkshopModule.Kind;
import com.gctn.tconstruct.tables.block.WorkshopBlock;
import com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity;
import com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity;
import com.gctn.tconstruct.tables.data.Mode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WorkshopItemRenderer<T extends BlockEntity & Container> implements BlockEntityRenderer<T> {
    @Override public void render(T entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!com.gctn.tconstruct.Config.RENDER_TABLE_ITEMS.get()) { return; }
        boolean crafting = entity instanceof WorkshopBlockEntity workshop && workshop.kind() == Kind.CRAFTING;
        if (entity instanceof WorkshopBlockEntity workshop && workshop.kind().storage()) { return; }
        pose.pushPose(); pose.translate(.5, 1.008, .5);
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getBlockState().getValue(WorkshopBlock.FACING).toYRot()));
        if (entity instanceof ToolStationBlockEntity station && station.getMode() == Mode.DISASSEMBLE && !station.getItem(6).isEmpty()) {
            draw(station.getItem(6), 0, 0, .65f, entity, pose, buffers, light, overlay);
        } else {
            int count = crafting ? 9 : entity instanceof ToolStationBlockEntity ? 6 : entity.getContainerSize();
            for (int slot = 0; slot < count; slot++) {
                float x = crafting ? (slot % 3 - 1) * .1875f : (slot % 3 - 1) * .27f;
                float z = crafting ? (slot / 3 - 1) * .1875f : (slot / 3 - .5f) * .37f;
                if (entity instanceof WorkshopBlockEntity workshop && workshop.kind() == Kind.STENCIL) { x = z = 0; }
                draw(entity.getItem(slot), x, z, crafting ? .125f : .3f, entity, pose, buffers, light, overlay);
            }
        }
        pose.popPose();
    }
    private void draw(ItemStack stack, float x, float z, float scale, BlockEntity entity, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (stack.isEmpty()) { return; }
        pose.pushPose(); pose.translate(x, 0, z);
        if (Minecraft.getInstance().getItemRenderer().getModel(stack, entity.getLevel(), null, 0).isGui3d()) { pose.translate(0, scale * .5f, 0); }
        else { pose.mulPose(Axis.XP.rotationDegrees(90)); }
        pose.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, entity.getLevel(), 0);
        pose.popPose();
    }
}
