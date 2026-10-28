package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.menu.TableMenuRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 只在物理客户端注册工具台界面。
 * 将界面类与通用初始化逻辑分离，使同一个模组 JAR 也能在专用服务端加载。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class TableClientEvents {
    @SubscribeEvent
    private static void registerTableScreens(RegisterMenuScreensEvent event) {
        event.register(TableMenuRegistries.TOOL_STATION_MENU.get(), ToolStationScreen::new);
    }
}
