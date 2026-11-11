/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

/** 每次组装单独创建；词条和扩展可在此调整属性，不修改共享材料定义。 */
public class ToolStats {
    public record Attribute(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier modifier, net.minecraft.world.entity.EquipmentSlotGroup slot) {}
    private final java.util.List<Attribute> attributes = new java.util.ArrayList<>();
    /** Native attributes supplied by traits or modifiers are persisted/networked with the assembled tool. */
    public void attribute(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, String id, double amount,
                          net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation, net.minecraft.world.entity.EquipmentSlotGroup slot) {
        if (attribute == null || operation == null || slot == null || !Double.isFinite(amount)) { throw new IllegalArgumentException("Invalid tool attribute"); }
        attributes.add(new Attribute(attribute, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.parse(id), amount, operation), slot));
    }
    public java.util.List<Attribute> attributes() { return java.util.List.copyOf(attributes); }
    public int durability = 1;
    public int baseDurability = 1;
    public int harvestLevel;
    public float miningSpeed = 1;
    public float attack;
    public float attackSpeed;
    public float baseAttackSpeed;
    public float drawSpeed = 1;
    public float range = 1;
    public float projectileDamage;
    public float accuracy = 1;
    public int growthDurability;
    public float growthAttack;
    public float growthSpeed;
    public ToolStats copy() {
        ToolStats result = new ToolStats();
        result.copyFrom(this);
        return result;
    }

    public void copyFrom(ToolStats other) {
        durability = other.durability;
        baseDurability = other.baseDurability;
        harvestLevel = other.harvestLevel;
        miningSpeed = other.miningSpeed;
        attack = other.attack;
        attackSpeed = other.attackSpeed;
        baseAttackSpeed = other.baseAttackSpeed;
        drawSpeed = other.drawSpeed;
        range = other.range;
        projectileDamage = other.projectileDamage;
        accuracy = other.accuracy;
        growthDurability = other.growthDurability;
        growthAttack = other.growthAttack;
        growthSpeed = other.growthSpeed;
        attributes.clear();
        attributes.addAll(other.attributes);
    }

    public void validate() {
        if (durability < 1 || harvestLevel < 0 || !Float.isFinite(miningSpeed) || miningSpeed < 0
                || !Float.isFinite(attack) || !Float.isFinite(attackSpeed) || !Float.isFinite(baseAttackSpeed)
                || !Float.isFinite(drawSpeed) || !Float.isFinite(range) || !Float.isFinite(projectileDamage)
                || !Float.isFinite(accuracy) || !Float.isFinite(growthAttack) || !Float.isFinite(growthSpeed)) {
            throw new IllegalArgumentException("Invalid calculated tool stats");
        }
    }
}
