package com.gctn.tconstruct.init;

import com.gctn.tconstruct.tables.block.entity.TableEntityRegistries;
import com.gctn.tconstruct.tables.menu.TableMenuRegistries;
import net.neoforged.bus.api.IEventBus;

/**
 * 将工具台菜单和方块实体类型接入 NeoForge 的注册生命周期。
 * 对象工厂稍后才读取方块供应器，确保此时方块已经完成注册。
 */
public class TableRegistryBus {
    public static void register(IEventBus eventBus) {
        TableMenuRegistries.register(eventBus);
        TableEntityRegistries.register(eventBus);
    }
}
