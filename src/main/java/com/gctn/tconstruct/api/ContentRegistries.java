package com.gctn.tconstruct.api;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

/** Startup definition registries. Existing material and trait lookups stay authoritative at runtime. */
public final class ContentRegistries {
    public static final ResourceKey<Registry<MaterialDefinition>> MATERIAL = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "material"));
    public static final ResourceKey<Registry<TraitDefinition>> TRAIT = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("tconstruct", "trait"));
    public static final Registry<MaterialDefinition> MATERIALS = new RegistryBuilder<>(MATERIAL).create();
    public static final Registry<TraitDefinition> TRAITS = new RegistryBuilder<>(TRAIT).create();

    private ContentRegistries() {}

    public static void register(IEventBus bus) {
        bus.addListener((NewRegistryEvent event) -> {
            event.register(MATERIALS);
            event.register(TRAITS);
        });
    }
}
