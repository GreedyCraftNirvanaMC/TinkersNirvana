package com.gctn.tconstruct.library.utils; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.mojang.datafixers.util.Either; // 引入 com.mojang.datafixers.util.Either，供本文件使用。
import net.minecraft.network.chat.FormattedText; // 引入 net.minecraft.network.chat.FormattedText，供本文件使用。
import net.minecraft.world.inventory.tooltip.TooltipComponent; // 引入 net.minecraft.world.inventory.tooltip.TooltipComponent，供本文件使用。
import net.minecraft.world.item.CreativeModeTabs; // 引入 net.minecraft.world.item.CreativeModeTabs，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 材料工具的只读界面辅助方法。
 * 耐久颜色会限制空值或异常比例的范围，创造模式分类提示检测用于避免重复添加空行。
 */
public class ToolHelper { // 定义 ToolHelper 的实现类型，具体职责见上方说明。
    public static boolean hasCreativeTabTip( // 定义 hasCreativeTabTip 的调用入口，参数和具体处理见下方实现。
            List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack // 在 ToolHelper 中补充当前声明或表达式的 List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack 部分。
    ) { // 在 ToolHelper 中补充当前声明或表达式的 ) { 部分。
        return CreativeModeTabs.tabs().stream() // 返回 CreativeModeTabs.tabs().stream()，将当前分支的结果交给调用者。
                .filter(tab -> !tab.hasSearchBar() && tab.contains(stack)) // 按条件筛选条目，只保留符合要求的数据。
                .map(tab -> tab.getDisplayName().getString()) // 读取字符串字段。
                .anyMatch(tabName -> tooltip.stream().anyMatch(element -> element.left() // 建立集合遍历流程，依次筛选或转换当前数据。
                        .map(text -> text.getString().equals(tabName)) // 读取字符串字段。
                        .orElse(false))); // 调用 orElse，执行当前流程所需的处理。
    } // 结束当前作用域。

    // 工具获取耐久相关颜色
    public static int getDurabilityColor(ItemStack stack) { // 定义 getDurabilityColor 的调用入口，参数和具体处理见下方实现。
        int maxDurability = stack.getMaxDamage(); // 设置 maxDurability 为 stack.getMaxDamage()。
        int remainingDurability = Math.max( // 设置 remainingDurability 为 Math.max(。
                0, // 向当前声明或调用传入 0。
                maxDurability - stack.getDamageValue() // 调用 getDamageValue，执行当前流程所需的处理。
        ); // 结束当前参数列表或表达式并完成本次调用。

        float percentage = maxDurability > 0 // 设置 percentage 为 maxDurability > 0。
                ? (float) remainingDurability / maxDurability // 在 ToolHelper 中补充当前声明或表达式的 ? (float) remainingDurability / maxDurability 部分。
                : 0.0F; // 在 ToolHelper 中补充当前声明或表达式的 : 0.0F 部分。

        return interpolateArgb( // 返回 interpolateArgb(，将当前分支的结果交给调用者。
                0xFFCC4747, // 空耐久
                0xFF47CC47, // 满耐久
                percentage // 在 ToolHelper 中补充当前声明或表达式的 percentage 部分。
        ); // 结束当前参数列表或表达式并完成本次调用。
    } // 结束当前作用域。

    private static int interpolateArgb(int startColor, int endColor, float percentage) { // 定义 interpolateArgb 的调用入口，参数和具体处理见下方实现。
        percentage = Math.clamp(percentage, 0.0F, 1.0F); // 设置 percentage 为 Math.clamp(percentage, 0.0F, 1.0F)。

        int startA = (startColor >>> 24) & 0xFF; // 设置 startA 为 (startColor >>> 24) & 0xFF。
        int startR = (startColor >>> 16) & 0xFF; // 设置 startR 为 (startColor >>> 16) & 0xFF。
        int startG = (startColor >>> 8) & 0xFF; // 设置 startG 为 (startColor >>> 8) & 0xFF。
        int startB = startColor & 0xFF; // 设置 startB 为 startColor & 0xFF。

        int endA = (endColor >>> 24) & 0xFF; // 设置 endA 为 (endColor >>> 24) & 0xFF。
        int endR = (endColor >>> 16) & 0xFF; // 设置 endR 为 (endColor >>> 16) & 0xFF。
        int endG = (endColor >>> 8) & 0xFF; // 设置 endG 为 (endColor >>> 8) & 0xFF。
        int endB = endColor & 0xFF; // 设置 endB 为 endColor & 0xFF。

        int a = Math.round(startA + (endA - startA) * percentage); // 设置 a 为 Math.round(startA + (endA - startA) * percentage)。
        int r = Math.round(startR + (endR - startR) * percentage); // 设置 r 为 Math.round(startR + (endR - startR) * percentage)。
        int g = Math.round(startG + (endG - startG) * percentage); // 设置 g 为 Math.round(startG + (endG - startG) * percentage)。
        int b = Math.round(startB + (endB - startB) * percentage); // 设置 b 为 Math.round(startB + (endB - startB) * percentage)。

        return (a << 24) | (r << 16) | (g << 8) | b; // 返回 (a << 24) | (r << 16) | (g << 8) | b，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
