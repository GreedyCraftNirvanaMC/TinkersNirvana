package com.gctn.tconstruct.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.gctn.tconstruct.TinkersNirvana;

/**
 * Owns ordinary items and block items in one deferred register.
 * Tool parts and assembled tools use separate registers so their category initialization is explicit.
 */
public class ItemRegister {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TinkersNirvana.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
