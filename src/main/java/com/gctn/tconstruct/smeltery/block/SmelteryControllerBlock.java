package com.gctn.tconstruct.smeltery.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity，供本文件使用。
import com.mojang.serialization.MapCodec; // 引入 com.mojang.serialization.MapCodec，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.Direction; // 引入 net.minecraft.core.Direction，供本文件使用。
import net.minecraft.core.particles.ParticleTypes; // 引入 net.minecraft.core.particles.ParticleTypes，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.util.RandomSource; // 引入 net.minecraft.util.RandomSource，供本文件使用。
import net.minecraft.world.*; // 引入 net.minecraft.world.*，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.context.BlockPlaceContext; // 引入 net.minecraft.world.item.context.BlockPlaceContext，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.*; // 引入 net.minecraft.world.level.block.*，供本文件使用。
import net.minecraft.world.level.block.entity.*; // 引入 net.minecraft.world.level.block.entity.*，供本文件使用。
import net.minecraft.world.level.block.state.*; // 引入 net.minecraft.world.level.block.state.*，供本文件使用。
import net.minecraft.world.level.block.state.properties.*; // 引入 net.minecraft.world.level.block.state.properties.*，供本文件使用。
import net.minecraft.world.phys.BlockHitResult; // 引入 net.minecraft.world.phys.BlockHitResult，供本文件使用。

/**
 * 具有朝向的冶炼炉控制器方块，负责校验并打开已成形的结构。
 * 只在服务端执行刻更新，客户端通过激活方块状态和数据包显示效果。
 */
public class SmelteryControllerBlock extends BaseEntityBlock { // 定义 SmelteryControllerBlock 的实现类型，具体职责见上方说明。
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING; // 设置 FACING 为 BlockStateProperties.HORIZONTAL_FACING。
    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT; // 设置 ACTIVE 为 BlockStateProperties.LIT。
    private static final MapCodec<SmelteryControllerBlock> CODEC = simpleCodec(SmelteryControllerBlock::new); // 设置 CODEC 为 simpleCodec(SmelteryControllerBlock::new)。
    public SmelteryControllerBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false)); } // 定义 SmelteryControllerBlock 的调用入口；定义方块放置后的默认状态。
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; } // 实现父类或接口约定；调用 codec，执行当前流程所需的处理。
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, ACTIVE); } // 实现父类或接口约定；定义 createBlockStateDefinition 的调用入口；向集合或构建器追加条目。
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); } // 实现父类或接口约定；定义 getStateForPlacement 的调用入口；返回 defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())，将当前分支的结果交给调用者。
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); } // 实现父类或接口约定；定义 rotate 的调用入口；返回 state.setValue(FACING, rotation.rotate(state.getValue(FACING)))，将当前分支的结果交给调用者。
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); } // 实现父类或接口约定；定义 mirror 的调用入口；返回 rotate(state, mirror.getRotation(state.getValue(FACING)))，将当前分支的结果交给调用者。
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; } // 实现父类或接口约定；定义 getRenderShape 的调用入口；返回 RenderShape.MODEL，将当前分支的结果交给调用者。
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new SmelteryControllerBlockEntity(pos, state); } // 实现父类或接口约定；定义 newBlockEntity 的调用入口；返回 new SmelteryControllerBlockEntity(pos, state)，将当前分支的结果交给调用者。
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { // 实现父类或接口约定；调用 getTicker，执行当前流程所需的处理。
        return level.isClientSide ? null : createTickerHelper(type, SmelteryRegistration.CONTROLLER_ENTITY.get(), SmelteryControllerBlockEntity::tick); // 返回 level.isClientSide ? null : createTickerHelper(type, SmelteryRegistration.CONTROLLER_ENTITY.get(), Smeltery…，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { // 实现父类或接口约定；定义 useItemOn 的调用入口，参数和具体处理见下方实现。
        open(level, pos, player); return ItemInteractionResult.sidedSuccess(level.isClientSide); // 调用 open，执行当前流程所需的处理。
    } // 结束当前作用域。
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { // 实现父类或接口约定；定义 useWithoutItem 的调用入口，参数和具体处理见下方实现。
        open(level, pos, player); return InteractionResult.sidedSuccess(level.isClientSide); // 调用 open，执行当前流程所需的处理。
    } // 结束当前作用域。
    private void open(Level level, BlockPos pos, Player player) { // 定义 open 的调用入口，参数和具体处理见下方实现。
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryControllerBlockEntity controller) { // 检查 !level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryControllerBlockEntity controller；按条件决定是否执行本分支。
            controller.checkStructure(); // 检查结构并更新其有效性与容量。
            if (controller.isFormed()) player.openMenu(controller, buffer -> buffer.writeBlockPos(pos)); // 检查 controller.isFormed()；按条件决定是否执行本分支。
            else player.displayClientMessage(Component.translatable("message.tconstruct.smeltery.incomplete"), true); // 处理前述条件均不满足时的替代分支。
        } // 结束当前作用域。
    } // 结束当前作用域。
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) { // 实现父类或接口约定；定义 onRemove 的调用入口，参数和具体处理见下方实现。
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryControllerBlockEntity controller) { // 检查 !state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryControllerBlockEntity controller；按条件决定是否执行本分支。
            controller.releaseStructure(); // 调用 releaseStructure，执行当前流程所需的处理。
            Containers.dropContents(level, pos, controller); // 调用 dropContents，执行当前流程所需的处理。
        } // 结束当前作用域。
        super.onRemove(state, level, pos, next, moving); // 调用 onRemove，执行当前流程所需的处理。
    } // 结束当前作用域。
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) { // 实现父类或接口约定；定义 animateTick 的调用入口，参数和具体处理见下方实现。
        if (!state.getValue(ACTIVE)) return; // 检查 !state.getValue(ACTIVE)；按条件决定是否执行本分支。
        Direction face = state.getValue(FACING); // 设置 face 为 state.getValue(FACING)。
        double x = pos.getX() + 0.5 + face.getStepX() * 0.52; // 设置 x 为 pos.getX() + 0.5 + face.getStepX() * 0.52。
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.52; // 设置 z 为 pos.getZ() + 0.5 + face.getStepZ() * 0.52。
        level.addParticle(ParticleTypes.FLAME, x, pos.getY() + 0.25 + random.nextDouble() * 0.5, z, 0, 0, 0); // 调用 addParticle，执行当前流程所需的处理。
    } // 结束当前作用域。
} // 结束当前作用域。
