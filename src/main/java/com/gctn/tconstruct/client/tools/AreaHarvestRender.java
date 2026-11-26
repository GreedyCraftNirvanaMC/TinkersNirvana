/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.client.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.mixin.MiningOutlineInvoker;
import com.gctn.tconstruct.mixin.MiningTargetAccessor;
import com.gctn.tconstruct.tools.DualToolHarvest;
import com.gctn.tconstruct.tools.ToolHarvest;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class AreaHarvestRender {
    private AreaHarvestRender() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void outlines(RenderHighlightEvent.Block event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator() || event.getCamera().getEntity() != player) { return; }
        BlockHitResult hit = event.getTarget();
        var camera = event.getCamera().getPosition();
        var outlines = (MiningOutlineInvoker) event.getLevelRenderer();
        for (BlockPos pos : targets(player, hit.getBlockPos(), hit, false)) {
            if (player.level().getWorldBorder().isWithinBounds(pos)) {
                outlines.tconstruct$renderMiningOutline(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()),
                        player, camera.x, camera.y, camera.z, pos, player.level().getBlockState(pos));
            }
        }
    }

    @SubscribeEvent
    public static void cracks(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) { return; }
        Minecraft minecraft = Minecraft.getInstance();
        var gameMode = minecraft.gameMode;
        var player = minecraft.player;
        if (player == null || gameMode == null || !gameMode.isDestroying() || event.getCamera().getEntity() != player) { return; }
        int stage = gameMode.getDestroyStage();
        if (stage < 0 || stage >= ModelBakery.DESTROY_TYPES.size()) { return; }
        BlockPos origin = ((MiningTargetAccessor) gameMode).tconstruct$destroyBlockPos();
        BlockHitResult hit = minecraft.hitResult instanceof BlockHitResult block ? block : null;
        var camera = event.getCamera().getPosition();
        var pose = event.getPoseStack();
        var level = minecraft.level;
        for (BlockPos pos : targets(player, origin, hit, true)) {
            var state = level.getBlockState(pos);
            if (state.hasBlockEntity() || state.getRenderShape() != RenderShape.MODEL) { continue; }
            pose.pushPose();
            pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            // 复用原版裂纹缓冲区，只在本地渲染额外方块，不发送额外破坏进度。
            var buffer = minecraft.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(stage));
            var decal = new SheetedDecalTextureGenerator(buffer, pose.last(), 1);
            minecraft.getBlockRenderer().renderBreakingTexture(state, pos, level, pose, decal, level.getModelData(pos));
            pose.popPose();
        }
    }

    private static List<BlockPos> targets(LocalPlayer player, BlockPos origin, BlockHitResult hit, boolean cracking) {
        var state = player.level().getBlockState(origin);
        var tool = DualToolHarvest.shouldUseOffhand(player, state) ? player.getOffhandItem() : player.getMainHandItem();
        if (cracking && !ToolHarvest.minesArea(tool)) { return List.of(); }
        return ToolHarvest.extraBlocks(player, tool, origin, state, hit);
    }
}
