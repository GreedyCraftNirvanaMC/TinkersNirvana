package com.gctn.tconstruct.library;

import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.ExtraMaterialStats;
import com.gctn.tconstruct.library.stats.HandleMaterialStats;
import com.gctn.tconstruct.library.stats.HeadMaterialStats;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

/**
 * 定义移植中的材料目录，并建立存档组件使用的材料标识符索引。
 * 大部分条目为 Antique 材料预留；只有提供所需部位属性的材料才能生成有效部件。
 * 木材与石材属性遵循 Antique 的 tools/TinkerMaterials.java，以及 ToolNBT 的计算顺序。
 */
public class TinkerMaterials {
    private static final Map<String, Material> MATERIALS_BY_ID = new LinkedHashMap<>();
    private static final List<Material> REGISTERED_MATERIALS = Lists.newArrayList();
    public static final List<Material> materials = Collections.unmodifiableList(REGISTERED_MATERIALS);
    private static final List<Material> CRAFTABLE_MATERIALS = Lists.newArrayList();
    private static final List<Material> CASTABLE_MATERIALS = Lists.newArrayList();
    public static final List<Material> craftableMaterials = Collections.unmodifiableList(CRAFTABLE_MATERIALS);
    public static final List<Material> castableMaterials = Collections.unmodifiableList(CASTABLE_MATERIALS);

    // 天然资源与方块材料。
    public static final Material wood       = mat("wood", 0xff8e661b);
    public static final Material stone      = mat("stone", 0xff999999);
    public static final Material flint      = mat("flint", 0xff696969);
    public static final Material cactus     = mat("cactus", 0xff00a10f);
    public static final Material bone       = mat("bone", 0xffede6bf);
    public static final Material obsidian   = mat("obsidian", 0xff601cc4);
    public static final Material prismarine = mat("prismarine", 0xff7edebc);
    public static final Material endstone   = mat("endstone", 0xffe0d890);
    public static final Material paper      = mat("paper", 0xffffffff);
    public static final Material sponge     = mat("sponge", 0xffcacc4e);
    public static final Material firewood   = mat("firewood", 0xffcc5300);

    // 黏液材料。
    public static final Material knightslime= mat("knightslime", 0xfff18ff0);
    public static final Material slime      = mat("slime", 0xff82c873);
    public static final Material blueslime  = mat("blueslime", 0xff74c8c7);
    public static final Material magmaslime = mat("magmaslime", 0xffff960d);

    // 金属材料。
    public static final Material iron       = mat("iron", 0xffcacaca);
    public static final Material pigiron    = mat("pigiron", 0xffef9e9b);

    // 下界材料。
    public static final Material netherrack = mat("netherrack", 0xffb84f4f);
    public static final Material ardite     = mat("ardite", 0xffd14210);
    public static final Material cobalt     = mat("cobalt", 0xff2882d4);
    public static final Material manyullyn  = mat("manyullyn", 0xffa15cf8);

    // 特殊骨质材料。
    public static final Material bloodbone  = mat("bloodbone", 0xffc70000);

    // 常见金属及合金。
    public static final Material copper     = mat("copper", 0xffed9f07);
    public static final Material bronze     = mat("bronze", 0xffe3bd68);
    public static final Material lead       = mat("lead", 0xff4d4968);
    public static final Material silver     = mat("silver", 0xffd1ecf6);
    public static final Material electrum   = mat("electrum", 0xffe8db49);
    public static final Material steel      = mat("steel", 0xffa7a7a7);
    public static final Material alubrass   = mat("alubrass", 0xfff0d467);
    public static final Material alumite    = mat("alumite", 0xffffa7e9);

    // 特殊材料。
    public static final Material xu;

    // 未知材料。
    public static final Material unknown;

    // 弓弦材料。
    public static final Material string    = mat("string", 0xffeeeeee);
    public static final Material vine      = mat("vine", 0xff40a10f);
    public static final Material slimevine_blue   = mat("slimevine_blue", 0xff74c8c7);
    //public static final Material slimevine_orange = mat("slimevine_orange", 0xffff960d);
    public static final Material slimevine_purple = mat("slimevine_purple", 0xffc873c8);

    // 额外箭杆材料。
    public static final Material blaze     = mat("blaze", 0xffffc100);
    public static final Material reed      = mat("reed", 0xffaadb74);
    public static final Material ice       = mat("ice", 0xff97d7e0);
    public static final Material endrod    = mat("endrod", 0xffe8ffd6);

    // 箭羽材料。
    public static final Material feather   = mat("feather", 0xffeeeeee);
    public static final Material leaf      = mat("leaf", 0xff1d730c);
    public static final Material slimeleaf_blue   = mat("slimeleaf_blue", 0xff74c8c7);
    public static final Material slimeleaf_orange = mat("slimeleaf_orange", 0xffff960d);
    public static final Material slimeleaf_purple = mat("slimeleaf_purple", 0xffc873c8);

    public static Material mat(String name, int color) {
        // 材料默认可见，兼容集成逻辑可按需隐藏。
        Material mat = new Material(name, color, false);
        if (MATERIALS_BY_ID.putIfAbsent(name, mat) != null) {
            throw new IllegalArgumentException("Duplicate material identifier: " + name);
        }
        REGISTERED_MATERIALS.add(mat);
        return mat;
    }

    public static void setCraftableMaterials(Material... matList) {
        for (Material mat : matList) {
            mat.setCraftable(true);
            if (!CRAFTABLE_MATERIALS.contains(mat)) CRAFTABLE_MATERIALS.add(mat);
        }
    }

    public static void setCastableMaterials(Material... matList) {
        for (Material mat : matList) {
            mat.setCastable(true);
            if (!CASTABLE_MATERIALS.contains(mat)) CASTABLE_MATERIALS.add(mat);
        }
    }

    // 注册材料属性
    public static void registerToolMaterialStats() {
        wood.addMaterialStats(new HeadMaterialStats(35, HarvestLevels.STONE, 2, 2),
                new HandleMaterialStats(1, 25),
                new ExtraMaterialStats(15));
        stone.addMaterialStats(new HeadMaterialStats(120, HarvestLevels.IRON, 3, 4),
                new HandleMaterialStats(0.5F, -50),
                new ExtraMaterialStats(20));
    }

    // 注册材料词条
    public static void registerToolMaterialTraits() {
        wood.addMaterialTraits(AbstractTraits.TraitPosPair.of(null, TinkerTraits.ECOLOGICAL_1));
        stone.addMaterialTraits(AbstractTraits.TraitPosPair.of("Head", TinkerTraits.CHEAPSKATE_1),
                AbstractTraits.TraitPosPair.of(null, TinkerTraits.CHEAP_1));
    }

    static {
        xu = new Material("unstable", 0xFFFFFFFF, true);
        unknown = new Material("unknown", 0xFFFFFFFF, true);
    }

    // 设定合成方式
    static {
        setCraftableMaterials(wood, stone, flint, cactus, bone, obsidian, prismarine, endstone, paper, sponge, firewood,
                knightslime, slime, blueslime, magmaslime);
        setCastableMaterials(iron, pigiron);

        registerToolMaterialStats();
        registerToolMaterialTraits();
    }

    public static Material getMaterial(String materialId) {
        return MATERIALS_BY_ID.get(materialId);
    }

}
