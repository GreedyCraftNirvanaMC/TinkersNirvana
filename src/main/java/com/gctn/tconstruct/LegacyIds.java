package com.gctn.tconstruct;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class LegacyIds {
    private static final String NAMESPACE = "tconstruct";

    private LegacyIds() {}

    static void migrateConfig() {
        if (ModList.get().isLoaded(NAMESPACE)) { return; }
        Path directory = FMLPaths.CONFIGDIR.get();
        Path oldConfig = directory.resolve(NAMESPACE + "-common.toml");
        Path newConfig = directory.resolve(TinkersNirvana.MODID + "-common.toml");
        if (!Files.isRegularFile(oldConfig) || Files.exists(newConfig)) { return; }
        try {
            Files.copy(oldConfig, newConfig);
        } catch (IOException exception) {
            TinkersNirvana.LOGGER.warn("Could not copy legacy configuration from {} to {}", oldConfig, newConfig, exception);
        }
    }

    static void registerAliases(RegisterEvent event) {
        // 如果安装了使用旧命名空间的其他模组，不能把它的存档 ID 映射到本模组。
        if (ModList.get().isLoaded(NAMESPACE)) { return; }

        var registry = event.getRegistry();
        for (ResourceLocation id : registry.keySet()) {
            if (id.getNamespace().equals(TinkersNirvana.MODID)) {
                registry.addAlias(ResourceLocation.fromNamespaceAndPath(NAMESPACE, id.getPath()), id);
            }
        }

        if (event.getRegistryKey().equals(Registries.BLOCK) || event.getRegistryKey().equals(Registries.ITEM)) {
            // 该方块和物品在命名空间变更前已由原版铜矿石取代。
            registry.addAlias(ResourceLocation.fromNamespaceAndPath(NAMESPACE, "copper_ore"), ResourceLocation.withDefaultNamespace("copper_ore"));
        }
    }
}
