package com.gctn.tconstruct.tools.traits;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CheapTraits extends AbstractTraits {
    public CheapTraits() {
        super("cheap", 1, -1, 0xFF555555);
    }

    @Override
    public int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
        return (int) Math.min(Integer.MAX_VALUE, (long) amount + original * 5L / 100);
    }
}
