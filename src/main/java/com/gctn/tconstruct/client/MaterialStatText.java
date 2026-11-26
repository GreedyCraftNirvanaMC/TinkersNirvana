/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.client;

import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 指南书和 JEI 共用材料属性的数值、单位和说明。 */
public final class MaterialStatText {
    private static final int GREEN = 0x47cc47, SPEED = 0x78a0cd, ATTACK = 0xd76464, MULTIPLIER = 0xb9b95a;

    private MaterialStatText() {}

    public static List<Component> stats(AbstractMaterialStats stats) {
        List<Component> lines = new ArrayList<>();
        if (stats instanceof HeadMaterialStats head) {
            number(lines, "head.durability", head.durability, GREEN);
            lines.add(Component.translatable("stat.head.harvestlevel.name").append(": ").append(HarvestLevels.getHarvestLevelName(head.harvestLevel)).withStyle(style -> style.withHoverEvent(description("head.harvestlevel"))));
            number(lines, "head.miningspeed", head.miningspeed, SPEED); number(lines, "head.attack", head.attack, ATTACK);
        } else if (stats instanceof HandleMaterialStats handle) { number(lines, "handle.modifier", handle.modifer, MULTIPLIER); number(lines, "handle.durability", handle.durability, GREEN); }
        else if (stats instanceof ExtraMaterialStats extra) { number(lines, "extra.durability", extra.durability, GREEN); }
        else if (stats instanceof BowMaterialStats bow) { number(lines, "bow.drawspeed", 1 / bow.drawSpeed, 0x808080); number(lines, "bow.range", bow.range, 0x8cafaf); number(lines, "bow.damage", bow.bonusDamage, 0x9b5041); }
        else if (stats instanceof BowStringMaterialStats string) { number(lines, "bowstring.modifier", string.modifier, MULTIPLIER); }
        else if (stats instanceof ArrowShaftMaterialStats shaft) { number(lines, "shaft.modifier", shaft.modifier, MULTIPLIER); number(lines, "shaft.ammo", shaft.bonusAmmo, GREEN); }
        else if (stats instanceof FletchingMaterialStats fletching) { number(lines, "fletching.accuracy", fletching.accuracy * 100, 0xcdaacd, "%"); number(lines, "fletching.modifier", fletching.modifier, MULTIPLIER); }
        return lines;
    }
    private static HoverEvent description(String key) { return new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("stat." + key + ".desc")); }
    private static void number(List<Component> lines, String key, double value, int color) { number(lines, key, value, color, ""); }
    private static void number(List<Component> lines, String key, double value, int color, String suffix) {
        if (value == 0 || !Double.isFinite(value)) { return; }
        String number = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        lines.add(Component.translatable("stat." + key + ".name").append(": ").append(Component.literal(number + suffix).withColor(color)).withStyle(style -> style.withHoverEvent(description(key))));
    }
    public static String type(String role) { return switch (role) { case "BowString" -> "bowstring"; case "Shaft" -> "shaft"; default -> role.toLowerCase(Locale.ROOT); }; }
}
