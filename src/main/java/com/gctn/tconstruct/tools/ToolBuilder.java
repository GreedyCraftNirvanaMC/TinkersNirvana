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
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class ToolBuilder {
    private static final String SHURIKEN_TRAITS = "ShurikenBladeTraits";
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
                    || (definition.action() == ToolDefinition.Action.SHURIKEN && !material.hasStats("Extra"))
                    || (part.stat().equals("Handle") && definition.action() == ToolDefinition.Action.CROSSBOW && !material.hasStats("Extra"))) {
                return ItemStack.EMPTY;
            }
            data.putString(part.key(), material.identifier);
            ToolTraits.addTraits(traits, material.getTraitsForStats(part.traitStats(definition.action())));
        }
        if (definition.action() == ToolDefinition.Action.SHURIKEN) { data.putBoolean(SHURIKEN_TRAITS, true); }
        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
        tool.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(traits));
        rebuild(tool, definition);
        Material display = materials.get(Math.min(1, materials.size() - 1));
        tool.set(DataComponents.ITEM_NAME, Component.translatable("item.tconsnirvana.material_part",
                display.getDisplayName(),
                Component.translatable("item.tconsnirvana." + definition.id() + ".name")));
        tool.setDamageValue(0);
        return tool;
    }

    /** 强化和材料变更共用的确定性重算入口；损耗和未知组件保留。 */
    public static void rebuild(ItemStack tool, ToolDefinition definition) {
        updateShurikenTraits(tool, definition);
        ToolStats stats = calculate(tool, definition);
        float originalAttack = stats.attack;
        ToolTraits.forEach(tool, (trait, level) -> trait.modifyStats(tool, level, stats));
        stats.durability = ToolTraits.modifyDurability(tool, stats.durability);
        stats.baseDurability = stats.durability;
        ToolModifiers.modifyStats(tool, stats, originalAttack);
        stats.durability += stats.growthDurability;
        stats.attack += stats.growthAttack;
        stats.miningSpeed += stats.growthSpeed;
        stats.attack *= definition.damageMultiplier();
        // 速度倍率作用于最终挖掘速度，材料属性和强化增量使用怀古的未缩放值。
        stats.miningSpeed *= switch (definition.action()) {
            case HAMMER -> .4F;
            case EXCAVATOR -> .28F;
            case LUMBERAXE -> .35F;
            case MATTOCK -> .95F;
            default -> 1;
        };
        if (isInfiTool(tool)) {
            // 怀古的创造变体覆盖最终值。保存在材料数据中，重算后也要保留。
            stats.durability = stats.baseDurability = 999999;
            stats.attack = stats.miningSpeed = 999999;
        }
        tool.set(DataComponents.MAX_DAMAGE, Math.max(1, stats.durability));
        tool.set(DataComponents.DAMAGE, Math.min(tool.getDamageValue(), tool.getMaxDamage()));
        List<Tool.Rule> rules = new ArrayList<>();
        var incorrectBlocks = HarvestLevels.getIncorrectBlocksForDrops(stats.harvestLevel);
        if (definition.action() != ToolDefinition.Action.MATTOCK) {
            rules.add(Tool.Rule.deniesDrops(incorrectBlocks));
            // 怀古组装工具在等级不足时按徒手速度挖掘。原版镐保留自己的速度规则。
            rules.add(Tool.Rule.overrideSpeed(incorrectBlocks, 1));
        }
        if (definition.action() == ToolDefinition.Action.AXE) {
            rules.add(Tool.Rule.overrideSpeed(BlockTags.LEAVES, stats.miningSpeed));
        }
        if (definition.isSword()) {
            // 怀古的剑类按材料速度的一半清理植被，蜘蛛网再乘以 7.5。
            rules.add(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), stats.miningSpeed * 3.75F));
            rules.add(Tool.Rule.overrideSpeed(HarvestLevels.SWORD_EFFICIENT, stats.miningSpeed * .5F));
        }
        for (var tag : definition.effectiveBlocks()) {
            if (definition.action() == ToolDefinition.Action.MATTOCK) {
                int tier = tag.equals(BlockTags.MINEABLE_WITH_AXE) ? headHarvestLevel(tool, "Head")
                        : tag.equals(BlockTags.MINEABLE_WITH_SHOVEL) ? headHarvestLevel(tool, "Shovel") : stats.harvestLevel;
                rules.add(HarvestLevels.restrictToolToTier(tag, tier));
            }
            rules.add(Tool.Rule.minesAndDrops(tag, stats.miningSpeed));
        }
        tool.set(DataComponents.TOOL, new Tool(rules, 1, 1));
        if (definition.isAmmo()) {
            // 怀古只在弹射命中时使用弹药的攻击属性，握在手里时按普通物品处理。
            tool.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        } else {
            var attributes = ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                            Math.max(0, stats.attack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                            stats.attackSpeed - 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            for (var entry : stats.attributes()) { attributes.add(entry.attribute(), entry.modifier(), entry.slot()); }
            tool.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes.build());
        }
        CompoundTag data = TinkerDataComponents.getMaterialTag(tool);
        CompoundTag saved = new CompoundTag();
        saved.putInt("BaseDurability", stats.baseDurability);
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
            tool.set(DataComponents.ITEM_NAME, Component.translatable("item.tconsnirvana.material_part",
                    Material.nameOf(displayId), Component.translatable("item.tconsnirvana." + definition.id() + ".name")));
        }
    }

    public static int headHarvestLevel(ItemStack tool, String partKey) {
        var material = TinkerMaterials.getMaterial(TinkerDataComponents.getMaterialTag(tool).getString(partKey));
        var stats = material == null ? null : material.getStats("Head", HeadMaterialStats.class);
        return stats == null ? HarvestLevels.STONE : stats.harvestLevel;
    }

    public static boolean ensureCurrent(ItemStack tool, ToolDefinition definition) {
        String current = com.gctn.tconstruct.tools.traits.TinkerTraits.definitionSession();
        var data = TinkerDataComponents.getMaterialTag(tool);
        if ((definition.action() == ToolDefinition.Action.SHURIKEN && !data.getBoolean(SHURIKEN_TRAITS))
                || (!current.isEmpty() && !current.equals(data.getString("DefinitionSession")))) {
            rebuild(tool, definition);
            return true;
        }
        return false;
    }

    private static void updateShurikenTraits(ItemStack tool, ToolDefinition definition) {
        if (definition.action() != ToolDefinition.Action.SHURIKEN) { return; }
        CompoundTag data = TinkerDataComponents.getMaterialTag(tool);
        if (data.getBoolean(SHURIKEN_TRAITS)) { return; }
        // 旧存档只安装了头部词条。替换材料来源的词条，同时保留运行状态和刻印。
        var previous = new CompoundTag();
        var replacement = new CompoundTag();
        for (var part : definition.parts()) {
            var material = TinkerMaterials.getMaterial(data.getString(part.key()));
            if (material == null) { continue; }
            ToolTraits.addTraits(previous, material.getTraits("Head"));
            ToolTraits.addTraits(replacement, material.getTraitsForStats(part.traitStats(definition.action())));
        }
        ToolTraits.addTraits(replacement, ToolTraits.entriesFrom(data.getCompound("EmbossedTraits")));
        var actual = TinkerDataComponents.getTraitsTag(tool);
        ToolTraits.replaceMaterialTraits(actual, previous, replacement,
                data.contains("Embossed") && !data.contains("EmbossedTraits"));
        data.putBoolean(SHURIKEN_TRAITS, true);
        tool.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(actual));
        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
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
                    if (definition.action() == ToolDefinition.Action.SHURIKEN && material.hasStats("Extra")) {
                        extras.add(material.getStats("Extra", ExtraMaterialStats.class));
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
        // 怀古在部件阶段分别取整，尤其是先按箭羽修正耐久，再加上箭杆提供的额外弹药。
        int durability = Math.max(1, (int) heads.stream().mapToInt(head -> head.durability).average().orElse(1));
        durability += (int) Math.round(extras.stream().mapToInt(extra -> extra.durability).average().orElse(0));
        if (!handles.isEmpty()) {
            durability = (int) Math.round(durability * handles.stream().mapToDouble(handle -> handle.modifer).average().orElse(1));
            durability += (int) Math.round(handles.stream().mapToInt(handle -> handle.durability).average().orElse(0));
            durability = Math.max(1, durability);
        }
        if (fletching != null) {
            durability = Math.max(1, Math.round(durability * fletching.modifier));
            stats.accuracy = Math.clamp(fletching.accuracy, 0, 1);
        }
        if (shaft != null) {
            durability = Math.max(1, Math.round(durability * shaft.modifier) + shaft.bonusAmmo * ToolRanged.AMMO_DURABILITY);
        }
        durability = Math.max(1, Math.round(durability * stringModifier));
        stats.durability = Math.max(1, (int) (durability * definition.durabilityMultiplier()));
        stats.harvestLevel = heads.stream().mapToInt(head -> head.harvestLevel).max().orElse(0);
        if (definition.action() == ToolDefinition.Action.HAMMER && !heads.isEmpty()) { stats.harvestLevel = heads.getFirst().harvestLevel; }
        stats.miningSpeed = (float) heads.stream().mapToDouble(head -> head.miningspeed).average().orElse(1);
        stats.attack = (float) heads.stream().mapToDouble(head -> head.attack).average().orElse(0);
        if (definition.action() == ToolDefinition.Action.CLEAVER) {
            stats.attack *= 1.3F;
        } else if (definition.action() == ToolDefinition.Action.ARROW) {
            stats.attack += 2;
        } else if (definition.action() == ToolDefinition.Action.SHURIKEN) {
            stats.attack += 1;
        }
        stats.attack += definition.bonusDamage();
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

    public static boolean isInfiTool(ItemStack tool) { return TinkerDataComponents.getMaterialTag(tool).getBoolean(Tags.INFI_TOOL); }
}
