/*
 * 根据 TinkersAntique 的 RenderTinkerSlime 纹理和着色行为移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

/** 内层和凝胶层共用原版灰度纹理，并分别保留完整的颜色与透明度。 */
public final class AntiqueSlimeRenderer extends SlimeRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/entity/slime.png");
    private final int color;

    public AntiqueSlimeRenderer(EntityRendererProvider.Context context, int color) {
        super(context);
        this.color = color;
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return TEXTURE;
    }

    @Override
    public void render(Slime slime, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        // 只处理实体顶点，避免让文字、阴影或牵引绳继承史莱姆的颜色。
        MultiBufferSource tinted = type -> type.format() == DefaultVertexFormat.NEW_ENTITY
                ? new TintConsumer(buffers.getBuffer(type), color) : buffers.getBuffer(type);
        super.render(slime, yaw, partialTick, pose, tinted, light);
    }

    private record TintConsumer(VertexConsumer target, int color) implements VertexConsumer {
        public VertexConsumer addVertex(float x, float y, float z) { target.addVertex(x, y, z); return this; }
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            target.setColor(red * ((color >>> 16) & 255) / 255, green * ((color >>> 8) & 255) / 255,
                    blue * (color & 255) / 255, alpha * (color >>> 24) / 255);
            return this;
        }
        public VertexConsumer setUv(float u, float v) { target.setUv(u, v); return this; }
        public VertexConsumer setUv1(int u, int v) { target.setUv1(u, v); return this; }
        public VertexConsumer setUv2(int u, int v) { target.setUv2(u, v); return this; }
        public VertexConsumer setNormal(float x, float y, float z) { target.setNormal(x, y, z); return this; }
    }
}
