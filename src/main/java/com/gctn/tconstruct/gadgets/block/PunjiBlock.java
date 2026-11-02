/*
 * 根据 TinkersAntique 的 BlockPunji 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 六面附着的尖竹钉，保留原版受伤、减速及同平面连接规则。 */
public class PunjiBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty NORTHEAST = BooleanProperty.create("northeast");
    public static final BooleanProperty NORTHWEST = BooleanProperty.create("northwest");

    public PunjiBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(NORTH, false)
                .setValue(EAST, false).setValue(NORTHEAST, false).setValue(NORTHWEST, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(PunjiBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, EAST, NORTHEAST, NORTHWEST);
    }

    public static Direction firstAxis(Direction facing) {
        return Direction.from3DDataValue((facing.ordinal() + 2) % 6);
    }

    public static Direction secondAxis(Direction facing) {
        return Direction.from3DDataValue((facing.ordinal() + 4 - facing.ordinal() % 2) % 6);
    }

    private boolean matches(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.is(this) && state.getValue(FACING) == facing;
    }

    private BlockState connected(BlockState state, BlockGetter level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Direction first = firstAxis(facing);
        Direction second = secondAxis(facing);
        return state.setValue(NORTH, matches(level, pos.relative(first), facing))
                .setValue(EAST, matches(level, pos.relative(second), facing))
                .setValue(NORTHEAST, matches(level, pos.relative(first).relative(second), facing))
                .setValue(NORTHWEST, matches(level, pos.relative(first).relative(second.getOpposite()), facing));
    }

    private void updateDiagonals(Level level, BlockPos pos, Direction facing) {
        Direction first = firstAxis(facing);
        Direction second = secondAxis(facing);
        for (int a : new int[]{-1, 1}) {
            for (int b : new int[]{-1, 1}) {
                BlockPos other = pos.relative(first, a).relative(second, b);
                BlockState state = level.getBlockState(other);
                if (state.is(this) && state.getValue(FACING) == facing) {
                    level.setBlock(other, connected(state, level, other), 3);
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? connected(state, context.getLevel(), context.getClickedPos()) : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos support = pos.relative(facing);
        return level.getBlockState(support).isFaceSturdy(level, support, facing.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return side == state.getValue(FACING) && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : connected(state, level, pos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moving) {
        if (!level.isClientSide && (!previous.is(this) || previous.getValue(FACING) != state.getValue(FACING))) {
            level.setBlock(pos, connected(state, level, pos), 3);
            // 原版连接在渲染时读取邻居；现代方块状态还需显式刷新对角邻居。
            updateDiagonals(level, pos, state.getValue(FACING));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!level.isClientSide && (!replacement.is(this) || replacement.getValue(FACING) != state.getValue(FACING))) {
            updateDiagonals(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> box(3, 0, 3, 13, 6, 13);
            case UP -> box(3, 10, 3, 13, 16, 13);
            case NORTH -> box(3, 3, 0, 13, 13, 6);
            case SOUTH -> box(3, 3, 10, 13, 13, 16);
            case WEST -> box(0, 3, 3, 6, 13, 13);
            case EAST -> box(10, 3, 3, 16, 13, 13);
        };
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            float damage = 3 + (living.fallDistance > 0 ? living.fallDistance * 1.5F + 2 : 0);
            living.hurt(level.damageSources().cactus(), damage);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1));
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
