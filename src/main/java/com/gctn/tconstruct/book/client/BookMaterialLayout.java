/* SPDX-License-Identifier: LGPL-3.0-only; adapts Antique ContentMaterial and ContentSingleStatMultMaterial. */
package com.gctn.tconstruct.book.client;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Original paper coordinates and presentation; all values still come from registered materials. */
final class BookMaterialLayout {
    static final List<String> RANGED = List.of("Bow", "BowString", "Shaft", "Fletching");
    private static final int GREEN = 0x47cc47, SPEED = 0x78a0cd, ATTACK = 0xd76464, MULTIPLIER = 0xb9b95a;
    private final Font font;
    private final List<BookDocument.Element> elements = new ArrayList<>();
    private BookMaterialLayout(Font font) { this.font = font; }

    static BookDocument.Leaf material(Font font, BookContents.Page source) {
        Material material = TinkerMaterials.getMaterial(source.data().get("material").getAsString());
        if (material == null) { return new BookDocument.Leaf(source.id(), source.section(), Component.empty(), List.of(), source.id()); }
        return new BookDocument.Leaf(source.id(), source.section(), Component.empty(), side(font, source, material, false), side(font, source, material, true), source.id());
    }
    private static List<BookDocument.Element> side(Font font, BookContents.Page source, Material material, boolean right) {
        var page = new BookMaterialLayout(font);
        page.title(source.title(), 0, true);
        int x = (right ? 0 : 22) + 10, width = BookDocument.WIDTH / 2 - 10;
        page.group(material, "Head", x, 25, width);
        page.group(material, "Handle", x + width, 25, width - 10);
        int bottom = 25 + 65 + 10 * material.getTraits("Head").size();
        page.group(material, "Extra", x, bottom, width);
        if (source.data().has("flavour")) {
            page.text(List.of(Component.literal("\"" + source.data().get("flavour").getAsString() + "\"").withStyle(ChatFormatting.ITALIC)), x + width, bottom, width - 16, bottom + 60);
        }
        int edge = right ? BookDocument.WIDTH - 18 : 0, y = 10;
        List<BookDocument.Icon> display = new ArrayList<>();
        if (!source.icons().isEmpty()) { display.add(new BookDocument.Icon(edge, y, List.of(source.icons().getFirst()))); }
        if (material.isCraftable()) {
            display.add(new BookDocument.Icon(edge, y, List.of(new ItemStack(com.gctn.tconstruct.tables.WorkshopModule.BLOCKS.get(com.gctn.tconstruct.tables.WorkshopModule.Kind.PART_BUILDER).get())), 1, Component.translatable("book.tconstruct.craftable")));
        }
        if (material.isCastable()) { display.add(new BookDocument.Icon(edge, y, List.of(new ItemStack(com.gctn.tconstruct.smeltery.SmelteryModule.BASIN.get())), 1, Component.translatable("book.tconstruct.castable"))); }
        for (String id : List.of("pickaxe", "mattock", "broadsword", "hammer", "cleaver", "shuriken", "frypan", "lumberaxe", "battlesign")) {
            if (display.size() >= 9) { break; }
            var tool = AntiqueTools.get(id); if (tool == null) { continue; }
            ItemStack built = ToolBuilder.assemble(tool, Collections.nCopies(tool.definition().parts().size(), material));
            if (!built.isEmpty()) { display.add(new BookDocument.Icon(edge, y, List.of(built))); }
        }
        for (var icon : display) { page.elements.add(new BookDocument.Icon(edge, y, icon.items(), icon.scale(), icon.tooltip())); y += 16; }
        return List.copyOf(page.elements);
    }
    static BookDocument.Leaf ranged(Font font, String id, String role, List<BookContents.Page> materials) {
        var page = new BookMaterialLayout(font);
        for (int i = 0; i < materials.size(); i++) {
            var source = materials.get(i); var material = TinkerMaterials.getMaterial(source.data().get("material").getAsString());
            int y = BookDocument.HEIGHT / 3 * i;
            page.title(source.title(), y, true);
            var parts = parts(material, role);
            if (!parts.isEmpty()) { page.elements.add(new BookDocument.Icon(10, y + 21, parts)); }
            page.text(stats(material.stats.get(role)), 30, y + 20, BookDocument.WIDTH / 2 - 10, y + 57);
            page.text(traits(material, role), 120, y + 20, BookDocument.WIDTH / 2 - 10, y + 57);
        }
        return new BookDocument.Leaf(id, "bowmaterials", Component.empty(), List.copyOf(page.elements), id);
    }
    private void group(Material material, String role, int x, int y, int width) {
        if (!material.hasStats(role)) { return; }
        var parts = parts(material, role);
        if (!parts.isEmpty()) { elements.add(new BookDocument.Icon(x, y + 1, parts, .5F, null)); }
        text(List.of(Component.translatable("stat." + type(role) + ".name").withStyle(ChatFormatting.UNDERLINE)), x + 10, y, width - 10, y + 10);
        List<Component> lines = new ArrayList<>(stats(material.stats.get(role))); lines.addAll(traits(material, role));
        text(lines, x, y + 12, width, BookDocument.HEIGHT - 10);
    }
    private static List<ItemStack> parts(Material material, String role) {
        return AntiqueParts.entries().values().stream().filter(part -> part.stat().equals(role) || AntiqueTools.entries().values().stream()
                        .flatMap(tool -> tool.definition().parts().stream()).anyMatch(required -> required.part().equals(part.id()) && required.stat().equals(role)))
                .map(part -> part.create(material)).filter(stack -> !stack.isEmpty()).toList();
    }
    private static List<Component> traits(Material material, String role) {
        List<Component> lines = new ArrayList<>();
        for (var entry : material.getTraits(role)) {
            lines.add(Component.literal(entry.trait().getTranslatedName()).withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY).withUnderlined(true)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, entry.trait().getDescription().copy().withColor(material.color)))));
        }
        return lines;
    }
    static List<Component> stats(AbstractMaterialStats stats) {
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
    static String type(String role) { return switch (role) { case "BowString" -> "bowstring"; case "Shaft" -> "shaft"; default -> role.toLowerCase(Locale.ROOT); }; }
    void title(Component title, int y, boolean shadow) {
        title = BookContents.paper(title.copy().withStyle(ChatFormatting.UNDERLINE));
        elements.add(new BookDocument.Text((BookDocument.WIDTH - (int) Math.ceil(font.width(title) * 1.2F)) / 2, y, title.getVisualOrderText(), 1.2F, shadow));
    }
    private void text(List<Component> paragraphs, int x, int top, int width, int bottom) {
        int y = top;
        for (var paragraph : paragraphs) {
            for (var line : font.split(BookContents.paper(paragraph), width)) {
                if (y + 9 > bottom) { return; }
                elements.add(new BookDocument.Text(x, y, line)); y += 9;
            }
        }
    }
}
