package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.entity.SearedTankBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.SearedTankBlockEntity，供本文件使用。
import com.mojang.blaze3d.vertex.PoseStack; // 引入 com.mojang.blaze3d.vertex.PoseStack，供本文件使用。
import net.minecraft.client.renderer.*; // 引入 net.minecraft.client.renderer.*，供本文件使用。
import net.minecraft.client.renderer.blockentity.*; // 引入 net.minecraft.client.renderer.blockentity.*，供本文件使用。

/**
 * 在透明的燃料储罐模型内部绘制已存储的熔岩。
 * 流体数量由方块实体同步，并换算成固定容量的占比。
 */
public class SearedTankRenderer implements BlockEntityRenderer<SearedTankBlockEntity> { // 定义 SearedTankRenderer 的实现类型，具体职责见上方说明。
    public SearedTankRenderer(BlockEntityRendererProvider.Context context) {} // 定义 SearedTankRenderer 的调用入口；此处无需额外处理。
    @Override public void render(SearedTankBlockEntity tank, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) { // 实现父类或接口约定；定义 render 的调用入口，参数和具体处理见下方实现。
        FluidRenderer.box(poses, buffers, tank.tank().getFluid(), 0.06F, 0.06F, 0.06F, 0.94F, // 构建或绘制指定范围的形状。
                0.06F + 0.88F * tank.tank().getFluidAmount() / SearedTankBlockEntity.CAPACITY, 0.94F, LightTexture.FULL_BRIGHT); // 调用 tank，执行当前流程所需的处理。
    } // 结束当前作用域。
} // 结束当前作用域。
