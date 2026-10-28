package com.gctn.tconstruct.library.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.gctn.tconstruct.TinkersNirvana.MODID;

/**
 * 移植代码共用的格式化与本地化辅助方法。
 * DecimalFormat 实例供客户端界面显示使用，不应直接用于服务端的并发计算。
 */
public class Util {
    public static final DecimalFormat df = new DecimalFormat("#,###,###.##", DecimalFormatSymbols.getInstance(Locale.US));
    public static final DecimalFormat dfPercent = new DecimalFormat("#%");

    public static Logger getLogger(String type) {
        String log = MODID;

        return LoggerFactory.getLogger(log + "-" + type);
    }

    public static String sanitizeLocalizationString(String string) {
        return string.toLowerCase(Locale.ROOT).replace(" ", "");
    }

    public static String translate(String key, Object... pars) {
        // 参数应传给译文，而不是用于格式化翻译键。
        // getString 返回解析后的文本，Component.toString 只提供调试表示。
        // 向玩家发送文字时应直接使用可翻译的 Component，
        // 使文字在接收客户端按其语言解析。
        return Component.translatable(key, pars).getString();
    }

    /**
     * 返回聊天格式对应的实际颜色值。
     */
    public static int enumChatFormattingToColor(ChatFormatting color) {
        // 当前 ChatFormatting 已直接保存 RGB 值，旧版按枚举序号推导颜色的公式
        // 会算错颜色；BOLD、RESET 等纯样式条目也没有对应的 RGB 值。
        Integer rgb = color.getColor();
        return rgb == null ? 0xFFFFFF : rgb;
    }

}
