/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's BlockSlimeDirt. */
package com.gctn.tconstruct.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

public class SlimeDirtBlock extends Block {
    public SlimeDirtBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends Block> codec() { return simpleCodec(SlimeDirtBlock::new); }
    public static TriState supports(BlockState plant) {
        Block block = plant.getBlock();
        // 旧版的 Plains 允许花草和树苗，耕地作物即使继承 BushBlock 也有独立的种植条件。
        return block instanceof BushBlock && !plant.is(BlockTags.CROPS) && !(block instanceof CropBlock)
                && !(block instanceof AttachedStemBlock) && !(block instanceof NetherWartBlock)
                && !(block instanceof MushroomBlock) && !(block instanceof DeadBushBlock)
                ? TriState.TRUE : TriState.DEFAULT;
    }
    @Override public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction direction, BlockState plant) { return supports(plant); }
}
