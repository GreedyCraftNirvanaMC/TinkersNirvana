package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.entity.SearedTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

/**
 * Draws stored lava inside the transparent fuel-tank model.
 * The amount is synchronized by the block entity and converted to a fraction of its fixed capacity.
 */
public class SearedTankRenderer implements BlockEntityRenderer<SearedTankBlockEntity> {
    public SearedTankRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(SearedTankBlockEntity tank, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        FluidRenderer.box(poses, buffers, tank.tank().getFluid(), 0.06F, 0.06F, 0.06F, 0.94F,
                0.06F + 0.88F * tank.tank().getFluidAmount() / SearedTankBlockEntity.CAPACITY, 0.94F, LightTexture.FULL_BRIGHT);
    }
}
