package com.gctn.tconstruct.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.gctn.tconstruct.TinkersNirvana;

/**
 * 使用同一个延迟注册器管理普通物品和方块物品。
 * 工具部件与组装后的工具使用独立注册器，以便明确控制各分类的初始化时机。
 */
public class ItemRegister {
    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(TinkersNirvana.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
