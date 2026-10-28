package com.gctn.tconstruct.tools.traits;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 根据原始恢复量计算 Antique 廉价词条的额外治疗，结果向下取整。
 * 使用原始值可避免重复放大前面词条的加成，使用 long 运算防止整数溢出。
 */
public final class TraitCheap extends AbstractTraits {
    public TraitCheap() {
        super("cheap", 1, -1, 0xFF555555);
    }

    @Override
    public int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
        return (int) Math.min(Integer.MAX_VALUE, (long) amount + original * 5L / 100);
    }
}
