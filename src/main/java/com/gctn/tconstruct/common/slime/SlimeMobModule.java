/*
 * 根据 TinkersAntique 的 TinkerWorld 史莱姆实体与刷怪蛋注册移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/** 实体跟踪范围、刷新间隔、颜色和刷怪蛋配色均来自原版注册。 */
public final class SlimeMobModule {
    public static final int BLUE_TINT = 0xff67f0f5;
    public static final int PURPLE_TINT = 0xefd236ff;
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TinkersNirvana.MODID);
    public static final Supplier<EntityType<AntiqueSlime>> BLUE = entity("blueslime");
    public static final Supplier<EntityType<AntiqueSlime>> PURPLE = entity("purpleslime");
    public static final Supplier<Item> BLUE_EGG = ItemRegister.ITEMS.register("blueslime_spawn_egg", () ->
            new DeferredSpawnEggItem(BLUE, 0x47eff5, 0xacfff4, new Item.Properties()));
    public static final Supplier<Item> PURPLE_EGG = ItemRegister.ITEMS.register("purpleslime_spawn_egg", () ->
            new DeferredSpawnEggItem(PURPLE, 0x6300b0, 0xd37cff, new Item.Properties()));

    private SlimeMobModule() {
    }

    private static Supplier<EntityType<AntiqueSlime>> entity(String name) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(AntiqueSlime::new, MobCategory.MONSTER)
                .sized(0.51000005F, 0.51000005F).eyeHeight(0.31875003F).spawnDimensionsScale(4)
                .clientTrackingRange(4).updateInterval(1).build(TinkersNirvana.MODID + ":" + name));
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(SlimeMobModule::attributes);
        bus.addListener(SlimeMobModule::placements);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(BLUE.get(), DefaultAttributes.getSupplier(EntityType.SLIME));
        event.put(PURPLE.get(), DefaultAttributes.getSupplier(EntityType.SLIME));
    }

    private static void placements(RegisterSpawnPlacementsEvent event) {
        for (var type : List.of(BLUE.get(), PURPLE.get())) {
            registerPlacement(event, type);
        }
    }

    private static void registerPlacement(RegisterSpawnPlacementsEvent event, EntityType<AntiqueSlime> type) {
        event.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AntiqueSlime::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
