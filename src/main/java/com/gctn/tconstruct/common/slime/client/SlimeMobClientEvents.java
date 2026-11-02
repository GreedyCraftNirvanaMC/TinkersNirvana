/*
 * 根据 TinkersAntique 的彩色史莱姆渲染器注册移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.slime.SlimeMobModule;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 每种实体注册独立渲染器，避免在渲染期间修改其他实体的共享颜色。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class SlimeMobClientEvents {
    private SlimeMobClientEvents() {
    }

    @SubscribeEvent
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SlimeMobModule.BLUE.get(), context -> new AntiqueSlimeRenderer(context, SlimeMobModule.BLUE_TINT));
        event.registerEntityRenderer(SlimeMobModule.PURPLE.get(), context -> new AntiqueSlimeRenderer(context, SlimeMobModule.PURPLE_TINT));
    }
}
