package com.gctn.tconstruct.library;

import com.gctn.tconstruct.library.materials.Material;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 将修复原料换算为单个材料单位的比例。
 * 木材通过实时物品标签兼容其他模组的木板和原木，其余内置原料通过物品标识符匹配。
 * 数值只表示一个物品的材料量，调用者需要另外处理物品栈数量。
 */
public class MaterialList {
    public static final Map<Material, Map<String, Double>> MATERIAL_MAP = new HashMap<>();

    public static void init(){
        MATERIAL_MAP.put(TinkerMaterials.wood, Map.ofEntries(
                Map.entry("minecraft:oak_planks", 1.0),
                Map.entry("minecraft:stick", 0.5)
        ));

        MATERIAL_MAP.put(TinkerMaterials.stone, Map.ofEntries(
                Map.entry("minecraft:cobblestone", 1.0),
                Map.entry("minecraft:stone", 1.0)
        ));
    }
    public static double getMaterialValue(Material material, ItemStack input) {
        if (material == TinkerMaterials.wood) {
            if (input.is(ItemTags.PLANKS)) {
                return 1.0;
            }
            if (input.is(ItemTags.LOGS)) {
                return 4.0;
            }
        }
        Map<String, Double> values = MATERIAL_MAP.get(material);
        return values == null ? 0.0 : values.getOrDefault(BuiltInRegistries.ITEM.getKey(input.getItem()).toString(), 0.0);
    }
}
