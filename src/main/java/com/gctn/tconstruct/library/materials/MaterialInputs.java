/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.library.materials;

import com.gctn.tconstruct.library.TinkerMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** 以一份材料为单位的可扩展匹配表；标签在匹配时解析，支持数据包标签重载。 */
public final class MaterialInputs {
    private static final List<Entry> INPUTS = new ArrayList<>();
    private static boolean initialized;
    private MaterialInputs() {}

    public static void register(Material material, Predicate<ItemStack> ingredient, float value) {
        if (material == null || ingredient == null || !Float.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Invalid material ingredient");
        }
        INPUTS.add(new Entry(material, ingredient, value));
    }

    public static void item(Material material, String id, float value) {
        ResourceLocation key = ResourceLocation.parse(id);
        register(material, stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(key), value);
    }

    public static void tag(Material material, String id, float value) {
        TagKey<net.minecraft.world.item.Item> key = TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
        register(material, stack -> stack.is(key), value);
    }

    public static Match match(ItemStack stack) {
        init();
        if (stack.isEmpty()) { return null; }
        var part = com.gctn.tconstruct.library.toolparts.AntiqueParts.get(stack);
        if (part != null) {
            if (part.id().equals("bolt_core") && com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(stack).contains("Coating")) { return null; }
            var material = TinkerMaterials.getMaterial(com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(stack).getString(com.gctn.tconstruct.library.utils.Tags.PART_MATERIAL));
            return material == null ? null : new Match(material, part.cost());
        }
        for (Entry entry : INPUTS) {
            if (entry.ingredient().test(stack)) { return new Match(entry.material(), entry.value()); }
        }
        return null;
    }

    public static float value(ItemStack stack, Material material) {
        Match match = match(stack);
        return match != null && match.material() == material ? match.value() : 0;
    }

    public static void init() {
        if (initialized) { return; }
        initialized = true;
        tag(TinkerMaterials.wood, "minecraft:planks", 1);
        tag(TinkerMaterials.wood, "minecraft:logs", 4);
        tag(TinkerMaterials.stone, "c:cobblestones", 1);
        item(TinkerMaterials.stone, "minecraft:stone", 1);
        item(TinkerMaterials.flint, "minecraft:flint", 1);
        item(TinkerMaterials.cactus, "minecraft:cactus", 1);
        item(TinkerMaterials.bone, "minecraft:bone", 1);
        item(TinkerMaterials.bloodbone, "tconstruct:bloodbone", 1);
        item(TinkerMaterials.obsidian, "minecraft:obsidian", 1);
        item(TinkerMaterials.prismarine, "minecraft:prismarine_shard", 1);
        item(TinkerMaterials.endstone, "minecraft:end_stone", 1);
        item(TinkerMaterials.netherrack, "minecraft:netherrack", 1);
        item(TinkerMaterials.paper, "minecraft:paper", 1);
        item(TinkerMaterials.sponge, "minecraft:sponge", 1);
        item(TinkerMaterials.firewood, "tconstruct:firewood", 1);
        item(TinkerMaterials.slime, "tconstruct:slimecrystal_green", 1);
        item(TinkerMaterials.blueslime, "tconstruct:slimecrystal_blue", 1);
        item(TinkerMaterials.magmaslime, "tconstruct:slimecrystal_magma", 1);
        for (String id : new String[]{"iron", "pigiron", "knightslime", "cobalt", "ardite", "manyullyn",
                "copper", "bronze", "lead", "silver", "electrum", "steel", "alubrass", "alumite"}) {
            Material material = TinkerMaterials.getMaterial(id);
            tag(material, "c:ingots/" + id, 1);
            tag(material, "c:nuggets/" + id, 1F / 9);
            tag(material, "c:storage_blocks/" + id, 9);
            item(material, (id.equals("iron") || id.equals("copper") ? "minecraft:" : "tconstruct:") + id + "_ingot", 1);
            item(material, (id.equals("iron") ? "minecraft:" : "tconstruct:") + id + "_nugget", 1F / 9);
        }
        tag(TinkerMaterials.string, "c:strings", 1);
        item(TinkerMaterials.string, "minecraft:string", 1);
        item(TinkerMaterials.vine, "minecraft:vine", 1);
        item(TinkerMaterials.blaze, "minecraft:blaze_rod", 1);
        item(TinkerMaterials.reed, "minecraft:sugar_cane", 1);
        item(TinkerMaterials.ice, "minecraft:packed_ice", 1);
        item(TinkerMaterials.endrod, "minecraft:end_rod", 1);
        item(TinkerMaterials.feather, "minecraft:feather", 1);
        for (String color : new String[]{"blue", "purple"}) {
            Material material = color.equals("blue") ? TinkerMaterials.slimevine_blue : TinkerMaterials.slimevine_purple;
            for (String stage : new String[]{"", "_mid", "_end"}) { item(material, "tconstruct:slime_vine_" + color + stage, 1); }
        }
        item(TinkerMaterials.slimeleaf_blue, "tconstruct:slime_leaves_blue", .5f);
        item(TinkerMaterials.slimeleaf_purple, "tconstruct:slime_leaves_purple", .5f);
        item(TinkerMaterials.slimeleaf_orange, "tconstruct:slime_leaves_orange", .5f);
        tag(TinkerMaterials.leaf, "minecraft:leaves", .5f);
    }

    private record Entry(Material material, Predicate<ItemStack> ingredient, float value) {}
    public record Match(Material material, float value) {}
}
