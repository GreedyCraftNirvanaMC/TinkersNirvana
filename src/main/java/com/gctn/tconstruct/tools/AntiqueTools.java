/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.tools.ToolDefinition.Action;
import com.gctn.tconstruct.tools.ToolDefinition.Part;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.tools.Pickaxe;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class AntiqueTools {
    private static final Map<String, Entry> TOOLS = new LinkedHashMap<>();
    public static final ToolDefinition PICKAXE = definition("pickaxe", 1, 1, 0, 1.2F, Action.PICKAXE,
            List.of(BlockTags.MINEABLE_WITH_PICKAXE), handle(), head("pickaxe_head"), extra("binding"));

    static {
        TOOLS.put("pickaxe", new Entry(PICKAXE, Pickaxe.PICKAXE));
        add("shovel", 1, .9F, 0, 1, Action.SHOVEL, List.of(BlockTags.MINEABLE_WITH_SHOVEL), handle(), head("shovel_head"), extra("binding"));
        add("hatchet", 1, 1.1F, .5F, 1.1F, Action.AXE, List.of(BlockTags.MINEABLE_WITH_AXE), handle(), head("axe_head"), extra("binding"));
        add("mattock", 1, .9F, 3, .9F, Action.MATTOCK, List.of(BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_SHOVEL),
                handle(), head("axe_head"), new Part("Shovel", "shovel_head", "Head"));
        add("kama", 1, 1, 0, 1.3F, Action.KAMA, List.of(BlockTags.LEAVES, BlockTags.WOOL, BlockTags.CROPS, BlockTags.REPLACEABLE_BY_TREES), handle(), head("kama_head"), extra("binding"));
        add("hammer", 2.5F, 1.2F, 0, .8F, Action.HAMMER, List.of(BlockTags.MINEABLE_WITH_PICKAXE),
                toughHandle(), head("hammer_head"), new Part("Plate", "large_plate", "Head"), new Part("Plate2", "large_plate", "Head"));
        add("excavator", 1.75F, 1.25F, 0, .7F, Action.EXCAVATOR, List.of(BlockTags.MINEABLE_WITH_SHOVEL),
                toughHandle(), head("excavator_head"), new Part("Plate", "large_plate", "Head"), extra("tough_binding"));
        add("lumberaxe", 2, 1.2F, 2, .8F, Action.LUMBERAXE, List.of(BlockTags.MINEABLE_WITH_AXE),
                toughHandle(), head("broad_axe_head"), new Part("Plate", "large_plate", "Head"), extra("tough_binding"));
        add("scythe", 2.2F, .75F, 0, .9F, Action.SCYTHE, List.of(BlockTags.LEAVES, BlockTags.WOOL, BlockTags.CROPS, BlockTags.REPLACEABLE_BY_TREES),
                toughHandle(), head("scythe_head"), extra("tough_binding"), new Part("Handle2", "tough_tool_rod", "Handle"));
        add("broadsword", 1.1F, 1, 1, 1.6F, Action.BROADSWORD, List.of(), handle(), head("sword_blade"), extra("wide_guard"));
        add("longsword", 1.05F, 1.1F, .5F, 1.4F, Action.LONGSWORD, List.of(), handle(), head("sword_blade"), extra("hand_guard"));
        add("rapier", .8F, .55F, 0, 3, Action.RAPIER, List.of(), handle(), head("sword_blade"), extra("cross_guard"));
        add("frypan", 1, 1, 0, 1.4F, Action.FRYPAN, List.of(), handle(), head("pan_head"));
        add("battlesign", 1, .86F, 0, 1.2F, Action.BATTLESIGN, List.of(), handle(), head("sign_head"));
        add("cleaver", 2, 1.2F, 3, .7F, Action.CLEAVER, List.of(), toughHandle(), head("large_sword_blade"),
                new Part("Plate", "large_plate", "Head"), new Part("Guard", "tough_tool_rod", "Extra"));
        add("shortbow", 1, .7F, 0, 1.5F, Action.SHORTBOW, List.of(), limb("Limb"), limb("Limb2"), string());
        add("longbow", 1.4F, .7F, 0, 1.3F, Action.LONGBOW, List.of(), limb("Limb"), limb("Limb2"), new Part("Plate", "large_plate", "Extra"), string());
        add("crossbow", 1, .8F, 0, 2, Action.CROSSBOW, List.of(), toughHandle(), limb("Limb"), extra("tough_binding"), string());
        add("arrow", 1, 1, 0, 1, Action.ARROW, List.of(), new Part("Shaft", "arrow_shaft", "Shaft"), head("arrow_head"), new Part("Fletching", "fletching", "Fletching"));
        add("bolt", 1, 1, 0, 1, Action.BOLT, List.of(), new Part("Shaft", "bolt_core", "Shaft"), new Part("Head", "bolt_core", "Head"), new Part("Fletching", "fletching", "Fletching"));
        add("shuriken", 1, .7F, 0, 1, Action.SHURIKEN, List.of(), head("knife_blade"),
                new Part("Blade2", "knife_blade", "Head"), new Part("Blade3", "knife_blade", "Head"), new Part("Blade4", "knife_blade", "Head"));
    }

    private AntiqueTools() {}
    private static Part handle() { return new Part("Handle", "tool_rod", "Handle"); }
    private static Part toughHandle() { return new Part("Handle", "tough_tool_rod", "Handle"); }
    private static Part head(String part) { return new Part("Head", part, "Head"); }
    private static Part extra(String part) { return new Part("Binding", part, "Extra"); }
    private static Part limb(String key) { return new Part(key, "bow_limb", "Bow"); }
    private static Part string() { return new Part("String", "bow_string", "BowString"); }

    private static ToolDefinition definition(String id, float durability, float damage, float bonus, float speed,
                                             Action action, List<TagKey<Block>> tags, Part... parts) {
        return new ToolDefinition(id, List.of(parts), tags, durability, damage, bonus, speed, action);
    }

    private static void add(String id, float durability, float damage, float bonus, float speed,
                            Action action, List<TagKey<Block>> tags, Part... parts) {
        register(definition(id, durability, damage, bonus, speed, action, tags, parts));
    }

    public static Entry register(ToolDefinition definition) {
        if (TOOLS.containsKey(definition.id())) {
            throw new IllegalArgumentException("Duplicate tool: " + definition.id());
        }
        Entry entry = new Entry(definition, TinkerTools.ITEMS.register("tools/" + definition.id(),
                () -> new AntiqueTool(definition)));
        TOOLS.put(definition.id(), entry);
        return entry;
    }

    public static Map<String, Entry> entries() { return Collections.unmodifiableMap(TOOLS); }
    public static Entry get(String id) { return TOOLS.get(id); }
    public static void addCreativeItems(java.util.function.Consumer<net.minecraft.world.item.ItemStack> output) {
        for (Entry entry : TOOLS.values()) { for (var material : com.gctn.tconstruct.library.TinkerMaterials.materials) {
            if (material.isHidden()) { continue; }
            var materials = new java.util.ArrayList<com.gctn.tconstruct.library.materials.Material>();
            for (var part : entry.definition.parts()) {
                materials.add(switch (part.stat()) {
                    case "BowString" -> com.gctn.tconstruct.library.TinkerMaterials.string;
                    case "Fletching" -> com.gctn.tconstruct.library.TinkerMaterials.feather;
                    case "Shaft" -> com.gctn.tconstruct.library.TinkerMaterials.wood;
                    default -> material;
                });
            }
            var stack = ToolBuilder.assemble(entry, materials);
            if (!stack.isEmpty()) { output.accept(stack); if (!com.gctn.tconstruct.Config.LIST_ALL_TOOL_MATERIALS.get()) { break; } }
        } }
    }
    public static void init() {}
    public record Entry(ToolDefinition definition, Supplier<? extends Item> item) {}
}
