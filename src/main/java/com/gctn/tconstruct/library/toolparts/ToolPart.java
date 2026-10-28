package com.gctn.tconstruct.library.toolparts;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Shared registered-item base for material-bearing tool parts.
 * The material cost is expressed in MaterialValue units for the future part-crafting pipeline.
 */
public abstract class ToolPart extends Item {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TinkersNirvana.MODID);

    private final int cost;

    public ToolPart(int cost) {
        super(new Item.Properties());
        this.cost = cost;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
