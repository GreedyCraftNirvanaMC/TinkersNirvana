package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.mojang.blaze3d.vertex.PoseStack; // 引入 com.mojang.blaze3d.vertex.PoseStack，供本文件使用。
import com.mojang.blaze3d.vertex.VertexConsumer; // 引入 com.mojang.blaze3d.vertex.VertexConsumer，供本文件使用。
import net.minecraft.client.Minecraft; // 引入 net.minecraft.client.Minecraft，供本文件使用。
import net.minecraft.client.renderer.*; // 引入 net.minecraft.client.renderer.*，供本文件使用。
import net.minecraft.client.renderer.texture.*; // 引入 net.minecraft.client.renderer.texture.*，供本文件使用。
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions; // 引入 net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。

/**
 * 在方块局部坐标中绘制流体长方体的六个半透明纹理面。
 * 纹理和颜色通过原生流体扩展取得，使储罐也能显示兼容的第三方流体。
 */
final class FluidRenderer { // 定义 FluidRenderer 的实现类型，具体职责见上方说明。
    private FluidRenderer() {} // 定义 FluidRenderer 的调用入口；此处无需额外处理。
    static void box(PoseStack poses, MultiBufferSource buffers, FluidStack fluid, // 构建或绘制指定范围的形状。
                    float x0, float y0, float z0, float x1, float y1, float z1, int light) { // 在 FluidRenderer 中补充当前声明或表达式的 float x0, float y0, float z0, float x1, float y1, float z1, int light) { 部分。
        if (fluid.isEmpty() || y1 <= y0) return; // 检查 fluid.isEmpty() || y1 <= y0；按条件决定是否执行本分支。
        var extension = IClientFluidTypeExtensions.of(fluid.getFluid()); // 设置 extension 为 IClientFluidTypeExtensions.of(fluid.getFluid())。
        var texture = extension.getStillTexture(fluid); // 设置 texture 为 extension.getStillTexture(fluid)。
        if (texture == null) return; // 检查 texture == null；按条件决定是否执行本分支。
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture); // 设置 sprite 为 Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture)。
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)); // 设置 vertices 为 buffers.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS))。
        int color = extension.getTintColor(fluid); // 设置 color 为 extension.getTintColor(fluid)。
        if ((color >>> 24) == 0) color |= 0xFF000000; // 检查 (color >>> 24) == 0；按条件决定是否执行本分支。
        quad(vertices, poses.last(), sprite, color, light, 0, 1, 0, x0,y1,z0, x0,y1,z1, x1,y1,z1, x1,y1,z0); // 调用 quad，执行当前流程所需的处理。
        quad(vertices, poses.last(), sprite, color, light, 0,-1, 0, x0,y0,z1, x0,y0,z0, x1,y0,z0, x1,y0,z1); // 调用 quad，执行当前流程所需的处理。
        quad(vertices, poses.last(), sprite, color, light, 0, 0,-1, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0); // 调用 quad，执行当前流程所需的处理。
        quad(vertices, poses.last(), sprite, color, light, 0, 0, 1, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1); // 调用 quad，执行当前流程所需的处理。
        quad(vertices, poses.last(), sprite, color, light,-1, 0, 0, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0); // 调用 quad，执行当前流程所需的处理。
        quad(vertices, poses.last(), sprite, color, light, 1, 0, 0, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1); // 调用 quad，执行当前流程所需的处理。
    } // 结束当前作用域。
    private static void quad(VertexConsumer vertices, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light, // 定义 quad 的调用入口，参数和具体处理见下方实现。
                             float nx, float ny, float nz, float... xyz) { // 在 FluidRenderer 中补充当前声明或表达式的 float nx, float ny, float nz, float... xyz) { 部分。
        for (int i = 0; i < 4; i++) { // 遍历 int i = 0; i < 4; i++，逐项执行当前处理。
            vertices.addVertex(pose.pose(), xyz[i * 3], xyz[i * 3 + 1], xyz[i * 3 + 2]).setColor(color) // 提交一个顶点及其后续渲染属性。
                    .setUv(i == 0 || i == 3 ? sprite.getU0() : sprite.getU1(), i < 2 ? sprite.getV1() : sprite.getV0()) // 调用 setUv，执行当前流程所需的处理。
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz); // 调用 setOverlay，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
