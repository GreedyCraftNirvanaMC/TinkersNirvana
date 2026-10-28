package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * 移除耐久耗尽的匠魂工具提供的战斗属性修饰。
 * 保留物品栈中保存的属性组件，使工具台修复后能够恢复正常属性。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class TinkerToolEvents {
    private TinkerToolEvents() {
    }

    @SubscribeEvent
    public static void removeBrokenToolAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof TinkerTools && TinkerTools.isBroken(stack)) {
            event.clearModifiers();
        }
    }
}
