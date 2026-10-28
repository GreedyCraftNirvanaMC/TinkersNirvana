package com.gctn.tconstruct.tools; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.event.ItemAttributeModifierEvent; // 引入 net.neoforged.neoforge.event.ItemAttributeModifierEvent，供本文件使用。

/**
 * 移除耐久耗尽的匠魂工具提供的战斗属性修饰。
 * 保留物品栈中保存的属性组件，使工具台修复后能够恢复正常属性。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public final class TinkerToolEvents { // 定义 TinkerToolEvents 的实现类型，具体职责见上方说明。
    private TinkerToolEvents() { // 定义 TinkerToolEvents 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void removeBrokenToolAttributes(ItemAttributeModifierEvent event) { // 定义 removeBrokenToolAttributes 的调用入口，参数和具体处理见下方实现。
        ItemStack stack = event.getItemStack(); // 设置 stack 为 event.getItemStack()。
        if (stack.getItem() instanceof TinkerTools && TinkerTools.isBroken(stack)) { // 检查 stack.getItem() instanceof TinkerTools && TinkerTools.isBroken(stack)；按条件决定是否执行本分支。
            event.clearModifiers(); // 调用 clearModifiers，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
