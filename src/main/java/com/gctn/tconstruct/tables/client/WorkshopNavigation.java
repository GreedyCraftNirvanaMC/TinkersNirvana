/* SPDX-License-Identifier: LGPL-3.0-only; connected table navigation adapted from Antique. */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.tables.TableMaterial;
import com.gctn.tconstruct.tables.WorkshopModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public final class WorkshopNavigation {
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 28;
    private static final int TAB_SPACING = 2;
    private static final ResourceLocation INACTIVE_TAB = sprite("unselected_1");
    private static final ResourceLocation ACTIVE_LEFT_TAB = sprite("selected_1");
    private static final ResourceLocation ACTIVE_TAB = sprite("selected_2");

    private WorkshopNavigation() {}

    static List<WorkshopModule.Tab> tabs(BlockPos origin) {
        Minecraft minecraft = Minecraft.getInstance();
        if (origin == null || minecraft.level == null || minecraft.player == null) { return List.of(); }
        return WorkshopModule.connectedTabs(minecraft.level, origin, minecraft.player);
    }

    static int panelTop(int top, List<WorkshopModule.Tab> tabs) {
        return tabs.isEmpty() ? top : Math.max(top, TAB_HEIGHT);
    }

    static void add(List<WorkshopModule.Tab> tabs, BlockPos origin, int containerId, int left, int top, Consumer<Button> add) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) { return; }
        for (int i = 0; i < tabs.size(); i++) {
            WorkshopModule.Tab tab = tabs.get(i);
            BlockPos target = tab.position();
            ItemStack icon = TableMaterial.pick(minecraft.level, target, new ItemStack(minecraft.level.getBlockState(target).getBlock()));
            Button button = new TabButton(left + i * (TAB_WIDTH + TAB_SPACING), top - TAB_HEIGHT,
                    icon, target.equals(origin), i == 0, pressed -> {
                if (minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(containerId, tab.buttonId()); }
            });
            button.setTooltip(Tooltip.create(icon.getHoverName()));
            add.accept(button);
        }
    }

    private static ResourceLocation sprite(String tab) {
        return ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_" + tab);
    }

    private static final class TabButton extends Button {
        private final ItemStack icon;
        private final ResourceLocation sprite;

        TabButton(int x, int y, ItemStack icon, boolean selected, boolean first, OnPress press) {
            super(x, y, TAB_WIDTH, TAB_HEIGHT, icon.getHoverName(), press, DEFAULT_NARRATION);
            this.icon = icon;
            this.active = !selected;
            this.sprite = !selected ? INACTIVE_TAB : first ? ACTIVE_LEFT_TAB : ACTIVE_TAB;
        }

        @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) { return true; }
            // 点击当前标签时仍视为界面内点击，避免丢出鼠标拿着的物品。
            return visible && mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
        }

        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // 1.21.1 将怀古使用的原版标签拆成了独立精灵图，未选中的图上方留了两像素。
            int spriteHeight = active ? TAB_HEIGHT : TAB_HEIGHT + 4;
            int v = active ? 2 : 0;
            graphics.blitSprite(sprite, TAB_WIDTH, 32, 0, v, getX(), getY(), TAB_WIDTH, spriteHeight);
            graphics.renderItem(icon, getX() + (TAB_WIDTH - 16) / 2, getY() + (spriteHeight - 16) / 2);
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
