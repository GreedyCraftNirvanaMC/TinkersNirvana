package com.gctn.tconstruct.tools.traits;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** 生命周期回调默认无效果，具体词条只需覆盖自己关心的触发点。 */
public interface ITraits {
    /** 仅服务端调用；traitTag 的修改由分发器写回，不要同时直接替换本词条的组件数据。 */
    default void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId,
                               boolean isSelected, CompoundTag traitTag) {}

    default int modifyDurability(ItemStack tool, int level, int durability) { return durability; }

    default int onToolDamage(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
        return amount;
    }

    /** 材料修复时 entity 为 null；计算必须确定且无外部副作用。 */
    default int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
        return amount;
    }

    /** 仅修改模拟结果的词条状态，不能改变耐久/材料；预览会重复调用，不能有外部副作用或随机数。 */
    default void onRepair(ItemStack tool, int level, int healedAmount) {}

    /** 挖掘速度在双端计算，不能在这里修改工具数据或世界。 */
    default void miningSpeed(ItemStack tool, int level, PlayerEvent.BreakSpeed event) {}

    default void beforeBlockBreak(ItemStack tool, int level, BlockEvent.BreakEvent event) {}

    default void afterBlockBreak(ItemStack tool, int level, Level world, BlockState state, BlockPos pos,
                                 LivingEntity entity, boolean effective) {}

    default void afterHit(ItemStack tool, int level, LivingEntity target, LivingEntity attacker) {}
}
