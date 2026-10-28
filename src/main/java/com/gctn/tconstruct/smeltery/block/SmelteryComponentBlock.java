package com.gctn.tconstruct.smeltery.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.entity.*; // 引入 com.gctn.tconstruct.smeltery.block.entity.*，供本文件使用。
import com.mojang.serialization.Codec; // 引入 com.mojang.serialization.Codec，供本文件使用。
import com.mojang.serialization.MapCodec; // 引入 com.mojang.serialization.MapCodec，供本文件使用。
import com.mojang.serialization.codecs.RecordCodecBuilder; // 引入 com.mojang.serialization.codecs.RecordCodecBuilder，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.world.InteractionHand; // 引入 net.minecraft.world.InteractionHand，供本文件使用。
import net.minecraft.world.ItemInteractionResult; // 引入 net.minecraft.world.ItemInteractionResult，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.BlockGetter; // 引入 net.minecraft.world.level.BlockGetter，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.*; // 引入 net.minecraft.world.level.block.*，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntity; // 引入 net.minecraft.world.level.block.entity.BlockEntity，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.minecraft.world.phys.BlockHitResult; // 引入 net.minecraft.world.phys.BlockHitResult，供本文件使用。
import net.neoforged.neoforge.fluids.FluidUtil; // 引入 net.neoforged.neoforge.fluids.FluidUtil，供本文件使用。

/**
 * 具有归属关系的冶炼炉构件，分别提供砖块、玻璃、排液口或熔岩储罐行为。
 * 只有排液口暴露控制器中的熔液库存，燃料储罐单独保存熔岩。
 */
public class SmelteryComponentBlock extends BaseEntityBlock { // 定义 SmelteryComponentBlock 的实现类型，具体职责见上方说明。
    public enum Kind { BRICK, GLASS, TANK, DRAIN } // 定义 Kind 的固定枚举取值，具体职责见上方说明。
    private static final MapCodec<SmelteryComponentBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group( // 设置 CODEC 为 RecordCodecBuilder.mapCodec(i -> i.group(。
            propertiesCodec(), Codec.STRING.xmap(Kind::valueOf, Kind::name).fieldOf("kind").forGetter(b -> b.kind) // 定义回调或映射规则，在对应条目被处理时执行。
    ).apply(i, SmelteryComponentBlock::new)); // 调用 apply，执行当前流程所需的处理。
    private final Kind kind; // 声明 kind，保存后续流程使用的状态或依赖。
    public SmelteryComponentBlock(Properties properties, Kind kind) { super(properties); this.kind = kind; } // 定义 SmelteryComponentBlock 的调用入口；在 SmelteryComponentBlock 中补充当前声明或表达式的 super(properties); this.kind = kind 部分。
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; } // 实现父类或接口约定；调用 codec，执行当前流程所需的处理。
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; } // 实现父类或接口约定；定义 getRenderShape 的调用入口；返回 RenderShape.MODEL，将当前分支的结果交给调用者。
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { // 实现父类或接口约定；定义 newBlockEntity 的调用入口，参数和具体处理见下方实现。
        return kind == Kind.TANK ? new SearedTankBlockEntity(pos, state) : new SmelteryComponentBlockEntity(pos, state); // 返回 kind == Kind.TANK ? new SearedTankBlockEntity(pos, state) : new SmelteryComponentBlockEntity(pos, state)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) { // 实现父类或接口约定；定义 useItemOn 的调用入口，参数和具体处理见下方实现。
        if (kind != Kind.TANK && kind != Kind.DRAIN) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // 检查 kind != Kind.TANK && kind != Kind.DRAIN；按条件决定是否执行本分支。
        if (level.isClientSide) return ItemInteractionResult.SUCCESS; // 检查 level.isClientSide；按条件决定是否执行本分支。
        var entity = (SmelteryComponentBlockEntity) level.getBlockEntity(pos); // 设置 entity 为 (SmelteryComponentBlockEntity) level.getBlockEntity(pos)。
        if (entity != null && FluidUtil.interactWithFluidHandler(player, hand, // 检查 entity != null && FluidUtil.interactWithFluidHandler(player, hand,；按条件决定是否执行本分支。
                entity instanceof SearedTankBlockEntity tank ? tank.tank() : entity.drainHandler())) return ItemInteractionResult.SUCCESS; // 调用 tank，执行当前流程所需的处理。
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // 返回 ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) { // 实现父类或接口约定；定义 onRemove 的调用入口，参数和具体处理见下方实现。
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.notifyController(); // 检查 !state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part；按条件决定是否执行本分支。
        super.onRemove(state, level, pos, next, moving); // 调用 onRemove，执行当前流程所需的处理。
    } // 结束当前作用域。
    @Override protected boolean skipRendering(BlockState state, BlockState adjacent, net.minecraft.core.Direction side) { // 实现父类或接口约定；定义 skipRendering 的调用入口，参数和具体处理见下方实现。
        return kind == Kind.GLASS && adjacent.is(this) || super.skipRendering(state, adjacent, side); // 返回 kind == Kind.GLASS && adjacent.is(this) || super.skipRendering(state, adjacent, side)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return kind == Kind.TANK; } // 实现父类或接口约定；定义 hasAnalogOutputSignal 的调用入口；返回 kind == Kind.TANK，将当前分支的结果交给调用者。
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { // 实现父类或接口约定；定义 getAnalogOutputSignal 的调用入口，参数和具体处理见下方实现。
        return level.getBlockEntity(pos) instanceof SearedTankBlockEntity tank ? tank.tank().getFluidAmount() * 15 / SearedTankBlockEntity.CAPACITY : 0; // 返回 level.getBlockEntity(pos) instanceof SearedTankBlockEntity tank ? tank.tank().getFluidAmount() * 15 / Seare…，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return kind == Kind.GLASS; } // 实现父类或接口约定；定义 propagatesSkylightDown 的调用入口；返回 kind == Kind.GLASS，将当前分支的结果交给调用者。
} // 结束当前作用域。
