package com.gctn.tconstruct.tables.block;

import com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class ToolStationBlock extends BaseEntityBlock {
    @Override public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult hit, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) { return com.gctn.tconstruct.tables.TableMaterial.pick(level, pos, new ItemStack(this)); }
    public static final MapCodec<ToolStationBlock> CODEC = simpleCodec(ToolStationBlock::new);

    public ToolStationBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WorkshopBlock.FACING, net.minecraft.core.Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(WorkshopBlock.FACING); }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) { return defaultBlockState().setValue(WorkshopBlock.FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) { return state.setValue(WorkshopBlock.FACING, rotation.rotate(state.getValue(WorkshopBlock.FACING))); }
    @Override protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(WorkshopBlock.FACING))); }
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.or(box(0, 12, 0, 16, 16, 16), box(0, 0, 0, 4, 12, 4), box(12, 0, 0, 16, 12, 4), box(0, 0, 12, 4, 12, 16), box(12, 0, 12, 16, 12, 16));
    }
    @Override protected java.util.List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        var drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY) instanceof ToolStationBlockEntity entity) {
            for (ItemStack stack : drops) { if (stack.is(asItem())) { entity.material.apply(stack); } }
        }
        return drops;
    }

    @Override
    protected MapCodec<ToolStationBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolStationBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        this.openMenu(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        this.openMenu(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            return;
        }
        if (level.getBlockEntity(pos) instanceof ToolStationBlockEntity blockEntity) {
            blockEntity.prepareForRemoval();
        }
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void openMenu(Level level, BlockPos pos, Player player) {
        com.gctn.tconstruct.tables.WorkshopModule.open(player, pos);
    }
}
