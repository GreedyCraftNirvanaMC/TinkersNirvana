package com.gctn.tconstruct.tables.menu;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 注册客户端和服务端相互匹配的工具台菜单构造器。
 * 客户端先使用占位容器，再由原版菜单同步流程填充实际内容。
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
