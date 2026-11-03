/* SPDX-License-Identifier: LGPL-3.0-only; Antique's generation switches, dimensions, and rarity. */
package com.gctn.tconstruct.world.feature;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.world.WorldModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class WorldgenFilter extends PlacementFilter {
    public static final MapCodec<WorldgenFilter> CODEC = com.mojang.serialization.Codec.STRING.fieldOf("setting").xmap(WorldgenFilter::new, filter -> filter.setting);
    private final String setting;
    public WorldgenFilter(String setting) {
        if (!java.util.Set.of("slime_island", "magma_island", "slime_pool", "slime_pool_humid", "magma_pool").contains(setting)) { throw new IllegalArgumentException("Unknown Antique generation setting: " + setting); }
        this.setting = setting;
    }
    @Override protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        boolean island = setting.endsWith("island"), magma = setting.startsWith("magma");
        var level = context.getLevel().getLevel();
        if (!(island ? Config.GEN_SLIME_ISLANDS.get() : Config.GEN_SLIME_POOLS.get())) { return false; }
        if (context.generator() instanceof FlatLevelSource && (!island || !Config.ISLANDS_SUPERFLAT.get())) { return false; }
        if (magma) {
            if (!level.dimensionType().ultraWarm()) { return false; }
        } else {
            var dimensions = island ? Config.ISLAND_DIMENSIONS.get() : Config.POOL_DIMENSIONS.get();
            boolean blacklist = island ? Config.ISLAND_DIMENSIONS_BLACKLIST.get() : Config.POOL_DIMENSIONS_BLACKLIST.get();
            if (dimensions.contains(level.dimension().location().toString()) == blacklist) { return false; }
            boolean surfaceOnly = island ? Config.ISLANDS_SURFACE_ONLY.get() : Config.POOLS_SURFACE_ONLY.get();
            if (surfaceOnly && (!level.dimensionType().hasSkyLight() || level.dimensionType().hasCeiling())) { return false; }
        }
        int rate = island ? (magma ? Config.MAGMA_ISLAND_RATE.get() : Config.SLIME_ISLAND_RATE.get()) : (magma ? Config.MAGMA_POOL_RATE.get() : Config.SLIME_POOL_RATE.get());
        if (setting.equals("slime_pool_humid")) {
            var climate = context.getLevel().getBiome(pos).value().getModifiedClimateSettings();
            if (climate.temperature() < .8f || climate.downfall() < .9f) { return false; }
            rate = rate / 4 + 1;
        }
        return random.nextInt(rate) == 0;
    }
    @Override public PlacementModifierType<?> type() { return WorldModule.GENERATION_FILTER.get(); }
}
