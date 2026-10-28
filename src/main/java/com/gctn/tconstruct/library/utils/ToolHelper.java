package com.gctn.tconstruct.library.utils;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Read-only presentation helpers for material tools.
 * Durability colors clamp empty/invalid ranges, and creative-tab detection avoids duplicating tooltip spacing.
 */
public class ToolHelper {
    public static boolean hasCreativeTabTip(
            List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack
    ) {
        return CreativeModeTabs.tabs().stream()
                .filter(tab -> !tab.hasSearchBar() && tab.contains(stack))
                .map(tab -> tab.getDisplayName().getString())
                .anyMatch(tabName -> tooltip.stream().anyMatch(element -> element.left()
                        .map(text -> text.getString().equals(tabName))
                        .orElse(false)));
    }

    // 工具获取耐久相关颜色
    public static int getDurabilityColor(ItemStack stack) {
        int maxDurability = stack.getMaxDamage();
        int remainingDurability = Math.max(
                0,
                maxDurability - stack.getDamageValue()
        );

        float percentage = maxDurability > 0
                ? (float) remainingDurability / maxDurability
                : 0.0F;

        return interpolateArgb(
                0xFFCC4747, // 空耐久
                0xFF47CC47, // 满耐久
                percentage
        );
    }

    private static int interpolateArgb(int startColor, int endColor, float percentage) {
        percentage = Math.clamp(percentage, 0.0F, 1.0F);

        int startA = (startColor >>> 24) & 0xFF;
        int startR = (startColor >>> 16) & 0xFF;
        int startG = (startColor >>> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endA = (endColor >>> 24) & 0xFF;
        int endR = (endColor >>> 16) & 0xFF;
        int endG = (endColor >>> 8) & 0xFF;
        int endB = endColor & 0xFF;

        int a = Math.round(startA + (endA - startA) * percentage);
        int r = Math.round(startR + (endR - startR) * percentage);
        int g = Math.round(startG + (endG - startG) * percentage);
        int b = Math.round(startB + (endB - startB) * percentage);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
