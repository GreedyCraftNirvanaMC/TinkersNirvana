package com.gctn.tconstruct.tables.block.entity;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.block.TableBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registers the station block-entity factory against its supported block.
 * Deferred suppliers delay resolving the block until the registry lifecycle permits it.
 */
public class TableEntityRegistries {
    public static final DeferredRegister<BlockEntityType<?>> TABLE_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);

    public static final Supplier<BlockEntityType<?>> TOOL_STATION_BLOCK_ENTITY =
            TABLE_ENTITY_TYPES.register("toolstation_blockentity", () -> BlockEntityType.Builder
                    .of(ToolStationBlockEntity::new, TableBlocks.TOOL_STATION.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        TABLE_ENTITY_TYPES.register(eventBus);
    }
}
