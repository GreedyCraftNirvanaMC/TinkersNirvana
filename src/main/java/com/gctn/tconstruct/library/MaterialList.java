package com.gctn.tconstruct.library; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。

import net.minecraft.core.registries.BuiltInRegistries; // 引入 net.minecraft.core.registries.BuiltInRegistries，供本文件使用。
import net.minecraft.tags.ItemTags; // 引入 net.minecraft.tags.ItemTags，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

import java.util.HashMap; // 引入 java.util.HashMap，供本文件使用。
import java.util.Map; // 引入 java.util.Map，供本文件使用。

/**
 * 将修复原料换算为单个材料单位的比例。
 * 木材通过实时物品标签兼容其他模组的木板和原木，其余内置原料通过物品标识符匹配。
 * 数值只表示一个物品的材料量，调用者需要另外处理物品栈数量。
 */
public class MaterialList { // 定义 MaterialList 的实现类型，具体职责见上方说明。
    public static final Map<Material, Map<String, Double>> MATERIAL_MAP = new HashMap<>(); // 向当前声明或调用传入 public static final Map<Material, Map<String, Double>> MATERIAL_MAP = new HashMap<>()。

    public static void init(){ // 定义 init 的调用入口，参数和具体处理见下方实现。
        MATERIAL_MAP.put(TinkerMaterials.wood, Map.ofEntries( // 按键保存对应数据。
                Map.entry("minecraft:oak_planks", 1.0), // 调用 entry，执行当前流程所需的处理。
                Map.entry("minecraft:stick", 0.5) // 调用 entry，执行当前流程所需的处理。
        )); // 结束当前参数列表或表达式并完成本次调用。

        MATERIAL_MAP.put(TinkerMaterials.stone, Map.ofEntries( // 按键保存对应数据。
                Map.entry("minecraft:cobblestone", 1.0), // 调用 entry，执行当前流程所需的处理。
                Map.entry("minecraft:stone", 1.0) // 调用 entry，执行当前流程所需的处理。
        )); // 结束当前参数列表或表达式并完成本次调用。
    } // 结束当前作用域。
    public static double getMaterialValue(Material material, ItemStack input) { // 定义 getMaterialValue 的调用入口，参数和具体处理见下方实现。
        if (material == TinkerMaterials.wood) { // 检查 material == TinkerMaterials.wood；按条件决定是否执行本分支。
            if (input.is(ItemTags.PLANKS)) { // 检查 input.is(ItemTags.PLANKS)；按条件决定是否执行本分支。
                return 1.0; // 返回 1.0，将当前分支的结果交给调用者。
            } // 结束当前作用域。
            if (input.is(ItemTags.LOGS)) { // 检查 input.is(ItemTags.LOGS)；按条件决定是否执行本分支。
                return 4.0; // 返回 4.0，将当前分支的结果交给调用者。
            } // 结束当前作用域。
        } // 结束当前作用域。
        Map<String, Double> values = MATERIAL_MAP.get(material); // 读取指定条目或延迟注册对象。
        return values == null ? 0.0 : values.getOrDefault(BuiltInRegistries.ITEM.getKey(input.getItem()).toString(), 0.0); // 返回 values == null ? 0.0 : values.getOrDefault(BuiltInRegistries.ITEM.getKey(input.getItem()).toString(), 0.0)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
