/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's slime and magma pools. */
package com.gctn.tconstruct.world.feature;

import com.gctn.tconstruct.common.slime.SlimeModule;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SlimePoolFeature extends Feature<NoneFeatureConfiguration> {
    private final boolean magma;
    public SlimePoolFeature(boolean magma) { super(NoneFeatureConfiguration.CODEC); this.magma = magma; }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var liquid = magma ? Blocks.LAVA.defaultBlockState() : SmelteryFluids.get("greenslime").defaultFluidState().createLegacyBlock();
        var slime = SlimeModule.CONGEALED.get(magma ? "magma" : "green").get().defaultBlockState();
        return SlimeLakeShape.generate(context.level(), context.random(), context.origin(), liquid, slime, java.util.List.of(slime), true, magma ? EntityType.MAGMA_CUBE : EntityType.SLIME);
    }
}
