package com.gctn.tconstruct.library.materials; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 沿用 Antique 的材料体积单位，用于部件成本和冶炼炉换算。
 * 保持精确的整数比例，避免后续移植部件加工时因取整丢失金属粒或碎片的材料量。
 */
public class MaterialValue { // 定义 MaterialValue 的实现类型，具体职责见上方说明。

    // 各种类型的材料值，以mB计，物品注册会转换
    // Antique 规定每锭为 144 mB，与熔炼和浇铸共用该单位，
    // 使部件成本保持精确：一锭可无损换算为九粒或四个碎片。
    public static final int VALUE_Ingot = 144; // 设置 VALUE_Ingot 为 144。
    public static final int VALUE_Nugget = VALUE_Ingot / 9; // 设置 VALUE_Nugget 为 VALUE_Ingot / 9。
    public static final int VALUE_Fragment = VALUE_Ingot / 4; // 设置 VALUE_Fragment 为 VALUE_Ingot / 4。
    public static final int VALUE_Shard = VALUE_Ingot / 2; // 设置 VALUE_Shard 为 VALUE_Ingot / 2。

    public static final int VALUE_Gem = 666; // 沿用 Antique 的单位，可被 3 整除。
    public static final int VALUE_Block = VALUE_Ingot * 9; // 设置 VALUE_Block 为 VALUE_Ingot * 9。

    public static final int VALUE_SearedBlock = VALUE_Ingot * 2; // 设置 VALUE_SearedBlock 为 VALUE_Ingot * 2。
    public static final int VALUE_SearedMaterial = VALUE_Ingot / 2; // 设置 VALUE_SearedMaterial 为 VALUE_Ingot / 2。
    public static final int VALUE_Glass = 1000; // 设置 VALUE_Glass 为 1000。

    public static final int VALUE_BrickBlock = VALUE_Ingot * 4; // 设置 VALUE_BrickBlock 为 VALUE_Ingot * 4。

    public static final int VALUE_SlimeBall = 250; // 设置 VALUE_SlimeBall 为 250。

} // 结束当前作用域。
