/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's slime and magma island generators. */
package com.gctn.tconstruct.world.feature;

import com.gctn.tconstruct.common.slime.SlimeModule;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.world.IslandBounds;
import com.gctn.tconstruct.world.WorldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Optional;

public class SlimeIslandFeature extends Feature<NoneFeatureConfiguration> {
    private final boolean magma;
    public SlimeIslandFeature(boolean magma) { super(NoneFeatureConfiguration.CODEC); this.magma = magma; }
    private static BlockState congealed(String color) { return SlimeModule.CONGEALED.get(color).get().defaultBlockState(); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level(); var random = context.random();
        int x = (context.origin().getX() & ~15) + 4 + random.nextInt(8), z = (context.origin().getZ() & ~15) + 4 + random.nextInt(8);
        int surface = magma ? context.chunkGenerator().getSeaLevel() : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 61 + random.nextInt(50);
        if (magma) {
            BlockPos sea = new BlockPos(x, surface - 1, z);
            if (!level.getFluidState(sea).is(net.minecraft.tags.FluidTags.LAVA)) { return false; }
            for (Direction direction : Direction.Plane.HORIZONTAL) { if (!level.getFluidState(sea.relative(direction)).is(net.minecraft.tags.FluidTags.LAVA)) { return false; } }
        }
        String dirt = "magma", foliage = "orange", plants = "orange";
        if (!magma) {
            int variant = random.nextInt(10);
            dirt = variant <= 1 ? "purple" : variant < 6 ? "green" : "blue";
            foliage = variant <= 1 ? "purple" : "blue";
            plants = variant <= 1 ? "blue" : "purple";
        }
        int width = 20 + random.nextInt(13), depth = 20 + random.nextInt(13), height = 11 + random.nextInt(3);
        BlockPos start = new BlockPos(x - width / 2, surface - height, z - depth / 2);
        if (start.getY() <= level.getMinBuildHeight() || surface + 10 >= level.getMaxBuildHeight()) { return false; }
        BlockPos firstTree = new BlockPos(x - width / 3, surface + 1, z);
        // Reserve an unobstructed canopy before writing any island blocks.
        for (BlockPos pos : BlockPos.betweenClosed(firstTree.offset(-4, 0, -4), firstTree.offset(4, 8, 4))) {
            if (!level.ensureCanWrite(pos) || level.getBlockEntity(pos) != null) { return false; }
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || !(state.isAir() || state.canBeReplaced())) { return false; }
        }
        // Refuse occupied inventories and out-of-region writes before carving the island.
        for (BlockPos pos : BlockPos.betweenClosed(start, start.offset(width, height + 2, depth))) {
            if (!level.ensureCanWrite(pos) || level.getBlockEntity(pos) != null) { return false; }
        }
        BlockState soil = WorldModule.DIRT.get(dirt).get().defaultBlockState();
        BlockState grass = WorldModule.GRASS.get(foliage + "_" + dirt).get().defaultBlockState();
        BlockState empty = magma ? Blocks.LAVA.defaultBlockState() : Blocks.AIR.defaultBlockState();
        for (int xx = 0; xx <= width; xx++) { for (int zz = 0; zz <= depth; zz++) {
            double dx = xx / (double) width - .5, dz = zz / (double) depth - .5;
            if (dx * dx + dz * dz < .25) { for (int y = 0; y <= height; y++) { level.setBlock(start.offset(xx, y, zz), soil, 2); } }
        } }
        for (int xx = 0; xx <= width; xx++) { for (int zz = 0; zz <= depth; zz++) { for (int y = 0; y <= 8; y++) {
            for (BlockPos pos : new BlockPos[]{start.offset(xx, 8 - y, zz), start.offset(width - xx, 8 - y, depth - zz)}) {
                if (level.getBlockState(pos) == soil && (level.getBlockState(pos.offset(-1, 1, 0)) != soil || level.getBlockState(pos.offset(1, 1, 0)) != soil
                        || level.getBlockState(pos.offset(0, 1, -1)) != soil || level.getBlockState(pos.offset(-1, 1, 1)) != soil || random.nextInt(100) <= 1)) { level.setBlock(pos, empty, 2); }
            }
        } } }
        for (int xx = 0; xx <= width; xx++) { for (int zz = 0; zz <= depth; zz++) { for (int y = 0; y <= 2; y++) {
            for (BlockPos pos : new BlockPos[]{start.offset(xx, y + height, zz), start.offset(width - xx, y + height, depth - zz)}) {
                boolean edge = false;
                for (Direction direction : Direction.Plane.HORIZONTAL) { if (level.getBlockState(pos.below().relative(direction)) != soil) { edge = true; break; } }
                if (edge) { level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2); }
            }
        } } }
        for (int xx = 0; xx <= width; xx++) { for (int zz = 0; zz <= depth; zz++) {
            for (int y = 0; y <= height; y++) {
                BlockPos pos = start.offset(xx, height - y, zz);
                if (level.getBlockState(pos) == soil && level.isEmptyBlock(pos.above())) { level.setBlock(pos, grass, 2); break; }
            }
        } }
        BlockState liquid = magma ? Blocks.LAVA.defaultBlockState() : SmelteryFluids.get(dirt.equals("purple") ? "purpleslime" : "blueslime").defaultFluidState().createLegacyBlock();
        List<BlockState> rim = magma ? List.of(congealed("magma"), congealed("magma"), congealed("magma"), congealed("magma"), congealed("magma"), congealed("blood"))
                : dirt.equals("purple") ? List.of(congealed("purple")) : List.of(congealed("green"), congealed("blue"));
        SlimeLakeShape.generate(level, random, new BlockPos(x, surface, z), liquid, congealed(dirt), rim, false, null);
        String treeFoliage = plants;
        var tree = new SlimeTreeFeature.Configuration(5, 4, congealed(magma ? "magma" : "green"), WorldModule.LEAVES.get(treeFoliage).get().defaultBlockState(),
                magma ? Optional.empty() : Optional.of(WorldModule.VINES.get(treeFoliage + "1").get().defaultBlockState()), true);
        // The lake may replace the reserved tree's soil, so restore its supporting grass.
        level.setBlock(firstTree.below(), grass, 2);
        var firstTreeConfig = new SlimeTreeFeature.Configuration(tree.minHeight(), tree.heightRange(),
                tree.trunk(), tree.leaves(), tree.vine(), false);
        SlimeTreeFeature.generate(level, random, firstTree, firstTreeConfig);
        int targetTrees = 1 + random.nextInt(3);
        int trees = 1;
        java.util.List<BlockPos> trunks = new java.util.ArrayList<>();
        trunks.add(firstTree);
        for (int attempt = 0; attempt < 32 && trees < targetTrees; attempt++) {
            BlockPos candidate = start.offset(random.nextInt(width), height, random.nextInt(depth));
            if (trunks.stream().anyMatch(pos -> Math.abs(pos.getX() - candidate.getX()) < 5
                    && Math.abs(pos.getZ() - candidate.getZ()) < 5)) { continue; }
            if (SlimeTreeFeature.generate(level, random, candidate, tree)) {
                trees++;
                trunks.add(candidate);
            }
        }
        plants(level, random, start, width, depth, height, plants);
        if (!magma) {
            BlockState vine = WorldModule.VINES.get(foliage + "0").get().defaultBlockState();
            for (int i = 0; i < 30; i++) { vines(level, random, start.offset(-1 + random.nextInt(width + 2), 0, -1 + random.nextInt(depth + 2)), height, foliage, vine); }
        }
        IslandBounds.add(level, new BlockPos(x, surface, z), new BoundingBox(start.getX(), start.getY(), start.getZ(), start.getX() + width, surface, start.getZ() + depth), magma);
        return true;
    }
    private static void plants(WorldGenLevel level, RandomSource random, BlockPos start, int width, int depth, int height, String foliage) {
        for (int i = 0; i < 128; i++) {
            BlockPos pos = start.offset(random.nextInt(width), height + 1, random.nextInt(depth));
            for (int y = 0; y < 4 && level.isEmptyBlock(pos.below()); y++) { pos = pos.below(); }
            BlockState plant = WorldModule.PLANTS.get(foliage + (i % 2 == 0 ? "_fern" : "_tall_grass")).get().defaultBlockState();
            if (level.isEmptyBlock(pos) && plant.canSurvive(level, pos)) { level.setBlock(pos, plant, 2); }
        }
    }
    private static void vines(WorldGenLevel level, RandomSource random, BlockPos below, int height, String foliage, BlockState vine) {
        BlockPos candidate = null; BlockState selected = vine;
        for (int y = 0; y < height; y++) {
            BlockPos pos = below.above(y);
            if (!level.isEmptyBlock(pos)) { continue; }
            BlockState state = vine; boolean attached = false;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                boolean support = VineBlock.isAcceptableNeighbour(level, pos.relative(direction), direction);
                state = state.setValue(VineBlock.getPropertyForFace(direction), support); attached |= support;
            }
            if (attached && (candidate == null || random.nextInt(10) == 0)) { candidate = pos; selected = state; }
        }
        if (candidate == null) { return; }
        level.setBlock(candidate, selected, 2);
        int length = 1 + random.nextInt(8), stage = 0, sameStage = 1;
        for (int i = 0; i < length && stage < 2; i++) {
            candidate = candidate.below(); if (!level.isEmptyBlock(candidate)) { break; }
            boolean floating = true;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (selected.getValue(VineBlock.getPropertyForFace(direction)) && VineBlock.isAcceptableNeighbour(level, candidate.above().relative(direction), direction)) { floating = false; break; }
            }
            if (floating && (sameStage > 2 || random.nextBoolean())) {
                stage++; sameStage = 0;
                BlockState next = WorldModule.VINES.get(foliage + stage).get().defaultBlockState();
                for (Direction direction : Direction.Plane.HORIZONTAL) { var property = VineBlock.getPropertyForFace(direction); next = next.setValue(property, selected.getValue(property)); }
                selected = next;
            }
            sameStage++; level.setBlock(candidate, selected, 2);
        }
    }
}
