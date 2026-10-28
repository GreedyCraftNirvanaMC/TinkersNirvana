package com.gctn.tconstruct.library.utils;

/**
 * 保留 Antique 移植过程中使用的旧版及当前 NBT 字段名。
 * 本类字段中目前只有 PART_MATERIAL 被直接使用；保留字段不代表对应功能已经实现。
 */
public final class Tags {

    /** 工具的基础构建数据，记录工具由哪些内容构成。 */
    public static final String BASE_DATA = "TinkerData";
    /** 保存组装工具的各个部件材料。 */
    public static final String BASE_MATERIALS = "Materials";
    /** 保存已经应用的所有强化。 */
    public static final String BASE_MODIFIERS = "Modifiers";
    /** 记录已使用的免费强化槽数量。 */
    public static final String BASE_USED_MODIFIERS = "UsedModifiers";
    /**
     * 保存此物品栈专用的额外构建数据，例如某把特殊镐额外增加的
     * 100 点耐久，就可以记录在这里。
     */
    public static final String TINKER_EXTRA = "Special";
    public static final String EXTRA_CATEGORIES = "Categories";

    //public static final String TOOL_RENDER = "Render";
    /** 保存工具计算完成后实际使用的运行属性。 */
    public static final String TOOL_DATA = "Stats";
    public static final String TOOL_DATA_ORIG = "StatsOriginal";

    public static final String TOOL_MODIFIERS = "Modifiers";
    public static final String TOOL_TRAITS = "Traits";

    /** 保存工具部件的材料信息。 */
    public static final String PART_MATERIAL = "Material";

    // 工具属性。
    public static final String DURABILITY = "Durability";
    public static final String ATTACK = "Attack";
    public static final String ATTACKSPEEDMULTIPLIER = "AttackSpeedMultiplier";
    public static final String MININGSPEED = "MiningSpeed";
    public static final String HARVESTLEVEL = "HarvestLevel";

    // 弓类属性。
    public static final String DRAWSPEED = "DrawSpeed";
    public static final String RANGE = "Range";
    public static final String PROJECTILE_BONUS_DAMAGE = "ProjectileBonusDamage";

    // 弹射物属性。
    public static final String ACCURACY = "Accuracy";

    // 其他工具信息。
    public static final String FREE_MODIFIERS = "FreeModifiers";
    public static final String BROKEN = "Broken";

    // 额外数据。
    public static final String REPAIR_COUNT = "RepairCount";

    public static final String ENCHANT_EFFECT = "EnchantEffect";
    public static final String RESET_FLAG = "ResetFlag";

    public static final String NO_RENAME = "NoRename";

    private Tags() {
    }
}
