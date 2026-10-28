package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.phys.AABB;

/**
 * 在检测出的多方块内部空间中按顺序渲染各流体层。
 * 渲染范围覆盖整个结构，因为控制器离开屏幕时仍可能看见炉内流体。
 * 超出容量的流体按比例压缩显示，避免结构缩小时渲染到炉体外部。
 */
public class SmelteryRenderer implements BlockEntityRenderer<SmelteryControllerBlockEntity> {
    public SmelteryRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(SmelteryControllerBlockEntity controller, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (!controller.isFormed() || controller.tank().amount() <= 0) return;
        var min = controller.min().subtract(controller.getBlockPos());
        var max = controller.max().subtract(controller.getBlockPos());
        float floor = min.getY() + 0.005F;
        float height = max.getY() - min.getY() + 0.99F;
        int capacity = Math.max(controller.tank().capacity(), controller.tank().amount());
        for (var fluid : controller.tank().contents()) {
            float next = floor + height * fluid.getAmount() / capacity;
            FluidRenderer.box(poses, buffers, fluid, min.getX() + 0.005F, floor, min.getZ() + 0.005F,
                    max.getX() + 0.995F, next, max.getZ() + 0.995F, LightTexture.FULL_BRIGHT);
            floor = next;
        }
    }
    @Override public boolean shouldRenderOffScreen(SmelteryControllerBlockEntity controller) { return true; }
    @Override public AABB getRenderBoundingBox(SmelteryControllerBlockEntity controller) {
        if (!controller.isFormed()) return new AABB(controller.getBlockPos());
        var min = controller.min(); var max = controller.max();
        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
    }
}
