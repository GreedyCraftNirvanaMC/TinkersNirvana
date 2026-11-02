/*
 * 根据 TinkersAntique 的 BlockGlow 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.gadgets.block;

import com.gctn.tconstruct.gadgets.GadgetModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 贴在支撑面的薄光斑，不阻挡实体，也不会掉落物品。 */
public final class GlowBlock extends Block {
    public static final MapCodec<GlowBlock> CODEC = simpleCodec(GlowBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape[] SHAPES = {
            Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 15, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 1), Block.box(0, 0, 15, 16, 16, 16),
            Block.box(0, 0, 0, 1, 16, 16), Block.box(15, 0, 0, 16, 16, 16)
    };

    public GlowBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<GlowBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).ordinal()];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supported(level, pos, state.getValue(FACING));
    }

    private boolean supported(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos support = pos.relative(facing);
        return level.getFluidState(pos).isEmpty()
                && level.getBlockState(support).isFaceSturdy(level, support, facing.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    /** 优先使用命中面，失败后依次寻找其他支撑面；客户端仅预测，不修改世界。 */
    public boolean addGlow(Level level, BlockPos pos, Direction preferred) {
        if (!level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        Direction selected = supported(level, pos, preferred) ? preferred : null;
        if (selected == null) {
            for (Direction direction : Direction.values()) {
                if (supported(level, pos, direction)) {
                    selected = direction;
                    break;
                }
            }
        }
        if (selected == null) {
            return false;
        }
        if (!level.isClientSide) {
            return level.setBlockAndUpdate(pos, defaultBlockState().setValue(FACING, selected));
        }
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(GadgetModule.GLOWBALL.get());
    }
}
