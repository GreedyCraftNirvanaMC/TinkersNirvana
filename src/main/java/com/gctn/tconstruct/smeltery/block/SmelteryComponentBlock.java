package com.gctn.tconstruct.smeltery.block;

import com.gctn.tconstruct.smeltery.block.entity.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * 具有归属关系的冶炼炉构件，分别提供砖块、玻璃、排液口或熔岩储罐行为。
 * 只有排液口暴露控制器中的熔液库存，燃料储罐单独保存熔岩。
 */
public class SmelteryComponentBlock extends BaseEntityBlock {
    public enum Kind { BRICK, GLASS, TANK, DRAIN }
    private static final MapCodec<SmelteryComponentBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            propertiesCodec(), Codec.STRING.xmap(Kind::valueOf, Kind::name).fieldOf("kind").forGetter(b -> b.kind)
    ).apply(i, SmelteryComponentBlock::new));
    private final Kind kind;
    public SmelteryComponentBlock(Properties properties, Kind kind) { super(properties); this.kind = kind; }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return kind == Kind.TANK ? new SearedTankBlockEntity(pos, state) : new SmelteryComponentBlockEntity(pos, state);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind != Kind.TANK && kind != Kind.DRAIN) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        var entity = (SmelteryComponentBlockEntity) level.getBlockEntity(pos);
        if (entity != null && FluidUtil.interactWithFluidHandler(player, hand,
                entity instanceof SearedTankBlockEntity tank ? tank.tank() : entity.drainHandler())) return ItemInteractionResult.SUCCESS;
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.notifyController();
        super.onRemove(state, level, pos, next, moving);
    }
    @Override protected boolean skipRendering(BlockState state, BlockState adjacent, net.minecraft.core.Direction side) {
        return kind == Kind.GLASS && adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return kind == Kind.TANK; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SearedTankBlockEntity tank ? tank.tank().getFluidAmount() * 15 / SearedTankBlockEntity.CAPACITY : 0;
    }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return kind == Kind.GLASS; }
}
