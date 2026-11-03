/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's BlockSlimeGrass. */
package com.gctn.tconstruct.world.block;

import com.gctn.tconstruct.world.WorldModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

public class SlimeGrassBlock extends SnowyDirtBlock implements BonemealableBlock {
    public final String foliage, dirt;
    public SlimeGrassBlock(String foliage, String dirt, Properties properties) { super(properties); this.foliage = foliage; this.dirt = dirt; }
    @Override public MapCodec<? extends SlimeGrassBlock> codec() { return simpleCodec(properties -> new SlimeGrassBlock(foliage, dirt, properties)); }
    @Override public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockState plant) { return SlimeDirtBlock.supports(plant); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 3) || level.getMaxLocalRawBrightness(pos.above()) < 9) { return; }
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            BlockState current = level.getBlockState(target), cover = level.getBlockState(target.above());
            if (level.getMaxLocalRawBrightness(target.above()) < 4 || cover.getLightBlock(level, target.above()) > 2) { continue; }
            String color = current.is(Blocks.DIRT) ? "vanilla" : null;
            for (String candidate : WorldModule.DIRT_COLORS) { if (current.is(WorldModule.DIRT.get(candidate).get())) { color = candidate; break; } }
            if (color != null) { level.setBlock(target, WorldModule.GRASS.get(foliage + "_" + color).get().defaultBlockState(), 3); }
        }
    }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) { return true; }
    @Override public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) { return true; }
    @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        for (int i = 0; i < 128; i++) {
            BlockPos target = pos.above(); boolean valid = true;
            for (int step = 0; step < i / 16; step++) {
                target = target.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!(level.getBlockState(target.below()).getBlock() instanceof SlimeGrassBlock) || level.getBlockState(target).isCollisionShapeFullBlock(level, target)) { valid = false; break; }
            }
            if (valid && level.isEmptyBlock(target)) {
                BlockState plant = WorldModule.PLANTS.get(foliage + (random.nextInt(8) == 0 ? "_fern" : "_tall_grass")).get().defaultBlockState();
                if (plant.canSurvive(level, target)) { level.setBlock(target, plant, 3); }
            }
        }
    }
}
