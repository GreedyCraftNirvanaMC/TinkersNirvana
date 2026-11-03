/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's GadgetEvents. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.Set;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class SpaghettiLoot {
    private static final Set<String> TABLES = Set.of("minecraft:chests/desert_pyramid", "minecraft:chests/stronghold_crossing", "minecraft:chests/jungle_temple");
    private SpaghettiLoot() {}
    @SubscribeEvent private static void load(LootTableLoadEvent event) {
        if (TABLES.contains(event.getName().toString()) && event.getTable().getPool("tconstruct:spaghetti") == null) {
            event.getTable().addPool(LootPool.lootPool().name("tconstruct:spaghetti").setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(.05F)).add(LootItem.lootTableItem(Spaghetti.HARD.get())).build());
        }
    }
}
