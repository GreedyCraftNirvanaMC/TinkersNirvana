package com.gctn.tconstruct.init;

import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolPart;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import net.neoforged.bus.api.IEventBus;

/**
 * 先初始化所有内置部件，再将它们共享的延迟物品注册器注册一次。
 * 显式触发类初始化，避免未被其他代码引用的部件始终无法登记其物品。
 */
public class ToolPartsRegistryBus {
    public static void register(IEventBus eventBus) {
        // 所有工具部件共用 ToolPart.ITEMS，先初始化每个内置条目，
        // 再将该 DeferredRegister 注册一次，避免重复挂接。
        Binding.init();
        PickaxeHead.init();
        ToolRod.init();
        ToolPart.register(eventBus);
    }
}
