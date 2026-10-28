package com.gctn.tconstruct.library.materials;

/**
 * 沿用 Antique 的材料体积单位，用于部件成本和冶炼炉换算。
 * 保持精确的整数比例，避免后续移植部件加工时因取整丢失金属粒或碎片的材料量。
 */
public class MaterialValue {

    // 各种类型的材料值，以mB计，物品注册会转换
    // Antique 规定每锭为 144 mB，与熔炼和浇铸共用该单位，
    // 使部件成本保持精确：一锭可无损换算为九粒或四个碎片。
    public static final int VALUE_Ingot = 144;
    public static final int VALUE_Nugget = VALUE_Ingot / 9;
    public static final int VALUE_Fragment = VALUE_Ingot / 4;
    public static final int VALUE_Shard = VALUE_Ingot / 2;

    public static final int VALUE_Gem = 666; // 沿用 Antique 的单位，可被 3 整除。
    public static final int VALUE_Block = VALUE_Ingot * 9;

    public static final int VALUE_SearedBlock = VALUE_Ingot * 2;
    public static final int VALUE_SearedMaterial = VALUE_Ingot / 2;
    public static final int VALUE_Glass = 1000;

    public static final int VALUE_BrickBlock = VALUE_Ingot * 4;

    public static final int VALUE_SlimeBall = 250;

}
