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

/** 土壤的物品提示按当前方块行为显示。 */
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
                case SLIMY_MUD_GREEN, SLIMY_MUD_BLUE -> "slimy_mud";
                case SLIMY_MUD_MAGMA -> null;
            };
            if (key != null) {
                event.getToolTip().add(Component.translatable("tooltip.tconstruct.soil." + key).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
