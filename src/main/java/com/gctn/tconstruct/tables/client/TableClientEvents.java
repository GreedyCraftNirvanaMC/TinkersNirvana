package com.gctn.tconstruct.tables.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.tables.menu.TableMenuRegistries; // 引入 com.gctn.tconstruct.tables.menu.TableMenuRegistries，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent; // 引入 net.neoforged.neoforge.client.event.RegisterMenuScreensEvent，供本文件使用。

/**
 * 只在物理客户端注册工具台界面。
 * 将界面类与通用初始化逻辑分离，使同一个模组 JAR 也能在专用服务端加载。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public class TableClientEvents { // 定义 TableClientEvents 的实现类型，具体职责见上方说明。
    @SubscribeEvent // 将此方法声明为事件处理入口。
    private static void registerTableScreens(RegisterMenuScreensEvent event) { // 定义 registerTableScreens 的调用入口，参数和具体处理见下方实现。
        event.register(TableMenuRegistries.TOOL_STATION_MENU.get(), ToolStationScreen::new); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
