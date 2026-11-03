/* SPDX-License-Identifier: LGPL-3.0-only; Antique gear colors, armor layers, and carry icons. */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.SlimeGear;
import com.gctn.tconstruct.gadgets.item.PiggybackItem;
import com.gctn.tconstruct.gadgets.item.SlimeBootsItem;
import com.gctn.tconstruct.gadgets.item.SlimeSlingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class GearClientEvents {
    private GearClientEvents() {}
    @SubscribeEvent private static void colors(RegisterColorHandlersEvent.Item event) {
        SlimeGear.SLINGS.forEach((color, item) -> event.register((stack, tint) -> 0xff000000 | SlimeGear.COLORS.get(color), item.get()));
        SlimeGear.BOOTS.forEach((color, item) -> event.register((stack, tint) -> 0xff000000 | SlimeGear.COLORS.get(color), item.get()));
    }
    @SubscribeEvent private static void extensions(RegisterClientExtensionsEvent event) {
        SlimeGear.BOOTS.forEach((color, item) -> event.registerItem(new IClientItemExtensions() {
            @Override public int getDefaultDyeColor(ItemStack stack) { return 0xff000000 | SlimeGear.COLORS.get(color); }
        }, item.get()));
        event.registerMobEffect(new IClientMobEffectExtensions() {
            private void icon(MobEffectInstance effect, GuiGraphics graphics, int x, int y, float alpha) {
                int count = Math.min(3, effect.getAmplifier() + 1);
                var texture = SlimeGear.id("textures/mob_effect/carry" + (count == 1 ? "" : "_" + count) + ".png");
                graphics.setColor(1, 1, 1, alpha); graphics.blit(texture, x, y, 0, 0, 18, 18, 18, 18); graphics.setColor(1, 1, 1, 1);
            }
            @Override public boolean renderInventoryIcon(MobEffectInstance effect, EffectRenderingInventoryScreen<?> screen, GuiGraphics graphics, int x, int y, int z) { icon(effect, graphics, x, y + 7, 1); return true; }
            @Override public boolean renderGuiIcon(MobEffectInstance effect, Gui gui, GuiGraphics graphics, int x, int y, float z, float alpha) { icon(effect, graphics, x + 3, y + 3, alpha); return true; }
        }, SlimeGear.CARRY);
    }
    @SubscribeEvent private static void tooltip(ItemTooltipEvent event) {
        var item = event.getItemStack().getItem();
        String key = item instanceof SlimeSlingItem ? "slimesling" : item instanceof SlimeBootsItem ? "slime_boots" : item instanceof PiggybackItem ? "piggybackpack" : null;
        if (key != null) { event.getToolTip().add(Component.translatable("tooltip.tconstruct." + key).withStyle(ChatFormatting.GRAY)); }
    }
}
