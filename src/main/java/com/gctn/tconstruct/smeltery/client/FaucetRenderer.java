package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.FaucetBlock; // 引入 com.gctn.tconstruct.smeltery.block.FaucetBlock，供本文件使用。
import com.gctn.tconstruct.smeltery.block.entity.*; // 引入 com.gctn.tconstruct.smeltery.block.entity.*，供本文件使用。
import com.mojang.blaze3d.vertex.PoseStack; // 引入 com.mojang.blaze3d.vertex.PoseStack，供本文件使用。
import com.mojang.math.Axis; // 引入 com.mojang.math.Axis，供本文件使用。
import net.minecraft.client.renderer.*; // 引入 net.minecraft.client.renderer.*，供本文件使用。
import net.minecraft.client.renderer.blockentity.*; // 引入 net.minecraft.client.renderer.blockentity.*，供本文件使用。

/**
 * 根据同步的流体种类绘制水平出液部分和下落的流体束。
 * 流体束在铸造盆的液面处结束，且允许超出浇注口方块本身的渲染范围。
 */
public class FaucetRenderer implements BlockEntityRenderer<FaucetBlockEntity> { // 定义 FaucetRenderer 的实现类型，具体职责见上方说明。
    public FaucetRenderer(BlockEntityRendererProvider.Context context) {} // 定义 FaucetRenderer 的调用入口；此处无需额外处理。
    @Override public void render(FaucetBlockEntity faucet, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) { // 实现父类或接口约定；定义 render 的调用入口，参数和具体处理见下方实现。
        if (faucet.flowing().isEmpty() || !faucet.getBlockState().getValue(FaucetBlock.POURING)) return; // 检查 faucet.flowing().isEmpty() || !faucet.getBlockState().getValue(FaucetBlock.POURING)；按条件决定是否执行本分支。
        float bottom = -0.06F; // 设置 bottom 为 -0.06F。
        if (faucet.getLevel() != null && faucet.getLevel().getBlockEntity(faucet.getBlockPos().below()) instanceof CastingBlockEntity casting && casting.isBasin()) { // 检查 faucet.getLevel() != null && faucet.getLevel().getBlockEntity(faucet.getBlockPos().below()) instanceof Cast…；按条件决定是否执行本分支。
            bottom = -0.749F + 0.68F * casting.fluidHandler().getFluidAmount() / casting.fluidHandler().getCapacity(); // 设置 bottom 为 -0.749F + 0.68F * casting.fluidHandler().getFluidAmount() / casting.fluidHandler().getCapacity()。
        } // 结束当前作用域。
        poses.pushPose(); // 保存当前渲染变换，避免影响后续绘制。
        poses.translate(0.5, 0, 0.5); // 平移当前渲染坐标。
        poses.mulPose(Axis.YP.rotationDegrees(180 - faucet.getBlockState().getValue(FaucetBlock.FACING).toYRot())); // 应用模型旋转。
        poses.translate(-0.5, 0, -0.5); // 平移当前渲染坐标。
        FluidRenderer.box(poses, buffers, faucet.flowing(), 0.39F, 0.377F, 0.625F, 0.61F, 0.41F, 1.001F, LightTexture.FULL_BRIGHT); // 构建或绘制指定范围的形状。
        FluidRenderer.box(poses, buffers, faucet.flowing(), 0.4375F, bottom, 0.625F, 0.5625F, 0.39F, 0.6875F, LightTexture.FULL_BRIGHT); // 构建或绘制指定范围的形状。
        poses.popPose(); // 恢复先前保存的渲染变换。
    } // 结束当前作用域。
    @Override public boolean shouldRenderOffScreen(FaucetBlockEntity faucet) { return true; } // 实现父类或接口约定；定义 shouldRenderOffScreen 的调用入口；返回 true，将当前分支的结果交给调用者。
} // 结束当前作用域。
