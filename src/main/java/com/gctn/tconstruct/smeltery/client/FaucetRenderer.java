package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.FaucetBlock;
import com.gctn.tconstruct.smeltery.block.entity.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;

/**
 * 根据同步的流体种类绘制水平出液部分和下落的流体束。
 * 流体束在铸造盆的液面处结束，且允许超出浇注口方块本身的渲染范围。
 */
public class FaucetRenderer implements BlockEntityRenderer<FaucetBlockEntity> {
    public FaucetRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(FaucetBlockEntity faucet, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        if (faucet.flowing().isEmpty() || !faucet.getBlockState().getValue(FaucetBlock.POURING)) return;
        float bottom = -0.06F;
        if (faucet.getLevel() != null && faucet.getLevel().getBlockEntity(faucet.getBlockPos().below()) instanceof CastingBlockEntity casting && casting.isBasin()) {
            bottom = -0.749F + 0.68F * casting.fluidHandler().getFluidAmount() / casting.fluidHandler().getCapacity();
        }
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.mulPose(Axis.YP.rotationDegrees(180 - faucet.getBlockState().getValue(FaucetBlock.FACING).toYRot()));
        poses.translate(-0.5, 0, -0.5);
        FluidRenderer.box(poses, buffers, faucet.flowing(), 0.39F, 0.377F, 0.625F, 0.61F, 0.41F, 1.001F, LightTexture.FULL_BRIGHT);
        FluidRenderer.box(poses, buffers, faucet.flowing(), 0.4375F, bottom, 0.625F, 0.5625F, 0.39F, 0.6875F, LightTexture.FULL_BRIGHT);
        poses.popPose();
    }
    @Override public boolean shouldRenderOffScreen(FaucetBlockEntity faucet) { return true; }
}
