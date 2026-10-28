package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.entity.CastingBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.CastingBlockEntity，供本文件使用。
import com.mojang.blaze3d.vertex.PoseStack; // 引入 com.mojang.blaze3d.vertex.PoseStack，供本文件使用。
import com.mojang.math.Axis; // 引入 com.mojang.math.Axis，供本文件使用。
import net.minecraft.client.Minecraft; // 引入 net.minecraft.client.Minecraft，供本文件使用。
import net.minecraft.client.renderer.*; // 引入 net.minecraft.client.renderer.*，供本文件使用。
import net.minecraft.client.renderer.blockentity.*; // 引入 net.minecraft.client.renderer.blockentity.*，供本文件使用。
import net.minecraft.world.item.ItemDisplayContext; // 引入 net.minecraft.world.item.ItemDisplayContext，供本文件使用。

/**
 * 绘制按配方容量计算的流体表面，以及铸模或冷却后的成品物品。
 * 铸件台与铸造盆使用不同的局部高度，使显示内容位于各自模型内部。
 */
public class CastingRenderer implements BlockEntityRenderer<CastingBlockEntity> { // 定义 CastingRenderer 的实现类型，具体职责见上方说明。
    public CastingRenderer(BlockEntityRendererProvider.Context context) {} // 定义 CastingRenderer 的调用入口；此处无需额外处理。
    @Override public void render(CastingBlockEntity casting, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) { // 实现父类或接口约定；定义 render 的调用入口，参数和具体处理见下方实现。
        var tank = casting.fluidHandler(); // 设置 tank 为 casting.fluidHandler()。
        float bottom = casting.isBasin() ? 0.251F : 0.939F; // 设置 bottom 为 casting.isBasin() ? 0.251F : 0.939F。
        float height = casting.isBasin() ? 0.68F : 0.045F; // 设置 height 为 casting.isBasin() ? 0.68F : 0.045F。
        FluidRenderer.box(poses, buffers, tank.getFluid(), 0.126F, bottom, 0.126F, 0.874F, // 构建或绘制指定范围的形状。
                bottom + height * tank.getFluidAmount() / tank.getCapacity(), 0.874F, LightTexture.FULL_BRIGHT); // 调用 getFluidAmount，执行当前流程所需的处理。
        var items = casting.itemHandler(); // 设置 items 为 casting.itemHandler()。
        var stack = items.getStackInSlot(1).isEmpty() ? items.getStackInSlot(0) : items.getStackInSlot(1); // 设置 stack 为 items.getStackInSlot(1).isEmpty() ? items.getStackInSlot(0) : items.getStackInSlot(1)。
        if (!stack.isEmpty()) { // 检查 !stack.isEmpty()；按条件决定是否执行本分支。
            poses.pushPose(); // 保存当前渲染变换，避免影响后续绘制。
            poses.translate(0.5, casting.isBasin() ? 0.6 : 0.957, 0.5); // 平移当前渲染坐标。
            poses.mulPose(Axis.XP.rotationDegrees(90)); // 应用模型旋转。
            poses.scale(0.65F, 0.65F, 0.65F); // 缩放当前渲染模型。
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, poses, buffers, casting.getLevel(), 0); // 按指定展示方式绘制物品模型。
            poses.popPose(); // 恢复先前保存的渲染变换。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
