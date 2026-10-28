package com.gctn.tconstruct.tools; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.tools.traits.ToolTraits; // 引入 com.gctn.tconstruct.tools.traits.ToolTraits，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.event.entity.player.PlayerEvent; // 引入 net.neoforged.neoforge.event.entity.player.PlayerEvent，供本文件使用。
import net.neoforged.neoforge.event.level.BlockEvent; // 引入 net.neoforged.neoforge.event.level.BlockEvent，供本文件使用。

/**
 * 将原版挖掘事件转发到统一的词条回调接口。
 * 挖掘速度在客户端与服务端分别计算，改变世界的回调只在服务端方块破坏事件中执行。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public final class TraitEvents { // 定义 TraitEvents 的实现类型，具体职责见上方说明。
    private TraitEvents() {} // 定义 TraitEvents 的调用入口；此处无需额外处理。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void miningSpeed(PlayerEvent.BreakSpeed event) { // 定义 miningSpeed 的调用入口，参数和具体处理见下方实现。
        ItemStack tool = event.getEntity().getMainHandItem(); // 设置 tool 为 event.getEntity().getMainHandItem()。
        if (tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)) { // 检查 tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)；按条件决定是否执行本分支。
            ToolTraits.forEach(tool, (trait, level) -> trait.miningSpeed(tool, level, event)); // 定义回调或映射规则，在对应条目被处理时执行。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void beforeBlockBreak(BlockEvent.BreakEvent event) { // 定义 beforeBlockBreak 的调用入口，参数和具体处理见下方实现。
        ItemStack tool = event.getPlayer().getMainHandItem(); // 设置 tool 为 event.getPlayer().getMainHandItem()。
        if (tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)) { // 检查 tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)；按条件决定是否执行本分支。
            ToolTraits.forEach(tool, (trait, level) -> trait.beforeBlockBreak(tool, level, event)); // 定义回调或映射规则，在对应条目被处理时执行。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
