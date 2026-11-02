/*
 * 根据 TinkersAntique 的 clearGlassSilkTouch 掉落配置移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.loot;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/** 每次掉落时读取配置，数据包无需重载即可响应管理员的配置变更。 */
public final class ClearGlassWithoutSilk implements LootItemCondition {
    public static final ClearGlassWithoutSilk INSTANCE = new ClearGlassWithoutSilk();
    public static final MapCodec<ClearGlassWithoutSilk> CODEC = MapCodec.unit(INSTANCE);

    private ClearGlassWithoutSilk() {
    }

    @Override
    public LootItemConditionType getType() {
        return SmelteryModule.CLEAR_GLASS_WITHOUT_SILK.get();
    }

    @Override
    public boolean test(LootContext context) {
        return !Config.CLEAR_GLASS_SILK_TOUCH.get();
    }
}
