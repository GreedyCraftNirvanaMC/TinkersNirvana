package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tools.TinkerTools; // 引入 com.gctn.tconstruct.tools.TinkerTools，供本文件使用。
import com.gctn.tconstruct.tools.tools.Pickaxe; // 引入 com.gctn.tconstruct.tools.tools.Pickaxe，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。

/**
 * 先登记内置工具，再挂接它们共享的延迟注册器。
 * 材料决定的属性保存在各个物品栈中，不为每种材料单独注册工具物品。
 */
public class ToolsRegistryBus { // 定义 ToolsRegistryBus 的实现类型，具体职责见上方说明。
    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        Pickaxe.init(); // 调用 init，执行当前流程所需的处理。
        TinkerTools.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
