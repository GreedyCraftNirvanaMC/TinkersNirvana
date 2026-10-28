package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.menu.TableMenuRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Registers station screens only on the physical client.
 * Keeping GUI classes out of common setup allows the same mod JAR to load on dedicated servers.
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class TableClientEvents {
    @SubscribeEvent
    private static void registerTableScreens(RegisterMenuScreensEvent event) {
        event.register(TableMenuRegistries.TOOL_STATION_MENU.get(), ToolStationScreen::new);
    }
}
