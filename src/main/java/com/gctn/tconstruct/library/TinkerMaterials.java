package com.gctn.tconstruct.library; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.stats.ExtraMaterialStats; // 引入 com.gctn.tconstruct.library.stats.ExtraMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.stats.HandleMaterialStats; // 引入 com.gctn.tconstruct.library.stats.HandleMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.stats.HeadMaterialStats; // 引入 com.gctn.tconstruct.library.stats.HeadMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.utils.HarvestLevels; // 引入 com.gctn.tconstruct.library.utils.HarvestLevels，供本文件使用。
import com.gctn.tconstruct.tools.traits.AbstractTraits; // 引入 com.gctn.tconstruct.tools.traits.AbstractTraits，供本文件使用。
import com.gctn.tconstruct.tools.traits.TinkerTraits; // 引入 com.gctn.tconstruct.tools.traits.TinkerTraits，供本文件使用。
import com.google.common.collect.Lists; // 引入 com.google.common.collect.Lists，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。
import java.util.Map; // 引入 java.util.Map，供本文件使用。
import java.util.LinkedHashMap; // 引入 java.util.LinkedHashMap，供本文件使用。
import java.util.Collections; // 引入 java.util.Collections，供本文件使用。

/**
 * 定义移植中的材料目录，并建立存档组件使用的材料标识符索引。
 * 大部分条目为 Antique 材料预留；只有提供所需部位属性的材料才能生成有效部件。
 * 木材与石材属性遵循 Antique 的 tools/TinkerMaterials.java，以及 ToolNBT 的计算顺序。
 */
public class TinkerMaterials { // 定义 TinkerMaterials 的实现类型，具体职责见上方说明。
    private static final Map<String, Material> MATERIALS_BY_ID = new LinkedHashMap<>(); // 向当前声明或调用传入 private static final Map<String, Material> MATERIALS_BY_ID = new LinkedHashMap<>()。
    private static final List<Material> REGISTERED_MATERIALS = Lists.newArrayList(); // 设置 REGISTERED_MATERIALS 为 Lists.newArrayList()。
    public static final List<Material> materials = Collections.unmodifiableList(REGISTERED_MATERIALS); // 设置 materials 为 Collections.unmodifiableList(REGISTERED_MATERIALS)。
    private static final List<Material> CRAFTABLE_MATERIALS = Lists.newArrayList(); // 设置 CRAFTABLE_MATERIALS 为 Lists.newArrayList()。
    private static final List<Material> CASTABLE_MATERIALS = Lists.newArrayList(); // 设置 CASTABLE_MATERIALS 为 Lists.newArrayList()。
    public static final List<Material> craftableMaterials = Collections.unmodifiableList(CRAFTABLE_MATERIALS); // 设置 craftableMaterials 为 Collections.unmodifiableList(CRAFTABLE_MATERIALS)。
    public static final List<Material> castableMaterials = Collections.unmodifiableList(CASTABLE_MATERIALS); // 设置 castableMaterials 为 Collections.unmodifiableList(CASTABLE_MATERIALS)。

    // 天然资源与方块材料。
    public static final Material wood       = mat("wood", 0xff8e661b); // 设置 wood 为 mat("wood", 0xff8e661b)。
    public static final Material stone      = mat("stone", 0xff999999); // 设置 stone 为 mat("stone", 0xff999999)。
    public static final Material flint      = mat("flint", 0xff696969); // 设置 flint 为 mat("flint", 0xff696969)。
    public static final Material cactus     = mat("cactus", 0xff00a10f); // 设置 cactus 为 mat("cactus", 0xff00a10f)。
    public static final Material bone       = mat("bone", 0xffede6bf); // 设置 bone 为 mat("bone", 0xffede6bf)。
    public static final Material obsidian   = mat("obsidian", 0xff601cc4); // 设置 obsidian 为 mat("obsidian", 0xff601cc4)。
    public static final Material prismarine = mat("prismarine", 0xff7edebc); // 设置 prismarine 为 mat("prismarine", 0xff7edebc)。
    public static final Material endstone   = mat("endstone", 0xffe0d890); // 设置 endstone 为 mat("endstone", 0xffe0d890)。
    public static final Material paper      = mat("paper", 0xffffffff); // 设置 paper 为 mat("paper", 0xffffffff)。
    public static final Material sponge     = mat("sponge", 0xffcacc4e); // 设置 sponge 为 mat("sponge", 0xffcacc4e)。
    public static final Material firewood   = mat("firewood", 0xffcc5300); // 设置 firewood 为 mat("firewood", 0xffcc5300)。

    // 黏液材料。
    public static final Material knightslime= mat("knightslime", 0xfff18ff0); // 设置 knightslime 为 mat("knightslime", 0xfff18ff0)。
    public static final Material slime      = mat("slime", 0xff82c873); // 设置 slime 为 mat("slime", 0xff82c873)。
    public static final Material blueslime  = mat("blueslime", 0xff74c8c7); // 设置 blueslime 为 mat("blueslime", 0xff74c8c7)。
    public static final Material magmaslime = mat("magmaslime", 0xffff960d); // 设置 magmaslime 为 mat("magmaslime", 0xffff960d)。

