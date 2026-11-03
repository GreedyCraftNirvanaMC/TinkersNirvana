/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's AchievementEvents. */
package com.gctn.tconstruct.common;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.AntiqueProjectile;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolDefinition;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class AntiqueAdvancements {
    private AntiqueAdvancements() {}
    @SubscribeEvent private static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) { return; }
        var stack = event.getCrafting();
        if (stack.is(Items.CRAFTING_TABLE)) { grant(player, "story/root"); }
        if (stack.getItem() instanceof AntiqueTool tool && (tool.definition().action() == ToolDefinition.Action.PICKAXE || tool.definition().action() == ToolDefinition.Action.HAMMER)) {
            int tier = ToolBuilder.savedStats(stack).getInt("HarvestLevel");
            if (tier > 0) { grant(player, "story/upgrade_tools"); }
            if (tier > 1) { grant(player, "story/iron_tools"); }
        }
    }
    @SubscribeEvent private static void damage(LivingDamageEvent.Post event) {
        var source = event.getSource();
        if (event.getNewDamage() > 0 && source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof AntiqueProjectile
                && source.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) { grant(player, "adventure/shoot_arrow"); }
    }
    private static void grant(ServerPlayer player, String id) {
        var advancement = player.server.getAdvancements().get(ResourceLocation.withDefaultNamespace(id));
        if (advancement != null) { var remaining = new java.util.ArrayList<String>(); player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria().forEach(remaining::add); for (String criterion : remaining) { player.getAdvancements().award(advancement, criterion); } }
    }
}
