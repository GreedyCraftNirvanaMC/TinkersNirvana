/* SPDX-License-Identifier: LGPL-3.0-only; Antique book text with live registry-driven reference pages. */
package com.gctn.tconstruct.book.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolModifiers;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.*;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class BookContents {
    public static final List<String> SECTIONS = List.of("intro", "tools", "materials", "bowmaterials", "modifiers", "seared_devices");
    private static volatile Map<ResourceLocation, JsonObject> resources = Map.of();
    public record Page(String id, String section, int order, Component title, List<Component> text, List<ItemStack> icons, JsonObject image, Structure structure, JsonObject data) {
        public Page(String id, String section, int order, Component title, List<Component> text, List<ItemStack> icons, JsonObject image, Structure structure) { this(id, section, order, title, text, icons, image, structure, new JsonObject()); }
    }
    public record Structure(int width, int height, int depth, List<PlacedBlock> blocks) {}
    public record PlacedBlock(int x, int y, int z, BlockState state) {}
    record Shortcut(String page, ItemStack icon) {}
    private record Input(ItemStack stack, float value) {}
    private BookContents() {}
    @SubscribeEvent private static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>>() {
            @Override protected Map<ResourceLocation, JsonObject> prepare(ResourceManager manager, ProfilerFiller profiler) {
                Map<ResourceLocation, JsonObject> result = new TreeMap<>();
                manager.listResources("book", id -> id.getPath().endsWith(".json") && (id.getPath().startsWith("book/pages/") || id.getPath().startsWith("book/language/") || id.getPath().equals("book/definition.json"))).forEach((id, resource) -> {
                    try (var reader = resource.openAsReader()) { result.put(id, JsonParser.parseReader(reader).getAsJsonObject()); }
                    catch (Exception error) { TinkersNirvana.LOGGER.error("Cannot load book resource {}", id, error); }
                });
                return Map.copyOf(result);
            }
            @Override protected void apply(Map<ResourceLocation, JsonObject> data, ResourceManager manager, ProfilerFiller profiler) { resources = data; }
        });
    }
    /** Resource packs add pages at assets/<namespace>/book/pages/<language>/<section>/<name>.json. */
    public static List<Page> pages() {
        String language = Minecraft.getInstance().getLanguageManager().getSelected();
        var localized = new LinkedHashMap<String, JsonObject>(); var labels = new HashMap<String, String>();
        for (String locale : language.equals("en_us") ? List.of("en_us") : List.of("en_us", language)) {
            resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                String path = entry.getKey().getPath(); JsonObject data = entry.getValue();
                if (path.startsWith("book/pages/" + locale + "/")) { localized.put(entry.getKey().getNamespace() + ":" + string(data, "id", path.substring(("book/pages/" + locale + "/").length())), data); }
                else if (path.equals("book/language/" + locale + ".json")) { data.entrySet().forEach(value -> labels.put(value.getKey(), value.getValue().getAsString())); }
            });
        }
        List<Page> pages = new ArrayList<>();
        var definition = resources.get(ResourceLocation.fromNamespaceAndPath("tconstruct", "book/definition.json"));
        Set<String> active = new HashSet<>();
        if (definition != null) { for (var section : definition.getAsJsonArray("sections")) { for (var id : section.getAsJsonObject().getAsJsonArray("pages")) { active.add("tconstruct:" + id.getAsString()); } } }
        List<ItemStack> catalog = BuiltInRegistries.ITEM.stream().filter(item -> item != net.minecraft.world.item.Items.AIR).map(ItemStack::new).toList();
        Map<Material, List<Input>> inputs = new IdentityHashMap<>();
        for (ItemStack stack : catalog) { var match = MaterialInputs.match(stack); if (match != null) { inputs.computeIfAbsent(match.material(), ignored -> new ArrayList<>()).add(new Input(stack, match.value())); } }
        for (var entry : localized.entrySet()) {
            if (definition != null && entry.getKey().startsWith("tconstruct:") && !active.contains(entry.getKey())) { continue; }
            JsonObject data = entry.getValue().deepCopy(); String section = string(data, "section", "intro"), title = string(data, "title", "");
            List<Component> text = text(data, "text"), iconsText = new ArrayList<>(); List<ItemStack> icons = new ArrayList<>();
            if (data.has("tool")) {
                String id = data.get("tool").getAsString(); var tool = AntiqueTools.get(id);
                title = title.isBlank() ? Component.translatable("item.tconstruct." + id + ".name").getString() : title;
                if (tool != null) { addTool(tool, icons, iconsText); }
            }
            if (data.has("modifier")) {
                String id = data.get("modifier").getAsString(); id = switch (id) { case "extramodifier" -> "creative"; case "extratrait" -> "embossment"; case "mod_autosmelt" -> "autosmelt"; case "bane_of_arthopods" -> "bane_of_arthropods"; default -> id; };
                data.addProperty("modifier", id);
                title = title.isBlank() ? Component.translatable("modifier.tconstruct." + id).getString() : title;
                addModifier(id, icons, iconsText, catalog);
            }
            for (String key : List.of("properties", "effects")) {
                if (data.has(key)) { text.add(Component.literal("")); for (JsonElement row : data.getAsJsonArray(key)) { text.add(Component.literal("• " + row.getAsString())); } }
            }
            text.addAll(iconsText);
            var heading = Component.literal(title.isBlank() ? string(data, "listing_title", "") : title);
            if (data.has("title_color")) { heading.withColor(data.get("title_color").getAsInt()); }
            pages.add(new Page(entry.getKey(), section, data.has("order") ? data.get("order").getAsInt() : 0, heading, List.copyOf(text), List.copyOf(icons), data.has("image") ? data.getAsJsonObject("image") : null, data.has("structure") ? structure(data.getAsJsonObject("structure")) : null, data));
        }
        for (Material material : TinkerMaterials.materials) { if (!material.isHidden()) { material(pages, material, labels, inputs.getOrDefault(material, List.of())); } }
        pages.sort(Comparator.comparingInt((Page page) -> { int i = SECTIONS.indexOf(page.section); return i < 0 ? SECTIONS.size() : i; }).thenComparing(Page::section).thenComparingInt(Page::order).thenComparing(page -> page.title.getString()));
        return List.copyOf(pages);
    }
    private static void addTool(AntiqueTools.Entry tool, List<ItemStack> icons, List<Component> text) {
        List<Material> materials = new ArrayList<>();
        text.add(Component.translatable("book.tconstruct.parts"));
        for (int index = 0; index < tool.definition().parts().size(); index++) {
            var part = tool.definition().parts().get(index);
            Material preferred = switch (part.stat()) { case "BowString" -> TinkerMaterials.string; case "Shaft" -> TinkerMaterials.wood; case "Fletching" -> TinkerMaterials.feather; default -> List.of(TinkerMaterials.wood, TinkerMaterials.iron, TinkerMaterials.cobalt, TinkerMaterials.manyullyn).get(index % 4); };
            Material material = preferred.hasStats(part.stat()) ? preferred : TinkerMaterials.materials.stream().filter(value -> !value.isHidden() && value.hasStats(part.stat()) && (!part.stat().equals("Bow") || value.hasStats("Head")) && (!part.stat().equals("Handle") || tool.definition().action() != com.gctn.tconstruct.tools.ToolDefinition.Action.CROSSBOW || value.hasStats("Extra"))).findFirst().orElse(null);
            if (material == null) { return; } materials.add(material);
            if (part.definition() != null) { ItemStack icon = part.definition().create(material); if (!icon.isEmpty()) { icons.add(icon); } }
            text.add(Component.literal("• ").append(Component.translatable("item.tconstruct." + part.part() + ".name")));
        }
        ItemStack example = ToolBuilder.assemble(tool, materials); if (!example.isEmpty()) { icons.addFirst(example); }
    }
    private static void addModifier(String id, List<ItemStack> icons, List<Component> text, List<ItemStack> catalog) {
        var definition = ToolModifiers.entries().get(id);
        if (definition == null) { return; }
        text.add(Component.translatable("book.tconstruct.modifier_limits", definition.maxLevel(), definition.unitsPerLevel()));
        // Predicates are shared with crafting, including addon/tag ingredients. Only create stacks when needed.
        catalog.stream().filter(stack -> ToolModifiers.ingredientValue(definition, stack) > 0).limit(12).forEach(icons::add);
    }
    private static void material(List<Page> pages, Material material, Map<String, String> labels, List<Input> inputs) {
        ItemStack representative = material.getRepresentativeItem();
        if (representative.isEmpty()) { representative = inputs.stream().min(Comparator.comparingInt(BookContents::representativePriority)).map(Input::stack).orElse(ItemStack.EMPTY); }
        for (boolean ranged : List.of(false, true)) {
            if (!ranged && !material.hasStats("Head")) { continue; }
            List<Component> text = new ArrayList<>(); List<ItemStack> icons = new ArrayList<>();
            if (!representative.isEmpty()) { icons.add(representative); }
            for (String position : List.of("Head", "Handle", "Extra", "Bow", "BowString", "Shaft", "Fletching")) {
                if (ranged != Set.of("Bow", "BowString", "Shaft", "Fletching").contains(position) || !material.hasStats(position)) { continue; }
                text.add(Component.translatable("book.tconstruct.group." + position)); AbstractMaterialStats stats = material.stats.get(position);
                if (stats instanceof HeadMaterialStats head) { stat(text, "durability", head.durability); stat(text, "speed", head.miningspeed); stat(text, "attack", head.attack); stat(text, "tier", head.harvestLevel); }
                else if (stats instanceof HandleMaterialStats handle) { stat(text, "modifier", handle.modifer); stat(text, "durability", handle.durability); }
                else if (stats instanceof ExtraMaterialStats extra) { stat(text, "durability", extra.durability); }
                else if (stats instanceof BowMaterialStats bow) { stat(text, "draw", bow.drawSpeed); stat(text, "range", bow.range); stat(text, "bonus", bow.bonusDamage); }
                else if (stats instanceof BowStringMaterialStats string) { stat(text, "modifier", string.modifier); }
                else if (stats instanceof ArrowShaftMaterialStats shaft) { stat(text, "modifier", shaft.modifier); stat(text, "ammo", shaft.bonusAmmo); }
                else if (stats instanceof FletchingMaterialStats fletching) { stat(text, "accuracy", fletching.accuracy); stat(text, "modifier", fletching.modifier); }
                for (var trait : material.getTraits(position)) { text.add(trait.trait().getDisplayName(trait.level())); text.add(trait.trait().getDescription()); }
                text.add(Component.literal(""));
            }
            if (text.isEmpty()) { continue; }
            if (labels.containsKey(material.identifier + ".flavour")) { text.addFirst(Component.literal(labels.get(material.identifier + ".flavour"))); }
            if (material.isCraftable()) { text.add(Component.translatable("book.tconstruct.craftable")); }
            if (material.isCastable()) { text.add(Component.translatable("book.tconstruct.castable")); }
            if (!inputs.isEmpty()) { text.add(Component.translatable("book.tconstruct.ingredients")); for (Input input : inputs) { if (!ItemStack.isSameItemSameComponents(input.stack, representative)) { icons.add(input.stack); } String value = java.math.BigDecimal.valueOf(input.value).setScale(3, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(); text.add(Component.translatable("book.tconstruct.material_value", input.stack.getHoverName(), value)); } }
            if (icons.isEmpty()) {
                AntiqueParts.entries().values().stream().filter(part -> material.hasStats(part.stat()))
                        .map(part -> part.create(material)).filter(stack -> !stack.isEmpty()).findFirst().ifPresent(icons::add);
            }
            var metadata = new JsonObject(); metadata.addProperty("material", material.identifier); metadata.addProperty("ranged", ranged);
            if (labels.containsKey(material.identifier + ".flavour")) { metadata.addProperty("flavour", labels.get(material.identifier + ".flavour")); }
            pages.add(new Page("material:" + material.identifier + (ranged ? ":ranged" : ""), ranged ? "bowmaterials" : "materials", TinkerMaterials.materials.indexOf(material), Component.translatable("material." + material.identifier + ".name").withColor(material.color), List.copyOf(text), List.copyOf(icons), null, null, metadata));
        }
    }
    static Component paper(Component text) {
        return text.copy().withStyle(style -> style.withFont(ResourceLocation.withDefaultNamespace("uniform")));
    }
    static ItemStack sectionIcon(String section) {
        var definition = resources.get(ResourceLocation.fromNamespaceAndPath("tconstruct", "book/definition.json"));
        if (definition != null) { for (var entry : definition.getAsJsonArray("sections")) {
            var data = entry.getAsJsonObject(); if (!data.get("id").getAsString().equals(section)) { continue; }
            return icon(data.get("icon").getAsString());
        } }
        return ItemStack.EMPTY;
    }
    static List<Shortcut> shortcuts() {
        var definition = resources.get(ResourceLocation.fromNamespaceAndPath("tconstruct", "book/definition.json"));
        if (definition == null || !definition.has("shortcuts")) { return List.of(); }
        List<Shortcut> result = new ArrayList<>();
        for (var entry : definition.getAsJsonArray("shortcuts")) {
            var data = entry.getAsJsonObject();
            result.add(new Shortcut(data.get("page").getAsString(), icon(data.get("icon").getAsString())));
        }
        return List.copyOf(result);
    }
    private static ItemStack icon(String name) {
        var id = ResourceLocation.tryParse(name);
        return id != null && BuiltInRegistries.ITEM.containsKey(id) ? new ItemStack(BuiltInRegistries.ITEM.get(id)) : ItemStack.EMPTY;
    }
    private static int representativePriority(Input input) {
        boolean ingot = input.stack.getTags().anyMatch(tag -> tag.location().getNamespace().equals("c")
                && (tag.location().getPath().equals("ingots") || tag.location().getPath().startsWith("ingots/")));
        return ingot ? 0 : Math.abs(input.value - 1) < 0.00001F ? 1 : 2;
    }
    private static void stat(List<Component> text, String name, Object value) { text.add(Component.translatable("book.tconstruct.stat." + name, value)); }
    static List<Component> text(JsonObject data, String name) {
        List<Component> result = new ArrayList<>();
        var paragraph = Component.empty();
        if (data.has(name)) { for (JsonElement row : data.getAsJsonArray(name)) {
            if (row.isJsonPrimitive()) { paragraph.append(Component.literal(row.getAsString())); }
            else {
                JsonObject object = row.getAsJsonObject();
                if (object.has("paragraph") && object.get("paragraph").getAsBoolean() && !paragraph.getString().isEmpty()) { result.add(paragraph); paragraph = Component.empty(); }
                var line = Component.literal(string(object, "text", "")); var style = net.minecraft.network.chat.Style.EMPTY;
                for (String flag : List.of("bold", "italic", "underlined", "strikethrough", "obfuscated")) {
                    if (object.has(flag) && object.get(flag).getAsBoolean()) { style = style.applyFormat(switch (flag) { case "bold" -> net.minecraft.ChatFormatting.BOLD; case "italic" -> net.minecraft.ChatFormatting.ITALIC; case "underlined" -> net.minecraft.ChatFormatting.UNDERLINE; case "strikethrough" -> net.minecraft.ChatFormatting.STRIKETHROUGH; default -> net.minecraft.ChatFormatting.OBFUSCATED; }); }
                }
                if (object.has("color")) { var color = net.minecraft.network.chat.TextColor.parseColor(object.get("color").getAsString()).result(); if (color.isPresent()) { style = style.withColor(color.get()); } }
                if (object.has("tooltip")) { var tooltip = new StringBuilder(); for (var part : object.getAsJsonArray("tooltip")) { if (!tooltip.isEmpty()) { tooltip.append('\n'); } tooltip.append(part.getAsString()); } style = style.withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal(tooltip.toString()))); }
                if (object.has("action")) { String action = object.get("action").getAsString(); for (String prefix : List.of("go-to-page-rtn:", "go-to-page:")) { if (action.startsWith(prefix)) { style = style.withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.CHANGE_PAGE, action)); break; } } }
                paragraph.append(line.setStyle(style));
            }
        } }
        if (!paragraph.getString().isEmpty()) { result.add(paragraph); }
        return result;
    }
    private static String string(JsonObject data, String name, String fallback) { return data.has(name) ? data.get(name).getAsString() : fallback; }
    private static Structure structure(JsonObject data) {
        JsonArray size = data.getAsJsonArray("size"); Map<net.minecraft.core.BlockPos, PlacedBlock> blocks = new LinkedHashMap<>();
        for (JsonElement dimension : size) { if (dimension.getAsInt() < 1 || dimension.getAsInt() > 32) { throw new IllegalArgumentException("Guide structure dimensions must be 1..32"); } }
        for (JsonElement element : data.getAsJsonArray("structure")) {
            JsonObject row = element.getAsJsonObject(); ResourceLocation id = ResourceLocation.tryParse(row.get("block").getAsString());
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) { TinkersNirvana.LOGGER.warn("Unknown guide structure block {}", id); continue; }
            BlockState state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
            if (row.has("state")) { for (var property : row.getAsJsonObject("state").entrySet()) { state = property(state, property.getKey(), property.getValue().getAsString()); } }
            JsonArray start = row.getAsJsonArray("pos"), end = row.has("endPos") ? row.getAsJsonArray("endPos") : start;
            for (int x = Math.max(0, start.get(0).getAsInt()); x <= Math.min(size.get(0).getAsInt() - 1, end.get(0).getAsInt()); x++) { for (int y = Math.max(0, start.get(1).getAsInt()); y <= Math.min(size.get(1).getAsInt() - 1, end.get(1).getAsInt()); y++) { for (int z = Math.max(0, start.get(2).getAsInt()); z <= Math.min(size.get(2).getAsInt() - 1, end.get(2).getAsInt()); z++) { blocks.putIfAbsent(new net.minecraft.core.BlockPos(x, y, z), new PlacedBlock(x, y, z, state)); } } }
        }
        return new Structure(size.get(0).getAsInt(), size.get(1).getAsInt(), size.get(2).getAsInt(), blocks.values().stream().filter(block -> !block.state().isAir()).sorted(Comparator.comparingInt(PlacedBlock::y).thenComparingInt(PlacedBlock::x).thenComparingInt(PlacedBlock::z)).toList());
    }
    private static <T extends Comparable<T>> BlockState property(BlockState state, String name, String value) {
        Property<T> property = (Property<T>) state.getBlock().getStateDefinition().getProperty(name);
        return property == null ? state : property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElse(state);
    }
}
