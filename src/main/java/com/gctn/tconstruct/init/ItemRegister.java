package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。
import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。

/**
 * 使用同一个延迟注册器管理普通物品和方块物品。
 * 工具部件与组装后的工具使用独立注册器，以便明确控制各分类的初始化时机。
 */
public class ItemRegister { // 定义 ItemRegister 的实现类型，具体职责见上方说明。
    public static final DeferredRegister.Items ITEMS = // 设置 ITEMS 为 。
        DeferredRegister.createItems(TinkersNirvana.MODID); // 调用 createItems，执行当前流程所需的处理。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        ITEMS.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
