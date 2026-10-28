package com.gctn.tconstruct.init;

import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.tools.Pickaxe;
import net.neoforged.bus.api.IEventBus;

/**
 * 先登记内置工具，再挂接它们共享的延迟注册器。
 * 材料决定的属性保存在各个物品栈中，不为每种材料单独注册工具物品。
 */
public class ToolsRegistryBus {
    public static void register(IEventBus eventBus) {
        Pickaxe.init();
        TinkerTools.register(eventBus);
    }
}
