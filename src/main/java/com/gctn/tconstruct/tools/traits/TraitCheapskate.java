package com.gctn.tconstruct.tools.traits;

import net.minecraft.world.item.ItemStack;

/**
 * Applies Antique Cheapskate once to final assembled durability.
 * It rounds down to 80 percent and keeps at least one durability so the stack remains a valid tool.
 */
public final class TraitCheapskate extends AbstractTraits {
    public TraitCheapskate() {
        super("cheapskate", 1, -1, 0xFFAAAAAA);
    }

    @Override
    public int modifyDurability(ItemStack tool, int level, int durability) {
        return Math.max(1, (int) (durability * 80L / 100));
    }
}
