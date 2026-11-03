package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.menu.TableMenuRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class TableClientEvents {
    @SubscribeEvent private static void models(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event) {
        java.util.Set<String> tables = java.util.Set.of("toolstation", "blockitem/toolstation", "toolforge", "crafting_station", "stencil_table", "part_builder", "item_rack", "drying_rack");
        event.getModels().replaceAll((key, model) -> key.id().getNamespace().equals(TinkersNirvana.MODID) && tables.contains(key.id().getPath()) ? new TableMaterialModel(model) : model);
    }
    @SubscribeEvent private static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.gctn.tconstruct.tables.WorkshopModule.ENTITY.get(), context -> new WorkshopItemRenderer<>());
        event.registerBlockEntityRenderer(com.gctn.tconstruct.tables.block.entity.TableEntityRegistries.TOOL_STATION_BLOCK_ENTITY.get(), context -> new WorkshopItemRenderer<>());
    }
    @SubscribeEvent
    private static void registerTableScreens(RegisterMenuScreensEvent event) {
        event.register(TableMenuRegistries.TOOL_STATION_MENU.get(), ToolStationScreen::new);
        event.register(com.gctn.tconstruct.tables.WorkshopModule.MENU.get(), WorkshopScreen::new);
        event.register(com.gctn.tconstruct.tables.WorkshopModule.CRAFTING_MENU.get(), PersistentCraftingScreen::new);
    }
}
