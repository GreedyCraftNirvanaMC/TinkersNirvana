/* SPDX-License-Identifier: LGPL-3.0-only; Antique's colored fluid conveyors. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.block.SlimeChannelBlock;
import com.gctn.tconstruct.init.BlockRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class SlimeChannels {
    public static final Map<String, DeferredBlock<SlimeChannelBlock>> BLOCKS = new LinkedHashMap<>();
    static {
        String[] colors = {"green", "blue", "purple", "blood", "magma", "pink"};
        int[] tints = {0x01cd4e, 0x01cbcd, 0xaf4cf6, 0xb50101, 0xff970d, 0x90708b};
        for (int i = 0; i < colors.length; i++) {
            int tint = tints[i];
            BLOCKS.put(colors[i], BlockRegister.registerBlockWithItem("slime_channel_" + colors[i], () -> new SlimeChannelBlock(tint,
                    BlockBehaviour.Properties.of().strength(.3F).sound(SoundType.SLIME_BLOCK).noCollission().noOcclusion())));
        }
    }
    private SlimeChannels() {}
    public static void init() {}
    /** Called after native fluid detection. Never loads chunks and preserves real water heights. */
    public static double waterHeight(Entity entity) {
        if (entity.touchingUnloadedChunk()) { return 0; }
        AABB box = entity.getBoundingBox().deflate(.001);
        double height = 0;
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            var state = entity.level().getBlockState(pos);
            if (state.getBlock() instanceof SlimeChannelBlock) { height = Math.max(height, SlimeChannelBlock.geometry(state).height(box.move(-pos.getX(), -pos.getY(), -pos.getZ()))); }
        }
        return height;
    }
    public static boolean wetEyes(Entity entity) {
        Vec3 eyes = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ()); BlockPos pos = BlockPos.containing(eyes);
        if (!entity.level().hasChunkAt(pos)) { return false; }
        var state = entity.level().getBlockState(pos);
        return state.getBlock() instanceof SlimeChannelBlock && SlimeChannelBlock.geometry(state).contains(eyes.subtract(Vec3.atLowerCornerOf(pos)));
    }
    @SubscribeEvent private static void expire(ItemExpireEvent event) {
        if (event.getEntity().level().getBlockState(event.getEntity().blockPosition()).getBlock() instanceof SlimeChannelBlock) {
            // Reset age instead of repeatedly extending the short-serialized lifespan past its limit.
            event.getEntity().setExtendedLifetime();
        }
    }
}
