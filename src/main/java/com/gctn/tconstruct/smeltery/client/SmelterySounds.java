/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class SmelterySounds {
    private static final Map<BlockEntity, Loop> PLAYING = new HashMap<>();

    private SmelterySounds() {
    }

    private static boolean active(BlockEntity entity) {
        return !entity.isRemoved() && entity.getLevel() == Minecraft.getInstance().level
                && (entity instanceof SmelteryBlockEntity controller ? controller.heating()
                : entity instanceof SmelteryComponentBlockEntity faucet && faucet.pouring);
    }

    public static void ensure(BlockEntity entity) {
        if (active(entity) && !PLAYING.containsKey(entity)) {
            boolean controller = entity instanceof SmelteryBlockEntity;
            Loop loop = new Loop(entity, controller ? SmelteryModule.LOOP.get() : SmelteryModule.POUR.get(), controller ? 0.8F : 1.0F);
            PLAYING.put(entity, loop);
            Minecraft.getInstance().getSoundManager().play(loop);
        }
    }

    @SubscribeEvent
    private static void tick(ClientTickEvent.Post event) {
        PLAYING.entrySet().removeIf(entry -> entry.getValue().isStopped() || entry.getKey().isRemoved()
                || entry.getKey().getLevel() != Minecraft.getInstance().level);
    }

    private static final class Loop extends AbstractTickableSoundInstance {
        private final BlockEntity entity;

        private Loop(BlockEntity entity, SoundEvent sound, float volume) {
            super(sound, SoundSource.BLOCKS, RandomSource.create());
            this.entity = entity;
            this.volume = volume;
            looping = true;
            x = entity.getBlockPos().getX() + 0.5;
            y = entity.getBlockPos().getY() + 0.5;
            z = entity.getBlockPos().getZ() + 0.5;
        }

        @Override
        public void tick() {
            // Antique fades by 0.02 per tick when heating/pouring ends.
            if (!active(entity)) {
                volume = Math.max(0, volume - 0.02F);
                if (volume == 0) {
                    stop();
                }
            }
        }
    }
}
