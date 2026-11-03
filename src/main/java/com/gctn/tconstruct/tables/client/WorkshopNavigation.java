/* SPDX-License-Identifier: LGPL-3.0-only; connected table navigation adapted from Antique. */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.tables.WorkshopModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class WorkshopNavigation {
    private WorkshopNavigation() {}
    public static void add(BlockPos origin, int containerId, int x, int y, Consumer<Button> add) {
        Minecraft minecraft = Minecraft.getInstance();
        if (origin == null || minecraft.level == null || minecraft.player == null) { return; }
        var tables = WorkshopModule.connected(minecraft.level, origin, minecraft.player);
        for (int i = 0; i < Math.min(8, tables.size()); i++) {
            BlockPos target = tables.get(i);
            ItemStack icon = new ItemStack(minecraft.level.getBlockState(target).getBlock());
            int action = 100 + i;
            Button button = new IconButton(x + i * 20, y, icon, pressed -> {
                if (minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(containerId, action); }
            });
            button.active = !target.equals(origin);
            button.setTooltip(Tooltip.create(icon.getHoverName()));
            add.accept(button);
        }
    }
    static class IconButton extends Button {
        private final ItemStack icon;
        IconButton(int x, int y, ItemStack icon, OnPress press) { super(x, y, 18, 18, Component.empty(), press, DEFAULT_NARRATION); this.icon = icon; }
        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick); graphics.renderItem(icon, getX() + 1, getY() + 1);
        }
    }
}
