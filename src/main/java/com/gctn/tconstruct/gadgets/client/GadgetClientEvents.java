/*
 * 根据 TinkersAntique 的褐砂石物品提示移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.GadgetModule;
import com.gctn.tconstruct.gadgets.SlimeChannels;
import com.gctn.tconstruct.gadgets.block.BrownstoneBlock;
import com.gctn.tconstruct.gadgets.block.BrownstoneSlabBlock;
import com.gctn.tconstruct.gadgets.block.BrownstoneStairBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** 方块、台阶和楼梯统一使用本地化的加速提示。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class GadgetClientEvents {
    private GadgetClientEvents() {
    }

    @SubscribeEvent private static void channelBlocks(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block event) {
        SlimeChannels.BLOCKS.values().forEach(block -> event.register((state, level, pos, tint) -> block.get().color, block.get()));
    }
    @SubscribeEvent private static void channelItems(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        SlimeChannels.BLOCKS.values().forEach(block -> event.register((stack, tint) -> block.get().color, block.get()));
    }
    @SubscribeEvent private static void channelLayers(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> SlimeChannels.BLOCKS.values().forEach(block -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(block.get(), net.minecraft.client.renderer.RenderType.translucent())));
        event.enqueueWork(() -> net.minecraft.client.renderer.item.ItemProperties.register(com.gctn.tconstruct.gadgets.Spaghetti.MOMS.get(), com.gctn.tconstruct.gadgets.SlimeGear.id("seasoning"),
                (stack, level, entity, seed) -> com.gctn.tconstruct.gadgets.item.MomsSpaghettiItem.seasoning(stack)));
    }

    @SubscribeEvent
    private static void screens(RegisterMenuScreensEvent event) {
        event.register(GadgetModule.WOODEN_HOPPER_MENU.get(), WoodenHopperScreen::new);
    }

    @SubscribeEvent
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GadgetModule.GLOWBALL_ENTITY.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(com.gctn.tconstruct.gadgets.RemainingGadgets.RACK_ENTITY.get(), context -> new RackItemRenderer());
    }

    @SubscribeEvent
    private static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(GadgetModule.EFLN.get())) { event.getToolTip().add(Component.translatable("tooltip.tconstruct.efln").withStyle(ChatFormatting.GRAY)); }
        if (event.getItemStack().getItem() instanceof BlockItem item && item.getBlock() instanceof com.gctn.tconstruct.gadgets.block.SlimeChannelBlock) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.slime_channel").withStyle(ChatFormatting.GRAY));
        }
        if (event.getItemStack().is(GadgetModule.GLOWBALL.get())) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.glowball").withStyle(ChatFormatting.GRAY));
        }
        if (event.getItemStack().getItem() instanceof BlockItem item
                && (item.getBlock() instanceof BrownstoneBlock || item.getBlock() instanceof BrownstoneSlabBlock
                || item.getBlock() instanceof BrownstoneStairBlock)) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.brownstone").withStyle(ChatFormatting.GRAY));
        }
        if (event.getItemStack().is(GadgetModule.WOODEN_HOPPER.get().asItem())) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.wooden_hopper").withStyle(ChatFormatting.GRAY));
        }
        if (event.getItemStack().is(GadgetModule.PUNJI.get().asItem())) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.punji").withStyle(ChatFormatting.GRAY));
        }
        if (event.getItemStack().is(GadgetModule.TRAPDOOR_RAIL.get().asItem())) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.wood_rail_trapdoor").withStyle(ChatFormatting.GRAY));
        }
    }
}
