/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's BlockToolTable. */
package com.gctn.tconstruct.tables.block;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.tables.WorkshopModule.Kind;
import com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class WorkshopBlock extends BaseEntityBlock {
    @Override public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult hit, net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) { return com.gctn.tconstruct.tables.TableMaterial.pick(level, pos, new ItemStack(this)); }
    public static final BooleanProperty CASTS = BooleanProperty.create("casts");
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape TABLE = Shapes.or(box(0, 12, 0, 16, 16, 16), box(0, 0, 0, 4, 12, 4),
            box(12, 0, 0, 16, 12, 4), box(0, 0, 12, 4, 12, 16), box(12, 0, 12, 16, 12, 16));
    private final Kind kind;
    private static final VoxelShape CHEST = Shapes.or(box(0, 15, 0, 16, 16, 16), box(1, 3, 1, 15, 15, 15),
            box(.5, 0, .5, 2.5, 3, 2.5), box(13.5, 0, .5, 15.5, 3, 2.5), box(.5, 0, 13.5, 2.5, 3, 15.5), box(13.5, 0, 13.5, 15.5, 3, 15.5));
    public WorkshopBlock(Kind kind, Properties properties) {
        super(properties); this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(CASTS, false).setValue(FACING, net.minecraft.core.Direction.NORTH));
    }
    public Kind kind() { return kind; }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(properties -> new WorkshopBlock(kind, properties)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(CASTS, FACING); }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return kind.storage() ? CHEST : TABLE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WorkshopBlockEntity(pos, state); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind.storage() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof WorkshopBlockEntity chest) {
            ItemStack remaining = ItemHandlerHelper.insertItemStacked(chest.itemHandler(), stack, true);
            if (remaining.getCount() < stack.getCount()) {
                if (!level.isClientSide) { player.setItemInHand(hand, ItemHandlerHelper.insertItemStacked(chest.itemHandler(), stack, false)); }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        open(level, pos, player); return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    private void open(Level level, BlockPos pos, Player player) {
        com.gctn.tconstruct.tables.WorkshopModule.open(player, pos);
    }
    private ItemStack storedItem(WorkshopBlockEntity entity) {
        ItemStack stack = new ItemStack(this);
        stack.applyComponents(entity.collectComponents());
        return stack;
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (kind.storage() && Config.CHESTS_KEEP_INVENTORY.get()
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof WorkshopBlockEntity entity) { return List.of(storedItem(entity)); }
        var drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof WorkshopBlockEntity entity) {
            for (ItemStack stack : drops) { if (stack.is(asItem())) { entity.material.apply(stack); } }
        }
        return drops;
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && kind.storage() && Config.CHESTS_KEEP_INVENTORY.get()
                && level.getBlockEntity(pos) instanceof WorkshopBlockEntity entity && !entity.isEmpty()) { popResource(level, pos, storedItem(entity)); }
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && !(kind.storage() && Config.CHESTS_KEEP_INVENTORY.get())) { Containers.dropContentsOnDestroy(state, next, level, pos); }
        super.onRemove(state, level, pos, next, moving);
    }
}
