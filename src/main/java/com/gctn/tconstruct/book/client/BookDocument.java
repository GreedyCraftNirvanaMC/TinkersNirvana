/* SPDX-License-Identifier: LGPL-3.0-only
 * Mantle's MIT-licensed book layout adapted to native 1.21.1 pages. See third_party/Mantle-LICENSE.txt.
 */
package com.gctn.tconstruct.book.client;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

/** Mantle and Antique page templates in their original paper coordinates. */
final class BookDocument {
    static final int WIDTH = 182, HEIGHT = 176, BOTTOM = 156;
    sealed interface Element permits Text, Icon, Image, Link, Tile, Section, Diagram, Structure {}
    record Text(int x, int y, FormattedCharSequence line, float scale, boolean shadow) implements Element {
        Text(int x, int y, FormattedCharSequence line) { this(x, y, line, 1, false); }
    }
    record Icon(int x, int y, List<ItemStack> items, float scale, Component tooltip) implements Element {
        Icon(int x, int y, List<ItemStack> items) { this(x, y, items, 1, null); }
    }
    record Image(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight) implements Element {}
    record Link(int x, int y, int width, Component label, String target, ItemStack icon) implements Element {}
    record Tile(int x, int y, int size, float scale, Component label, String target, ItemStack icon) implements Element {}
    record Section(int x, int y, Component label, String target, ItemStack icon) implements Element {}
    record Diagram(int x, int y, List<ItemStack> tools, List<List<ItemStack>> inputs, boolean modifier) implements Element {}
    record Structure(int x, int y, int width, int height, BookContents.Structure value) implements Element {}
    record Leaf(String id, String section, Component title, List<Element> elements, List<Element> rightElements, String source) {
        Leaf(String id, String section, Component title, List<Element> elements, String source) { this(id, section, title, elements, elements, source); }
        List<Element> elements(boolean right) { return right ? rightElements : elements; }
    }
    final List<Leaf> leaves = new ArrayList<>();
    final Map<String, Integer> locations = new LinkedHashMap<>();
    final List<BookContents.Page> sources;
    final Map<String, BookContents.Page> sourceById = new HashMap<>();
    private final Font font;

