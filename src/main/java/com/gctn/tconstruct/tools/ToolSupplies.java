/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ToolSupplies {
    public static final Map<String, Supplier<? extends Item>> ITEMS = new LinkedHashMap<>();
    static {
        for (String id : new String[]{"reinforcement", "necrotic_bone", "silky_jewel", "lava_crystal", "mending_moss",
                "expander_horizontal", "expander_vertical", "creative_modifier", "bloodbone", "silky_cloth", "moss"}) {
            ITEMS.put(id, ItemRegister.ITEMS.register(id, () -> new Item(new Item.Properties())));
        }
        ITEMS.put("bacon", ItemRegister.ITEMS.register("bacon", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(.6F).build()))));
    }
    private ToolSupplies() {}
    public static Item item(String id) { return ITEMS.get(id).get(); }
    public static void init() {}
}
