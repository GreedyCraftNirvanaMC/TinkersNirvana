package com.gctn.tconstruct.smeltery.block;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.gctn.tconstruct.smeltery.block.entity.CastingBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * 铸件台或铸造盆方块，支持原版流体容器交互和物品领取。
 * 服务端通过刻更新处理浇铸，客户端渲染器读取同步后的流体与物品内容。
 */
public class CastingBlock extends BaseEntityBlock {
    // 缓存不可变的形状并集，避免每次射线检测、
    // 碰撞检测或绘制选中边框时重新创建体素形状。
    private static final VoxelShape TABLE_SHAPE = Shapes.or(box(0, 10, 0, 16, 16, 16), box(0, 0, 0, 4, 10, 4),
            box(12, 0, 0, 16, 10, 4), box(0, 0, 12, 4, 10, 16), box(12, 0, 12, 16, 10, 16));
    private static final VoxelShape BASIN_SHAPE = Shapes.or(box(0, 0, 0, 16, 4, 16), box(0, 4, 0, 2, 16, 16),
            box(14, 4, 0, 16, 16, 16), box(2, 4, 0, 14, 16, 2), box(2, 4, 14, 14, 16, 16));
    private static final MapCodec<CastingBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            propertiesCodec(), Codec.BOOL.fieldOf("basin").forGetter(CastingBlock::isBasin)).apply(i, CastingBlock::new));
    private final boolean basin;
    public CastingBlock(Properties properties, boolean basin) { super(properties); this.basin = basin; }
    public boolean isBasin() { return basin; }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CastingBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, SmelteryRegistration.CASTING_ENTITY.get(), CastingBlockEntity::tick);
    }
    @Override protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return basin ? BASIN_SHAPE : TABLE_SHAPE;
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CastingBlockEntity casting) {
            if (FluidUtil.interactWithFluidHandler(player, hand, casting.fluidHandler())) return ItemInteractionResult.SUCCESS;
            casting.interact(player, hand);
        }
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CastingBlockEntity casting) casting.interact(player, InteractionHand.MAIN_HAND);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CastingBlockEntity casting) {
            for (int slot = 0; slot < 2; slot++) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, casting.itemHandler().getStackInSlot(slot).copy());
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
