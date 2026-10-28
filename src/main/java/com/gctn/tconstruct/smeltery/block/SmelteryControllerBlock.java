package com.gctn.tconstruct.smeltery.block;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public class SmelteryControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;
    private static final MapCodec<SmelteryControllerBlock> CODEC = simpleCodec(SmelteryControllerBlock::new);
    public SmelteryControllerBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, ACTIVE); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SmelteryControllerBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, SmelteryRegistration.CONTROLLER_ENTITY.get(), SmelteryControllerBlockEntity::tick);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        open(level, pos, player); return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    private void open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryControllerBlockEntity controller) {
            controller.checkStructure();
            if (controller.isFormed()) player.openMenu(controller, buffer -> buffer.writeBlockPos(pos));
            else player.displayClientMessage(Component.translatable("message.tconstruct.smeltery.incomplete"), true);
        }
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryControllerBlockEntity controller) {
            controller.releaseStructure();
            Containers.dropContents(level, pos, controller);
        }
        super.onRemove(state, level, pos, next, moving);
    }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;
        Direction face = state.getValue(FACING);
        double x = pos.getX() + 0.5 + face.getStepX() * 0.52;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.52;
        level.addParticle(ParticleTypes.FLAME, x, pos.getY() + 0.25 + random.nextDouble() * 0.5, z, 0, 0, 0);
    }
}
