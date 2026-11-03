/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.common;

import com.gctn.tconstruct.Config;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

/** Checked by the native conditional recipe loader when datapacks reload. */
public record FlintRecipeCondition() implements ICondition {
    public static final MapCodec<FlintRecipeCondition> CODEC = MapCodec.unit(FlintRecipeCondition::new);
    @Override public boolean test(IContext context) { return Config.FLINT_RECIPE.get(); }
    @Override public MapCodec<? extends ICondition> codec() { return CODEC; }
}
