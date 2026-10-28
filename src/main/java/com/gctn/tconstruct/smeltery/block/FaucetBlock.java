package com.gctn.tconstruct.smeltery.block;

import com.gctn.tconstruct.smeltery.block.entity.FaucetBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class FaucetBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POURING = BlockStateProperties.POWERED;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final MapCodec<FaucetBlock> CODEC = simpleCodec(FaucetBlock::new);
    public FaucetBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POURING, false).setValue(TRIGGERED, false)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FaucetBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, POURING, TRIGGERED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return defaultBlockState().setValue(FACING, face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite());
    }
    @Override protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> box(4, 4, 10, 12, 10, 16);
            case EAST -> box(0, 4, 4, 6, 10, 12);
            case SOUTH -> box(4, 4, 0, 12, 10, 6);
            default -> box(10, 4, 4, 16, 10, 12);
        };
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.cycle(POURING), 3);
            level.scheduleTick(pos, this, 1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (!level.isClientSide && powered != state.getValue(TRIGGERED)) {
            level.setBlock(pos, state.setValue(TRIGGERED, powered).setValue(POURING, powered || state.getValue(POURING)), 3);
            if (powered) level.scheduleTick(pos, this, 1);
        }
    }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide && state.getValue(POURING)) level.scheduleTick(pos, this, 1);
        if (!level.isClientSide && !state.is(old.getBlock())) neighborChanged(state, level, pos, this, pos, moving);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var visual = level.getBlockEntity(pos) instanceof FaucetBlockEntity faucet ? faucet : null;
        if (!state.getValue(POURING)) { if (visual != null) visual.setFlowing(net.neoforged.neoforge.fluids.FluidStack.EMPTY); return; }
        Direction facing = state.getValue(FACING);
        BlockPos sourcePos = pos.relative(facing.getOpposite()), targetPos = pos.below();
        if (!level.hasChunkAt(sourcePos) || !level.hasChunkAt(targetPos)) { level.scheduleTick(pos, this, 20); return; }
        var source = level.getCapability(Capabilities.FluidHandler.BLOCK, sourcePos, facing);
        var target = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, Direction.UP);
        boolean moved = false;
        if (source != null && target != null) {
            var offered = source.drain(6, FluidAction.SIMULATE);
            int accepted = target.fill(offered, FluidAction.SIMULATE);
            if (accepted > 0) {
                var drained = source.drain(offered.copyWithAmount(accepted), FluidAction.EXECUTE);
                int filled = target.fill(drained, FluidAction.EXECUTE);
                // Preserve fluid even if a third-party handler changes its answer during the transfer.
                if (filled < drained.getAmount()) source.fill(drained.copyWithAmount(drained.getAmount() - filled), FluidAction.EXECUTE);
                moved = filled > 0;
                if (moved && visual != null) visual.setFlowing(drained);
            }
        }
        if (moved) level.scheduleTick(pos, this, 1);
        else {
            level.setBlock(pos, state.setValue(POURING, false), 3);
            if (visual != null) visual.setFlowing(net.neoforged.neoforge.fluids.FluidStack.EMPTY);
        }
    }
}
