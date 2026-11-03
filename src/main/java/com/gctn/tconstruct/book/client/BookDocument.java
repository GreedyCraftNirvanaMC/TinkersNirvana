/* SPDX-License-Identifier: LGPL-3.0-only
 * Mantle's MIT-licensed book layout adapted to native 1.21.1 pages. See third_party/Mantle-LICENSE.txt.
 */
package com.gctn.tconstruct.book.client;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/** Fixed paper coordinates, paginated before drawing. No page body is clipped into a scrolling panel. */
final class BookDocument {
    static final int WIDTH = 182, HEIGHT = 176, BOTTOM = 156;
    sealed interface Element permits Text, Icon, Image, Link, Section, Diagram, Structure {}
    record Text(int x, int y, FormattedCharSequence line) implements Element {}
    record Icon(int x, int y, List<ItemStack> items) implements Element {}
    record Image(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight) implements Element {}
    record Link(int x, int y, int width, Component label, String target, ItemStack icon) implements Element {}
    record Section(int x, int y, Component label, String target, ItemStack icon) implements Element {}
    record Diagram(int x, int y, ItemStack tool, List<List<ItemStack>> inputs, boolean modifier) implements Element {}
    record Structure(int x, int y, int width, int height, BookContents.Structure value) implements Element {}
    record Leaf(String id, String section, Component title, List<Element> elements, String source) {}
    final List<Leaf> leaves = new ArrayList<>();
    final Map<String, Integer> locations = new LinkedHashMap<>();
    final List<BookContents.Page> sources;
    private final Font font;

