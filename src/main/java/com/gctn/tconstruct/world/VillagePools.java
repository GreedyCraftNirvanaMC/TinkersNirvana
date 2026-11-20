/* SPDX-License-Identifier: LGPL-3.0-only; Antique's workshop and smeltery village additions. */
package com.gctn.tconstruct.world;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
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
            var pool = registry.get(ResourceLocation.withDefaultNamespace("village/" + biome + "/houses"));
            var additions = registry.get(WorldModule.id("village/" + biome + "/antique"));
            if (pool == null || additions == null) { continue; }
            ResourceLocation workshop = WorldModule.id("village/" + biome + "/tool_workshop");
            ResourceLocation smeltery = WorldModule.id("village/" + biome + "/smeltery");
            pool.rawTemplates = new ArrayList<>(pool.rawTemplates);
            for (var pair : additions.rawTemplates) {
                ResourceLocation template = location(pair.getFirst());
                // 数据包可以改权重或预先向原版池加入同一模板。按模板 ID 区分建筑并避免重复。
                if (pool.rawTemplates.stream().anyMatch(existing -> existing.getFirst() == pair.getFirst()
                        || template != null && template.equals(location(existing.getFirst())))) { continue; }
                int weight = workshop.equals(template) ? Config.VILLAGE_WORKSHOP_WEIGHT.get()
                        : smeltery.equals(template) ? Config.VILLAGE_SMELTERY_WEIGHT.get() : pair.getSecond();
                pool.rawTemplates.add(Pair.of(pair.getFirst(), weight));
                for (int i = 0; i < weight; i++) { pool.templates.add(pair.getFirst()); }
            }
            pool.maxSize = Integer.MIN_VALUE;
        }
    }

    private static ResourceLocation location(StructurePoolElement element) {
        return element instanceof SinglePoolElement single ? single.template.left().orElse(null) : null;
    }
}