    // 金属材料。
    public static final Material iron       = mat("iron", 0xffcacaca); // 设置 iron 为 mat("iron", 0xffcacaca)。
    public static final Material pigiron    = mat("pigiron", 0xffef9e9b); // 设置 pigiron 为 mat("pigiron", 0xffef9e9b)。

    // 下界材料。
    public static final Material netherrack = mat("netherrack", 0xffb84f4f); // 设置 netherrack 为 mat("netherrack", 0xffb84f4f)。
    public static final Material ardite     = mat("ardite", 0xffd14210); // 设置 ardite 为 mat("ardite", 0xffd14210)。
    public static final Material cobalt     = mat("cobalt", 0xff2882d4); // 设置 cobalt 为 mat("cobalt", 0xff2882d4)。
    public static final Material manyullyn  = mat("manyullyn", 0xffa15cf8); // 设置 manyullyn 为 mat("manyullyn", 0xffa15cf8)。

    // 特殊骨质材料。
    public static final Material bloodbone  = mat("bloodbone", 0xffc70000); // 设置 bloodbone 为 mat("bloodbone", 0xffc70000)。

    // 常见金属及合金。
    public static final Material copper     = mat("copper", 0xffed9f07); // 设置 copper 为 mat("copper", 0xffed9f07)。
    public static final Material bronze     = mat("bronze", 0xffe3bd68); // 设置 bronze 为 mat("bronze", 0xffe3bd68)。
    public static final Material lead       = mat("lead", 0xff4d4968); // 设置 lead 为 mat("lead", 0xff4d4968)。
    public static final Material silver     = mat("silver", 0xffd1ecf6); // 设置 silver 为 mat("silver", 0xffd1ecf6)。
    public static final Material electrum   = mat("electrum", 0xffe8db49); // 设置 electrum 为 mat("electrum", 0xffe8db49)。
    public static final Material steel      = mat("steel", 0xffa7a7a7); // 设置 steel 为 mat("steel", 0xffa7a7a7)。
    public static final Material alubrass   = mat("alubrass", 0xfff0d467); // 设置 alubrass 为 mat("alubrass", 0xfff0d467)。
    public static final Material alumite    = mat("alumite", 0xffffa7e9); // 设置 alumite 为 mat("alumite", 0xffffa7e9)。

    // 特殊材料。
    public static final Material xu; // 声明 xu，保存后续流程使用的状态或依赖。

    // 未知材料。
    public static final Material unknown; // 声明 unknown，保存后续流程使用的状态或依赖。

    // 弓弦材料。
    public static final Material string    = mat("string", 0xffeeeeee); // 设置 string 为 mat("string", 0xffeeeeee)。
    public static final Material vine      = mat("vine", 0xff40a10f); // 设置 vine 为 mat("vine", 0xff40a10f)。
    public static final Material slimevine_blue   = mat("slimevine_blue", 0xff74c8c7); // 设置 slimevine_blue 为 mat("slimevine_blue", 0xff74c8c7)。
    //public static final Material slimevine_orange = mat("slimevine_orange", 0xffff960d);
    public static final Material slimevine_purple = mat("slimevine_purple", 0xffc873c8); // 设置 slimevine_purple 为 mat("slimevine_purple", 0xffc873c8)。

    // 额外箭杆材料。
    public static final Material blaze     = mat("blaze", 0xffffc100); // 设置 blaze 为 mat("blaze", 0xffffc100)。
    public static final Material reed      = mat("reed", 0xffaadb74); // 设置 reed 为 mat("reed", 0xffaadb74)。
    public static final Material ice       = mat("ice", 0xff97d7e0); // 设置 ice 为 mat("ice", 0xff97d7e0)。
    public static final Material endrod    = mat("endrod", 0xffe8ffd6); // 设置 endrod 为 mat("endrod", 0xffe8ffd6)。

    // 箭羽材料。
    public static final Material feather   = mat("feather", 0xffeeeeee); // 设置 feather 为 mat("feather", 0xffeeeeee)。
    public static final Material leaf      = mat("leaf", 0xff1d730c); // 设置 leaf 为 mat("leaf", 0xff1d730c)。
    public static final Material slimeleaf_blue   = mat("slimeleaf_blue", 0xff74c8c7); // 设置 slimeleaf_blue 为 mat("slimeleaf_blue", 0xff74c8c7)。
    public static final Material slimeleaf_orange = mat("slimeleaf_orange", 0xffff960d); // 设置 slimeleaf_orange 为 mat("slimeleaf_orange", 0xffff960d)。
    public static final Material slimeleaf_purple = mat("slimeleaf_purple", 0xffc873c8); // 设置 slimeleaf_purple 为 mat("slimeleaf_purple", 0xffc873c8)。

