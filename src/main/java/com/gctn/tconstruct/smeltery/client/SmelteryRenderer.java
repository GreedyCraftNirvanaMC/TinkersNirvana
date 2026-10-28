package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity，供本文件使用。
import com.mojang.blaze3d.vertex.PoseStack; // 引入 com.mojang.blaze3d.vertex.PoseStack，供本文件使用。
import net.minecraft.client.renderer.*; // 引入 net.minecraft.client.renderer.*，供本文件使用。
import net.minecraft.client.renderer.blockentity.*; // 引入 net.minecraft.client.renderer.blockentity.*，供本文件使用。
import net.minecraft.world.phys.AABB; // 引入 net.minecraft.world.phys.AABB，供本文件使用。

/**
 * 在检测出的多方块内部空间中按顺序渲染各流体层。
 * 渲染范围覆盖整个结构，因为控制器离开屏幕时仍可能看见炉内流体。
 * 超出容量的流体按比例压缩显示，避免结构缩小时渲染到炉体外部。
 */
public class SmelteryRenderer implements BlockEntityRenderer<SmelteryControllerBlockEntity> { // 定义 SmelteryRenderer 的实现类型，具体职责见上方说明。
    public SmelteryRenderer(BlockEntityRendererProvider.Context context) {} // 定义 SmelteryRenderer 的调用入口；此处无需额外处理。
    @Override public void render(SmelteryControllerBlockEntity controller, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) { // 实现父类或接口约定；定义 render 的调用入口，参数和具体处理见下方实现。
        if (!controller.isFormed() || controller.tank().amount() <= 0) return; // 检查 !controller.isFormed() || controller.tank().amount() <= 0；按条件决定是否执行本分支。
        var min = controller.min().subtract(controller.getBlockPos()); // 设置 min 为 controller.min().subtract(controller.getBlockPos())。
        var max = controller.max().subtract(controller.getBlockPos()); // 设置 max 为 controller.max().subtract(controller.getBlockPos())。
        float floor = min.getY() + 0.005F; // 设置 floor 为 min.getY() + 0.005F。
        float height = max.getY() - min.getY() + 0.99F; // 设置 height 为 max.getY() - min.getY() + 0.99F。
        int capacity = Math.max(controller.tank().capacity(), controller.tank().amount()); // 设置 capacity 为 Math.max(controller.tank().capacity(), controller.tank().amount())。
        for (var fluid : controller.tank().contents()) { // 遍历 var fluid : controller.tank().contents()，逐项执行当前处理。
            float next = floor + height * fluid.getAmount() / capacity; // 设置 next 为 floor + height * fluid.getAmount() / capacity。
            FluidRenderer.box(poses, buffers, fluid, min.getX() + 0.005F, floor, min.getZ() + 0.005F, // 构建或绘制指定范围的形状。
                    max.getX() + 0.995F, next, max.getZ() + 0.995F, LightTexture.FULL_BRIGHT); // 调用 getX，执行当前流程所需的处理。
            floor = next; // 设置 floor 为 next。
        } // 结束当前作用域。
    } // 结束当前作用域。
    @Override public boolean shouldRenderOffScreen(SmelteryControllerBlockEntity controller) { return true; } // 实现父类或接口约定；定义 shouldRenderOffScreen 的调用入口；返回 true，将当前分支的结果交给调用者。
    @Override public AABB getRenderBoundingBox(SmelteryControllerBlockEntity controller) { // 实现父类或接口约定；定义 getRenderBoundingBox 的调用入口，参数和具体处理见下方实现。
        if (!controller.isFormed()) return new AABB(controller.getBlockPos()); // 检查 !controller.isFormed()；按条件决定是否执行本分支。
        var min = controller.min(); var max = controller.max(); // 设置 min 为 controller.min(); var max = controller.max()。
        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1); // 返回 new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
