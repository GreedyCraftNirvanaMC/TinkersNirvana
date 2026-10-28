package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.entity.SearedTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

/**
 * 在透明的燃料储罐模型内部绘制已存储的熔岩。
 * 流体数量由方块实体同步，并换算成固定容量的占比。
 */
public class SearedTankRenderer implements BlockEntityRenderer<SearedTankBlockEntity> {
    public SearedTankRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(SearedTankBlockEntity tank, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        FluidRenderer.box(poses, buffers, tank.tank().getFluid(), 0.06F, 0.06F, 0.06F, 0.94F,
                0.06F + 0.88F * tank.tank().getFluidAmount() / SearedTankBlockEntity.CAPACITY, 0.94F, LightTexture.FULL_BRIGHT);
    }
}
