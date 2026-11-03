/* SPDX-License-Identifier: LGPL-3.0-only; Antique's configurable ore and pool distributions. */
package com.gctn.tconstruct.world.feature;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.world.WorldModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Set;
import java.util.stream.Stream;

public class AntiqueHeightPlacement extends PlacementModifier {
    public static final MapCodec<AntiqueHeightPlacement> CODEC = com.mojang.serialization.Codec.STRING.fieldOf("setting").xmap(AntiqueHeightPlacement::new, filter -> filter.setting);
    private final String setting;
    public AntiqueHeightPlacement(String setting) {
        if (!Set.of("cobalt", "ardite", "copper", "tin", "aluminum", "slime_pool", "slime_pool_humid", "magma_pool").contains(setting)) { throw new IllegalArgumentException("Unknown Antique height setting: " + setting); }
        this.setting = setting;
    }
    @Override public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        Stream.Builder<BlockPos> result = Stream.builder();
        if (setting.endsWith("pool") || setting.equals("slime_pool_humid")) {
            int max = setting.equals("magma_pool") ? 128 : Config.SLIME_POOL_HEIGHT.get() * (setting.endsWith("humid") ? 3 : 1);
            result.add(new BlockPos(origin.getX() + 8 + random.nextInt(8), random.nextInt(max), origin.getZ() + 8 + random.nextInt(8)));
            return result.build();
        }
        boolean enabled = switch (setting) {
            case "cobalt" -> Config.GEN_COBALT.get(); case "ardite" -> Config.GEN_ARDITE.get(); case "copper" -> Config.GEN_COPPER.get();
            case "tin" -> Config.GEN_TIN.get(); default -> Config.GEN_ALUMINUM.get();
        };
        if (!enabled) { return Stream.empty(); }
        int count = switch (setting) {
            case "cobalt" -> Config.COBALT_RATE.get(); case "ardite" -> Config.ARDITE_RATE.get(); case "copper" -> Config.COPPER_RATE.get();
            case "tin" -> Config.TIN_RATE.get(); default -> Config.ALUMINUM_RATE.get();
        };
        if (setting.equals("cobalt") || setting.equals("ardite")) {
            if (!context.getLevel().dimensionType().ultraWarm()) { return Stream.empty(); }
            for (int i = 0; i < count; i += 2) {
                result.add(origin.offset(random.nextInt(16), 32 + random.nextInt(64) - origin.getY(), random.nextInt(16)));
                result.add(origin.offset(random.nextInt(16), random.nextInt(128) - origin.getY(), random.nextInt(16)));
            }
        } else {
            if (!context.getLevel().dimensionType().hasSkyLight()) { return Stream.empty(); }
            int min = setting.equals("copper") ? Config.COPPER_MIN_HEIGHT.get() : setting.equals("tin") ? Config.TIN_MIN_HEIGHT.get() : Config.ALUMINUM_MIN_HEIGHT.get();
            int max = setting.equals("copper") ? Config.COPPER_MAX_HEIGHT.get() : setting.equals("tin") ? Config.TIN_MAX_HEIGHT.get() : Config.ALUMINUM_MAX_HEIGHT.get();
            if (max <= min) { return Stream.empty(); }
            for (int i = 0; i < count; i++) { result.add(new BlockPos(origin.getX() + random.nextInt(16), min + random.nextInt(max - min), origin.getZ() + random.nextInt(16))); }
        }
        return result.build();
    }
    @Override public PlacementModifierType<?> type() { return WorldModule.HEIGHT_PLACEMENT.get(); }
}
