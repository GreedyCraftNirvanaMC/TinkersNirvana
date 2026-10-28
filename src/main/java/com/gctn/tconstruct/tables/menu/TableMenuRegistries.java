package com.gctn.tconstruct.tables.menu; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.world.flag.FeatureFlags; // 引入 net.minecraft.world.flag.FeatureFlags，供本文件使用。
import net.minecraft.world.inventory.MenuType; // 引入 net.minecraft.world.inventory.MenuType，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

/**
 * 注册客户端和服务端相互匹配的工具台菜单构造器。
 * 客户端先使用占位容器，再由原版菜单同步流程填充实际内容。
 */
public class TableMenuRegistries { // 定义 TableMenuRegistries 的实现类型，具体职责见上方说明。
    public static final DeferredRegister<MenuType<?>> TABLE_MENU_TYPES = // 设置 TABLE_MENU_TYPES 为 。
            DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID); // 调用 create，执行当前流程所需的处理。

    public static final Supplier<MenuType<ToolStationMenu>> TOOL_STATION_MENU = // 设置 TOOL_STATION_MENU 为 。
            TABLE_MENU_TYPES.register("toolstation", () -> new MenuType<>( // 定义回调或映射规则，在对应条目被处理时执行。
                    ToolStationMenu::new, // 向当前声明或调用传入 ToolStationMenu::new。
                    FeatureFlags.DEFAULT_FLAGS // 在 TableMenuRegistries 中补充当前声明或表达式的 FeatureFlags.DEFAULT_FLAGS 部分。
            )); // 结束当前参数列表或表达式并完成本次调用。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        TABLE_MENU_TYPES.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
