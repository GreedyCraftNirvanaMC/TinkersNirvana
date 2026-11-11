/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's WorldEvents. */
package com.gctn.tconstruct.world;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.slime.AntiqueSlime;
import com.gctn.tconstruct.common.slime.SlimeMobModule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class WorldSpawnEvents {
    private static final int SURFACE_ATTEMPTS = 4;

    private WorldSpawnEvents() {}
    // NaturalSpawner checks that the selected entry is still in a second query's list.
    // SpawnerData compares by identity, so each query must return the same instances.
    private static final class IslandSpawns {
        private static final List<MobSpawnSettings.SpawnerData> NORMAL = List.of(
                new MobSpawnSettings.SpawnerData(SlimeMobModule.BLUE.get(), 20, 2, 4),
                new MobSpawnSettings.SpawnerData(SlimeMobModule.PURPLE.get(), 20, 2, 4));
        private static final List<MobSpawnSettings.SpawnerData> MAGMA = List.of(
                new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 150, 4, 6));
    }

    @SubscribeEvent
    private static void spawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Island island = islandAt(level, event.getPos());
        if (island != null) {
            List.copyOf(event.getSpawnerDataList()).forEach(event::removeSpawnerData);
            (island.magma ? IslandSpawns.MAGMA : IslandSpawns.NORMAL).forEach(event::addSpawnerData);
        }
    }

    @SubscribeEvent
    private static void limitSlimes(MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() != MobSpawnType.NATURAL || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof AntiqueSlime slime)) {
            return;
        }
        Island island = islandAt(level, slime.blockPosition());
        if (island != null && !island.magma && island.isFull(level)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    @SubscribeEvent
    private static void spawnNearIslandSurface(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.tickRateManager().runsNormally()
                || level.getGameTime() % Config.ISLAND_SPAWN_INTERVAL.get() != 0
                || level.getDifficulty() == Difficulty.PEACEFUL || !level.getServer().isSpawningMonsters()
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }

        Set<Long> checkedChunks = new HashSet<>();
        Set<Island> islands = new HashSet<>();
        for (var player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            ChunkPos.rangeClosed(player.chunkPosition(), NaturalSpawner.SPAWN_DISTANCE_CHUNK).forEach(pos -> {
                if (!checkedChunks.add(pos.toLong())) {
                    return;
                }
                var chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
                if (chunk != null && chunk.hasData(WorldModule.ISLAND_BOUNDS)) {
                    var bounds = chunk.getData(WorldModule.ISLAND_BOUNDS);
                    bounds.normal().forEach(box -> islands.add(new Island(box, false)));
                }
            });
        }
        for (Island island : islands) {
            for (int attempt = 0; attempt < SURFACE_ATTEMPTS && !island.isFull(level); attempt++) {
                island.trySpawn(level);
            }
        }
    }

    @Nullable
    private static Island islandAt(ServerLevel level, BlockPos spawnPos) {
        BlockPos pos = spawnPos.below(3);
        ChunkPos center = new ChunkPos(pos);
        Island magma = null;
        for (int x = center.x - 1; x <= center.x + 1; x++) {
            for (int z = center.z - 1; z <= center.z + 1; z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null || !chunk.hasData(WorldModule.ISLAND_BOUNDS)) {
                    continue;
                }
                var data = chunk.getData(WorldModule.ISLAND_BOUNDS);
                for (BoundingBox box : data.normal()) {
                    if (box.isInside(pos)) {
                        return new Island(box, false);
                    }
                }
                for (BoundingBox box : data.magma()) {
                    if (box.isInside(pos)) {
                        magma = new Island(box, true);
                        break;
                    }
                }
            }
        }
        return magma;
    }

    private static boolean isIslandSlime(EntityType<?> type) {
        return type == SlimeMobModule.BLUE.get() || type == SlimeMobModule.PURPLE.get();
    }

    private static boolean playerInRange(ServerLevel level, BlockPos pos) {
        return level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                NaturalSpawner.SPAWN_DISTANCE_BLOCK, false) != null;
    }

    private record Island(BoundingBox bounds, boolean magma) {
        private boolean isFull(ServerLevel level) {
            AABB area = AABB.of(bounds).inflate(3, 8, 3);
            return level.getEntitiesOfClass(AntiqueSlime.class, area, AntiqueSlime::isAlive)
                    .size() >= Config.ISLAND_SLIME_CAP.get();
        }

        private void trySpawn(ServerLevel level) {
            int x = bounds.minX() + level.random.nextInt(bounds.getXSpan());
            int z = bounds.minZ() + level.random.nextInt(bounds.getZSpan());
            BlockPos top = new BlockPos(x, bounds.maxY() + 1, z);
            var chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
            if (chunk == null || !level.isNaturalSpawningAllowed(top) || !playerInRange(level, top)) {
                return;
            }

            // Sample the island surface directly, then let NaturalSpawner check the group, distance and collisions.
            for (int depth = 0; depth <= 3; depth++) {
                BlockPos pos = top.below(depth);
                var soil = chunk.getBlockState(pos.below());
                boolean suitable = soil.is(AntiqueSlime.BLUE_SPAWNABLE) || soil.is(AntiqueSlime.PURPLE_SPAWNABLE);
                if (suitable && chunk.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                    NaturalSpawner.spawnCategoryForPosition(MobCategory.MONSTER, level, chunk, pos,
                            (type, candidate, ignored) -> isIslandSlime(type)
                                    && bounds.isInside(candidate.below(3)) && playerInRange(level, candidate),
                            (mob, ignored) -> {});
                    return;
                }
            }
        }
    }
}
