package com.gctn.tconstruct.init;

import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolPart;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import net.neoforged.bus.api.IEventBus;

public class ToolPartsRegistryBus {
    public static void register(IEventBus eventBus) {
        // All tool parts share ToolPart.ITEMS. Initialize every built-in entry
        // before registering that DeferredRegister exactly once.
        com.gctn.tconstruct.library.toolparts.AntiqueParts.init();
        Binding.init();
        PickaxeHead.init();
        ToolRod.init();
        ToolPart.register(eventBus);
    }
}
