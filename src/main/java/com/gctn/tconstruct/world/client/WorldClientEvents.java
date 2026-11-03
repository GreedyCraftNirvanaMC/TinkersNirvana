/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's SlimeColorizer and WorldClientProxy. */
package com.gctn.tconstruct.world.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.world.WorldModule;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class WorldClientEvents {
    private static final int[] FALLBACK = {0x2aec81, 0xa92dff, 0xd09800};
    private static volatile int[][] colors = new int[3][];
    private WorldClientEvents() {}
    private static int color(String foliage, BlockPos pos, boolean leaves) {
        int type = foliage.equals("purple") ? 1 : foliage.equals("orange") ? 2 : 0;
        int[] palette = colors[type];
        if (pos == null || palette == null) { return 0xff000000 | FALLBACK[type]; }
        int x = pos.getX() + (leaves ? 128 : 0), z = pos.getZ() + (leaves ? 128 : 0);
        float fx = Math.abs((256 - Math.abs((long) x) % 512) / 256f), fz = Math.abs((256 - Math.abs((long) z) % 512) / 256f);
        int index = (int) (Math.max(fx, fz) * 255) << 8 | (int) (Math.min(fx, fz) * 255);
        return palette[index];
    }
    @SubscribeEvent private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            WorldModule.GRASS.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped()));
            WorldModule.LEAVES.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped()));
            WorldModule.PLANTS.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
            WorldModule.SAPLINGS.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
            WorldModule.VINES.values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        });
    }
    @SubscribeEvent private static void blocks(RegisterColorHandlersEvent.Block event) {
        WorldModule.GRASS.forEach((key, block) -> event.register((state, level, pos, tint) -> color(key.split("_")[0], pos, false), block.get()));
        WorldModule.LEAVES.forEach((foliage, block) -> event.register((state, level, pos, tint) -> color(foliage, pos, true), block.get()));
        WorldModule.PLANTS.forEach((key, block) -> event.register((state, level, pos, tint) -> color(key.split("_")[0], pos, false), block.get()));
        WorldModule.VINES.forEach((key, block) -> event.register((state, level, pos, tint) -> color(key.substring(0, key.length() - 1), pos, true), block.get()));
    }
    @SubscribeEvent private static void items(RegisterColorHandlersEvent.Item event) {
        WorldModule.GRASS.forEach((key, block) -> event.register((stack, tint) -> color(key.split("_")[0], null, false), block.get()));
        WorldModule.LEAVES.forEach((foliage, block) -> event.register((stack, tint) -> color(foliage, null, true), block.get()));
        WorldModule.PLANTS.forEach((key, block) -> event.register((stack, tint) -> color(key.split("_")[0], null, false), block.get()));
        WorldModule.VINES.forEach((key, block) -> event.register((stack, tint) -> color(key.substring(0, key.length() - 1), null, true), block.get()));
    }
    @SubscribeEvent private static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<int[][]>() {
            @Override protected int[][] prepare(ResourceManager manager, ProfilerFiller profiler) {
                int[][] result = new int[3][];
                String[] files = {"slimegrasscolor", "purplegrasscolor", "orangegrasscolor"};
                for (int type = 0; type < files.length; type++) {
                    try (var input = manager.open(WorldModule.id("textures/colormap/" + files[type] + ".png")); var image = NativeImage.read(input)) {
                        if (image.getWidth() != 256 || image.getHeight() != 256) { throw new java.io.IOException("Expected 256x256 slime colormap"); }
                        result[type] = image.getPixelsRGBA();
                        for (int i = 0; i < result[type].length; i++) { int abgr = result[type][i]; result[type][i] = 0xff000000 | (abgr & 255) << 16 | abgr & 0xff00 | abgr >>> 16 & 255; }
                    } catch (java.io.IOException error) { TinkersNirvana.LOGGER.error("Unable to load slime colormap {}", files[type], error); }
                }
                return result;
            }
            @Override protected void apply(int[][] prepared, ResourceManager manager, ProfilerFiller profiler) { colors = prepared; }
        });
    }
}
