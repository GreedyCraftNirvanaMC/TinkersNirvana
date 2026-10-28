package com.gctn.tconstruct.library.materials;

/**
 * Material-volume units inherited from Antique and used for part costs and smeltery conversion.
 * Keeping exact integer ratios avoids rounding away nuggets/fragments during future crafting ports.
 */
public class MaterialValue {

    // 各种类型的材料值，以mB计，物品注册会转换
    // Antique uses 144 mB per ingot. Sharing this unit with melting/casting keeps
    // part costs exact: an ingot is nine nuggets or four fragments without loss.
    public static final int VALUE_Ingot = 144;
    public static final int VALUE_Nugget = VALUE_Ingot / 9;
    public static final int VALUE_Fragment = VALUE_Ingot / 4;
    public static final int VALUE_Shard = VALUE_Ingot / 2;

    public static final int VALUE_Gem = 666; // Antique's unit, divisible by three.
    public static final int VALUE_Block = VALUE_Ingot * 9;

    public static final int VALUE_SearedBlock = VALUE_Ingot * 2;
    public static final int VALUE_SearedMaterial = VALUE_Ingot / 2;
    public static final int VALUE_Glass = 1000;

    public static final int VALUE_BrickBlock = VALUE_Ingot * 4;

    public static final int VALUE_SlimeBall = 250;

}
