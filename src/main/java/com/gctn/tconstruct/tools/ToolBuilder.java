/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;

import java.util.ArrayList;
import java.util.List;

public final class ToolBuilder {
    private ToolBuilder() {}

    public static ItemStack assemble(AntiqueTools.Entry entry, List<Material> materials) {
        return assemble(entry.item().get(), entry.definition(), materials);
    }

    public static ItemStack assemble(Item item, ToolDefinition definition, List<Material> materials) {
        if (materials.size() != definition.parts().size()) {
            return ItemStack.EMPTY;
        }
        ItemStack tool = new ItemStack(item);
        CompoundTag data = new CompoundTag();
        CompoundTag traits = new CompoundTag();
        for (int i = 0; i < materials.size(); i++) {
            Material material = materials.get(i);
            ToolDefinition.Part part = definition.parts().get(i);
            if (material == null || !material.hasStats(part.stat())
                    || (part.stat().equals("Bow") && !material.hasStats("Head"))
                    || (part.stat().equals("Handle") && definition.action() == ToolDefinition.Action.CROSSBOW && !material.hasStats("Extra"))) {
                return ItemStack.EMPTY;
            }
            data.putString(part.key(), material.identifier);
            ToolTraits.addTraits(traits, material.getTraitsForStats(part.traitStats(definition.action())));
        }
        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
        tool.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(traits));
        rebuild(tool, definition);
        Material display = materials.get(Math.min(1, materials.size() - 1));
        tool.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_part",
                display.getDisplayName(),
                Component.translatable("item.tconstruct." + definition.id() + ".name")));
        tool.setDamageValue(0);
        return tool;
    }

    /** 强化和材料变更共用的确定性重算入口；损耗和未知组件保留。 */
    public static void rebuild(ItemStack tool, ToolDefinition definition) {
        ToolStats stats = calculate(tool, definition);
        ToolTraits.forEach(tool, (trait, level) -> trait.modifyStats(tool, level, stats));
        stats.durability = ToolTraits.modifyDurability(tool, stats.durability);
        stats.baseDurability = stats.durability;
        ToolModifiers.modifyStats(tool, stats);
        stats.durability += stats.growthDurability;
        stats.attack += stats.growthAttack;
        stats.miningSpeed += stats.growthSpeed;
        stats.attack *= definition.damageMultiplier();
        tool.set(DataComponents.MAX_DAMAGE, Math.max(1, stats.durability));
        tool.set(DataComponents.DAMAGE, Math.min(tool.getDamageValue(), tool.getMaxDamage()));
        List<Tool.Rule> rules = new ArrayList<>();
        rules.add(Tool.Rule.deniesDrops(HarvestLevels.getIncorrectBlocksForDrops(stats.harvestLevel)));
        for (var tag : definition.effectiveBlocks()) {
            rules.add(Tool.Rule.minesAndDrops(tag, stats.miningSpeed));
        }
        tool.set(DataComponents.TOOL, new Tool(rules, 1, 1));
        var attributes = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                        Math.max(0, stats.attack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                        stats.attackSpeed - 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        for (var entry : stats.attributes()) { attributes.add(entry.attribute(), entry.modifier(), entry.slot()); }
        tool.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes.build());
        CompoundTag data = TinkerDataComponents.getMaterialTag(tool);
        CompoundTag saved = new CompoundTag();
        saved.putFloat("MiningSpeed", stats.miningSpeed);
        saved.putInt("HarvestLevel", stats.harvestLevel);
        saved.putFloat("Attack", stats.attack);
        saved.putFloat("DrawSpeed", stats.drawSpeed);
        saved.putFloat("Range", stats.range);
        saved.putFloat("ProjectileDamage", stats.projectileDamage);
        saved.putFloat("Accuracy", stats.accuracy);
        data.put("Stats", saved);
        data.putString("DefinitionSession", com.gctn.tconstruct.tools.traits.TinkerTraits.definitionSession());
        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
        if (!definition.parts().isEmpty()) {
            String displayId = data.getString(definition.parts().get(Math.min(1, definition.parts().size() - 1)).key());
            tool.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_part",
                    Material.nameOf(displayId), Component.translatable("item.tconstruct." + definition.id() + ".name")));
        }
    }

    public static boolean ensureCurrent(ItemStack tool, ToolDefinition definition) {
        String current = com.gctn.tconstruct.tools.traits.TinkerTraits.definitionSession();
        if (!current.isEmpty() && !current.equals(TinkerDataComponents.getMaterialTag(tool).getString("DefinitionSession"))) {
            rebuild(tool, definition);
            return true;
        }
        return false;
    }

    public static ToolStats calculate(ItemStack tool, ToolDefinition definition) {
        CompoundTag data = TinkerDataComponents.getMaterialTag(tool);
        List<HeadMaterialStats> heads = new ArrayList<>();
        List<HandleMaterialStats> handles = new ArrayList<>();
        List<ExtraMaterialStats> extras = new ArrayList<>();
        List<BowMaterialStats> limbs = new ArrayList<>();
        ArrowShaftMaterialStats shaft = null;
        FletchingMaterialStats fletching = null;
        float stringModifier = 1;
        for (ToolDefinition.Part part : definition.parts()) {
            Material material = TinkerMaterials.getMaterial(data.getString(part.key()));
            if (material == null) { continue; }
            switch (part.stat()) {
                case "Head", "Bow" -> {
                    HeadMaterialStats head = material.getStats("Head", HeadMaterialStats.class);
                    if (head != null) {
                        heads.add(head);
                        if (definition.action() == ToolDefinition.Action.HAMMER && part.key().equals("Head")) { heads.add(head); }
                    }
                    if (part.stat().equals("Bow")) {
                        BowMaterialStats bow = material.getStats("Bow", BowMaterialStats.class);
                        if (bow != null) { limbs.add(bow); }
                    }
                }
                case "Handle" -> {
                    handles.add(material.getStats("Handle", HandleMaterialStats.class));
                    if (definition.action() == ToolDefinition.Action.CROSSBOW) { extras.add(material.getStats("Extra", ExtraMaterialStats.class)); }
                }
                case "Extra" -> extras.add(material.getStats("Extra", ExtraMaterialStats.class));
                case "Shaft" -> shaft = material.getStats("Shaft", ArrowShaftMaterialStats.class);
                case "Fletching" -> fletching = material.getStats("Fletching", FletchingMaterialStats.class);
                case "BowString" -> stringModifier = material.getStats("BowString", BowStringMaterialStats.class).modifier;
                default -> { }
            }
        }
        ToolStats stats = new ToolStats();
        float durability = (float) heads.stream().mapToInt(head -> head.durability).average().orElse(1);
        durability += extras.stream().mapToInt(extra -> extra.durability).average().orElse(0);
        if (!handles.isEmpty()) {
            durability = Math.round(durability * handles.stream().mapToDouble(handle -> handle.modifer).average().orElse(1));
            durability += Math.round(handles.stream().mapToInt(handle -> handle.durability).average().orElse(0));
        }
        if (shaft != null) { durability = durability * shaft.modifier + shaft.bonusAmmo * 10; }
        if (fletching != null) { durability *= fletching.modifier; stats.accuracy = fletching.accuracy; }
        stats.durability = Math.max(1, (int) (durability * definition.durabilityMultiplier() * stringModifier));
        stats.harvestLevel = heads.stream().mapToInt(head -> head.harvestLevel).max().orElse(0);
        if (definition.action() == ToolDefinition.Action.HAMMER && !heads.isEmpty()) { stats.harvestLevel = heads.getFirst().harvestLevel; }
        stats.miningSpeed = (float) heads.stream().mapToDouble(head -> head.miningspeed).average().orElse(1);
        stats.attack = (float) heads.stream().mapToDouble(head -> head.attack).average().orElse(0) + definition.bonusDamage();
        if (definition.action() == ToolDefinition.Action.SHURIKEN) {
            stats.attack += 1;
            stats.durability += (int) definition.parts().stream().map(part -> TinkerMaterials.getMaterial(data.getString(part.key())))
                    .filter(java.util.Objects::nonNull).mapToInt(material -> material.getStats("Extra", ExtraMaterialStats.class).durability).average().orElse(0);
        }
        stats.attackSpeed = definition.attackSpeed();
        stats.baseAttackSpeed = definition.attackSpeed();
        if (!limbs.isEmpty()) {
            stats.drawSpeed = (float) limbs.stream().mapToDouble(limb -> limb.drawSpeed).average().orElse(1);
            stats.range = (float) limbs.stream().mapToDouble(limb -> limb.range).average().orElse(1);
            stats.projectileDamage = (float) limbs.stream().mapToDouble(limb -> limb.bonusDamage).average().orElse(0);
            if (definition.action() == ToolDefinition.Action.CROSSBOW) { stats.projectileDamage *= 1.5F; }
        }
        return stats;
    }

    public static CompoundTag savedStats(ItemStack tool) { return TinkerDataComponents.getMaterialTag(tool).getCompound("Stats"); }
}
