/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.library.stats;

public class ArrowShaftMaterialStats extends AbstractMaterialStats {
    public final float modifier;
    public final int bonusAmmo;
    public ArrowShaftMaterialStats(float modifier, int bonusAmmo) {
        super("Shaft"); this.modifier = modifier; this.bonusAmmo = bonusAmmo;
    }
}
