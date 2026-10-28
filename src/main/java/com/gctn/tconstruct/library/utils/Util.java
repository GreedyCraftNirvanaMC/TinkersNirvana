package com.gctn.tconstruct.library.utils; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.ChatFormatting; // 引入 net.minecraft.ChatFormatting，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import org.slf4j.Logger; // 引入 org.slf4j.Logger，供本文件使用。
import org.slf4j.LoggerFactory; // 引入 org.slf4j.LoggerFactory，供本文件使用。

import java.text.DecimalFormat; // 引入 java.text.DecimalFormat，供本文件使用。
import java.text.DecimalFormatSymbols; // 引入 java.text.DecimalFormatSymbols，供本文件使用。
import java.util.Locale; // 引入 java.util.Locale，供本文件使用。

import static com.gctn.tconstruct.TinkersNirvana.MODID; // 引入 com.gctn.tconstruct.TinkersNirvana.MODID，供本文件使用。

/**
 * 移植代码共用的格式化与本地化辅助方法。
 * DecimalFormat 实例供客户端界面显示使用，不应直接用于服务端的并发计算。
 */
public class Util { // 定义 Util 的实现类型，具体职责见上方说明。
    public static final DecimalFormat df = new DecimalFormat("#,###,###.##", DecimalFormatSymbols.getInstance(Locale.US)); // 设置 df 为 new DecimalFormat("#,###,###.##", DecimalFormatSymbols.getInstance(Locale.US))。
    public static final DecimalFormat dfPercent = new DecimalFormat("#%"); // 设置 dfPercent 为 new DecimalFormat("#%")。

    public static Logger getLogger(String type) { // 定义 getLogger 的调用入口，参数和具体处理见下方实现。
        String log = MODID; // 设置 log 为 MODID。

        return LoggerFactory.getLogger(log + "-" + type); // 返回 LoggerFactory.getLogger(log + "-" + type)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static String sanitizeLocalizationString(String string) { // 定义 sanitizeLocalizationString 的调用入口，参数和具体处理见下方实现。
        return string.toLowerCase(Locale.ROOT).replace(" ", ""); // 返回 string.toLowerCase(Locale.ROOT).replace(" ", "")，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static String translate(String key, Object... pars) { // 定义 translate 的调用入口，参数和具体处理见下方实现。
        // 参数应传给译文，而不是用于格式化翻译键。
        // getString 返回解析后的文本，Component.toString 只提供调试表示。
        // 向玩家发送文字时应直接使用可翻译的 Component，
        // 使文字在接收客户端按其语言解析。
        return Component.translatable(key, pars).getString(); // 返回 Component.translatable(key, pars).getString()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /**
     * 返回聊天格式对应的实际颜色值。
     */
    public static int enumChatFormattingToColor(ChatFormatting color) { // 定义 enumChatFormattingToColor 的调用入口，参数和具体处理见下方实现。
        // 当前 ChatFormatting 已直接保存 RGB 值，旧版按枚举序号推导颜色的公式
        // 会算错颜色；BOLD、RESET 等纯样式条目也没有对应的 RGB 值。
        Integer rgb = color.getColor(); // 设置 rgb 为 color.getColor()。
        return rgb == null ? 0xFFFFFF : rgb; // 返回 rgb == null ? 0xFFFFFF : rgb，将当前分支的结果交给调用者。
    } // 结束当前作用域。

} // 结束当前作用域。
