package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * 注册冶炼炉界面、方块实体渲染器，以及流体纹理和着色扩展。
 * 所有方法均限定在客户端加载，避免专用服务端访问渲染类。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class SmelteryClientEvents {
    @SubscribeEvent private static void screens(RegisterMenuScreensEvent event) { event.register(SmelteryRegistration.MENU.get(), SmelteryScreen::new); }
    @SubscribeEvent private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SmelteryRegistration.CONTROLLER_ENTITY.get(), SmelteryRenderer::new);
        event.registerBlockEntityRenderer(SmelteryRegistration.TANK_ENTITY.get(), SearedTankRenderer::new);
        event.registerBlockEntityRenderer(SmelteryRegistration.CASTING_ENTITY.get(), CastingRenderer::new);
        event.registerBlockEntityRenderer(SmelteryRegistration.FAUCET_ENTITY.get(), FaucetRenderer::new);
    }
    @SubscribeEvent private static void fluids(RegisterClientExtensionsEvent event) {
        for (var entry : SmelteryFluids.entries()) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override public int getTintColor() { return entry.color(); }
                @Override public ResourceLocation getStillTexture() { return SmelteryRegistration.id("block/fluids/molten_metal"); }
                @Override public ResourceLocation getFlowingTexture() { return SmelteryRegistration.id("block/fluids/molten_metal_flow"); }
            }, entry.type().get());
        }
    }
}
