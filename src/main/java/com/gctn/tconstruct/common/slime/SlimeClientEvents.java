/*
 * 根据 TinkersAntique 的史莱姆方块提示移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** 方块说明与可食用球的药水效果均使用翻译组件。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class SlimeClientEvents {
    private SlimeClientEvents() {
    }

    @SubscribeEvent
    private static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem item) {
            String key = item.getBlock() instanceof ColoredSlimeBlock ? "tooltip.tconstruct.slime"
                    : item.getBlock() instanceof CongealedSlimeBlock ? "tooltip.tconstruct.slime_congealed" : null;
            if (key != null) {
                event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
