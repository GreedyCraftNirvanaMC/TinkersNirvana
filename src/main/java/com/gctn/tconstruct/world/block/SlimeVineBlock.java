/* SPDX-License-Identifier: LGPL-3.0-only; Antique's downward-only, three-stage slime vines. */
package com.gctn.tconstruct.world.block;

import com.gctn.tconstruct.world.WorldModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SlimeVineBlock extends VineBlock {
    public final String foliage;
    public final int stage;
    public SlimeVineBlock(String foliage, int stage, Properties properties) { super(properties); this.foliage = foliage; this.stage = stage; }
    private BlockState supported(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            var property = getPropertyForFace(direction);
            if (state.getValue(property) && !isAcceptableNeighbour(level, pos.relative(direction), direction)
                    && !(above.getBlock() instanceof SlimeLeavesBlock) && !(above.getBlock() instanceof VineBlock && above.getValue(property))) { state = state.setValue(property, false); }
        }
        return state.setValue(UP, false);
    }
    private boolean hasFace(BlockState state) { for (Direction direction : Direction.Plane.HORIZONTAL) { if (state.getValue(getPropertyForFace(direction))) { return true; } } return false; }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) { return hasFace(supported(state, level, pos)); }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState adjacent, LevelAccessor level, BlockPos pos, BlockPos adjacentPos) {
        BlockState updated = supported(state, level, pos); return hasFace(updated) ? updated : Blocks.AIR.defaultBlockState();
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!state.is(this)) { state = defaultBlockState(); }
        for (Direction direction : Direction.Plane.HORIZONTAL) { state = state.setValue(getPropertyForFace(direction), isAcceptableNeighbour(context.getLevel(), context.getClickedPos().relative(direction), direction)); }
        if (!hasFace(state) && context.getLevel().getBlockState(context.getClickedPos().above()).getBlock() instanceof SlimeLeavesBlock) { state = state.setValue(NORTH, true); }
        return hasFace(state) ? state : null;
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (stage == 2 || !level.getGameRules().getBoolean(GameRules.RULE_DO_VINES_SPREAD) || random.nextInt(4) != 0 || !level.isAreaLoaded(pos, 1) || !level.isEmptyBlock(pos.below())) { return; }
        boolean floating = true;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (state.getValue(getPropertyForFace(direction)) && isAcceptableNeighbour(level, pos.relative(direction), direction)) { floating = false; break; }
        }
        if (floating) {
            int length = 0;
            while (length < 4 && level.getBlockState(pos.above(length)).is(this)) { length++; }
            if (length > 2 || random.nextBoolean()) {
                BlockState next = WorldModule.VINES.get(foliage + (stage + 1)).get().defaultBlockState();
                for (Direction direction : Direction.Plane.HORIZONTAL) { var property = getPropertyForFace(direction); next = next.setValue(property, state.getValue(property)); }
                state = next;
            }
        }
        level.setBlock(pos.below(), state, 3);
    }
}