    BookDocument(Font font, List<BookContents.Page> sources) {
        this.font = font; this.sources = sources;
        Map<String, List<BookContents.Page>> sections = new LinkedHashMap<>();
        for (var source : sources) { sections.computeIfAbsent(source.section(), ignored -> new ArrayList<>()).add(source); }
        List<Element> index = new ArrayList<>(); int number = 0;
        for (var section : sections.entrySet()) {
            index.add(new Section(23 + number % 3 * 47, 14 + number / 3 % 3 * 46, Component.translatable("book.tconstruct.section." + section.getKey()), "section:" + section.getKey(), sectionIcon(section.getKey(), section.getValue())));
            number++;
            if (number % 9 == 0) { add(new Leaf(number == 9 ? "index" : "index:" + (number / 9 - 1), "index", Component.empty(), List.copyOf(index), "index")); index.clear(); }
        }
        if (!index.isEmpty()) { add(new Leaf(number < 9 ? "index" : "index:" + number / 9, "index", Component.empty(), List.copyOf(index), "index")); }
        for (var section : sections.entrySet()) {
            var entries = section.getValue();
            if (!section.getKey().equals("intro") && !section.getKey().equals("seared_devices") && !section.getKey().equals("world")) { contents(section.getKey(), entries); }
            else { locations.put("section:" + section.getKey(), leaves.size()); }
            for (var source : entries) { content(source); }
        }
    }
    private void add(Leaf leaf) { locations.putIfAbsent(leaf.id, leaves.size()); leaves.add(leaf); }
    private static ItemStack sectionIcon(String section, List<BookContents.Page> entries) {
        return switch (section) {
            case "intro" -> new ItemStack(com.gctn.tconstruct.book.TinkerBook.ITEM.get());
            case "tools" -> ToolBuilder.assemble(AntiqueTools.get("pickaxe"), List.of(TinkerMaterials.iron, TinkerMaterials.iron, TinkerMaterials.iron));
            case "materials" -> new ItemStack(Items.IRON_INGOT);
            case "bowmaterials" -> new ItemStack(Items.BOW);
            case "traits" -> new ItemStack(Items.PRISMARINE_CRYSTALS);
            case "modifiers" -> new ItemStack(Items.DIAMOND);
            case "seared_devices" -> new ItemStack(com.gctn.tconstruct.smeltery.SmelteryModule.BLOCKS.get("smeltery_controller").get());
            case "world" -> new ItemStack(com.gctn.tconstruct.gadgets.SlimeGear.SLINGS.get("green").get());
            default -> entries.stream().flatMap(page -> page.icons().stream()).findFirst().orElse(new ItemStack(Items.BOOK));
        };
    }
    private void contents(String section, List<BookContents.Page> entries) {
        for (int start = 0; start < entries.size(); start += 24) {
            List<Element> links = new ArrayList<>();
            int count = Math.min(24, entries.size() - start), columns = count > 12 ? 2 : 1;
            for (int row = 0; row < count; row++) {
                var source = entries.get(start + row); int x = row / 12 * WIDTH / columns, y = 20 + row % 12 * 11;
                ItemStack icon = source.icons().isEmpty() ? ItemStack.EMPTY : source.icons().getFirst();
                links.add(new Link(x, y, WIDTH / columns - 3, source.title(), source.id(), icon));
            }
            add(new Leaf("section:" + section + (start == 0 ? "" : ":" + start / 24), section, Component.translatable("book.tconstruct.section." + section), links, "section:" + section));
        }
    }
    int find(String id) {
        Integer exact = locations.get(id); if (exact != null) { return exact; }
        String legacy = id.replace('.', '/');
        for (var source : sources) {
            String raw = source.data().has("id") ? source.data().get("id").getAsString() : "";
            if (raw.equals(legacy) || source.id().equals("material:" + id) || source.id().endsWith(":" + legacy)) { return locations.getOrDefault(source.id(), 0); }
        }
        return 0;
    }
    private void content(BookContents.Page source) {
        locations.put(source.id(), leaves.size());
        if (source.data().has("material")) { material(source); return; }
        if (source.data().has("tool") || source.data().has("modifier") || source.id().startsWith("tool:") || source.id().startsWith("modifier:")) { station(source); return; }
        Flow flow = new Flow(source);
        if (source.structure() != null) {
            flow.elements.add(new Structure(0, 16, WIDTH, 133, source.structure())); flow.y = BOTTOM;
        } else if (source.image() != null) {
            var image = source.image(); var texture = ResourceLocation.tryParse(image.get("file").getAsString());
            if (texture != null) {
                float w = image.has("width") ? image.get("width").getAsFloat() : 150, h = image.has("height") ? image.get("height").getAsFloat() : 90;
                float size = Math.min(1, Math.min(WIDTH / Math.max(1, w), 125 / Math.max(1, h)));
                int height = Math.max(1, Math.round(h * size)), width = Math.max(1, Math.round(w * size));
                flow.elements.add(new Image((WIDTH - width) / 2, flow.y, width, height, texture, image.has("texture_width") ? image.get("texture_width").getAsInt() : width, image.has("texture_height") ? image.get("texture_height").getAsInt() : height)); flow.y += height + 5;
            }
        }
        if (source.structure() == null) { flow.icons(source.icons()); }
        flow.text(source.text()); flow.finish();
    }
    private void station(BookContents.Page source) {
        boolean modifier = source.data().has("modifier") || source.id().startsWith("modifier:");
        Flow flow = new Flow(source); List<Component> description = source.data().has("text") ? BookContents.text(source.data(), "text") : source.text();
        List<FormattedCharSequence> rest = flow.lines(description, 0, flow.y, WIDTH, 72);
        List<ItemStack> diagram = new ArrayList<>(source.icons());
        ItemStack tool = modifier ? ToolBuilder.assemble(AntiqueTools.get("pickaxe"), List.of(TinkerMaterials.iron, TinkerMaterials.iron, TinkerMaterials.iron)) : diagram.isEmpty() ? ItemStack.EMPTY : diagram.removeFirst();
        List<List<ItemStack>> inputs = new ArrayList<>();
        if (modifier) {
            String id = source.data().has("modifier") ? source.data().get("modifier").getAsString() : source.id().substring("modifier:".length());
            switch (id) {
                case "beheading" -> { inputs.add(List.of(new ItemStack(Items.ENDER_PEARL))); inputs.add(List.of(new ItemStack(Items.OBSIDIAN))); }
                case "glowing" -> { inputs.add(List.of(new ItemStack(Items.ENDER_EYE))); inputs.add(List.of(new ItemStack(Items.GLOWSTONE_DUST, 2))); }
                case "embossment" -> {
                    inputs.add(List.of(AntiqueParts.get("pick_head").create(TinkerMaterials.iron)));
                    for (String slime : List.of("green", "blue", "magma")) { inputs.add(List.of(new ItemStack(com.gctn.tconstruct.common.soil.SoilModule.CRYSTALS.get(slime).get()))); }
                    inputs.add(List.of(new ItemStack(Items.GOLD_BLOCK)));
                }
                case "fortify" -> {
                    inputs.add(TinkerMaterials.materials.stream().filter(material -> !material.isHidden() && material.hasStats("Head")).map(material -> AntiqueParts.get("sharpening_kit").create(material)).toList());
                    inputs.add(List.of(new ItemStack(Items.FLINT)));
                }
                default -> { if (!diagram.isEmpty()) { inputs.add(List.copyOf(diagram)); } }
            }
        } else { for (var part : diagram) { inputs.add(List.of(part)); } }
        if (!tool.isEmpty()) { flow.elements.add(new Diagram(103, modifier ? 74 : 84, tool, List.copyOf(inputs), modifier)); }
        String field = modifier ? "effects" : "properties";
        List<Component> properties = new ArrayList<>();
        if (source.data().has(field)) {
            properties.add(Component.translatable("book.tconstruct.properties").withStyle(ChatFormatting.UNDERLINE));
            for (var property : source.data().getAsJsonArray(field)) { properties.add(Component.literal("• " + property.getAsString())); }
        }
        if (modifier) {
            String id = source.data().has("modifier") ? source.data().get("modifier").getAsString() : source.id().substring("modifier:".length());
            var definition = ToolModifiers.entries().get(id);
            if (definition != null) { properties.add(Component.translatable("book.tconstruct.modifier_limits", definition.maxLevel(), definition.unitsPerLevel())); }
        }
        rest.addAll(flow.lines(properties, 0, 84, 97, BOTTOM));
        flow.y = BOTTOM;
        if (!rest.isEmpty()) { flow.next(); flow.sequences(rest); }
        flow.finish();
    }
    private void material(BookContents.Page source) {
        Material material = TinkerMaterials.getMaterial(source.data().get("material").getAsString());
        if (material == null) { new Flow(source).finish(); return; }
        Flow flow = new Flow(source);
        boolean ranged = source.data().get("ranged").getAsBoolean();
        List<String> groups = ranged ? List.of("Bow", "BowString", "Shaft", "Fletching") : List.of("Head", "Handle", "Extra");
        int secondRow = 90;
        for (int index = 0; index < groups.size(); index++) {
            String role = groups.get(index); if (!material.hasStats(role)) { continue; }
            int x = 24 + index % 2 * 79, y = index < 2 ? 21 : secondRow;
            List<Component> lines = new ArrayList<>(); lines.add(Component.translatable("book.tconstruct.group." + role).withStyle(ChatFormatting.UNDERLINE));
            var stats = material.stats.get(role);
            if (stats instanceof HeadMaterialStats head) { stat(lines, "durability", head.durability); stat(lines, "speed", head.miningspeed); stat(lines, "attack", head.attack); stat(lines, "tier", head.harvestLevel); }
            else if (stats instanceof HandleMaterialStats handle) { stat(lines, "modifier", handle.modifer); stat(lines, "durability", handle.durability); }
            else if (stats instanceof ExtraMaterialStats extra) { stat(lines, "durability", extra.durability); }
            else if (stats instanceof BowMaterialStats bow) { stat(lines, "draw", bow.drawSpeed); stat(lines, "range", bow.range); stat(lines, "bonus", bow.bonusDamage); }
            else if (stats instanceof BowStringMaterialStats string) { stat(lines, "modifier", string.modifier); }
            else if (stats instanceof ArrowShaftMaterialStats shaft) { stat(lines, "modifier", shaft.modifier); stat(lines, "ammo", shaft.bonusAmmo); }
            else if (stats instanceof FletchingMaterialStats fletching) { stat(lines, "accuracy", fletching.accuracy); stat(lines, "modifier", fletching.modifier); }
            for (var trait : material.getTraits(role)) { lines.add(trait.trait().getDisplayName(trait.level()).copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, trait.trait().getDescription())))); }
            int height = lines.stream().mapToInt(line -> Math.max(1, font.split(line, 76).size()) * 10).sum();
            if (index < 2) { secondRow = Math.max(secondRow, 21 + height + 4); }
            flow.lines(lines, x, y, 76, 141);
        }
        int y = 20;
        for (var part : AntiqueParts.entries().values()) {
            if (material.hasStats(part.stat()) && !part.id().equals("bolt_core")) {
                ItemStack icon = part.create(material); if (!icon.isEmpty()) { flow.elements.add(new Icon(0, y, List.of(icon))); y += 20; if (y > 140) { break; } }
            }
        }
        if (!ranged && source.data().has("flavour")) { flow.lines(List.of(Component.literal("“" + source.data().get("flavour").getAsString() + "”").withStyle(ChatFormatting.ITALIC)), 103, secondRow, 76, 139); }
        flow.elements.add(new Link(24, 145, WIDTH - 24, Component.translatable("book.tconstruct.material_details"), source.id() + "#1", ItemStack.EMPTY));
        flow.y = BOTTOM; flow.next();
        flow.icons(source.icons()); flow.text(source.text()); flow.finish();
    }
    private static void stat(List<Component> list, String key, Object value) { list.add(Component.translatable("book.tconstruct.stat." + key, value)); }

    private final class Flow {
        final BookContents.Page source; final List<Element> elements = new ArrayList<>(); int y = 19, continuation;
        Flow(BookContents.Page source) { this.source = source; }
        void next() { finish(); continuation++; elements.clear(); y = 19; }
        void finish() { add(new Leaf(source.id() + (continuation == 0 ? "" : "#" + continuation), source.section(), continuation == 0 ? source.title() : Component.translatable("book.tconstruct.continued", source.title()), List.copyOf(elements), source.id())); }
        List<FormattedCharSequence> lines(List<Component> paragraphs, int x, int top, int width, int bottom) {
            List<FormattedCharSequence> overflow = new ArrayList<>(); int cursor = top;
            for (Component paragraph : paragraphs) {
                var lines = font.split(paragraph, width);
                if (lines.isEmpty()) { lines = List.of(FormattedCharSequence.EMPTY); }
                for (var line : lines) { if (cursor + 9 <= bottom) { elements.add(new Text(x, cursor, line)); } else { overflow.add(line); } cursor += 10; }
            }
            return overflow;
        }
        void sequences(List<FormattedCharSequence> lines) {
            for (var line : lines) { if (y + 9 > BOTTOM) { next(); } elements.add(new Text(0, y, line)); y += 10; }
        }
        void text(List<Component> paragraphs) {
            for (var paragraph : paragraphs) {
                var lines = font.split(paragraph, WIDTH);
                if (lines.isEmpty()) { lines = List.of(FormattedCharSequence.EMPTY); }
                sequences(lines); y += 3;
            }
        }
        void icons(List<ItemStack> icons) {
            int column = 0;
            for (var icon : icons) {
                if (y + 18 > BOTTOM) { next(); }
                elements.add(new Icon(column * 20, y, List.of(icon)));
                if (++column == 9) { column = 0; y += 20; }
            }
            if (column > 0) { y += 21; }
        }
    }
}