    public static Material mat(String name, int color) { // 定义 mat 的调用入口，参数和具体处理见下方实现。
        // 材料默认可见，兼容集成逻辑可按需隐藏。
        Material mat = new Material(name, color, false); // 设置 mat 为 new Material(name, color, false)。
        if (MATERIALS_BY_ID.putIfAbsent(name, mat) != null) { // 检查 MATERIALS_BY_ID.putIfAbsent(name, mat) != null；按条件决定是否执行本分支。
            throw new IllegalArgumentException("Duplicate material identifier: " + name); // 抛出异常并终止当前无效操作，避免继续使用不合法状态。
        } // 结束当前作用域。
        REGISTERED_MATERIALS.add(mat); // 向集合或构建器追加条目。
        return mat; // 返回 mat，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static void setCraftableMaterials(Material... matList) { // 定义 setCraftableMaterials 的调用入口，参数和具体处理见下方实现。
        for (Material mat : matList) { // 遍历 Material mat : matList，逐项执行当前处理。
            mat.setCraftable(true); // 调用 setCraftable，执行当前流程所需的处理。
            if (!CRAFTABLE_MATERIALS.contains(mat)) CRAFTABLE_MATERIALS.add(mat); // 检查 !CRAFTABLE_MATERIALS.contains(mat)；按条件决定是否执行本分支。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public static void setCastableMaterials(Material... matList) { // 定义 setCastableMaterials 的调用入口，参数和具体处理见下方实现。
        for (Material mat : matList) { // 遍历 Material mat : matList，逐项执行当前处理。
            mat.setCastable(true); // 调用 setCastable，执行当前流程所需的处理。
            if (!CASTABLE_MATERIALS.contains(mat)) CASTABLE_MATERIALS.add(mat); // 检查 !CASTABLE_MATERIALS.contains(mat)；按条件决定是否执行本分支。
        } // 结束当前作用域。
    } // 结束当前作用域。

    // 注册材料属性
    public static void registerToolMaterialStats() { // 定义 registerToolMaterialStats 的调用入口，参数和具体处理见下方实现。
        wood.addMaterialStats(new HeadMaterialStats(35, HarvestLevels.STONE, 2, 2), // 调用 addMaterialStats，执行当前流程所需的处理。
                new HandleMaterialStats(1, 25), // 创建 new HandleMaterialStats(1, 25),，作为当前对象或调用的组成部分。
                new ExtraMaterialStats(15)); // 创建 new ExtraMaterialStats(15))，作为当前对象或调用的组成部分。
        stone.addMaterialStats(new HeadMaterialStats(120, HarvestLevels.IRON, 3, 4), // 调用 addMaterialStats，执行当前流程所需的处理。
                new HandleMaterialStats(0.5F, -50), // 创建 new HandleMaterialStats(0.5F, -50),，作为当前对象或调用的组成部分。
                new ExtraMaterialStats(20)); // 创建 new ExtraMaterialStats(20))，作为当前对象或调用的组成部分。
    } // 结束当前作用域。

    // 注册材料词条
    public static void registerToolMaterialTraits() { // 定义 registerToolMaterialTraits 的调用入口，参数和具体处理见下方实现。
        wood.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, TinkerTraits.ECOLOGICAL_1)); // 调用 addMaterialTraits，执行当前流程所需的处理。
        stone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", TinkerTraits.CHEAPSKATE_1), // 调用 addMaterialTraits，执行当前流程所需的处理。
                AbstractTraits.TraitPosPair.of(null, TinkerTraits.CHEAP_1)); // 调用 of，执行当前流程所需的处理。
    } // 结束当前作用域。

    static { // 在 TinkerMaterials 中补充当前声明或表达式的 static { 部分。
        xu = new Material("unstable", 0xFFFFFFFF, true); // 设置 xu 为 new Material("unstable", 0xFFFFFFFF, true)。
        unknown = new Material("unknown", 0xFFFFFFFF, true); // 设置 unknown 为 new Material("unknown", 0xFFFFFFFF, true)。
    } // 结束当前作用域。

    // 设定合成方式
    static { // 在 TinkerMaterials 中补充当前声明或表达式的 static { 部分。
        setCraftableMaterials(wood, stone, flint, cactus, bone, obsidian, prismarine, endstone, paper, sponge, firewood, // 调用 setCraftableMaterials，执行当前流程所需的处理。
                knightslime, slime, blueslime, magmaslime); // 向当前声明或调用传入 knightslime, slime, blueslime, magmaslime)。
        setCastableMaterials(iron, pigiron); // 调用 setCastableMaterials，执行当前流程所需的处理。

        registerToolMaterialStats(); // 调用 registerToolMaterialStats，执行当前流程所需的处理。
        registerToolMaterialTraits(); // 调用 registerToolMaterialTraits，执行当前流程所需的处理。
    } // 结束当前作用域。

    public static Material getMaterial(String materialId) { // 定义 getMaterial 的调用入口，参数和具体处理见下方实现。
        return MATERIALS_BY_ID.get(materialId); // 返回 MATERIALS_BY_ID.get(materialId)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

} // 结束当前作用域。
