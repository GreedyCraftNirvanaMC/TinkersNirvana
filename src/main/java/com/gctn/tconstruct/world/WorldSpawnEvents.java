/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's WorldEvents. */
package com.gctn.tconstruct.world;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.slime.SlimeMobModule;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class WorldSpawnEvents {
    private WorldSpawnEvents() {}
    @SubscribeEvent private static void spawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER || !(event.getLevel() instanceof ServerLevel level)) { return; }
        var pos = event.getPos().below(3);
        boolean normal = false, magma = false;
        for (int x = -1; x <= 1; x++) { for (int z = -1; z <= 1; z++) {
            var chunk = level.getChunkSource().getChunkNow((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z);
            if (chunk == null || !chunk.hasData(WorldModule.ISLAND_BOUNDS)) { continue; }
            var data = chunk.getData(WorldModule.ISLAND_BOUNDS);
            normal |= data.normal().stream().anyMatch(box -> box.isInside(pos));
            magma |= data.magma().stream().anyMatch(box -> box.isInside(pos));
        } }
        if (!normal && !magma) { return; }
        for (var entry : java.util.List.copyOf(event.getSpawnerDataList())) { event.removeSpawnerData(entry); }
        if (normal) {
            event.addSpawnerData(new MobSpawnSettings.SpawnerData(SlimeMobModule.BLUE.get(), 20, 2, 4));
            event.addSpawnerData(new MobSpawnSettings.SpawnerData(SlimeMobModule.PURPLE.get(), 20, 2, 4));
        } else { event.addSpawnerData(new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 150, 4, 6)); }
    }
}
