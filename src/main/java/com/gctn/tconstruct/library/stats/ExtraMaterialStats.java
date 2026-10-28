package com.gctn.tconstruct.library.stats;

/**
 * 额外部件提供的固定耐久，在手柄系数生效前与头部耐久相加。
 * 绑定结不决定挖掘速度，也不决定工具可以使用哪种材料修复。
 */
public class ExtraMaterialStats extends AbstractMaterialStats {
    public final int durability;
    public ExtraMaterialStats(int durability) {
        super("Extra");
        this.durability = durability;
    }
}
