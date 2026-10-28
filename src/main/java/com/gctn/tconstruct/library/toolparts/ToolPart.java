package com.gctn.tconstruct.library.toolparts; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

/**
 * 携带材料信息的工具部件共用的物品基类。
 * 材料成本使用 MaterialValue 单位表示，供后续部件加工流程使用。
 */
public abstract class ToolPart extends Item { // 定义 ToolPart 的实现类型，具体职责见上方说明。
    public static final DeferredRegister.Items ITEMS = // 设置 ITEMS 为 。
            DeferredRegister.createItems(TinkersNirvana.MODID); // 调用 createItems，执行当前流程所需的处理。

    private final int cost; // 声明 cost，保存后续流程使用的状态或依赖。

    public ToolPart(int cost) { // 定义 ToolPart 的调用入口，参数和具体处理见下方实现。
        super(new Item.Properties()); // 调用 Properties，执行当前流程所需的处理。
        this.cost = cost; // 设置 this.cost 为 cost。
    } // 结束当前作用域。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        ITEMS.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。

} // 结束当前作用域。
