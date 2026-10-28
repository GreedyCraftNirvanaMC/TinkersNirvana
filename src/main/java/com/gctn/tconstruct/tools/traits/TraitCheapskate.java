package com.gctn.tconstruct.tools.traits;

import net.minecraft.world.item.ItemStack;

/**
 * 对组装后的最终耐久应用一次 Antique 吝啬词条效果。
 * 耐久取原值的 80% 并向下取整，最低保留 1 点，保证物品栈仍是有效工具。
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
