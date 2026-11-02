/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;

/** Ports Antique's CastTexture into the modern atlas pipeline, including its shaded imprint edges. */
public record CastSpriteSource(ResourceLocation sprite, ResourceLocation mask, int offsetX, int offsetY) implements SpriteSource {
    public static final MapCodec<CastSpriteSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(CastSpriteSource::sprite),
            ResourceLocation.CODEC.fieldOf("mask").forGetter(CastSpriteSource::mask),
            Codec.INT.optionalFieldOf("offset_x", 0).forGetter(CastSpriteSource::offsetX),
            Codec.INT.optionalFieldOf("offset_y", 0).forGetter(CastSpriteSource::offsetY)).apply(instance, CastSpriteSource::new));
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    @Override
    public void run(ResourceManager manager, Output output) {
        output.add(sprite, loader -> {
            NativeImage cast = null;
            try (var baseStream = manager.getResourceOrThrow(ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/item/cast.png")).open();
                 var maskStream = manager.getResourceOrThrow(TEXTURE_ID_CONVERTER.idToFile(mask)).open();
                 NativeImage part = NativeImage.read(maskStream)) {
                cast = NativeImage.read(baseStream);
                for (int y = 0; y < cast.getHeight(); y++) {
                    for (int x = 0; x < cast.getWidth(); x++) {
                        int pixel = cast.getPixelRGBA(x, y);
                        if (pixel >>> 24 == 0) {
                            continue;
                        }
                        int sx = x * part.getWidth() / cast.getWidth();
                        int sy = y * part.getHeight() / cast.getHeight();
                        int px = sx - offsetX;
                        int py = sy - offsetY;
                        if (px < 0 || py < 0 || px >= part.getWidth() || py >= part.getHeight()) {
                            continue;
                        }
                        if (alpha(part, px, py) > 64 && sx != 0 && sx != part.getWidth() - 1 && sy != 0 && sy != part.getHeight() - 1) {
                            cast.setPixelRGBA(x, y, 0);
                            continue;
                        }
                        int count = 0;
                        if (alpha(part, px - 1, py) < 64) { count++; }
                        if (alpha(part, px, py + 1) < 64) { count++; }
                        if (alpha(part, px + 1, py) < 64) { count++; }
                        if (alpha(part, px, py - 1) < 64) { count -= 3; }
                        if (count != 0) {
                            float multiplier = count < 0 ? 0.8F : 1.1F;
                            int red = Math.min(255, (int) ((pixel & 255) * multiplier));
                            int green = Math.min(255, (int) ((pixel >> 8 & 255) * multiplier));
                            int blue = Math.min(255, (int) ((pixel >> 16 & 255) * multiplier));
                            cast.setPixelRGBA(x, y, 0xff000000 | blue << 16 | green << 8 | red);
                        }
                    }
                }
                return new SpriteContents(sprite, new FrameSize(cast.getWidth(), cast.getHeight()), cast, ResourceMetadata.EMPTY);
            } catch (Exception exception) {
                if (cast != null) {
                    cast.close();
                }
                TinkersNirvana.LOGGER.error("Unable to load cast sprite {}", sprite, exception);
                return null;
            }
        });
    }

    private static int alpha(NativeImage image, int x, int y) {
        return x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight() ? 0 : image.getPixelRGBA(x, y) >>> 24;
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
