package com.gctn.tconstruct.compat.kubejs;

import com.gctn.tconstruct.api.ContentRegistries;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.registry.RegistryType;
import dev.latvian.mods.rhino.type.TypeInfo;
import com.gctn.tconstruct.api.MaterialDefinition;
import com.gctn.tconstruct.api.TraitDefinition;

public final class TinkerKubeJSPlugin implements KubeJSPlugin {
    private static final EventGroup EVENTS = EventGroup.of("TinkerEvents");
    private static final EventHandler MODIFY_TRAITS = EVENTS.startup("modifyTraits", () -> TraitModificationEvent.class);
    private static final EventHandler MODIFY_MATERIALS = EVENTS.startup("modifyMaterials", () -> MaterialModificationEvent.class);

    @Override public void init() {
        RegistryType.register(ContentRegistries.MATERIAL, TypeInfo.of(MaterialDefinition.class));
        RegistryType.register(ContentRegistries.TRAIT, TypeInfo.of(TraitDefinition.class));
    }

    @Override public void registerEvents(EventGroupRegistry registry) { registry.register(EVENTS); }

    @Override public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.addDefault(ContentRegistries.MATERIAL, MaterialKubeBuilder.class, MaterialKubeBuilder::new);
        registry.addDefault(ContentRegistries.TRAIT, TraitKubeBuilder.class, TraitKubeBuilder::new);
    }

    @Override public void afterInit() {
        TinkerTraits.registerTraits();
        installDefinitions(ContentRegistries.TRAITS, TinkerTraits::registerTrait);
        MODIFY_TRAITS.post(ScriptType.STARTUP, new TraitModificationEvent());
        installDefinitions(ContentRegistries.MATERIALS, MaterialDefinition::install);
        MaterialModificationEvent materials = new MaterialModificationEvent();
        MODIFY_MATERIALS.post(ScriptType.STARTUP, materials);
        materials.apply();
    }
    private static <T> void installDefinitions(net.minecraft.core.Registry<T> registry, java.util.function.Consumer<T> action) {
        net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>> key = net.minecraft.resources.ResourceKey.createRegistryKey(registry.key().location());
        var builders = dev.latvian.mods.kubejs.registry.RegistryObjectStorage.of(key);
        for (T definition : registry) {
            var id = registry.getKey(definition);
            var builder = builders.objects.get(id);
            var line = builder == null ? dev.latvian.mods.kubejs.script.SourceLine.UNKNOWN : builder.sourceLine;
            try {
                action.accept(definition);
            } catch (RuntimeException exception) {
                throw new dev.latvian.mods.kubejs.error.KubeRuntimeException("Invalid definition " + id + " at " + line, exception).source(line);
            }
        }
    }
}