    BookDocument(Font font, List<BookContents.Page> sources) {
        this.font = font; this.sources = sources;
        for (var page : sources) { sourceById.put(page.id(), page); }
        Map<String, List<BookContents.Page>> sections = new LinkedHashMap<>();
        for (var source : sources) { sections.computeIfAbsent(source.section(), ignored -> new ArrayList<>()).add(source); }
        List<Element> index = new ArrayList<>(); int number = 0;
        for (var section : sections.entrySet()) {
            index.add(new Section(23 + number % 3 * 47, 20 + number / 3 % 3 * 47, Component.translatable("book.tconstruct.section." + section.getKey()), "section:" + section.getKey(), sectionIcon(section.getKey(), section.getValue())));
            number++;
            if (number % 9 == 0) { add(new Leaf(number == 9 ? "index" : "index:" + (number / 9 - 1), "index", Component.empty(), List.copyOf(index), "index")); index.clear(); }
        }
        if (!index.isEmpty()) { add(new Leaf(number < 9 ? "index" : "index:" + number / 9, "index", Component.empty(), List.copyOf(index), "index")); }
        for (var section : sections.entrySet()) {
            var entries = section.getValue();
            if (section.getKey().equals("bowmaterials")) { ranged(entries); continue; }
            if (!section.getKey().equals("intro") && !section.getKey().equals("seared_devices") && !section.getKey().equals("world")) { contents(section.getKey(), entries); }
            else { locations.put("section:" + section.getKey(), leaves.size()); }
            for (var source : entries) { content(source); }
        }
    }
    private void add(Leaf leaf) { locations.putIfAbsent(leaf.id, leaves.size()); leaves.add(leaf); }
    private static ItemStack sectionIcon(String section, List<BookContents.Page> entries) {
        ItemStack defined = BookContents.sectionIcon(section);
        return defined.isEmpty() ? entries.stream().flatMap(page -> page.icons().stream()).findFirst().orElse(new ItemStack(Items.BOOK)) : defined;
    }
    private void contents(String section, List<BookContents.Page> entries) {
        if (section.equals("materials")) { materialContents("section:" + section, section, Component.translatable("book.tconstruct.section." + section), entries, 2.5F); return; }
        entries = entries.stream().filter(page -> !page.data().has("show_in_contents") || page.data().get("show_in_contents").getAsBoolean()).toList();
        for (int start = 0; start < entries.size(); start += 30) {
            List<Element> links = new ArrayList<>();
            int count = Math.min(30, entries.size() - start), columns = count * 9 + 20 > HEIGHT - 30 ? 2 : 1;
            for (int row = 0; row < count; row++) {
                var source = entries.get(start + row); int x = row / 15 * WIDTH / columns, y = 20 + row % 15 * 9;
                Component label = source.data().has("listing_title") && !source.data().get("listing_title").getAsString().isBlank() ? Component.literal(source.data().get("listing_title").getAsString()) : source.title();
                links.add(new Link(x, y, WIDTH / columns, label, source.id(), ItemStack.EMPTY));
            }
            add(new Leaf("section:" + section + (start == 0 ? "" : ":" + start / 30), section, Component.translatable("book.tconstruct.section." + section), links, "section:" + section));
        }
    }
    private void materialContents(String id, String section, Component title, List<BookContents.Page> entries, float maximumScale) {
        for (int start = 0; start < entries.size(); start += 49) {
            int count = Math.min(49, entries.size() - start); float scale = entries.size() > 49 ? 1 : maximumScale;
            int size = 20;
            while (scale > 1) { scale -= .25F; size = (int) (20 * scale); if ((WIDTH - 30) / size * ((HEIGHT - 20) / size) >= count) { break; } }
            List<Element> icons = new ArrayList<>(); int columns = (WIDTH - 30) / size;
            for (int number = 0; number < count; number++) {
                var source = entries.get(start + number); var icon = source.icons().isEmpty() ? ItemStack.EMPTY : source.icons().getFirst();
                icons.add(new Tile(15 + number % columns * size, 20 + number / columns * size, size, scale, source.title(), source.id(), icon));
            }
            add(new Leaf(id + (start == 0 ? "" : ":overview:" + start / 49), section, title, icons, id));
        }
    }
    private void ranged(List<BookContents.Page> entries) {
        List<Element> listing = new ArrayList<>(); int row = 0;
        for (String role : BookMaterialLayout.RANGED) {
            if (entries.stream().anyMatch(page -> TinkerMaterials.getMaterial(page.data().get("material").getAsString()).hasStats(role))) {
                listing.add(new Link(0, 20 + row++ * 9, WIDTH, Component.translatable("stat." + BookMaterialLayout.type(role) + ".name"), "ranged:" + role, ItemStack.EMPTY));
            }
        }
        add(new Leaf("section:bowmaterials", "bowmaterials", Component.translatable("book.tconstruct.section.bowmaterials"), listing, "section:bowmaterials"));
        for (String role : BookMaterialLayout.RANGED) {
            var valid = entries.stream().filter(page -> TinkerMaterials.getMaterial(page.data().get("material").getAsString()).hasStats(role)).toList();
            List<BookContents.Page> icons = new ArrayList<>();
            for (int i = 0; i < valid.size(); i++) {
                var page = valid.get(i);
                icons.add(new BookContents.Page("ranged:" + role + ":" + i / 3, "bowmaterials", i, page.title(), page.text(), page.icons(), null, null, page.data()));
            }
            materialContents("ranged:" + role, "bowmaterials", Component.translatable("stat." + BookMaterialLayout.type(role) + ".name"), icons, 1);
            for (int i = 0; i < valid.size(); i += 3) {
                String id = "ranged:" + role + ":" + i / 3;
                for (int j = i; j < Math.min(i + 3, valid.size()); j++) { locations.putIfAbsent(valid.get(j).id(), leaves.size()); }
                add(BookMaterialLayout.ranged(font, id, role, valid.subList(i, Math.min(i + 3, valid.size()))));
            }
        }
    }
    int find(String id) {
        Integer exact = locations.get(id); if (exact != null) { return exact; }
        if (id.contains("#")) { return find(id.substring(0, id.indexOf('#'))); }
        String legacy = id.replace('.', '/');
        for (var source : sources) {
            String raw = source.data().has("id") ? source.data().get("id").getAsString() : "";
            if (raw.equals(legacy) || source.id().equals("material:" + id) || source.id().endsWith(":" + legacy)) { return locations.getOrDefault(source.id(), 0); }
        }
        return 0;
    }
    private void content(BookContents.Page source) {
        locations.put(source.id(), leaves.size());
        if (source.data().has("layout") && source.data().get("layout").getAsString().equals("blank")) { add(new Leaf(source.id(), source.section(), Component.empty(), List.of(), source.id())); return; }
        if (source.data().has("material")) { material(source); return; }
        if (source.data().has("tool") || source.data().has("modifier") || source.id().startsWith("tool:") || source.id().startsWith("modifier:")) { station(source); return; }
        Flow flow = new Flow(source);
        if (source.structure() != null) {
            boolean caption = !source.text().isEmpty(); int offset = caption ? 15 : 0;
            flow.elements.add(new Structure(offset, flow.y, WIDTH - offset * 2, HEIGHT - flow.y - 10 - offset * 2, source.structure()));
            flow.y = HEIGHT - 10 - offset * 2;
        } else if (source.image() != null) {
            var image = source.image(); var texture = ResourceLocation.tryParse(image.get("file").getAsString());
            if (texture != null) {
                int width = image.has("width") ? image.get("width").getAsInt() : WIDTH, height = image.has("height") ? image.get("height").getAsInt() : 100;
                boolean centered = source.data().has("layout") && source.data().get("layout").getAsString().equals("imageText2");
                flow.elements.add(new Image(centered ? (HEIGHT - width) / 2 : 0, flow.y, width, height, texture, image.has("texture_width") ? image.get("texture_width").getAsInt() : width, image.has("texture_height") ? image.get("texture_height").getAsInt() : height)); flow.y += centered ? height + 5 : 105;
            }
        }
        if (source.structure() == null) { flow.icons(source.icons()); }
        flow.text(source.text()); flow.finish();
    }
    private void station(BookContents.Page source) {
        boolean modifier = source.data().has("modifier") || source.id().startsWith("modifier:");
        Flow flow = new Flow(source); List<Component> description = source.data().has("text") ? BookContents.text(source.data(), "text") : source.text();
        flow.lines(description, modifier ? 10 : 5, modifier ? 20 : 16, WIDTH - (modifier ? 20 : 10), modifier ? 20 + WIDTH / 3 - 10 : 16 + WIDTH / 3 - 10);
        List<ItemStack> diagram = new ArrayList<>(source.icons());
        ItemStack tool = modifier ? ToolBuilder.assemble(AntiqueTools.get("pickaxe"), List.of(TinkerMaterials.wood, TinkerMaterials.cobalt, TinkerMaterials.ardite)) : diagram.isEmpty() ? ItemStack.EMPTY : diagram.removeFirst();
        List<List<ItemStack>> inputs = new ArrayList<>();
        if (modifier) {
            String id = source.data().has("modifier") ? source.data().get("modifier").getAsString() : source.id().substring("modifier:".length());
            switch (id) {
                case "beheading" -> { inputs.add(List.of(new ItemStack(Items.ENDER_PEARL))); inputs.add(List.of(new ItemStack(Items.OBSIDIAN))); }
                case "glowing" -> { inputs.add(List.of(new ItemStack(Items.ENDER_EYE))); inputs.add(List.of(new ItemStack(Items.GLOWSTONE_DUST, 2))); }
                case "embossment" -> {
                    inputs.add(AntiqueTools.PICKAXE.parts().stream().map(com.gctn.tconstruct.tools.ToolDefinition.Part::definition)
                            .filter(Objects::nonNull).map(part -> part.create(TinkerMaterials.iron)).filter(stack -> !stack.isEmpty()).toList());
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
        List<ItemStack> tools = new ArrayList<>();
        if (modifier && source.data().has("demoTool")) {
            for (var name : source.data().getAsJsonArray("demoTool")) {
                var key = ResourceLocation.tryParse(name.getAsString()); var entry = key == null ? null : AntiqueTools.get(key.getPath());
                if (entry == null) { continue; }
                List<Material> materials = new ArrayList<>();
                for (int i = 0; i < entry.definition().parts().size(); i++) {
                    String stat = entry.definition().parts().get(i).stat();
                    Material material = stat.equals("BowString") ? TinkerMaterials.string : stat.equals("Shaft") ? TinkerMaterials.wood : stat.equals("Fletching") ? TinkerMaterials.feather : List.of(TinkerMaterials.wood, TinkerMaterials.cobalt, TinkerMaterials.ardite, TinkerMaterials.manyullyn).get(i % 4);
                    materials.add(material);
                }
                var demo = ToolBuilder.assemble(entry, materials); if (!demo.isEmpty()) { tools.add(demo); }
            }
        }
        if (tools.isEmpty() && !tool.isEmpty()) { tools.add(tool); }
        if (!tools.isEmpty()) { flow.elements.add(new Diagram(modifier ? WIDTH / 2 + 20 : WIDTH - 72 - 8, modifier ? HEIGHT / 2 + 30 : HEIGHT - 72 - 16, List.copyOf(tools), List.copyOf(inputs), modifier)); }
        String field = modifier ? "effects" : "properties";
        List<Component> properties = new ArrayList<>();
        if (source.data().has(field)) {
            properties.add((source.data().has("properties_label") ? Component.literal(source.data().get("properties_label").getAsString()) : Component.translatable("book.tconstruct.properties")).withStyle(ChatFormatting.UNDERLINE));
            flow.lines(properties, modifier ? 10 : 5, modifier ? 70 : 82, modifier ? WIDTH / 2 - 5 : 81, HEIGHT);
            properties.clear();
            for (var property : source.data().getAsJsonArray(field)) { properties.add(Component.literal("●\u00a0" + property.getAsString())); }
        }
        flow.lines(properties, modifier ? 10 : 5, modifier ? 80 : 92, WIDTH / 2 + 5, HEIGHT);
        flow.finish();
    }
    private void material(BookContents.Page source) { add(BookMaterialLayout.material(font, source)); }

    private final class Flow {
        final BookContents.Page source; final List<Element> elements = new ArrayList<>(); int y;
        Flow(BookContents.Page source) {
            this.source = source;
            boolean titleless = source.data().has("titleless") && source.data().get("titleless").getAsBoolean();
            y = titleless ? 0 : 16;
        }
        void finish() {
            boolean titleless = source.data().has("titleless") && source.data().get("titleless").getAsBoolean() && !source.data().has("tool") && !source.data().has("modifier");
            add(new Leaf(source.id(), source.section(), titleless ? Component.empty() : source.title(), List.copyOf(elements), source.id()));
        }
        List<FormattedCharSequence> lines(List<Component> paragraphs, int x, int top, int width, int bottom) {
            List<FormattedCharSequence> overflow = new ArrayList<>(); int cursor = top;
            for (Component paragraph : paragraphs) {
                var lines = font.split(BookContents.paper(paragraph), width);
                if (lines.isEmpty()) { lines = List.of(FormattedCharSequence.EMPTY); }
                for (var line : lines) { if (cursor + 9 <= bottom) { elements.add(new Text(x, cursor, line)); } else { overflow.add(line); } cursor += 9; }
            }
            return overflow;
        }
        void sequences(List<FormattedCharSequence> lines) {
            for (var line : lines) { if (y + 9 > HEIGHT) { return; } elements.add(new Text(0, y, line)); y += 9; }
        }
        void text(List<Component> paragraphs) {
            for (var paragraph : paragraphs) {
                var lines = font.split(BookContents.paper(paragraph), WIDTH);
                if (lines.isEmpty()) { lines = List.of(FormattedCharSequence.EMPTY); }
                sequences(lines); y += 9;
            }
        }
        void icons(List<ItemStack> icons) {
            int column = 0;
            for (var icon : icons) {
                if (y + 18 > BOTTOM) { return; }
                elements.add(new Icon(column * 20, y, List.of(icon)));
                if (++column == 9) { column = 0; y += 20; }
            }
            if (column > 0) { y += 21; }
        }
    }
}
