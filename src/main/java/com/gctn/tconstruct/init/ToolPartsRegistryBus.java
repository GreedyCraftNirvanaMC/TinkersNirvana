package com.gctn.tconstruct.init;

import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolPart;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import net.neoforged.bus.api.IEventBus;

/**
 * Initializes every built-in part, then registers their shared deferred item registry once.
 * Without explicit initialization, an otherwise unreferenced part class would never enqueue its item.
 */
public class ToolPartsRegistryBus {
    public static void register(IEventBus eventBus) {
        // All tool parts share ToolPart.ITEMS. Initialize every built-in entry
        // before registering that DeferredRegister exactly once.
        Binding.init();
        PickaxeHead.init();
        ToolRod.init();
        ToolPart.register(eventBus);
    }
}
