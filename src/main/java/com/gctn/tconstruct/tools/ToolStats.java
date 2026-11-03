/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

/** 每次组装单独创建；词条和扩展可在此调整属性，不修改共享材料定义。 */
public class ToolStats {
    public int durability = 1;
    public int baseDurability = 1;
    public int harvestLevel;
    public float miningSpeed = 1;
    public float attack;
    public float attackSpeed;
    public float drawSpeed = 1;
    public float range = 1;
    public float projectileDamage;
    public float accuracy = 1;
    public int growthDurability;
    public float growthAttack;
    public float growthSpeed;
}
