/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's SlimeTreeGenerator. */
package com.gctn.tconstruct.world.feature;

import com.gctn.tconstruct.world.WorldModule;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Original diamond canopy and drippers; states are configurable through a datapack. */
public class SlimeTreeFeature extends Feature<SlimeTreeFeature.Configuration> {
    public record Configuration(int minHeight, int heightRange, BlockState trunk, BlockState leaves, Optional<BlockState> vine, boolean seekHeight) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 64).optionalFieldOf("min_height", 5).forGetter(Configuration::minHeight),
                Codec.intRange(1, 32).optionalFieldOf("height_range", 4).forGetter(Configuration::heightRange),
                BlockState.CODEC.fieldOf("trunk").forGetter(Configuration::trunk), BlockState.CODEC.fieldOf("leaves").forGetter(Configuration::leaves),
                BlockState.CODEC.optionalFieldOf("vine").forGetter(Configuration::vine), Codec.BOOL.optionalFieldOf("seek_height", true).forGetter(Configuration::seekHeight)
        ).apply(instance, Configuration::new));
    }
    public SlimeTreeFeature() { super(Configuration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<Configuration> context) { return generate(context.level(), context.random(), context.origin(), context.config()); }
    public static boolean generate(WorldGenLevel level, RandomSource random, BlockPos origin, Configuration config) {
        BlockPos pos = origin;
        if (config.seekHeight) {
            while (pos.getY() > level.getMinBuildHeight()) {
                if (WorldModule.soil(level.getBlockState(pos.below())) && replaceable(level.getBlockState(pos))) { break; }
                if (WorldModule.soil(level.getBlockState(pos)) && replaceable(level.getBlockState(pos.above()))) { pos = pos.above(); break; }
                pos = pos.below();
            }
        }
        int height = config.minHeight + random.nextInt(config.heightRange);
        if (!WorldModule.soil(level.getBlockState(pos.below())) || pos.getY() + height + 1 >= level.getMaxBuildHeight()) { return false; }
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        for (int y = 0; y < height; y++) {
            blocks.put(pos.above(y), config.trunk);
        }
        BlockState leaves = config.leaves.hasProperty(LeavesBlock.DISTANCE)
                ? config.leaves.setValue(LeavesBlock.DISTANCE, 4) : config.leaves;
        BlockPos crown = pos.above(height);
        for (int layer = 0; layer < Math.min(4, height + 1); layer++) {
            int radius = layer + 1;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) + Math.abs(z) <= radius) {
                        // 树冠和树干相交时保留树干，树叶也依靠这些凝固黏液块避免腐烂。
                        blocks.putIfAbsent(crown.offset(x, -layer, z), leaves);
                    }
                }
            }
        }
        if (height >= 5) {
            addCanopyDetails(config, random, blocks, leaves, crown.below(3));
        }
        // 花边和垂藤伸出树冠范围，必须一并检查后才可以开始放置。
        for (BlockPos target : blocks.keySet()) {
            if (!level.ensureCanWrite(target) || level.getBlockEntity(target) != null
                    || !replaceable(level.getBlockState(target))) {
                return false;
            }
        }
        blocks.forEach((target, state) -> level.setBlock(target, state, 2));
        return true;
    }
    private static void addCanopyDetails(Configuration config, RandomSource random, Map<BlockPos, BlockState> blocks,
                                         BlockState leaves, BlockPos bottom) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blocks.remove(bottom.relative(direction, 4));
            blocks.put(bottom.below().relative(direction, 3), leaves);
            if (config.vine.isPresent()) {
                blocks.put(bottom.below(2).relative(direction, 3), randomizedVine(config.vine.get(), random));
            }
        }
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                if (config.vine.isPresent()) {
                    blocks.remove(bottom.offset(x, 0, z));
                    BlockState vine = randomizedVine(config.vine.get(), random);
                    blocks.put(bottom.offset(x * 2, -1, z * 2), vine);
                    blocks.put(bottom.offset(x * 2, -2, z * 2), vine);
                } else {
                    blocks.put(bottom.offset(x, -1, z), leaves);
                }
            }
        }
    }

    private static boolean replaceable(BlockState state) { return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES); }
    public static BlockState randomizedVine(BlockState state, RandomSource random) {
        // 数据包也可用没有附着面属性的方块装饰垂藤，此时直接采用指定状态。
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!state.hasProperty(VineBlock.getPropertyForFace(direction))) { return state; }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) { state = state.setValue(VineBlock.getPropertyForFace(direction), false); }
        for (int i = 1 + random.nextInt(3); i > 0; i--) { state = state.setValue(VineBlock.getPropertyForFace(Direction.from2DDataValue(random.nextInt(4))), true); }
        return state;
    }
}
