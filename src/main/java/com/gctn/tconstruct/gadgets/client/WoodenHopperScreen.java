/*
 * 根据 TinkersAntique 的 WoodenHopperGUIDrawEvent 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.client;

import com.gctn.tconstruct.gadgets.menu.WoodenHopperMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** 使用原始贴图与居中的单槽布局。 */
public class WoodenHopperScreen extends AbstractContainerScreen<WoodenHopperMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tconstruct", "textures/gui/hopper.png");

    public WoodenHopperScreen(WoodenHopperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 133;
        inventoryLabelY = 39;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTick) {
        super.render(graphics, x, y, partialTick);
        renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int x, int y) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
