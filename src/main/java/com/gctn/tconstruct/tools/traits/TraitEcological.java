package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.tools.TinkerTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 工具由生物携带时，尝试缓慢恢复其耐久。
 * 只由服务端进行随机判定；跳过正在使用的工具，healTool 也会拒绝恢复已损坏工具。
 */
public final class TraitEcological extends AbstractTraits {
    public TraitEcological() {
        super("ecological", 1, -1, 0xff8e661b);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CompoundTag traitTag) {
        // 每个背包 tick 有 1/400 概率恢复 1 点；正在使用或已损坏的工具不能被动修复。
        if (!level.isClientSide && entity instanceof LivingEntity living && living.getUseItem() != stack
                && level.random.nextInt(400) == 0) {
            TinkerTools.healTool(stack, 1, living);
        }
    }
}
