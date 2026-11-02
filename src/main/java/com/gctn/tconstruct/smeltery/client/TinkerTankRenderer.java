/*
 * 根据 TinkersAntique 的 TinkerTankRenderer 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.TinkerTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

/** 储液铺满外壳内侧，控制器窗口显示当前最底层液体。 */
public class TinkerTankRenderer implements BlockEntityRenderer<TinkerTankBlockEntity> {
    public TinkerTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TinkerTankBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!entity.active() || entity.min() == null || entity.max() == null) {
            return;
        }
        var tank = entity.liquids;
        // 原版空储罐也以水色显示控制器已成形，这不是实际存入的液体。
        FluidStack front = tank.fluids().isEmpty() ? new FluidStack(Fluids.WATER, 1000) : tank.fluids().getFirst();
        Direction facing = entity.getBlockState().getValue(SmelteryBlock.FACING);
        float x1 = facing == Direction.WEST ? 0.002F : facing == Direction.EAST ? 0.996F : 0.005F;
        float x2 = facing == Direction.WEST ? 0.004F : facing == Direction.EAST ? 0.998F : 0.995F;
        float z1 = facing == Direction.NORTH ? 0.002F : facing == Direction.SOUTH ? 0.996F : 0.005F;
        float z2 = facing == Direction.NORTH ? 0.004F : facing == Direction.SOUTH ? 0.998F : 0.995F;
        SmelteryRenderer.cuboid(pose, buffers, front, x1, 0.005F, z1, x2, 0.695F, z2, light);

        BlockPos min = entity.min().subtract(entity.getBlockPos());
        BlockPos max = entity.max().subtract(entity.getBlockPos());
        int[] heights = SmelteryRendering.heights(tank.fluids(), tank.capacity(),
                (max.getY() - min.getY() + 1) * 1000 - 10, 100);
        float y = min.getY() + 0.005F;
        for (int i = 0; i < heights.length; i++) {
            float top = y + heights[i] / 1000.0F;
            SmelteryRenderer.cuboid(pose, buffers, tank.fluids().get(i), min.getX() - 0.9375F, y, min.getZ() - 0.9375F,
                    max.getX() + 1.9375F, top, max.getZ() + 1.9375F, light);
            y = top;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TinkerTankBlockEntity entity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TinkerTankBlockEntity entity) {
        return entity.min() == null || entity.max() == null ? new AABB(entity.getBlockPos())
                : new AABB(entity.min().getX() - 1, entity.min().getY() - 1, entity.min().getZ() - 1,
                        entity.max().getX() + 2, entity.max().getY() + 2, entity.max().getZ() + 2);
    }
}
