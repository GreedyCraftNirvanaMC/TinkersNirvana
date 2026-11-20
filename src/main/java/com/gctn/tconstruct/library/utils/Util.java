package com.gctn.tconstruct.library.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.gctn.tconstruct.TinkersNirvana.MODID;

public class Util {
    public static final DecimalFormat df = new DecimalFormat("#,###,###.##", DecimalFormatSymbols.getInstance(Locale.US));
    public static final DecimalFormat dfPercent = new DecimalFormat("#%");

    public static Logger getLogger(String type) {
        String log = MODID;

        return LoggerFactory.getLogger(log + "-" + type);
    }

    public static String sanitizeLocalizationString(String string) {
        return string.toLowerCase(Locale.US).replaceAll(" ", "");
    }

    public static String translate(String key, Object... pars) {
        // 第一次翻译可能返回另一个翻译键，按怀古的行为再解析一次。
        String translatedKey = Component.translatable(String.format(key, pars)).getString().trim();
        return Component.translatable(translatedKey).getString().trim();

    }

    /**
     * 返回原版 RGB 值。纯样式和重置格式沿用白色。
     */
    public static int enumChatFormattingToColor(ChatFormatting color) {
        Integer rgb = color.getColor();
        return rgb != null ? rgb : 0xffffff;
    }

}
