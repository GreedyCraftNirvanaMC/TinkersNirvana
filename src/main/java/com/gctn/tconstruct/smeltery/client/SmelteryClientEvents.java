/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class SmelteryClientEvents {
    private SmelteryClientEvents() {
    }

    @SubscribeEvent
    private static void screens(RegisterMenuScreensEvent event) {
        event.register(SmelteryModule.MENU.get(), SmelteryScreen::new);
    }

    @SubscribeEvent
    private static void sprites(net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent event) {
        event.register(SmelteryFluids.id("cast"), CastSpriteSource.TYPE);
    }

    @SubscribeEvent
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SmelteryModule.CONTROLLER_ENTITY.get(), SmelteryRenderer::new);
        event.registerBlockEntityRenderer(SmelteryModule.COMPONENT_ENTITY.get(), SmelteryRenderer::new);
    }

    @SubscribeEvent
    private static void fluids(RegisterClientExtensionsEvent event) {
        for (SmelteryFluids.Entry fluid : SmelteryFluids.ENTRIES.values()) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                public int getTintColor() { return 0xff000000 | fluid.color; }
                public ResourceLocation getStillTexture() { return texture(fluid.name, false); }
                public ResourceLocation getFlowingTexture() { return texture(fluid.name, true); }
            }, fluid.type.get());
        }
        event.registerItem(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private TankItemRenderer renderer;
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new TankItemRenderer();
                }
                return renderer;
            }
        }, SmelteryModule.TANK.get().asItem(), SmelteryModule.GAUGE.get().asItem(), SmelteryModule.WINDOW.get().asItem());
    }

    @SubscribeEvent
    private static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        var fluid = event.getItemStack().getOrDefault(SmelteryModule.TANK_FLUID.get(), net.neoforged.neoforge.fluids.SimpleFluidContent.EMPTY).copy();
        if (!fluid.isEmpty()) {
            event.getToolTip().add(fluid.getHoverName());
            event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.tank.amount", fluid.getAmount()));
        }
    }

    private static ResourceLocation texture(String name, boolean flowing) {
        String texture = switch (name) {
            case "stone", "obsidian", "clay", "dirt", "notmilk" -> "liquid_stone";
            case "blood" -> "liquid";
            case "venom" -> "liquid_poison";
            case "greenslime", "blueslime", "purpleslime" -> "liquid_slime";
            default -> "molten_metal";
        };
        return SmelteryFluids.id("block/fluids/" + texture + (flowing ? "_flow" : ""));
    }

    @SubscribeEvent
    private static void buckets(RegisterColorHandlersEvent.Item event) {
        for (SmelteryFluids.Entry fluid : SmelteryFluids.ENTRIES.values()) {
            event.register((stack, tint) -> tint == 1 ? 0xff000000 | fluid.color : -1, fluid.bucket.get());
        }
    }
}
