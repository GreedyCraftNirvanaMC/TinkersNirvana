package com.gctn.tconstruct.tools.traits;

import net.minecraft.world.item.ItemStack;

public final class CheapskateTraits extends AbstractTraits {
    public CheapskateTraits() {
        super("cheapskate", 1, 1, -1, 0xFFAAAAAA);
    }

    @Override
    public int modifyDurability(ItemStack tool, int level, int durability) {
        return Math.max(1, (int) (durability * 80L / 100));
    }
}
