package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * 将原版挖掘事件转发到统一的词条回调接口。
 * 挖掘速度在客户端与服务端分别计算，改变世界的回调只在服务端方块破坏事件中执行。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class TraitEvents {
    private TraitEvents() {}

    @SubscribeEvent
    public static void miningSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        if (tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)) {
            ToolTraits.forEach(tool, (trait, level) -> trait.miningSpeed(tool, level, event));
        }
    }

    @SubscribeEvent
    public static void beforeBlockBreak(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getMainHandItem();
        if (tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)) {
            ToolTraits.forEach(tool, (trait, level) -> trait.beforeBlockBreak(tool, level, event));
        }
    }
}
