/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.smeltery.network.SmelteryNetwork;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SmelteryScreen extends AbstractContainerScreen<SmelteryMenu> {
    private static final ResourceLocation BACKGROUND = SmelteryFluids.id("textures/gui/smeltery.png");
    private static final ResourceLocation GENERIC = SmelteryFluids.id("textures/gui/generic.png");
    private boolean dragging;

    public SmelteryScreen(SmelteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (menu.controller == null || !menu.controller.active() || menu.controller.getContainerSize() != menu.count) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        List<Component> tooltip = tooltip(mouseX - leftPos, mouseY - topPos);
        if (!tooltip.isEmpty()) {
            graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // The original 256px texture and all source rectangles are retained at 1:1 GUI scale.
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawSideInventory(graphics, mouseX, mouseY);
        if (menu.controller != null) {
            int[] heights = layerHeights();
            int bottom = topPos + 68;
            for (int i = 0; i < heights.length; i++) {
                bottom -= heights[i];
                SmelteryRendering.fluid(graphics, menu.controller.liquids.fluids().get(i), leftPos + 8, bottom, 52, heights[i]);
            }
            var fuel = menu.controller.fuelDisplay();
            int height = fuel.capacity() <= 0 ? 0 : 52 * fuel.amount() / fuel.capacity();
            SmelteryRendering.fluid(graphics, fuel.fluid(), leftPos + 71, topPos + 68 - height, 12, height);
        }
        graphics.blit(BACKGROUND, leftPos + 8, topPos + 16, 176, 76, 52, 52);
    }

    private void drawSideInventory(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos - menu.sideWidth + 6;
        int y = topPos;
        int h = 14 + menu.visibleRows * 18;
        // Mantle's seven-pixel border and overlap join are copied from GuiGeneric.
        generic(graphics, x, y, 0, 0, 7, 7);
        repeat(graphics, x + 7, y, menu.sideWidth - 14, 7, 7, 0, 50, 7);
        generic(graphics, x + menu.sideWidth - 7, y, 7, 0, 7, 7);
        repeat(graphics, x, y + 7, 7, h - 14, 0, 7, 7, 50);
        repeat(graphics, x + menu.sideWidth - 7, y + 7, 7, h - 14, 21, 45, 7, 14);
        generic(graphics, x, y + h - 7, 0, 57, 7, 7);
        repeat(graphics, x + 7, y + h - 7, menu.sideWidth - 14, 7, 7, 57, 50, 7);
        generic(graphics, x + menu.sideWidth - 7, y + h - 7, 14, 47, 7, 7);
        for (int row = 0; row < menu.visibleRows; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = (menu.firstRow() + row) * 3 + col;
                graphics.blit(BACKGROUND, x + 7 + col * 22, y + 7 + row * 18,
                        slot < menu.count ? 0 : 22, 166, 22, 18);
            }
        }
        for (int i = menu.firstRow() * 3; i < Math.min(menu.count, (menu.firstRow() + menu.visibleRows) * 3); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.hasItem()) {
                Bar bar = bar(i);
                int height = 1 + Math.round(Math.clamp(bar.progress, 0, 1) * 15);
                graphics.blit(BACKGROUND, leftPos + slot.x - 4, topPos + slot.y + 16 - height,
                        bar.u, 150 + 16 - height, 3, height);
            }
        }
        if (menu.rows > menu.visibleRows) {
            int sx = x + 73;
            generic(graphics, sx, y + 7, 43, 7, 12, 1);
            repeat(graphics, sx, y + 8, 12, menu.visibleRows * 18 - 2, 43, 8, 12, 30);
            generic(graphics, sx, y + 6 + menu.visibleRows * 18, 43, 38, 12, 1);
            int travel = menu.visibleRows * 18 - 15;
            int thumbY = y + 7 + Math.round((float) menu.firstRow() / (menu.rows - menu.visibleRows) * travel);
            generic(graphics, sx + 1, thumbY, dragging ? 27 : mouseX >= sx && mouseX < sx + 12 && mouseY >= thumbY && mouseY < thumbY + 15 ? 17 : 7, 25, 10, 15);
        }
    }

    private static void generic(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(GENERIC, x, y, u, v, width, height, 64, 64);
    }

    private static void repeat(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int tileWidth, int tileHeight) {
        for (int dy = 0; dy < height; dy += tileHeight) {
            for (int dx = 0; dx < width; dx += tileWidth) {
                generic(graphics, x + dx, y + dy, u, v, Math.min(tileWidth, width - dx), Math.min(tileHeight, height - dy));
            }
        }
    }

    private Bar bar(int slot) {
        int required = menu.required(slot);
        if (required == 0) {
            return new Bar(185, 1, "gui.smeltery.progress.no_recipe");
        }
        float progress = (float) menu.heat(slot) / required;
        if (menu.fuel() == 0) {
            return new Bar(179, Math.clamp(progress, 0, 1), "gui.smeltery.progress.no_fuel");
        }
        if (menu.temperature() < required / 8) {
            return new Bar(179, 1, "gui.smeltery.progress.no_heat");
        }
        if (progress > 2) {
            return new Bar(182, 1, "gui.smeltery.progress.no_space");
        }
        return new Bar(176, Math.min(1, progress), "");
    }

    private int[] layerHeights() {
        return menu.controller == null ? new int[0] : SmelteryRendering.heights(menu.controller.liquids.fluids(),
                Math.max(menu.controller.liquids.amount(), menu.controller.liquids.capacity()), 52, com.gctn.tconstruct.Config.MIN_FLUID_HEIGHT.get());
    }

    private int hoveredFluid(int x, int y) {
        if (x < 8 || x >= 60 || y < 16 || y >= 68) {
            return -1;
        }
        int bottom = 68;
        int[] heights = layerHeights();
        for (int i = 0; i < heights.length; i++) {
            bottom -= heights[i];
            if (y >= bottom) {
                return i;
            }
        }
        return -1;
    }

    private List<Component> tooltip(int x, int y) {
        List<Component> text = new ArrayList<>();
        if (menu.controller == null) {
            return text;
        }
        if (x >= 8 && x < 60 && y >= 16 && y < 68) {
            int index = hoveredFluid(x, y);
            if (index >= 0) {
                FluidStack fluid = menu.controller.liquids.fluids().get(index);
                text.add(fluid.getHoverName());
                amount(text, fluid.getAmount(), fluid);
            } else {
                text.add(Component.translatable("gui.smeltery.capacity"));
                amount(text, menu.controller.liquids.capacity(), FluidStack.EMPTY);
                text.add(Component.translatable("gui.smeltery.capacity_available"));
                amount(text, menu.controller.liquids.capacity() - menu.controller.liquids.amount(), FluidStack.EMPTY);
                text.add(Component.translatable("gui.smeltery.capacity_used"));
                amount(text, menu.controller.liquids.amount(), FluidStack.EMPTY);
                if (!hasShiftDown()) {
                    text.add(Component.empty());
                    text.add(Component.translatable("tooltip.tank.holdShift"));
                }
            }
        } else if (x >= 71 && x < 83 && y >= 16 && y < 68) {
            var fuel = menu.controller.fuelDisplay();
            text.add(Component.translatable("gui.smeltery.fuel"));
            if (fuel.fluid().isEmpty()) {
                text.add(Component.translatable("gui.smeltery.fuel.empty"));
            } else if (SmelteryRecipes.FUELS.stream().anyMatch(recipe -> fuel.fluid().is(recipe.fluid()))) {
                text.add(fuel.fluid().getHoverName());
                amount(text, fuel.amount(), fuel.fluid());
                boolean celsius = com.gctn.tconstruct.Config.TEMPERATURE_CELSIUS.get();
                text.add(Component.translatable("gui.smeltery.fuel.heat", Component.translatable("gui.general.temperature." + (celsius ? "celsius" : "kelvin"),
                        celsius ? fuel.temperature() - 300 : fuel.temperature())));
            } else {
                text.add(Component.translatable("gui.smeltery.fuel.invalid", fuel.fluid().getHoverName()));
            }
        } else {
            for (int i = menu.firstRow() * 3; i < Math.min(menu.count, (menu.firstRow() + menu.visibleRows) * 3); i++) {
                Slot slot = menu.slots.get(i);
                if (slot.hasItem() && x >= slot.x - 4 && x < slot.x - 1 && y >= slot.y && y < slot.y + 16) {
                    Bar bar = bar(i);
                    if (!bar.tooltip.isEmpty()) {
                        text.add(Component.translatable(bar.tooltip));
                    }
                }
            }
        }
        return text;
    }

    private void amount(List<Component> text, int amount, FluidStack fluid) {
        FluidTankTooltip.amount(text, amount, fluid, hasShiftDown());
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0) {
            int fluid = hoveredFluid((int) x - leftPos, (int) y - topPos);
            if (fluid >= 0) {
                PacketDistributor.sendToServer(new SmelteryNetwork.SelectFluid(menu.containerId, fluid));
                return true;
            }
            int sx = leftPos - menu.sideWidth + 79;
            if (menu.rows > menu.visibleRows && x >= sx && x < sx + 12 && y >= topPos + 7 && y < topPos + 7 + menu.visibleRows * 18) {
                dragging = true;
                dragScroll(y);
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            dragScroll(y);
            return true;
        }
        return super.mouseDragged(x, y, button, deltaX, deltaY);
    }

    private void dragScroll(double y) {
        double ratio = (y - topPos - 7 - 7.5) / Math.max(1, menu.visibleRows * 18 - 15);
        menu.scrollTo((int) Math.round(ratio * (menu.rows - menu.visibleRows)));
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        dragging = false;
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= leftPos - menu.sideWidth + 6 && x < leftPos + 6 && y >= topPos && y < topPos + 14 + menu.visibleRows * 18) {
            menu.scrollTo(menu.firstRow() - (int) Math.signum(vertical));
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override
    protected boolean hasClickedOutside(double x, double y, int guiLeft, int guiTop, int button) {
        return super.hasClickedOutside(x, y, guiLeft, guiTop, button)
                && !(x >= leftPos - menu.sideWidth + 6 && x < leftPos + 6 && y >= topPos && y < topPos + 14 + menu.visibleRows * 18);
    }

    private record Bar(int u, float progress, String tooltip) {
    }
}
