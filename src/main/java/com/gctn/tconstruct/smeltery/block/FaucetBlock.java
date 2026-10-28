package com.gctn.tconstruct.smeltery.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.entity.FaucetBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.FaucetBlockEntity，供本文件使用。
import com.mojang.serialization.MapCodec; // 引入 com.mojang.serialization.MapCodec，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.Direction; // 引入 net.minecraft.core.Direction，供本文件使用。
import net.minecraft.server.level.ServerLevel; // 引入 net.minecraft.server.level.ServerLevel，供本文件使用。
import net.minecraft.util.RandomSource; // 引入 net.minecraft.util.RandomSource，供本文件使用。
import net.minecraft.world.InteractionResult; // 引入 net.minecraft.world.InteractionResult，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.item.context.BlockPlaceContext; // 引入 net.minecraft.world.item.context.BlockPlaceContext，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.*; // 引入 net.minecraft.world.level.block.*，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntity; // 引入 net.minecraft.world.level.block.entity.BlockEntity，供本文件使用。
import net.minecraft.world.level.block.state.*; // 引入 net.minecraft.world.level.block.state.*，供本文件使用。
import net.minecraft.world.level.block.state.properties.*; // 引入 net.minecraft.world.level.block.state.properties.*，供本文件使用。
import net.minecraft.world.phys.BlockHitResult; // 引入 net.minecraft.world.phys.BlockHitResult，供本文件使用。
import net.minecraft.world.phys.shapes.*; // 引入 net.minecraft.world.phys.shapes.*，供本文件使用。
import net.neoforged.neoforge.capabilities.Capabilities; // 引入 net.neoforged.neoforge.capabilities.Capabilities，供本文件使用。
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction; // 引入 net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction，供本文件使用。

/**
 * 通过服务端计划刻，将浇注口后方的流体能力连接到下方方块。
 * 右键切换浇注状态，红石信号上升沿启动浇注，空闲时不安排传输更新。
 * 实际消耗源流体前，先通过模拟确定允许转移的数量。
 */
