package com.gctn.tconstruct.smeltery.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import com.gctn.tconstruct.smeltery.block.entity.CastingBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.CastingBlockEntity，供本文件使用。
import com.mojang.serialization.Codec; // 引入 com.mojang.serialization.Codec，供本文件使用。
import com.mojang.serialization.MapCodec; // 引入 com.mojang.serialization.MapCodec，供本文件使用。
import com.mojang.serialization.codecs.RecordCodecBuilder; // 引入 com.mojang.serialization.codecs.RecordCodecBuilder，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.world.*; // 引入 net.minecraft.world.*，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.*; // 引入 net.minecraft.world.level.block.*，供本文件使用。
import net.minecraft.world.level.block.entity.*; // 引入 net.minecraft.world.level.block.entity.*，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.minecraft.world.phys.BlockHitResult; // 引入 net.minecraft.world.phys.BlockHitResult，供本文件使用。
import net.minecraft.world.phys.shapes.*; // 引入 net.minecraft.world.phys.shapes.*，供本文件使用。
import net.neoforged.neoforge.fluids.FluidUtil; // 引入 net.neoforged.neoforge.fluids.FluidUtil，供本文件使用。

/**
 * 铸件台或铸造盆方块，支持原版流体容器交互和物品领取。
 * 服务端通过刻更新处理浇铸，客户端渲染器读取同步后的流体与物品内容。
 */
public class CastingBlock extends BaseEntityBlock { // 定义 CastingBlock 的实现类型，具体职责见上方说明。
    // 缓存不可变的形状并集，避免每次射线检测、
    // 碰撞检测或绘制选中边框时重新创建体素形状。
    private static final VoxelShape TABLE_SHAPE = Shapes.or(box(0, 10, 0, 16, 16, 16), box(0, 0, 0, 4, 10, 4), // 设置 TABLE_SHAPE 为 Shapes.or(box(0, 10, 0, 16, 16, 16), box(0, 0, 0, 4, 10, 4),。
            box(12, 0, 0, 16, 10, 4), box(0, 0, 12, 4, 10, 16), box(12, 0, 12, 16, 10, 16)); // 构建或绘制指定范围的形状。
    private static final VoxelShape BASIN_SHAPE = Shapes.or(box(0, 0, 0, 16, 4, 16), box(0, 4, 0, 2, 16, 16), // 设置 BASIN_SHAPE 为 Shapes.or(box(0, 0, 0, 16, 4, 16), box(0, 4, 0, 2, 16, 16),。
            box(14, 4, 0, 16, 16, 16), box(2, 4, 0, 14, 16, 2), box(2, 4, 14, 14, 16, 16)); // 构建或绘制指定范围的形状。
    private static final MapCodec<CastingBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group( // 设置 CODEC 为 RecordCodecBuilder.mapCodec(i -> i.group(。
            propertiesCodec(), Codec.BOOL.fieldOf("basin").forGetter(CastingBlock::isBasin)).apply(i, CastingBlock::new)); // 调用 propertiesCodec，执行当前流程所需的处理。
    private final boolean basin; // 声明 basin，保存后续流程使用的状态或依赖。
    public CastingBlock(Properties properties, boolean basin) { super(properties); this.basin = basin; } // 定义 CastingBlock 的调用入口；在 CastingBlock 中补充当前声明或表达式的 super(properties); this.basin = basin 部分。
    public boolean isBasin() { return basin; } // 定义 isBasin 的调用入口；返回 basin，将当前分支的结果交给调用者。
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; } // 实现父类或接口约定；调用 codec，执行当前流程所需的处理。
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; } // 实现父类或接口约定；定义 getRenderShape 的调用入口；返回 RenderShape.MODEL，将当前分支的结果交给调用者。
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new CastingBlockEntity(pos, state); } // 实现父类或接口约定；定义 newBlockEntity 的调用入口；返回 new CastingBlockEntity(pos, state)，将当前分支的结果交给调用者。
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { // 实现父类或接口约定；调用 getTicker，执行当前流程所需的处理。
        return level.isClientSide ? null : createTickerHelper(type, SmelteryRegistration.CASTING_ENTITY.get(), CastingBlockEntity::tick); // 返回 level.isClientSide ? null : createTickerHelper(type, SmelteryRegistration.CASTING_ENTITY.get(), CastingBloc…，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) { // 实现父类或接口约定；定义 getShape 的调用入口，参数和具体处理见下方实现。
        return basin ? BASIN_SHAPE : TABLE_SHAPE; // 返回 basin ? BASIN_SHAPE : TABLE_SHAPE，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { // 实现父类或接口约定；定义 useItemOn 的调用入口，参数和具体处理见下方实现。
        if (level.isClientSide) return ItemInteractionResult.SUCCESS; // 检查 level.isClientSide；按条件决定是否执行本分支。
        if (level.getBlockEntity(pos) instanceof CastingBlockEntity casting) { // 检查 level.getBlockEntity(pos) instanceof CastingBlockEntity casting；按条件决定是否执行本分支。
            if (FluidUtil.interactWithFluidHandler(player, hand, casting.fluidHandler())) return ItemInteractionResult.SUCCESS; // 检查 FluidUtil.interactWithFluidHandler(player, hand, casting.fluidHandler())；按条件决定是否执行本分支。
            casting.interact(player, hand); // 调用 interact，执行当前流程所需的处理。
        } // 结束当前作用域。
        return ItemInteractionResult.SUCCESS; // 返回 ItemInteractionResult.SUCCESS，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { // 实现父类或接口约定；定义 useWithoutItem 的调用入口，参数和具体处理见下方实现。
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CastingBlockEntity casting) casting.interact(player, InteractionHand.MAIN_HAND); // 检查 !level.isClientSide && level.getBlockEntity(pos) instanceof CastingBlockEntity casting；按条件决定是否执行本分支。
        return InteractionResult.sidedSuccess(level.isClientSide); // 返回 InteractionResult.sidedSuccess(level.isClientSide)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) { // 实现父类或接口约定；定义 onRemove 的调用入口，参数和具体处理见下方实现。
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CastingBlockEntity casting) { // 检查 !state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CastingBlockEntity casting；按条件决定是否执行本分支。
            for (int slot = 0; slot < 2; slot++) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, casting.itemHandler().getStackInSlot(slot).copy()); // 遍历 int slot = 0; slot < 2; slot++) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.get…，逐项执行当前处理。
        } // 结束当前作用域。
        super.onRemove(state, level, pos, next, moving); // 调用 onRemove，执行当前流程所需的处理。
    } // 结束当前作用域。
} // 结束当前作用域。
