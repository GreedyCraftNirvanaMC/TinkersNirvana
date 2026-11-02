/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/** Renders the same tank frame and fluid when it is carried, including partially filled fuel tanks. */
public class TankItemRenderer extends BlockEntityWithoutLevelRenderer {
    public TankItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof BlockItem item)) {
            return;
        }
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(item.getBlock().defaultBlockState(), pose, buffers, light, overlay);
        var fluid = stack.getOrDefault(SmelteryModule.TANK_FLUID.get(), SimpleFluidContent.EMPTY).copy();
        if (!fluid.isEmpty()) {
            SmelteryRenderer.cuboid(pose, buffers, fluid, 0.005F, 0.005F, 0.005F, 0.995F,
                    0.005F + 0.99F * fluid.getAmount() / 4000.0F, 0.995F, light);
        }
    }
}
