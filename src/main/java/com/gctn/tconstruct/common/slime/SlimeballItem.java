/*
 * 根据 TinkersAntique 的可食用史莱姆球与 Mantle 的效果提示行为移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** 效果名称使用游戏现有翻译，不在物品中硬编码语言或药水名称。 */
public final class SlimeballItem extends Item {
    public SlimeballItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var food = stack.get(DataComponents.FOOD);
        if (food != null) {
            // 原版只显示效果名称，等级与持续时间仍由食物组件保留。
            for (var effect : food.effects()) {
                tooltip.add(Component.translatable(effect.effect().getDescriptionId()));
            }
        }
    }
}
