/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.library.toolparts;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.utils.Tags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** 部件目录共用于创造栏、加工、浇筑、工具定义及第三方扩展。 */
public final class AntiqueParts {
    private static final Map<String, Definition> PARTS = new LinkedHashMap<>();

    static {
        existing("tool_rod", 1, "Handle", ToolRod.TOOL_ROD);
        existing("pickaxe_head", 2, "Head", PickaxeHead.PICKAXEHEAD);
        existing("binding", 1, "Extra", Binding.BINDING);
        for (String name : new String[]{"shovel_head", "axe_head", "sword_blade", "kama_head", "arrow_head"}) {
            register(name, 2, "Head");
        }
        for (String name : new String[]{"broad_axe_head", "large_sword_blade", "hammer_head", "excavator_head", "scythe_head", "large_plate"}) {
            register(name, 8, "Head");
        }
        register("pan_head", 3, "Head");
        register("sign_head", 3, "Head");
        register("knife_blade", 1, "Head");
        register("tough_tool_rod", 3, "Handle");
        register("tough_binding", 3, "Extra");
        for (String name : new String[]{"wide_guard", "hand_guard", "cross_guard"}) {
            register(name, 1, "Extra");
        }
        register("bow_limb", 3, "Bow");
        register("bow_string", 1, "BowString");
        register("arrow_shaft", 2, "Shaft");
        register("fletching", 2, "Fletching");
        register("bolt_core", 2, "Shaft");
        register("sharpening_kit", 2, "Head");
        register("shard", 0.5F, "Head");
    }

    private AntiqueParts() {}

    public static Definition register(String id, float cost, String stat) {
        if (cost <= 0 || !Float.isFinite(cost) || PARTS.containsKey(id)) {
            throw new IllegalArgumentException("Invalid or duplicate part: " + id);
        }
        return existing(id, cost, stat, ToolPart.ITEMS.register("parts/" + id,
                () -> new ToolPart(Math.round(cost * 144)) {}));
    }

    private static Definition existing(String id, float cost, String stat, Supplier<? extends Item> item) {
        Definition definition = new Definition(id, cost, stat, item);
        PARTS.put(id, definition);
        return definition;
    }

    public static Map<String, Definition> entries() { return Collections.unmodifiableMap(PARTS); }
    public static Definition get(String id) { return PARTS.get(id); }
    public static void init() {}

    public static Definition get(ItemStack stack) {
        return PARTS.values().stream().filter(part -> stack.is(part.item().get())).findFirst().orElse(null);
    }

    public static void addCreativeItems(Collection<ItemStack> output) {
        for (Definition part : PARTS.values()) {
            for (Material material : TinkerMaterials.materials) {
                if (part.id().equals("bolt_core")) {
                    if (!material.hasStats("Shaft")) { continue; }
                    for (Material coating : TinkerMaterials.materials) {
                        if (!coating.isCastable() || !coating.hasStats("Head")) { continue; }
                        ItemStack core = part.create(material);
                        CompoundTag data = TinkerDataComponents.getMaterialTag(core);
                        data.putString("Coating", coating.identifier);
                        core.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
                        output.add(core);
                        if (!com.gctn.tconstruct.Config.LIST_ALL_PART_MATERIALS.get()) { break; }
                    }
                    if (!com.gctn.tconstruct.Config.LIST_ALL_PART_MATERIALS.get()) { break; }
                    continue;
                }
                if (!material.isHidden() && material.hasStats(part.stat())) {
                    ItemStack stack = part.create(material);
                    if (!stack.isEmpty()) { output.add(stack); }
                    if (!com.gctn.tconstruct.Config.LIST_ALL_PART_MATERIALS.get()) { break; }
                }
            }
        }
    }

    public record Definition(String id, float cost, String stat, Supplier<? extends Item> item) {
        public ItemStack create(Material material) {
            if (!material.hasStats(stat) || (stat.equals("Bow") && !material.hasStats("Head"))) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = new ItemStack(item.get());
            CompoundTag data = new CompoundTag();
            data.putString(Tags.PART_MATERIAL, material.identifier);
            stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
            stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_part",
                    material.getDisplayName(),
                    Component.translatable("item.tconstruct." + id + ".name")));
            return stack;
        }
    }
}
