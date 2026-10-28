package com.gctn.tconstruct.init;

import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.tools.Pickaxe;
import net.neoforged.bus.api.IEventBus;

/**
 * Queues built-in tool items before attaching their shared deferred register.
 * Material-specific stats belong to individual stacks, rather than separate registered item types.
 */
public class ToolsRegistryBus {
    public static void register(IEventBus eventBus) {
        Pickaxe.init();
        TinkerTools.register(eventBus);
    }
}
