/*
 * 根据 TinkersAntique 的 BlockWoodenHopper 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import com.gctn.tconstruct.gadgets.GadgetModule;
import com.gctn.tconstruct.gadgets.block.entity.WoodenHopperBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 不受红石控制的单槽木漏斗，收取和输出仍使用原生漏斗实现。 */
public class WoodenHopperBlock extends HopperBlock {
    private static final VoxelShape COLLISION = Shapes.or(Block.box(0, 0, 0, 16, 10, 16),
            Block.box(0, 0, 0, 2, 16, 16), Block.box(14, 0, 0, 16, 16, 16),
            Block.box(0, 0, 0, 16, 16, 2), Block.box(0, 0, 14, 16, 16, 16));
    private static final VoxelShape BODY = Shapes.or(Block.box(0, 10, 0, 16, 16, 16), Block.box(4, 4, 4, 12, 10, 12));

    public WoodenHopperBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<HopperBlock> codec() {
        return simpleCodec(WoodenHopperBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WoodenHopperBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, GadgetModule.WOODEN_HOPPER_ENTITY.get(), HopperBlockEntity::pushItemsTick);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moving) {
        // 原版木漏斗省略红石检查，始终保持放置时的启用状态。
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        // 相邻方块更新也不能把木漏斗锁住。
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        VoxelShape spout = switch (facing) {
            case NORTH -> Block.box(6, 4, 0, 10, 8, 4);
            case SOUTH -> Block.box(6, 4, 12, 10, 8, 16);
            case WEST -> Block.box(0, 4, 6, 4, 8, 10);
            case EAST -> Block.box(12, 4, 6, 16, 8, 10);
            default -> Block.box(6, 0, 6, 10, 4, 10);
        };
        return Shapes.or(BODY, spout);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }
}