public class FaucetBlock extends BaseEntityBlock { // 定义 FaucetBlock 的实现类型，具体职责见上方说明。
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING; // 设置 FACING 为 BlockStateProperties.HORIZONTAL_FACING。
    public static final BooleanProperty POURING = BlockStateProperties.POWERED; // 设置 POURING 为 BlockStateProperties.POWERED。
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED; // 设置 TRIGGERED 为 BlockStateProperties.TRIGGERED。
    private static final MapCodec<FaucetBlock> CODEC = simpleCodec(FaucetBlock::new); // 设置 CODEC 为 simpleCodec(FaucetBlock::new)。
    public FaucetBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POURING, false).setValue(TRIGGERED, false)); } // 定义 FaucetBlock 的调用入口；定义方块放置后的默认状态。
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; } // 实现父类或接口约定；调用 codec，执行当前流程所需的处理。
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FaucetBlockEntity(pos, state); } // 实现父类或接口约定；定义 newBlockEntity 的调用入口；返回 new FaucetBlockEntity(pos, state)，将当前分支的结果交给调用者。
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; } // 实现父类或接口约定；定义 getRenderShape 的调用入口；返回 RenderShape.MODEL，将当前分支的结果交给调用者。
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, POURING, TRIGGERED); } // 实现父类或接口约定；定义 createBlockStateDefinition 的调用入口；向集合或构建器追加条目。
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { // 实现父类或接口约定；定义 getStateForPlacement 的调用入口，参数和具体处理见下方实现。
        Direction face = context.getClickedFace(); // 设置 face 为 context.getClickedFace()。
        return defaultBlockState().setValue(FACING, face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite()); // 返回 defaultBlockState().setValue(FACING, face.getAxis().isHorizontal() ? face : context.getHorizontalDirection(…，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) { // 实现父类或接口约定；定义 getShape 的调用入口，参数和具体处理见下方实现。
        return switch (state.getValue(FACING)) { // 返回 switch (state.getValue(FACING)) {，将当前分支的结果交给调用者。
            case NORTH -> box(4, 4, 10, 12, 10, 16); // 定义 case NORTH 分支，构建或绘制指定范围的形状。
            case EAST -> box(0, 4, 4, 6, 10, 12); // 定义 case EAST 分支，构建或绘制指定范围的形状。
            case SOUTH -> box(4, 4, 0, 12, 10, 6); // 定义 case SOUTH 分支，构建或绘制指定范围的形状。
            default -> box(10, 4, 4, 16, 10, 12); // 定义 default 分支，构建或绘制指定范围的形状。
        }; // 结束当前作用域并完成本次表达式或声明。
    } // 结束当前作用域。
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); } // 实现父类或接口约定；定义 rotate 的调用入口；返回 state.setValue(FACING, rotation.rotate(state.getValue(FACING)))，将当前分支的结果交给调用者。
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); } // 实现父类或接口约定；定义 mirror 的调用入口；返回 rotate(state, mirror.getRotation(state.getValue(FACING)))，将当前分支的结果交给调用者。
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { // 实现父类或接口约定；定义 useWithoutItem 的调用入口，参数和具体处理见下方实现。
        if (!level.isClientSide) { // 检查 !level.isClientSide；按条件决定是否执行本分支。
            level.setBlock(pos, state.cycle(POURING), 3); // 更新世界中的方块状态。
            level.scheduleTick(pos, this, 1); // 安排后续服务端计划刻。
        } // 结束当前作用域。
        return InteractionResult.sidedSuccess(level.isClientSide); // 返回 InteractionResult.sidedSuccess(level.isClientSide)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moving) { // 实现父类或接口约定；定义 neighborChanged 的调用入口，参数和具体处理见下方实现。
        boolean powered = level.hasNeighborSignal(pos); // 设置 powered 为 level.hasNeighborSignal(pos)。
        if (!level.isClientSide && powered != state.getValue(TRIGGERED)) { // 检查 !level.isClientSide && powered != state.getValue(TRIGGERED)；按条件决定是否执行本分支。
            level.setBlock(pos, state.setValue(TRIGGERED, powered).setValue(POURING, powered || state.getValue(POURING)), 3); // 更新世界中的方块状态。
            if (powered) level.scheduleTick(pos, this, 1); // 检查 powered；按条件决定是否执行本分支。
        } // 结束当前作用域。
    } // 结束当前作用域。
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) { // 实现父类或接口约定；定义 onPlace 的调用入口，参数和具体处理见下方实现。
        if (!level.isClientSide && state.getValue(POURING)) level.scheduleTick(pos, this, 1); // 检查 !level.isClientSide && state.getValue(POURING)；按条件决定是否执行本分支。
        if (!level.isClientSide && !state.is(old.getBlock())) neighborChanged(state, level, pos, this, pos, moving); // 检查 !level.isClientSide && !state.is(old.getBlock())；按条件决定是否执行本分支。
    } // 结束当前作用域。
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { // 实现父类或接口约定；定义 tick 的调用入口，参数和具体处理见下方实现。
        var visual = level.getBlockEntity(pos) instanceof FaucetBlockEntity faucet ? faucet : null; // 设置 visual 为 level.getBlockEntity(pos) instanceof FaucetBlockEntity faucet ? faucet : null。
        if (!state.getValue(POURING)) { if (visual != null) visual.setFlowing(net.neoforged.neoforge.fluids.FluidStack.EMPTY); return; } // 检查 !state.getValue(POURING)；按条件决定是否执行本分支。
        Direction facing = state.getValue(FACING); // 设置 facing 为 state.getValue(FACING)。
        BlockPos sourcePos = pos.relative(facing.getOpposite()), targetPos = pos.below(); // 设置 sourcePos 为 pos.relative(facing.getOpposite()), targetPos = pos.below()。
        if (!level.hasChunkAt(sourcePos) || !level.hasChunkAt(targetPos)) { level.scheduleTick(pos, this, 20); return; } // 检查 !level.hasChunkAt(sourcePos) || !level.hasChunkAt(targetPos)；按条件决定是否执行本分支。
        var source = level.getCapability(Capabilities.FluidHandler.BLOCK, sourcePos, facing); // 设置 source 为 level.getCapability(Capabilities.FluidHandler.BLOCK, sourcePos, facing)。
        var target = level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, Direction.UP); // 设置 target 为 level.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, Direction.UP)。
        boolean moved = false; // 设置 moved 为 false。
        if (source != null && target != null) { // 检查 source != null && target != null；按条件决定是否执行本分支。
            var offered = source.drain(6, FluidAction.SIMULATE); // 设置 offered 为 source.drain(6, FluidAction.SIMULATE)。
            int accepted = target.fill(offered, FluidAction.SIMULATE); // 设置 accepted 为 target.fill(offered, FluidAction.SIMULATE)。
            if (accepted > 0) { // 检查 accepted > 0；按条件决定是否执行本分支。
                var drained = source.drain(offered.copyWithAmount(accepted), FluidAction.EXECUTE); // 设置 drained 为 source.drain(offered.copyWithAmount(accepted), FluidAction.EXECUTE)。
                int filled = target.fill(drained, FluidAction.EXECUTE); // 设置 filled 为 target.fill(drained, FluidAction.EXECUTE)。
                // 若第三方处理器在传输期间改变接收结果，尝试将未接收的流体退回源端。
                if (filled < drained.getAmount()) source.fill(drained.copyWithAmount(drained.getAmount() - filled), FluidAction.EXECUTE); // 检查 filled < drained.getAmount()；按条件决定是否执行本分支。
                moved = filled > 0; // 设置 moved 为 filled > 0。
                if (moved && visual != null) visual.setFlowing(drained); // 检查 moved && visual != null；按条件决定是否执行本分支。
            } // 结束当前作用域。
        } // 结束当前作用域。
        if (moved) level.scheduleTick(pos, this, 1); // 检查 moved；按条件决定是否执行本分支。
        else { // 处理前述条件均不满足时的替代分支。
            level.setBlock(pos, state.setValue(POURING, false), 3); // 更新世界中的方块状态。
            if (visual != null) visual.setFlowing(net.neoforged.neoforge.fluids.FluidStack.EMPTY); // 检查 visual != null；按条件决定是否执行本分支。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
