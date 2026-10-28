package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.toolparts.Binding; // 引入 com.gctn.tconstruct.library.toolparts.Binding，供本文件使用。
import com.gctn.tconstruct.library.toolparts.PickaxeHead; // 引入 com.gctn.tconstruct.library.toolparts.PickaxeHead，供本文件使用。
import com.gctn.tconstruct.library.toolparts.ToolPart; // 引入 com.gctn.tconstruct.library.toolparts.ToolPart，供本文件使用。
import com.gctn.tconstruct.library.toolparts.ToolRod; // 引入 com.gctn.tconstruct.library.toolparts.ToolRod，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。

/**
 * 先初始化所有内置部件，再将它们共享的延迟物品注册器注册一次。
 * 显式触发类初始化，避免未被其他代码引用的部件始终无法登记其物品。
 */
public class ToolPartsRegistryBus { // 定义 ToolPartsRegistryBus 的实现类型，具体职责见上方说明。
    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        // 所有工具部件共用 ToolPart.ITEMS，先初始化每个内置条目，
        // 再将该 DeferredRegister 注册一次，避免重复挂接。
        Binding.init(); // 调用 init，执行当前流程所需的处理。
        PickaxeHead.init(); // 调用 init，执行当前流程所需的处理。
        ToolRod.init(); // 调用 init，执行当前流程所需的处理。
        ToolPart.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
