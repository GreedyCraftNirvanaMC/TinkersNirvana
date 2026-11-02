/*
 * 根据 TinkersAntique 的 GuiTinkerTank 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.menu.TinkerTankMenu;
import com.gctn.tconstruct.smeltery.network.SmelteryNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** 保留原版 122×130 窗口、106×106 储液区和刻度覆盖层。 */
public class TinkerTankScreen extends AbstractContainerScreen<TinkerTankMenu> {
    private static final ResourceLocation BACKGROUND = SmelteryFluids.id("textures/gui/tinker_tank.png");

    public TinkerTankScreen(TinkerTankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 122;
        imageHeight = 130;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (menu.controller == null || !menu.controller.active()) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.controller != null) {
            int bottom = topPos + 122;
            int[] heights = heights();
            for (int i = 0; i < heights.length; i++) {
                bottom -= heights[i];
                SmelteryRendering.fluid(graphics, menu.controller.liquids.fluids().get(i), leftPos + 8, bottom, 106, heights[i]);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, 8, 16, 122, 0, 106, 106);
        graphics.drawString(font, title, 8, 6, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (menu.controller == null || mouseX < leftPos + 8 || mouseX >= leftPos + 114
                || mouseY < topPos + 16 || mouseY >= topPos + 122) {
            return;
        }
        List<Component> text = new ArrayList<>();
        var tank = menu.controller.liquids;
        int index = fluidAt(mouseX - leftPos, mouseY - topPos);
        if (index >= 0) {
            FluidStack fluid = tank.fluids().get(index);
            text.add(fluid.getHoverName());
            FluidTankTooltip.amount(text, fluid.getAmount(), fluid, hasShiftDown());
        } else {
            text.add(Component.translatable("gui.smeltery.capacity"));
            FluidTankTooltip.amount(text, tank.capacity(), FluidStack.EMPTY, hasShiftDown());
            text.add(Component.translatable("gui.smeltery.capacity_available"));
            FluidTankTooltip.amount(text, Math.max(0, tank.capacity() - tank.amount()), FluidStack.EMPTY, hasShiftDown());
            text.add(Component.translatable("gui.smeltery.capacity_used"));
            FluidTankTooltip.amount(text, tank.amount(), FluidStack.EMPTY, hasShiftDown());
            if (!hasShiftDown()) {
                text.add(Component.empty());
                text.add(Component.translatable("tooltip.tank.holdShift"));
            }
        }
        graphics.renderTooltip(font, text, Optional.empty(), mouseX, mouseY);
    }

    private int[] heights() {
        return menu.controller == null ? new int[0] : SmelteryRendering.heights(menu.controller.liquids.fluids(),
                menu.controller.liquids.capacity(), 106, Config.MIN_FLUID_HEIGHT.get());
    }

    private int fluidAt(int x, int y) {
        if (x < 8 || x >= 114 || y < 16 || y >= 122) {
            return -1;
        }
        // 点击判定与绘制使用同一组高度，少量液体也能准确选中。
        int bottom = 122;
        int[] heights = heights();
        for (int i = 0; i < heights.length; i++) {
            bottom -= heights[i];
            if (y >= bottom) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int index = fluidAt((int) x - leftPos, (int) y - topPos);
        if (button == 0 && index >= 0) {
            PacketDistributor.sendToServer(new SmelteryNetwork.SelectFluid(menu.containerId, index));
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
}
