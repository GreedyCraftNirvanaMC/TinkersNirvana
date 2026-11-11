package com.gctn.tconstruct.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Nested translatable fallbacks remain translatable when sent from a server. */
public final class ContentNames {
    private ContentNames() {}

    public static String key(String kind, String id, String suffix) {
        return kind + "." + id.replace(':', '.').replace('/', '.') + "." + suffix;
    }

    public static Component translated(String key, Component fallback) {
        return Component.translatableWithFallback(key, "%s", fallback);
    }

    public static String traitId(String id) {
        ResourceLocation key = ResourceLocation.parse(id);
        String namespace = id.contains(":") ? key.getNamespace() : "tconstruct";
        return namespace + ":" + key.getPath();
    }
}
