package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.entity.Entity; // 引入 net.minecraft.world.entity.Entity，供本文件使用。
import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.neoforged.neoforge.event.entity.player.PlayerEvent; // 引入 net.neoforged.neoforge.event.entity.player.PlayerEvent，供本文件使用。
import net.neoforged.neoforge.event.level.BlockEvent; // 引入 net.neoforged.neoforge.event.level.BlockEvent，供本文件使用。

/**
 * 可选的词条生命周期回调，未覆盖的方法保持原有行为不变。
 * 数量修正回调同时接收原始值和累计值，使有序执行的多个词条能够稳定组合。
 * 预览回调必须具有确定性，且只能修改传入的工具副本。
 */
public interface ITraits { // 定义 ITraits 的行为契约，具体职责见上方说明。
    /** 仅服务端调用；traitTag 的修改由分发器写回，不要同时直接替换本词条的组件数据。 */
    default void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, // 调用 inventoryTick，执行当前流程所需的处理。
                               boolean isSelected, CompoundTag traitTag) {} // 在 ITraits 中补充当前声明或表达式的 boolean isSelected, CompoundTag traitTag) {} 部分。

    default int modifyDurability(ItemStack tool, int level, int durability) { return durability; } // 调用 modifyDurability，执行当前流程所需的处理。

    default int onToolDamage(ItemStack tool, int level, int original, int amount, LivingEntity entity) { // 调用 onToolDamage，执行当前流程所需的处理。
        return amount; // 返回 amount，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 材料修复时 entity 为 null；计算必须确定且无外部副作用。 */
    default int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) { // 调用 onToolHeal，执行当前流程所需的处理。
        return amount; // 返回 amount，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 仅修改模拟结果的词条状态，不能改变耐久/材料；预览会重复调用，不能有外部副作用或随机数。 */
    default void onRepair(ItemStack tool, int level, int healedAmount) {} // 调用 onRepair，执行当前流程所需的处理。

    /** 挖掘速度在双端计算，不能在这里修改工具数据或世界。 */
    default void miningSpeed(ItemStack tool, int level, PlayerEvent.BreakSpeed event) {} // 调用 miningSpeed，执行当前流程所需的处理。

    default void beforeBlockBreak(ItemStack tool, int level, BlockEvent.BreakEvent event) {} // 调用 beforeBlockBreak，执行当前流程所需的处理。

    default void afterBlockBreak(ItemStack tool, int level, Level world, BlockState state, BlockPos pos, // 调用 afterBlockBreak，执行当前流程所需的处理。
                                 LivingEntity entity, boolean effective) {} // 在 ITraits 中补充当前声明或表达式的 LivingEntity entity, boolean effective) {} 部分。

    default void afterHit(ItemStack tool, int level, LivingEntity target, LivingEntity attacker) {} // 调用 afterHit，执行当前流程所需的处理。
} // 结束当前作用域。
