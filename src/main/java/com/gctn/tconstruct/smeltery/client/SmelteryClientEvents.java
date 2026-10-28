package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.smeltery.*; // 引入 com.gctn.tconstruct.smeltery.*，供本文件使用。
import net.minecraft.resources.ResourceLocation; // 引入 net.minecraft.resources.ResourceLocation，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.client.event.*; // 引入 net.neoforged.neoforge.client.event.*，供本文件使用。
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions; // 引入 net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions，供本文件使用。
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent; // 引入 net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent，供本文件使用。

/**
 * 注册冶炼炉界面、方块实体渲染器，以及流体纹理和着色扩展。
 * 所有方法均限定在客户端加载，避免专用服务端访问渲染类。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public final class SmelteryClientEvents { // 定义 SmelteryClientEvents 的实现类型，具体职责见上方说明。
    @SubscribeEvent private static void screens(RegisterMenuScreensEvent event) { event.register(SmelteryRegistration.MENU.get(), SmelteryScreen::new); } // 将此方法声明为事件处理入口。
    @SubscribeEvent private static void renderers(EntityRenderersEvent.RegisterRenderers event) { // 将此方法声明为事件处理入口。
        event.registerBlockEntityRenderer(SmelteryRegistration.CONTROLLER_ENTITY.get(), SmelteryRenderer::new); // 读取指定条目或延迟注册对象。
        event.registerBlockEntityRenderer(SmelteryRegistration.TANK_ENTITY.get(), SearedTankRenderer::new); // 读取指定条目或延迟注册对象。
        event.registerBlockEntityRenderer(SmelteryRegistration.CASTING_ENTITY.get(), CastingRenderer::new); // 读取指定条目或延迟注册对象。
        event.registerBlockEntityRenderer(SmelteryRegistration.FAUCET_ENTITY.get(), FaucetRenderer::new); // 读取指定条目或延迟注册对象。
    } // 结束当前作用域。
    @SubscribeEvent private static void fluids(RegisterClientExtensionsEvent event) { // 将此方法声明为事件处理入口。
        for (var entry : SmelteryFluids.entries()) { // 遍历 var entry : SmelteryFluids.entries()，逐项执行当前处理。
            event.registerFluidType(new IClientFluidTypeExtensions() { // 调用 registerFluidType，执行当前流程所需的处理。
                @Override public int getTintColor() { return entry.color(); } // 实现父类或接口约定；定义 getTintColor 的调用入口；返回 entry.color()，将当前分支的结果交给调用者。
                @Override public ResourceLocation getStillTexture() { return SmelteryRegistration.id("block/fluids/molten_metal"); } // 实现父类或接口约定；定义 getStillTexture 的调用入口；返回 SmelteryRegistration.id("block/fluids/molten_metal")，将当前分支的结果交给调用者。
                @Override public ResourceLocation getFlowingTexture() { return SmelteryRegistration.id("block/fluids/molten_metal_flow"); } // 实现父类或接口约定；定义 getFlowingTexture 的调用入口；返回 SmelteryRegistration.id("block/fluids/molten_metal_flow")，将当前分支的结果交给调用者。
            }, entry.type().get()); // 读取指定条目或延迟注册对象。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
