/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's BlockRack. */
package com.gctn.tconstruct.gadgets.block;

import com.gctn.tconstruct.gadgets.RemainingGadgets;
import com.gctn.tconstruct.gadgets.block.entity.RackBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RackBlock extends BaseEntityBlock {
    @Override public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult hit, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) { return com.gctn.tconstruct.tables.TableMaterial.pick(level, pos, new ItemStack(this)); }
    public final boolean drying;
    public RackBlock(boolean drying, Properties properties) {
        super(properties); this.drying = drying;
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(properties -> new RackBlock(drying, properties)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState support = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if (support.getBlock() instanceof RackBlock) { return defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, support.getValue(BlockStateProperties.ATTACH_FACE)).setValue(BlockStateProperties.HORIZONTAL_FACING, support.getValue(BlockStateProperties.HORIZONTAL_FACING)); }
        Direction side = context.getClickedFace();
        return defaultBlockState().setValue(BlockStateProperties.ATTACH_FACE, side == Direction.UP ? AttachFace.FLOOR : side == Direction.DOWN ? AttachFace.CEILING : AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, side.getAxis().isHorizontal() ? side : context.getHorizontalDirection().getOpposite());
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING))); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING); AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
        if (face != AttachFace.WALL) { int y = face == AttachFace.FLOOR ? 0 : 12; return facing.getAxis() == Direction.Axis.Z ? box(0, y, 6, 16, y + 4, 10) : box(6, y, 0, 10, y + 4, 16); }
        return switch (facing) { case NORTH -> box(0, 12, 12, 16, 16, 16); case SOUTH -> box(0, 12, 0, 16, 16, 4); case EAST -> box(0, 12, 0, 4, 16, 16); default -> box(12, 12, 0, 16, 16, 16); };
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RackBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return drying ? createTickerHelper(type, RemainingGadgets.RACK_ENTITY.get(), RackBlockEntity::tick) : null; }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return drying; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return level.getBlockEntity(pos) instanceof RackBlockEntity rack && !rack.getItem(1).isEmpty() ? 15 : 0; }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { interact(level, pos, player, InteractionHand.MAIN_HAND); return InteractionResult.sidedSuccess(level.isClientSide); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { interact(level, pos, player, hand); return ItemInteractionResult.sidedSuccess(level.isClientSide); }
    private void interact(Level level, BlockPos pos, Player player, InteractionHand hand) { if (!level.isClientSide && level.getBlockEntity(pos) instanceof RackBlockEntity rack) { rack.interact(player, hand); } }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock())) { Containers.dropContentsOnDestroy(state, next, level, pos); }
        super.onRemove(state, level, pos, next, moving);
    }
    @Override protected java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        var drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof RackBlockEntity rack) { for (var stack : drops) { if (stack.is(asItem())) { rack.material.apply(stack); } } }
        return drops;
    }
}
