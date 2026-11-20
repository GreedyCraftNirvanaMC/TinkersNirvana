package com.gctn.tconstruct.api;

import com.gctn.tconstruct.TinkersNirvana;
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
        String namespace = key.getNamespace();
        if (!id.contains(":") || namespace.equals("tconstruct")) {
            namespace = TinkersNirvana.MODID;
        }
        return namespace + ":" + key.getPath();
    }
}
