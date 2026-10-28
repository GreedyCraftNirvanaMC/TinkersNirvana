package com.gctn.tconstruct.smeltery.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 在方块局部坐标中绘制流体长方体的六个半透明纹理面。
 * 纹理和颜色通过原生流体扩展取得，使储罐也能显示兼容的第三方流体。
 */
final class FluidRenderer {
    private FluidRenderer() {}
    static void box(PoseStack poses, MultiBufferSource buffers, FluidStack fluid,
                    float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        if (fluid.isEmpty() || y1 <= y0) return;
        var extension = IClientFluidTypeExtensions.of(fluid.getFluid());
        var texture = extension.getStillTexture(fluid);
        if (texture == null) return;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        int color = extension.getTintColor(fluid);
        if ((color >>> 24) == 0) color |= 0xFF000000;
        quad(vertices, poses.last(), sprite, color, light, 0, 1, 0, x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0);
        quad(vertices, poses.last(), sprite, color, light, 0,-1, 0, x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1);
        quad(vertices, poses.last(), sprite, color, light, 0, 0,-1, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0);
        quad(vertices, poses.last(), sprite, color, light, 0, 0, 1, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1);
        quad(vertices, poses.last(), sprite, color, light,-1, 0, 0, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0);
        quad(vertices, poses.last(), sprite, color, light, 1, 0, 0, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1);
    }
    private static void quad(VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light,
                             float nx, float ny, float nz, float... xyz) {
        for (int i = 0; i < 4; i++) {
            vertices.addVertex(pose.pose(), xyz[i * 3], xyz[i * 3 + 1], xyz[i * 3 + 2]).setColor(color)
                    .setUv(i == 0 || i == 3 ? sprite.getU0() : sprite.getU1(), i < 2 ? sprite.getV1() : sprite.getV0())
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        }
    }
}
