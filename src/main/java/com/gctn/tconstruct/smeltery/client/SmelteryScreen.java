package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Client GUI for inventory pages, heating progress, fuel and selectable fluid layers.
 * Click hit-testing uses the same layer sizing as rendering, and selection is committed by the server menu.
 */
public class SmelteryScreen extends AbstractContainerScreen<SmelteryMenu> {
    public SmelteryScreen(SmelteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 236; imageHeight = 222; inventoryLabelY = 128;
    }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), b -> send(SmelteryMenu.PREVIOUS)).bounds(leftPos + 8, topPos + 103, 22, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> send(SmelteryMenu.NEXT)).bounds(leftPos + 148, topPos + 103, 22, 20).build());
    }
    private void send(int button) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        int index = fluidAt(mouseX, mouseY);
        if (index >= 0 && menu.controller() != null) {
            var fluid = menu.controller().tank().contents().get(index);
            graphics.renderTooltip(font, Component.translatable("gui.tconstruct.smeltery.fluid", fluid.getHoverName(), fluid.getAmount()), mouseX, mouseY);
        } else if (isHovering(8, 88, 162, 10, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.tconstruct.smeltery.fuel"), mouseX, mouseY);
        }
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF373331);
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC6C6C6);
        for (var slot : menu.slots) if (slot.isActive()) {
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xFF373737);
            graphics.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xFF8B8B8B);
        }
        graphics.fill(x + 8, y + 88, x + 170, y + 98, 0xFF373737);
        graphics.fill(x + 9, y + 89, x + 9 + menu.fuel() * 160 / 100, y + 97, 0xFFDC6A20);
        for (int i = 0; i < SmelteryMenu.VISIBLE_SLOTS; i++) {
            int progress = menu.progress(i);
            if (progress > 0) graphics.fill(x + 8 + i % 9 * 18, y + 42 + i / 9 * 18, x + 8 + i % 9 * 18 + progress * 16 / 1000, y + 44 + i / 9 * 18, 0xFFFF8A20);
        }
        graphics.fill(x + 182, y + 27, x + 228, y + 124, 0xFF24201E);
        if (menu.controller() != null) {
            var tank = menu.controller().tank();
            int bottom = 123;
            for (var fluid : tank.contents()) {
                int height = Math.max(1, (int) (95L * fluid.getAmount() / Math.max(1, Math.max(tank.capacity(), tank.amount()))));
                height = Math.min(height, bottom - 28);
                if (height <= 0) break;
                int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid) | 0xFF000000;
                graphics.fill(x + 183, y + bottom - height, x + 227, y + bottom, color);
                bottom -= height;
            }
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.tconstruct.smeltery.inventory"), inventoryLabelX, inventoryLabelY, 0x404040, false);
        graphics.drawCenteredString(font, Component.translatable("gui.tconstruct.smeltery.page", menu.currentPage() + 1, menu.pages()), 89, 109, 0xFFFFFF);
        if (menu.controller() != null) graphics.drawString(font, Component.translatable("gui.tconstruct.smeltery.capacity", menu.controller().tank().amount(), menu.controller().tank().capacity()), 8, 17, 0x404040, false);
    }
    private int fluidAt(double mouseX, double mouseY) {
        if (!isHovering(183, 28, 44, 95, mouseX, mouseY) || menu.controller() == null) return -1;
        var tank = menu.controller().tank();
        int bottom = 123, index = 0;
        for (var fluid : tank.contents()) {
            int height = Math.min(bottom - 28, Math.max(1, (int) (95L * fluid.getAmount() / Math.max(1, Math.max(tank.capacity(), tank.amount())))));
            if (height <= 0) break;
            if (mouseY - topPos >= bottom - height && mouseY - topPos < bottom) return index;
            bottom -= height; index++;
        }
        return -1;
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = fluidAt(mouseX, mouseY);
        if (button == 0 && index >= 0) { send(index); return true; }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
