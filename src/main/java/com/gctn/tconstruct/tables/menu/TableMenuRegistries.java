package com.gctn.tconstruct.tables.menu;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registers matching client and server constructors for the station menu.
 * The client constructor uses a placeholder container; normal menu synchronization supplies its contents.
 */
public class TableMenuRegistries {
    public static final DeferredRegister<MenuType<?>> TABLE_MENU_TYPES =
            DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID);

    public static final Supplier<MenuType<ToolStationMenu>> TOOL_STATION_MENU =
            TABLE_MENU_TYPES.register("toolstation", () -> new MenuType<>(
                    ToolStationMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static void register(IEventBus eventBus) {
        TABLE_MENU_TYPES.register(eventBus);
    }
}
