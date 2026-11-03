/*
 * 根据 TinkersAntique 的土壤物品提示移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.soil;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** 蓝色与熔岩泥泞的原版提示共用绿色泥泞的说明，使用真正的翻译键解析。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class SoilClientEvents {
    private SoilClientEvents() {
    }

    @SubscribeEvent
    private static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem item && item.getBlock() instanceof AntiqueSoilBlock soil) {
            String key = switch (soil.kind) {
                case GROUT -> "grout";
                case GRAVEYARD -> "graveyard";
                case CONSECRATED -> "consecrated";
                default -> "slimy_mud";
            };
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.soil." + key).withStyle(ChatFormatting.GRAY));
        }
    }
}
