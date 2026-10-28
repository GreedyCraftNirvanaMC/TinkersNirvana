package com.gctn.tconstruct.init;

import com.gctn.tconstruct.tables.block.entity.TableEntityRegistries;
import com.gctn.tconstruct.tables.menu.TableMenuRegistries;
import net.neoforged.bus.api.IEventBus;

/**
 * Attaches station menu and block-entity types to NeoForge lifecycle events.
 * Factories resolve their block suppliers later, after block registration has completed.
 */
public class TableRegistryBus {
    public static void register(IEventBus eventBus) {
        TableMenuRegistries.register(eventBus);
        TableEntityRegistries.register(eventBus);
    }
}
