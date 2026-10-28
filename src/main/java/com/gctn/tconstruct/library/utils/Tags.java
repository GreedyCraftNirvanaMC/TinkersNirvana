package com.gctn.tconstruct.library.utils; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 保留 Antique 移植过程中使用的旧版及当前 NBT 字段名。
 * 本类字段中目前只有 PART_MATERIAL 被直接使用；保留字段不代表对应功能已经实现。
 */
public final class Tags { // 定义 Tags 的实现类型，具体职责见上方说明。

    /** 工具的基础构建数据，记录工具由哪些内容构成。 */
    public static final String BASE_DATA = "TinkerData"; // 设置 BASE_DATA 为 "TinkerData"。
    /** 保存组装工具的各个部件材料。 */
    public static final String BASE_MATERIALS = "Materials"; // 设置 BASE_MATERIALS 为 "Materials"。
    /** 保存已经应用的所有强化。 */
    public static final String BASE_MODIFIERS = "Modifiers"; // 设置 BASE_MODIFIERS 为 "Modifiers"。
    /** 记录已使用的免费强化槽数量。 */
    public static final String BASE_USED_MODIFIERS = "UsedModifiers"; // 设置 BASE_USED_MODIFIERS 为 "UsedModifiers"。
    /**
     * 保存此物品栈专用的额外构建数据，例如某把特殊镐额外增加的
     * 100 点耐久，就可以记录在这里。
     */
    public static final String TINKER_EXTRA = "Special"; // 设置 TINKER_EXTRA 为 "Special"。
    public static final String EXTRA_CATEGORIES = "Categories"; // 设置 EXTRA_CATEGORIES 为 "Categories"。

    //public static final String TOOL_RENDER = "Render";
    /** 保存工具计算完成后实际使用的运行属性。 */
    public static final String TOOL_DATA = "Stats"; // 设置 TOOL_DATA 为 "Stats"。
    public static final String TOOL_DATA_ORIG = "StatsOriginal"; // 设置 TOOL_DATA_ORIG 为 "StatsOriginal"。

    public static final String TOOL_MODIFIERS = "Modifiers"; // 设置 TOOL_MODIFIERS 为 "Modifiers"。
    public static final String TOOL_TRAITS = "Traits"; // 设置 TOOL_TRAITS 为 "Traits"。

    /** 保存工具部件的材料信息。 */
    public static final String PART_MATERIAL = "Material"; // 设置 PART_MATERIAL 为 "Material"。

    // 工具属性。
    public static final String DURABILITY = "Durability"; // 设置 DURABILITY 为 "Durability"。
    public static final String ATTACK = "Attack"; // 设置 ATTACK 为 "Attack"。
    public static final String ATTACKSPEEDMULTIPLIER = "AttackSpeedMultiplier"; // 设置 ATTACKSPEEDMULTIPLIER 为 "AttackSpeedMultiplier"。
    public static final String MININGSPEED = "MiningSpeed"; // 设置 MININGSPEED 为 "MiningSpeed"。
    public static final String HARVESTLEVEL = "HarvestLevel"; // 设置 HARVESTLEVEL 为 "HarvestLevel"。

    // 弓类属性。
    public static final String DRAWSPEED = "DrawSpeed"; // 设置 DRAWSPEED 为 "DrawSpeed"。
    public static final String RANGE = "Range"; // 设置 RANGE 为 "Range"。
    public static final String PROJECTILE_BONUS_DAMAGE = "ProjectileBonusDamage"; // 设置 PROJECTILE_BONUS_DAMAGE 为 "ProjectileBonusDamage"。

    // 弹射物属性。
    public static final String ACCURACY = "Accuracy"; // 设置 ACCURACY 为 "Accuracy"。

    // 其他工具信息。
    public static final String FREE_MODIFIERS = "FreeModifiers"; // 设置 FREE_MODIFIERS 为 "FreeModifiers"。
    public static final String BROKEN = "Broken"; // 设置 BROKEN 为 "Broken"。

    // 额外数据。
    public static final String REPAIR_COUNT = "RepairCount"; // 设置 REPAIR_COUNT 为 "RepairCount"。

    public static final String ENCHANT_EFFECT = "EnchantEffect"; // 设置 ENCHANT_EFFECT 为 "EnchantEffect"。
    public static final String RESET_FLAG = "ResetFlag"; // 设置 RESET_FLAG 为 "ResetFlag"。

    public static final String NO_RENAME = "NoRename"; // 设置 NO_RENAME 为 "NoRename"。

    private Tags() { // 定义 Tags 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。
} // 结束当前作用域。
