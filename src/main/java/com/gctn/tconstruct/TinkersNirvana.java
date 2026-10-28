package com.gctn.tconstruct;

import com.gctn.tconstruct.init.ToolPartsRegistryBus;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.init.ToolsRegistryBus;
import com.gctn.tconstruct.library.MaterialList;
import com.gctn.tconstruct.init.TableRegistryBus;
import com.gctn.tconstruct.init.TinkerRegistry;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
/**
 * NeoForge entry point: installs shared definitions and deferred registries on the mod bus.
 * Registration only queues suppliers; registry objects must not be resolved here before
 * NeoForge finishes the corresponding registration events. Client hooks live in Dist.CLIENT subscribers.
 */
@Mod(TinkersNirvana.MODID)
public class TinkersNirvana {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tconstruct";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public TinkersNirvana(IEventBus modEventBus, ModContainer modContainer) {
        // Material initialization refers to trait entries, so definitions come
        // first. The content calls below queue suppliers rather than resolve them.
        TinkerTraits.registerTraits();
        TinkerDataComponents.register(modEventBus);
        TableRegistryBus.register(modEventBus);
        SmelteryRegistration.register(modEventBus);
        ToolPartsRegistryBus.register(modEventBus);
        ToolsRegistryBus.register(modEventBus);
        TinkerRegistry.register(modEventBus);
        BlockRegister.register(modEventBus);
        ItemRegister.register(modEventBus);
        // Repair-ingredient initialization also initializes the static material
        // catalog; constructing an unused TinkerMaterials instance adds nothing.
        MaterialList.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    
}
