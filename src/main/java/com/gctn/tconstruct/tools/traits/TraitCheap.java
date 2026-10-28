package com.gctn.tconstruct.tools.traits;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Adds Antique Cheap healing from the original amount, rounded down.
 * Using the original value avoids compounding earlier trait bonuses, and long arithmetic avoids overflow.
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
