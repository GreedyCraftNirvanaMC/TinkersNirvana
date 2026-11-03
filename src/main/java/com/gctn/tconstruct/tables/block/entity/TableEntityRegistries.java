package com.gctn.tconstruct.tables.block.entity;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.block.TableBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TableEntityRegistries {
    public static final DeferredRegister<BlockEntityType<?>> TABLE_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);

    public static final Supplier<BlockEntityType<ToolStationBlockEntity>> TOOL_STATION_BLOCK_ENTITY =
            TABLE_ENTITY_TYPES.register("toolstation_blockentity", () -> BlockEntityType.Builder
                    .of(ToolStationBlockEntity::new, TableBlocks.TOOL_STATION.get(), TableBlocks.TOOL_FORGE.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        TABLE_ENTITY_TYPES.register(eventBus);
    }
}
