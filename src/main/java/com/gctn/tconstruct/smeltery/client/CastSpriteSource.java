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

/** 使用怀古的部件轮廓生成铸模孔型和木模板刻痕。 */
public record CastSpriteSource(ResourceLocation sprite, ResourceLocation mask, int offsetX, int offsetY,
                               boolean woodenPattern) implements SpriteSource {
    public static final MapCodec<CastSpriteSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(CastSpriteSource::sprite),
            ResourceLocation.CODEC.fieldOf("mask").forGetter(CastSpriteSource::mask),
            Codec.INT.optionalFieldOf("offset_x", 0).forGetter(CastSpriteSource::offsetX),
            Codec.INT.optionalFieldOf("offset_y", 0).forGetter(CastSpriteSource::offsetY),
            Codec.BOOL.optionalFieldOf("wooden_pattern", false).forGetter(CastSpriteSource::woodenPattern))
            .apply(instance, CastSpriteSource::new));
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    public CastSpriteSource(ResourceLocation sprite, ResourceLocation mask, int offsetX, int offsetY) {
        this(sprite, mask, offsetX, offsetY, false);
    }

    @Override
    public void run(ResourceManager manager, Output output) {
        output.add(sprite, loader -> {
            NativeImage cast = null;
            var baseTexture = ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID,
                    woodenPattern ? "item/toolsupplies/pattern" : "item/cast");
            try (var baseStream = manager.getResourceOrThrow(TEXTURE_ID_CONVERTER.idToFile(baseTexture)).open();
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
                        if (woodenPattern) {
                            if (sx >= part.getWidth() / 8 && sx <= part.getWidth() * 7 / 8
                                    && sy >= part.getHeight() / 8 && sy <= part.getHeight() * 7 / 8
                                    && alpha(part, px, py) >= 64) {
                                boolean edge = alpha(part, px - 1, py) < 64 || alpha(part, px + 1, py) < 64
                                        || alpha(part, px, py - 1) < 64 || alpha(part, px, py + 1) < 64;
                                cast.setPixelRGBA(x, y, shade(pixel, edge ? 0.6F : 0.5F));
                            }
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
                            cast.setPixelRGBA(x, y, shade(pixel, count < 0 ? 0.8F : 1.1F) | 0xff000000);
                        }
                    }
                }
                return new SpriteContents(sprite, new FrameSize(cast.getWidth(), cast.getHeight()), cast, ResourceMetadata.EMPTY);
            } catch (Exception exception) {
                if (cast != null) {
                    cast.close();
                }
                TinkersNirvana.LOGGER.error("Unable to generate sprite {}", sprite, exception);
                return null;
            }
        });
    }

    private static int shade(int pixel, float multiplier) {
        int red = Math.min(255, (int) ((pixel & 255) * multiplier));
        int green = Math.min(255, (int) ((pixel >> 8 & 255) * multiplier));
        int blue = Math.min(255, (int) ((pixel >> 16 & 255) * multiplier));
        return (pixel & 0xff000000) | blue << 16 | green << 8 | red;
    }

    private static int alpha(NativeImage image, int x, int y) {
        return x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight() ? 0 : image.getPixelRGBA(x, y) >>> 24;
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
