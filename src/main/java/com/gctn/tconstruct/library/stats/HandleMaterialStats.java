package com.gctn.tconstruct.library.stats;

/**
 * 手柄提供耐久倍率，以及在乘算完成后加上的固定耐久。
 * Antique 允许固定耐久为负值，例如石制手柄；最终工具耐久会限制在有效范围内。
 */
public class HandleMaterialStats extends AbstractMaterialStats {
    public final int durability;
    public final float modifer;
    public HandleMaterialStats(float modifer, int durability) {
        super("Handle");
        this.modifer = modifer;
        this.durability = durability;
    }
}
