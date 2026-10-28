package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.block.entity.TableEntityRegistries; // 引入 com.gctn.tconstruct.tables.block.entity.TableEntityRegistries，供本文件使用。
import com.gctn.tconstruct.tables.menu.TableMenuRegistries; // 引入 com.gctn.tconstruct.tables.menu.TableMenuRegistries，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。

/**
 * 将工具台菜单和方块实体类型接入 NeoForge 的注册生命周期。
 * 对象工厂稍后才读取方块供应器，确保此时方块已经完成注册。
 */
public class TableRegistryBus { // 定义 TableRegistryBus 的实现类型，具体职责见上方说明。
    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        TableMenuRegistries.register(eventBus); // 登记对象工厂或事件处理器。
        TableEntityRegistries.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
