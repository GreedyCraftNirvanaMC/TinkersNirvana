/*
 * 根据 TinkersAntique 的褐砂石物品提示移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.TinkersNirvana;
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

/** 方块、台阶和楼梯统一使用本地化的加速提示。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class GadgetClientEvents {
    private GadgetClientEvents() {
    }

    @SubscribeEvent
    private static void tooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem item
                && (item.getBlock() instanceof BrownstoneBlock || item.getBlock() instanceof BrownstoneSlabBlock
                || item.getBlock() instanceof BrownstoneStairBlock)) {
            event.getToolTip().add(Component.translatable("tooltip.tconstruct.brownstone").withStyle(ChatFormatting.GRAY));
        }
    }
}
