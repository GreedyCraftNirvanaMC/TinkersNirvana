/* SPDX-License-Identifier: LGPL-3.0-only; Antique's workshop and smeltery village additions. */
package com.gctn.tconstruct.world;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class VillagePools {
    private VillagePools() {}
    @SubscribeEvent private static void add(ServerAboutToStartEvent event) {
        if (!Config.GEN_VILLAGE_STRUCTURES.get()) { return; }
        var registry = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        for (String biome : new String[]{"plains", "desert", "savanna", "snowy", "taiga"}) {
            var pool = registry.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("village/" + biome + "/houses"));
            var additions = registry.get(WorldModule.id("village/" + biome + "/antique"));
            if (pool == null || additions == null) { continue; }
            // A registry instance may be reused by an integrated server. Element identity makes this idempotent.
            pool.rawTemplates = new ArrayList<>(pool.rawTemplates);
            for (var pair : additions.rawTemplates) {
                if (pool.rawTemplates.stream().anyMatch(existing -> existing.getFirst() == pair.getFirst())) { continue; }
                int weight = pair.getSecond() == 30 ? Config.VILLAGE_WORKSHOP_WEIGHT.get() : Config.VILLAGE_SMELTERY_WEIGHT.get();
                pool.rawTemplates.add(Pair.of(pair.getFirst(), weight));
                for (int i = 0; i < weight; i++) { pool.templates.add(pair.getFirst()); }
            }
            pool.maxSize = Integer.MIN_VALUE;
        }
    }
}
