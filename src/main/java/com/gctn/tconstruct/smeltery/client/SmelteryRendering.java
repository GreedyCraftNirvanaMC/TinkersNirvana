/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public final class SmelteryRendering {
    private SmelteryRendering() {
    }

    /** Antique's minimum visible layer rule, shared by hit testing, the GUI, and world rendering. */
    public static int[] heights(List<FluidStack> fluids, int capacity, int height, int minimum) {
        int[] heights = new int[fluids.size()];
        int total = 0;
        int sum = 0;
        for (int i = 0; i < heights.length; i++) {
            total += fluids.get(i).getAmount();
            heights[i] = Math.max(minimum, (int) Math.ceil((float) fluids.get(i).getAmount() / Math.max(1, capacity) * height));
            sum += heights[i];
        }
        if (total < capacity) {
            height -= minimum;
        }
        while (sum > height && heights.length > 0) {
            int largest = 0;
            for (int i = 1; i < heights.length; i++) {
                if (heights[i] > heights[largest]) {
                    largest = i;
                }
            }
            if (heights[largest] == 0) {
                break;
            }
            heights[largest]--;
            sum--;
        }
        return heights;
    }

    public static TextureAtlasSprite sprite(FluidStack fluid) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid));
    }

    public static void fluid(GuiGraphics graphics, FluidStack fluid, int x, int y, int width, int height) {
        if (fluid.isEmpty() || width <= 0 || height <= 0) {
            return;
        }
        TextureAtlasSprite sprite = sprite(fluid);
        int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        graphics.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >>> 24) / 255.0F);
        // Scissor the last tile instead of stretching the fluid texture at the tank's edges.
        graphics.enableScissor(x, y, x + width, y + height);
        for (int col = 0; col < width; col += 16) {
            for (int row = 0; row < height; row += 16) {
                graphics.blit(x + col, y + row, 0, 16, 16, sprite);
            }
        }
        graphics.disableScissor();
        graphics.setColor(1, 1, 1, 1);
    }
}
