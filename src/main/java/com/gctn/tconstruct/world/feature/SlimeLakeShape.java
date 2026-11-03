/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's slime lake and pool generators. */
package com.gctn.tconstruct.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Shared 16x16x8 union of ellipsoids, with validation before any block changes. */
public final class SlimeLakeShape {
    private SlimeLakeShape() {}
    private static int index(int x, int y, int z) { return (x * 16 + z) * 8 + y; }
    private static boolean edge(boolean[] shape, int x, int y, int z) {
        return !shape[index(x, y, z)] && (x < 15 && shape[index(x + 1, y, z)] || x > 0 && shape[index(x - 1, y, z)]
                || z < 15 && shape[index(x, y, z + 1)] || z > 0 && shape[index(x, y, z - 1)]
                || y < 7 && shape[index(x, y + 1, z)] || y > 0 && shape[index(x, y - 1, z)]);
    }
    private static boolean[] shape(RandomSource random) {
        boolean[] shape = new boolean[2048];
        int count = random.nextInt(4) + 4;
        for (int i = 0; i < count; i++) {
            double sx = random.nextDouble() * 6 + 3, sy = random.nextDouble() * 4 + 2, sz = random.nextDouble() * 6 + 3;
            double cx = random.nextDouble() * (14 - sx) + 1 + sx / 2, cy = random.nextDouble() * (4 - sy) + 2 + sy / 2, cz = random.nextDouble() * (14 - sz) + 1 + sz / 2;
            for (int x = 1; x < 15; x++) { for (int z = 1; z < 15; z++) { for (int y = 1; y < 7; y++) {
                double dx = (x - cx) / (sx / 2), dy = (y - cy) / (sy / 2), dz = (z - cz) / (sz / 2);
                if (dx * dx + dy * dy + dz * dz < 1) { shape[index(x, y, z)] = true; }
            } } }
        }
        return shape;
    }
    public static boolean generate(WorldGenLevel level, RandomSource random, BlockPos origin, BlockState liquid, BlockState bottom, List<BlockState> rim, boolean pool, EntityType<? extends Slime> spawns) {
        BlockPos ground = pool ? origin.offset(-8, 0, -8) : origin;
        while (ground.getY() > level.getMinBuildHeight() + 5 && level.isEmptyBlock(ground)) { ground = ground.below(); }
        if (ground.getY() <= level.getMinBuildHeight() + 4) { return false; }
        BlockPos base = pool ? ground.below(4) : ground.offset(-8, -4, -8);
        if (base.getY() < level.getMinBuildHeight() || base.getY() + 8 >= level.getMaxBuildHeight()) { return false; }
        boolean[] shape = shape(random);
        for (int x = 0; x < 16; x++) { for (int z = 0; z < 16; z++) { for (int y = 0; y < 8; y++) {
            BlockPos pos = base.offset(x, y, z);
            if (!level.ensureCanWrite(pos) || level.getBlockEntity(pos) != null) { return false; }
            if (!edge(shape, x, y, z)) { continue; }
            BlockState current = level.getBlockState(pos);
            if (y >= 4 && !current.getFluidState().isEmpty()) { return false; }
            if (pool && y < 4 && !current.isSolid() && !current.is(liquid.getBlock())) { return false; }
        } } }
        List<BlockPos> fluidPositions = new ArrayList<>();
        for (int x = 0; x < 16; x++) { for (int z = 0; z < 16; z++) { for (int y = 0; y < 8; y++) {
            if (!shape[index(x, y, z)]) { continue; }
            BlockPos pos = base.offset(x, y, z);
            if (!pool && level.isEmptyBlock(pos.below())) { continue; }
            level.setBlock(pos, y >= 4 ? Blocks.AIR.defaultBlockState() : liquid, 2);
            if (y < 4) { fluidPositions.add(pos); }
        } } }
        for (int x = 0; x < 16; x++) { for (int z = 0; z < 16; z++) { for (int y = 0; y < 8; y++) {
            if (!edge(shape, x, y, z) || (y >= 4 && random.nextInt(2) == 0)) { continue; }
            BlockPos pos = base.offset(x, y, z);
            if (!level.getBlockState(pos).isSolid()) { continue; }
            if (!level.getFluidState(pos.above()).isEmpty()) {
                if (!pool && random.nextInt(10) == 0) { level.setBlock(pos, bottom, 2); }
            } else if (!rim.isEmpty()) { level.setBlock(pos, rim.get(random.nextInt(rim.size())), 2); }
        } } }
        if (pool) {
            for (int x = 0; x < 16; x++) { for (int z = 0; z < 16; z++) { for (int y = 4; y < 8; y++) {
                BlockPos pos = base.offset(x, y - 1, z);
                if (shape[index(x, y, z)] && level.getBlockState(pos).is(Blocks.DIRT) && level.getLevel().dimensionType().hasSkyLight() && level.canSeeSky(pos.above())) {
                    level.setBlock(pos, level.getBiome(pos).is(net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS) ? Blocks.MYCELIUM.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                }
            } } }
        }
        for (BlockPos pos : fluidPositions) { var fluid = level.getFluidState(pos); level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level)); }
        if (spawns != null && !fluidPositions.isEmpty() && level.getDifficulty() != Difficulty.PEACEFUL) {
            for (int i = fluidPositions.size() - 1; i > 0; i--) { int selected = random.nextInt(i + 1); BlockPos previous = fluidPositions.set(i, fluidPositions.get(selected)); fluidPositions.set(selected, previous); }
            int count = Math.min(random.nextInt(5) + 2, fluidPositions.size());
            for (int i = 0; i < count; i++) {
                BlockPos pos = fluidPositions.get(i);
                if (!level.isEmptyBlock(pos.above())) { continue; }
                Slime mob = spawns.create(level.getLevel());
                if (mob != null) { mob.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, random.nextFloat() * 360, 0); level.addFreshEntity(mob); }
            }
        }
        return true;
    }
}
