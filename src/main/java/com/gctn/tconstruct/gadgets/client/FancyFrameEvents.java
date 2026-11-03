/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.FancyFrames;
import com.gctn.tconstruct.gadgets.SlimeGear;
import com.gctn.tconstruct.gadgets.entity.FancyItemFrame;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class FancyFrameEvents {
    private FancyFrameEvents() {}
    @SubscribeEvent private static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(FancyFrames.ENTITY.get(), FancyFrameRenderer::new); }
    @SubscribeEvent private static void models(ModelEvent.RegisterAdditional event) {
        for (var kind : FancyFrames.Kind.values()) { event.register(FancyFrameRenderer.model(kind, false)); event.register(FancyFrameRenderer.model(kind, true)); }
        event.register(ModelResourceLocation.standalone(SlimeGear.id("block/fancy_frame/empty")));
    }
    @SubscribeEvent private static void item(RenderItemInFrameEvent event) {
        if (event.getItemFrameEntity() instanceof FancyItemFrame frame && frame.kind() == FancyFrames.Kind.CLEAR) { event.getPoseStack().translate(0, 0, .03125); }
    }
}
