/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's tall slime grass. */
package com.gctn.tconstruct.world.block;

import com.gctn.tconstruct.world.WorldModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlimePlantBlock extends BushBlock {
    public SlimePlantBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BushBlock> codec() { return simpleCodec(SlimePlantBlock::new); }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) { return WorldModule.soil(state); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos); return box(2, 0, 2, 14, 13, 14).move(offset.x, offset.y, offset.z);
    }
}
