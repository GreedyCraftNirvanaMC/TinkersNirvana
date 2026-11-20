/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SearedFurnaceBlockEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class SmelteryRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final float TABLE_ITEM_Y = 0.970F;
    private static final float TABLE_ITEM_SCALE = 0.875F;
    public SmelteryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (entity instanceof SearedFurnaceBlockEntity furnace) {
            SmelterySounds.ensure(furnace);
        }
        if (entity instanceof SmelteryBlockEntity controller && controller.active() && controller.min() != null && controller.max() != null) {
            SmelterySounds.ensure(controller);
            BlockPos min = controller.min().subtract(controller.getBlockPos());
            BlockPos max = controller.max().subtract(controller.getBlockPos());
            int[] heights = SmelteryRendering.heights(controller.liquids.fluids(),
                    Math.max(controller.liquids.capacity(), controller.liquids.amount()), (max.getY() - min.getY() + 1) * 1000 - 10, 100);
            float y = min.getY() + 0.005F;
            for (int i = 0; i < heights.length; i++) {
                float top = y + heights[i] / 1000.0F;
                cuboid(pose, buffers, controller.liquids.fluids().get(i), min.getX() + 0.005F, y, min.getZ() + 0.005F,
                        max.getX() + 0.995F, top, max.getZ() + 0.995F, light);
                y = top;
            }
            int width = max.getX() - min.getX() + 1;
            int depth = max.getZ() - min.getZ() + 1;
            int renderLimit = com.gctn.tconstruct.Config.MAX_SMELTERY_ITEM_RENDERS.get();
            if (renderLimit >= 0 && controller.getContainerSize() > renderLimit) {
                return;
            }
            for (int i = 0; i < controller.getContainerSize(); i++) {
                ItemStack stack = controller.getItem(i);
                if (!stack.isEmpty()) {
                    item(pose, buffers, stack, min.getX() + i % width + 0.5F,
                            min.getY() + i / (width * depth) + 0.5F, min.getZ() + i / width % depth + 0.5F,
                            1.0F, Direction.NORTH, light);
                }
            }
        } else if (entity instanceof SmelteryComponentBlockEntity component) {
            renderComponent(component, partialTick, pose, buffers, light);
        }
    }

    private void renderComponent(SmelteryComponentBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        FluidStack fluid = entity.tank.getFluid();
        switch (entity.kind()) {
            case TANK -> {
                if (!fluid.isEmpty()) {
                    cuboid(pose, buffers, fluid, 0.005F, 0.005F, 0.005F, 0.995F,
                            0.005F + 0.99F * entity.tank.getFluidAmount() / entity.tank.getCapacity(), 0.995F, light);
                }
            }
            case TABLE, BASIN -> renderCasting(entity, partialTick, pose, buffers, fluid, light);
            case FAUCET -> {
                SmelterySounds.ensure(entity);
                if (entity.pouring && !entity.buffered.isEmpty()) {
                    Direction direction = entity.getBlockState().getValue(SmelteryBlock.FACING);
                    float bottom = flowBottom(entity);
                    if (direction == Direction.UP) {
                        cuboid(pose, buffers, entity.buffered, 0.375F, bottom, 0.375F, 0.625F, 1, 0.625F, light);
                    } else {
                        pose.pushPose();
                        pose.translate(0.5, 0, 0.5);
                        pose.mulPose(Axis.YP.rotationDegrees(-direction.toYRot() + 180));
                        pose.translate(-0.5, 0, -0.5);
                        cuboid(pose, buffers, entity.buffered, 0.375F, 0.375F, 0, 0.625F, 0.625F, 0.375F, light);
                        cuboid(pose, buffers, entity.buffered, 0.375F, bottom, 0.375F, 0.625F, 0.625F, 0.5F, light);
                        pose.popPose();
                    }
                }
            }
            case CHANNEL -> {
                if (!fluid.isEmpty()) {
                    cuboid(pose, buffers, fluid, 0.375F, 0.375F, 0.375F, 0.625F, 0.46875F, 0.625F, light);
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        if (entity.flow[side.ordinal()] > 0) {
                            float x1 = side == Direction.WEST ? 0 : 0.375F;
                            float z1 = side == Direction.NORTH ? 0 : 0.375F;
                            float x2 = side == Direction.EAST ? 1 : 0.625F;
                            float z2 = side == Direction.SOUTH ? 1 : 0.625F;
                            cuboid(pose, buffers, fluid, x1, 0.375F, z1, x2, 0.46875F, z2, light);
                        }
                    }
                    if (entity.flow[Direction.DOWN.ordinal()] > 0) {
                        cuboid(pose, buffers, fluid, 0.375F, flowBottom(entity), 0.375F, 0.625F, 0.46875F, 0.625F, light);
                    }
                }
            }
            default -> { }
        }
    }

    private static void renderCasting(SmelteryComponentBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers,
                                      FluidStack fluid, int light) {
        boolean basin = entity.kind() == SmelteryBlock.Kind.BASIN;
        ItemStack input = entity.inventory.getStackInSlot(0);
        ItemStack output = entity.inventory.getStackInSlot(1);
        var inputId = BuiltInRegistries.ITEM.getKey(input.getItem());
        boolean tableCast = !basin && inputId.getNamespace().equals(TinkersNirvana.MODID)
                && (inputId.getPath().startsWith("cast_") || inputId.getPath().startsWith("clay_cast_"));
        Direction facing = entity.getBlockState().getValue(SmelteryBlock.FACING);
        float itemY = basin ? 0.625F : TABLE_ITEM_Y;
        float scale = basin ? 0.751F : TABLE_ITEM_SCALE;
        float progress = entity.castingRenderProgress(partialTick);
        boolean maskedFluid = false;

        if (!fluid.isEmpty()) {
            float fill = entity.castingRenderFill(partialTick);
            // 对照怀古的冷却阶段，前段保持熔液颜色，最后三分之一才降低熔液不透明度。
            float alpha = 1.0F - Math.max(0.0F, progress - 2.0F / 3.0F);
            maskedFluid = tableCast && renderCastFluid(entity, input, pose, buffers, fluid, fill, facing, light, alpha);
            if (!maskedFluid) {
                float bottom = basin ? 0.25F : 0.9375F;
                float inset = basin ? 0.125F : 0.0625F;
                float top = tableCast ? Mth.lerp(fill, bottom, TABLE_ITEM_Y) : Mth.lerp(fill, bottom, 1.0F) + 0.001F;
                cuboid(pose, buffers, fluid, inset, bottom, inset, 1 - inset, top, 1 - inset, light, alpha);
            }
        }

        if (tableCast || output.isEmpty()) {
            item(pose, buffers, input, 0.5F, itemY, 0.5F, scale, facing, light);
        }
        // 可复用的铸模仍在输入槽中，成品稍微抬高以避免两个模型的表面重叠。
        float resultY = tableCast ? itemY + 0.003F : itemY;
        if (!output.isEmpty()) {
            item(pose, buffers, output, 0.5F, resultY, 0.5F, scale, facing, light);
        } else if (progress > 0) {
            ItemStack result = entity.castingResult();
            if (!maskedFluid || !renderFlatCastingPreview(pose, buffers, result, resultY, facing, light, progress)) {
                MultiBufferSource translucent = type -> new AlphaConsumer(buffers.getBuffer(CastingPreviewLayer.TYPE), progress);
                item(pose, translucent, result, 0.5F, resultY, 0.5F, scale, facing, light);
            }
        }
    }

    private static boolean renderFlatCastingPreview(PoseStack pose, MultiBufferSource buffers, ItemStack result,
                                                    float y, Direction facing, int light, float progress) {
        if (result.isEmpty() || result.getItem() instanceof BlockItem) {
            return false;
        }
        var minecraft = Minecraft.getInstance();
        var renderer = minecraft.getItemRenderer();
        var model = renderer.getModel(result, minecraft.level, null, 0);
        if (model.isGui3d() || model.isCustomRenderer()) {
            return false;
        }

        pose.pushPose();
        pose.translate(0.5F, y, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.scale(TABLE_ITEM_SCALE, TABLE_ITEM_SCALE, TABLE_ITEM_SCALE);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        model = model.applyTransform(ItemDisplayContext.NONE, pose, false);
        pose.translate(-0.5F, -0.5F, -0.5F);

        // 冷却时只混合顶面，避免物品的侧面和背面与液面交叉。
        boolean rendered = false;
        var random = RandomSource.create(42L);
        for (var pass : model.getRenderPasses(result, true)) {
            random.setSeed(42L);
            var top = pass.getQuads(null, null, random).stream()
                    .filter(quad -> quad.getDirection() == Direction.SOUTH).toList();
            if (!top.isEmpty()) {
                var vertices = new AlphaConsumer(buffers.getBuffer(CastingPreviewLayer.TYPE), progress);
                renderer.renderQuadList(pose, vertices, top, result, light, OverlayTexture.NO_OVERLAY);
                rendered = true;
            }
        }
        pose.popPose();
        return rendered;
    }

    private static boolean renderCastFluid(SmelteryComponentBlockEntity entity, ItemStack cast, PoseStack pose,
                                        MultiBufferSource buffers, FluidStack fluid, float fill, Direction facing,
                                        int light, float alpha) {
        // 铸模物品模型的粒子贴图就是其主贴图，透明像素描述孔型。
        var mask = Minecraft.getInstance().getItemRenderer().getModel(cast, entity.getLevel(), null, 0)
                .getParticleIcon().contents();
        if (mask.name().equals(MissingTextureAtlasSprite.getLocation())) {
            return false;
        }
        int width = mask.width();
        int height = mask.height();
        int frame = mask.getUniqueFrames().findFirst().orElse(0);
        TextureAtlasSprite sprite = SmelteryRendering.sprite(fluid);
        int color = fluidColor(fluid, alpha);
        if (fluid.getFluidType().getLightLevel(fluid) > 0) {
            light = LightTexture.FULL_BRIGHT;
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        // 生成的物品模型厚一个像素，将液面限制在铸模表面下方。
        float y = Mth.lerp(fill, 0.9375F, TABLE_ITEM_Y + TABLE_ITEM_SCALE / 32 - 0.001F);
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.scale(TABLE_ITEM_SCALE, 1, TABLE_ITEM_SCALE);
        pose.translate(-0.5, 0, -0.5);
        for (int row = 0; row < height; row++) {
            int start = 0;
            for (int x = 0; x < width; x++) {
                if (!mask.isTransparent(frame, x, row)) {
                    start = x + 1;
                    continue;
                }
                if (x + 1 == width || !mask.isTransparent(frame, x + 1, row)) {
                    float x1 = (float) start / width, x2 = (float) (x + 1) / width;
                    float z1 = (float) row / height, z2 = (float) (row + 1) / height;
                    fluidSurfaceVertex(vertices, pose, sprite, color, light, x1, y, z1);
                    fluidSurfaceVertex(vertices, pose, sprite, color, light, x1, y, z2);
                    fluidSurfaceVertex(vertices, pose, sprite, color, light, x2, y, z2);
                    fluidSurfaceVertex(vertices, pose, sprite, color, light, x2, y, z1);
                }
            }
        }
        pose.popPose();
        return true;
    }

    private static void fluidSurfaceVertex(VertexConsumer vertices, PoseStack pose, TextureAtlasSprite sprite,
                                           int color, int light, float x, float y, float z) {
        vertices.addVertex(pose.last(), x, y, z).setColor(color).setUv(sprite.getU(x), sprite.getV(z))
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(), 0, 1, 0);
    }

    private static float flowBottom(SmelteryComponentBlockEntity entity) {
        if (entity.getLevel().getBlockState(entity.getBlockPos().below()).getBlock() instanceof SmelteryBlock block) {
            return switch (block.kind) {
                case TABLE -> -0.125F;
                case BASIN -> -0.725F;
                case CHANNEL -> -0.53125F;
                case TANK -> -1.0F;
                default -> -0.9375F;
            };
        }
        return -0.9375F;
    }

    private static void item(PoseStack pose, MultiBufferSource buffers, ItemStack stack, float x, float y, float z,
                             float scale, Direction facing, int light) {
        if (stack.isEmpty()) {
            return;
        }
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.scale(scale, scale, scale);
        if (!(stack.getItem() instanceof BlockItem)) {
            pose.mulPose(Axis.XP.rotationDegrees(-90));
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY,
                pose, buffers, Minecraft.getInstance().level, 0);
        pose.popPose();
    }

    public static void cuboid(PoseStack pose, MultiBufferSource buffers, FluidStack fluid, float x1, float y1, float z1,
                               float x2, float y2, float z2, int light) {
        cuboid(pose, buffers, fluid, x1, y1, z1, x2, y2, z2, light, 1.0F);
    }

    private static void cuboid(PoseStack pose, MultiBufferSource buffers, FluidStack fluid, float x1, float y1, float z1,
                               float x2, float y2, float z2, int light, float alpha) {
        if (y2 <= y1) {
            return;
        }
        var sprite = SmelteryRendering.sprite(fluid);
        int color = fluidColor(fluid, alpha);
        if (fluid.getFluidType().getLightLevel(fluid) > 0) {
            light = LightTexture.FULL_BRIGHT;
        }
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        // Tile each face in block-sized pieces so wide smelteries retain the original fluid texture density.
        for (float x = x1; x < x2; x += 1) {
            for (float z = z1; z < z2; z += 1) {
                float xx = Math.min(x + 1, x2), zz = Math.min(z + 1, z2);
                quad(vertices, pose, color, light, sprite, Direction.UP, new float[][]{{x,y2,z},{x,y2,zz},{xx,y2,zz},{xx,y2,z}});
                quad(vertices, pose, color, light, sprite, Direction.DOWN, new float[][]{{x,y1,z},{xx,y1,z},{xx,y1,zz},{x,y1,zz}});
            }
            for (float y = y1; y < y2; y += 1) {
                float xx = Math.min(x + 1, x2), yy = Math.min(y + 1, y2);
                quad(vertices, pose, color, light, sprite, Direction.NORTH, new float[][]{{x,y,z1},{x,yy,z1},{xx,yy,z1},{xx,y,z1}});
                quad(vertices, pose, color, light, sprite, Direction.SOUTH, new float[][]{{xx,y,z2},{xx,yy,z2},{x,yy,z2},{x,y,z2}});
            }
        }
        for (float z = z1; z < z2; z += 1) {
            for (float y = y1; y < y2; y += 1) {
                float zz = Math.min(z + 1, z2), yy = Math.min(y + 1, y2);
                quad(vertices, pose, color, light, sprite, Direction.WEST, new float[][]{{x1,y,zz},{x1,yy,zz},{x1,yy,z},{x1,y,z}});
                quad(vertices, pose, color, light, sprite, Direction.EAST, new float[][]{{x2,y,z},{x2,yy,z},{x2,yy,zz},{x2,y,zz}});
            }
        }
    }

    private static int fluidColor(FluidStack fluid, float alpha) {
        int tint = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        return tint & 0x00ffffff | (int) ((tint >>> 24) * alpha) << 24;
    }

    private static final class CastingPreviewLayer extends RenderStateShard {
        // 切换到独立的半透明批次会先提交熔液。与怀古一样，预览参与深度测试，但只写颜色。
        private static final RenderType TYPE = RenderType.create("tconsnirvana_casting_preview", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

        private CastingPreviewLayer() {
            super("tconsnirvana_casting_preview", () -> {}, () -> {});
        }
    }

    private record AlphaConsumer(VertexConsumer target, float alpha) implements VertexConsumer {
        public VertexConsumer addVertex(float x, float y, float z) { target.addVertex(x, y, z); return this; }
        public VertexConsumer setColor(int red, int green, int blue, int alpha) { target.setColor(red, green, blue, (int) (alpha * this.alpha)); return this; }
        public VertexConsumer setUv(float u, float v) { target.setUv(u, v); return this; }
        public VertexConsumer setUv1(int u, int v) { target.setUv1(u, v); return this; }
        public VertexConsumer setUv2(int u, int v) { target.setUv2(u, v); return this; }
        public VertexConsumer setNormal(float x, float y, float z) { target.setNormal(x, y, z); return this; }
    }

    private static void quad(VertexConsumer vertices, PoseStack pose, int color, int light,
                             net.minecraft.client.renderer.texture.TextureAtlasSprite sprite, Direction normal, float[][] points) {
        float uMax = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * edgeLength(points[1], points[2]);
        float vMin = sprite.getV1() - (sprite.getV1() - sprite.getV0()) * edgeLength(points[0], points[1]);
        float[] u = {sprite.getU0(), sprite.getU0(), uMax, uMax};
        float[] v = {sprite.getV1(), vMin, vMin, sprite.getV1()};
        for (int i = 0; i < 4; i++) {
            vertices.addVertex(pose.last(), points[i][0], points[i][1], points[i][2]).setColor(color).setUv(u[i], v[i])
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose.last(), normal.getStepX(), normal.getStepY(), normal.getStepZ());
        }
    }

    private static float edgeLength(float[] first, float[] second) {
        float x = second[0] - first[0], y = second[1] - first[1], z = second[2] - first[2];
        return Math.min(1, (float) Math.sqrt(x * x + y * y + z * z));
    }

    @Override
    public boolean shouldRenderOffScreen(T entity) {
        // The controller can be behind a wall while the fluid surface remains visible.
        return entity instanceof SmelteryBlockEntity || entity instanceof SmelteryComponentBlockEntity component
                && component.kind() == SmelteryBlock.Kind.FAUCET;
    }
}
