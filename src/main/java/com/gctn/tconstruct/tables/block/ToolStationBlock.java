package com.gctn.tconstruct.tables.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity; // 引入 com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity，供本文件使用。
import com.mojang.serialization.MapCodec; // 引入 com.mojang.serialization.MapCodec，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.world.Containers; // 引入 net.minecraft.world.Containers，供本文件使用。
import net.minecraft.world.InteractionHand; // 引入 net.minecraft.world.InteractionHand，供本文件使用。
import net.minecraft.world.InteractionResult; // 引入 net.minecraft.world.InteractionResult，供本文件使用。
import net.minecraft.world.ItemInteractionResult; // 引入 net.minecraft.world.ItemInteractionResult，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.BaseEntityBlock; // 引入 net.minecraft.world.level.block.BaseEntityBlock，供本文件使用。
import net.minecraft.world.level.block.RenderShape; // 引入 net.minecraft.world.level.block.RenderShape，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntity; // 引入 net.minecraft.world.level.block.entity.BlockEntity，供本文件使用。
import net.minecraft.world.level.block.state.BlockBehaviour; // 引入 net.minecraft.world.level.block.state.BlockBehaviour，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.minecraft.world.phys.BlockHitResult; // 引入 net.minecraft.world.phys.BlockHitResult，供本文件使用。

import javax.annotation.Nullable; // 引入 javax.annotation.Nullable，供本文件使用。

/**
 * 打开服务端管理的工具台菜单，并在拆除时只掉落真实库存。
 * 调用原版库存掉落逻辑前，必须清除派生的合成结果和尚未确认的拆解部件预览。
 */
public class ToolStationBlock extends BaseEntityBlock { // 定义 ToolStationBlock 的实现类型，具体职责见上方说明。
    public static final MapCodec<ToolStationBlock> CODEC = simpleCodec(ToolStationBlock::new); // 设置 CODEC 为 simpleCodec(ToolStationBlock::new)。

    public ToolStationBlock(BlockBehaviour.Properties properties) { // 定义 ToolStationBlock 的调用入口，参数和具体处理见下方实现。
        super(properties); // 向当前声明或调用传入 super(properties)。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected MapCodec<ToolStationBlock> codec() { // 定义 codec 的调用入口，参数和具体处理见下方实现。
        return CODEC; // 返回 CODEC，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Nullable // 标明此返回值或参数允许为空，调用者需处理缺失情况。
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { // 定义 newBlockEntity 的调用入口，参数和具体处理见下方实现。
        return new ToolStationBlockEntity(pos, state); // 返回 new ToolStationBlockEntity(pos, state)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected RenderShape getRenderShape(BlockState state) { // 定义 getRenderShape 的调用入口，参数和具体处理见下方实现。
        return RenderShape.MODEL; // 返回 RenderShape.MODEL，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected ItemInteractionResult useItemOn( // 定义 useItemOn 的调用入口，参数和具体处理见下方实现。
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult // 在 ToolStationBlock 中补充当前声明或表达式的 ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHit… 部分。
    ) { // 在 ToolStationBlock 中补充当前声明或表达式的 ) { 部分。
        this.openMenu(level, pos, player); // 调用 openMenu，执行当前流程所需的处理。
        return ItemInteractionResult.sidedSuccess(level.isClientSide); // 返回 ItemInteractionResult.sidedSuccess(level.isClientSide)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) { // 定义 useWithoutItem 的调用入口，参数和具体处理见下方实现。
        this.openMenu(level, pos, player); // 调用 openMenu，执行当前流程所需的处理。
        return InteractionResult.sidedSuccess(level.isClientSide); // 返回 InteractionResult.sidedSuccess(level.isClientSide)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) { // 定义 onRemove 的调用入口，参数和具体处理见下方实现。
        if (state.is(newState.getBlock())) { // 检查 state.is(newState.getBlock())；按条件决定是否执行本分支。
            super.onRemove(state, level, pos, newState, movedByPiston); // 调用 onRemove，执行当前流程所需的处理。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        if (level.getBlockEntity(pos) instanceof ToolStationBlockEntity blockEntity) { // 检查 level.getBlockEntity(pos) instanceof ToolStationBlockEntity blockEntity；按条件决定是否执行本分支。
            blockEntity.prepareForRemoval(); // 调用 prepareForRemoval，执行当前流程所需的处理。
        } // 结束当前作用域。
        Containers.dropContentsOnDestroy(state, newState, level, pos); // 调用 dropContentsOnDestroy，执行当前流程所需的处理。
        super.onRemove(state, level, pos, newState, movedByPiston); // 调用 onRemove，执行当前流程所需的处理。
    } // 结束当前作用域。

    private void openMenu(Level level, BlockPos pos, Player player) { // 定义 openMenu 的调用入口，参数和具体处理见下方实现。
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ToolStationBlockEntity blockEntity) { // 检查 !level.isClientSide && level.getBlockEntity(pos) instanceof ToolStationBlockEntity blockEntity；按条件决定是否执行本分支。
            player.openMenu(blockEntity); // 调用 openMenu，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
