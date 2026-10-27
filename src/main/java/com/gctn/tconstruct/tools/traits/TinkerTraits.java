package com.gctn.tconstruct.tools.traits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 集中注册并查询所有匠魂词条。 */
public final class TinkerTraits {
    /** 按注册顺序保存词条，便于遍历所有词条。 */
    public static final List<AbstractTraits> traits = new ArrayList<>();
    private static final Map<String, AbstractTraits> TRAITS_BY_NAME = new LinkedHashMap<>();
    private static boolean registered;

    private TinkerTraits() {
    }

    /**
     * 在这里注册所有内置词条。添加新词条时，在此函数中增加一行 registerTrait(...)
     * 即可同时让遍历、反向查找和颜色查询支持该词条。
     */
    public static void registerTraits() {
        if (registered) {
            return;
        }
        registered = true;

        registerTrait(new EcologicalTraits(1));
    }

    /** 注册一个词条；同一 identifier 不能注册两次。 */
    public static <T extends AbstractTraits> T registerTrait(T trait) {
        if (TRAITS_BY_NAME.containsKey(trait.identifier)) {
            throw new IllegalArgumentException("Duplicate trait identifier: " + trait.identifier);
        }
        traits.add(trait);
        TRAITS_BY_NAME.put(trait.identifier, trait);
        return trait;
    }

    /** 根据词条标识符反向查找已注册的词条，找不到时返回 null。 */
    public static AbstractTraits getTrait(String traitsName) {
        registerTraits();
        return TRAITS_BY_NAME.get(traitsName);
    }

    /** 根据词条标识符获取颜色；找不到时返回白色。 */
    public static int getTraitColor(String traitsName) {
        AbstractTraits trait = getTrait(traitsName);
        return trait == null ? 0xFFFFFFFF : trait.color;
    }

    /** 返回只读的注册表视图。 */
    public static List<AbstractTraits> getAllTraits() {
        registerTraits();
        return Collections.unmodifiableList(traits);
    }
}
