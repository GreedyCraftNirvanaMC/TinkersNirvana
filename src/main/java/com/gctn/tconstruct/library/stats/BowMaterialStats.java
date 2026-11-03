/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.library.stats;

public class BowMaterialStats extends AbstractMaterialStats {
    public final float drawSpeed;
    public final float range;
    public final float bonusDamage;
    public BowMaterialStats(float drawSpeed, float range, float bonusDamage) {
        super("Bow");
        this.drawSpeed = drawSpeed;
        this.range = range;
        this.bonusDamage = bonusDamage;
    }
}
