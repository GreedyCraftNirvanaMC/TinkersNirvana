package com.gctn.tconstruct.tools.traits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 集中注册并查询所有匠魂词条。 */
public final class TinkerTraits {
    /** 按注册顺序保存词条，便于遍历所有词条。 */
    private static final List<AbstractTraits> traits = new ArrayList<>();
    private static final Map<String, AbstractTraits> TRAITS_BY_NAME = new LinkedHashMap<>();
    private static boolean registered;

    private static final TraitEcological ECOLOGICAL = new TraitEcological();
    private static final TraitCheap CHEAP = new TraitCheap();
    private static final TraitCheapskate CHEAPSKATE = new TraitCheapskate();

    /** 生成工具/部件时使用的初始配置；工具后续的实际等级保存在自身 Tag 中。 */
    public static final AbstractTraits.TraitEntry ECOLOGICAL_1 =
            new AbstractTraits.TraitEntry(ECOLOGICAL, 1);
    public static final AbstractTraits.TraitEntry CHEAP_1 =
            new AbstractTraits.TraitEntry(CHEAP, 1);
    public static final AbstractTraits.TraitEntry CHEAPSKATE_1 =
            new AbstractTraits.TraitEntry(CHEAPSKATE, 1);

    private TinkerTraits() {
    }

    /**
     * 在这里注册所有内置词条定义，与初始等级常量共用同一个定义对象。
     * 添加新词条时，在此函数中增加一行 registerTrait(...)，
     * 即可同时让遍历、反向查找和颜色查询支持该词条。
     */
    public static void registerTraits() {
        if (registered) {
            return;
        }
        registered = true;

        registerTrait(ECOLOGICAL);
        registerTrait(CHEAP);
        registerTrait(CHEAPSKATE);
        AntiqueTraits.register();
    }

    /** 注册一个词条；同一 identifier 不能注册两次。 */
    public static <T extends AbstractTraits> T registerTrait(T trait) {
        if (TRAITS_BY_NAME.containsKey(com.gctn.tconstruct.api.ContentNames.traitId(trait.identifier))) {
            throw new IllegalArgumentException("Duplicate trait identifier: " + trait.identifier);
        }
        traits.add(trait);
        TRAITS_BY_NAME.put(com.gctn.tconstruct.api.ContentNames.traitId(trait.identifier), trait);
        definitionsChanged();
        return trait;
    }

    private static String definitionSession = "";

    public static String definitionSession() { return definitionSession; }
    public static void definitionsChanged() { definitionSession = java.util.UUID.randomUUID().toString(); }

    public static void replaceTrait(AbstractTraits replacement) {
        registerTraits();
        String key = com.gctn.tconstruct.api.ContentNames.traitId(replacement.identifier);
        AbstractTraits previous = TRAITS_BY_NAME.get(key);
        if (previous == null) { throw new IllegalArgumentException("Unknown trait: " + replacement.identifier); }
        traits.set(traits.indexOf(previous), replacement);
        TRAITS_BY_NAME.put(key, replacement);
        definitionsChanged();
    }

    public static void removeTrait(String id) {
        registerTraits();
        AbstractTraits previous = TRAITS_BY_NAME.remove(com.gctn.tconstruct.api.ContentNames.traitId(id));
        if (previous == null) { throw new IllegalArgumentException("Unknown trait: " + id); }
        traits.remove(previous);
        definitionsChanged();
    }

    /** 根据词条标识符反向查找已注册的词条，找不到时返回 null。 */
    public static AbstractTraits getTrait(String traitsName) {
        registerTraits();
        if (traitsName == null || net.minecraft.resources.ResourceLocation.tryParse(traitsName) == null) { return null; }
        return TRAITS_BY_NAME.get(com.gctn.tconstruct.api.ContentNames.traitId(traitsName));
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
