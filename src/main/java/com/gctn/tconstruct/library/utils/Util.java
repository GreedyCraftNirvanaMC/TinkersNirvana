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
 * Small formatting and localization helpers shared by ported code.
 * DecimalFormat instances are intended for client presentation, not concurrent server calculations.
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
        // Arguments belong to the translation, not to the translation key.
        // getString resolves text; Component.toString is a debugging representation.
        // Use a translatable Component directly when sending text to a player so
        // localization happens on the receiving client instead of the server.
        return Component.translatable(key, pars).getString();
    }

    /**
     * Returns the actual color value for a chatformatting
     */
    public static int enumChatFormattingToColor(ChatFormatting color) {
        // Modern ChatFormatting already stores RGB. The old ordinal-based formula
        // corrupts those colors, and style-only entries (BOLD, RESET) have no RGB.
        Integer rgb = color.getColor();
        return rgb == null ? 0xFFFFFF : rgb;
    }

}
