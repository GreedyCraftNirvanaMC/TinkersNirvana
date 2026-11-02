/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SearedFurnaceBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class SmelteryRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
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
            renderComponent(component, pose, buffers, light);
        }
    }

    private void renderComponent(SmelteryComponentBlockEntity entity, PoseStack pose, MultiBufferSource buffers, int light) {
        FluidStack fluid = entity.tank.getFluid();
        switch (entity.kind()) {
            case TANK -> {
                if (!fluid.isEmpty()) {
                    cuboid(pose, buffers, fluid, 0.005F, 0.005F, 0.005F, 0.995F,
                            0.005F + 0.99F * entity.tank.getFluidAmount() / entity.tank.getCapacity(), 0.995F, light);
                }
            }
            case TABLE, BASIN -> {
                boolean basin = entity.kind() == SmelteryBlock.Kind.BASIN;
                float bottom = basin ? 0.25F : 0.9375F;
                float progress = entity.progress();
                if (!fluid.isEmpty()) {
                    float height = bottom + (1.0F - bottom) * entity.tank.getFluidAmount() / Math.max(1, entity.tank.getCapacity());
                    cuboid(pose, buffers, fluid, basin ? 0.125F : 0.0625F, bottom, basin ? 0.125F : 0.0625F,
                            basin ? 0.875F : 0.9375F, height + 0.001F, basin ? 0.875F : 0.9375F, light, 1.0F - progress / 3.0F);
                }
                ItemStack stack = entity.inventory.getStackInSlot(1).isEmpty() ? entity.inventory.getStackInSlot(0) : entity.inventory.getStackInSlot(1);
                item(pose, buffers, stack, 0.5F, basin ? 0.625F : 0.970F, 0.5F, basin ? 0.751F : 0.875F,
                        entity.getBlockState().getValue(SmelteryBlock.FACING), light);
                if (progress > 0 && entity.inventory.getStackInSlot(1).isEmpty()) {
                    // The finished item becomes visible gradually while the molten surface cools.
                    MultiBufferSource translucent = type -> new AlphaConsumer(buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS)), progress);
                    item(pose, translucent, entity.castingResult(), 0.5F, basin ? 0.625F : 0.970F, 0.5F,
                            basin ? 0.751F : 0.875F, entity.getBlockState().getValue(SmelteryBlock.FACING), light);
                }
            }
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
        int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        color = color & 0x00ffffff | (int) ((color >>> 24) * alpha) << 24;
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
