/* SPDX-License-Identifier: LGPL-3.0-only; Antique's configurable entity-class integrations. */
package com.gctn.tconstruct.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.function.Predicate;

public final class ConfiguredEntities {
    private static final Map<String, String> ALIASES = Map.of("minecraft:snowman", "minecraft:snow_golem", "minecraft:villager_golem", "minecraft:iron_golem",
            "minecraft:evocation_illager", "minecraft:evoker", "minecraft:illusion_illager", "minecraft:illusioner", "minecraft:vindication_illager", "minecraft:vindicator", "minecraft:zombie_pigman", "minecraft:zombified_piglin");
    private ConfiguredEntities() {}
    /** Creates a predicate once per configuration snapshot. Prototypes are never added to the world or retained. */
    public static Predicate<LivingEntity> matcher(String name, String subtypes, ServerLevel level) {
        if (!subtypes.equalsIgnoreCase("true") && !subtypes.equalsIgnoreCase("false")) { throw new IllegalArgumentException("subtypes must be true or false"); }
        if (name.startsWith("#")) {
            var tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(name.substring(1)));
            return entity -> entity.getType().is(tag);
        }
        ResourceLocation id = ResourceLocation.parse(ALIASES.getOrDefault(name, name));
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) { return entity -> false; }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (!Boolean.parseBoolean(subtypes)) { return entity -> entity.getType() == type; }
        if (type == EntityType.PLAYER) { return entity -> entity instanceof Player; }
        Entity prototype = type.create(level);
        if (!(prototype instanceof LivingEntity)) { return entity -> false; }
        Class<?> family = prototype.getClass();
        return family::isInstance;
    }
}
