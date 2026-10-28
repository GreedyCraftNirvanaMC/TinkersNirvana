package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.Collections; // 引入 java.util.Collections，供本文件使用。
import java.util.LinkedHashMap; // 引入 java.util.LinkedHashMap，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。
import java.util.Map; // 引入 java.util.Map，供本文件使用。

/**
 * 每种内置词条只保留一个不可变定义实例，并集中保存其初始材料配置。
 * 通过索引查询标识符，并在重复注册时立即报错，避免改变已保存工具的词条行为。
 */
public final class TinkerTraits { // 定义 TinkerTraits 的实现类型，具体职责见上方说明。
    /** 按注册顺序保存词条，便于遍历所有词条。 */
    private static final List<AbstractTraits> traits = new ArrayList<>(); // 设置 traits 为 new ArrayList<>()。
    private static final Map<String, AbstractTraits> TRAITS_BY_NAME = new LinkedHashMap<>(); // 向当前声明或调用传入 private static final Map<String, AbstractTraits> TRAITS_BY_NAME = new LinkedHashMap<>()。
    private static boolean registered; // 声明 registered，保存后续流程使用的状态或依赖。

    private static final TraitEcological ECOLOGICAL = new TraitEcological(); // 设置 ECOLOGICAL 为 new TraitEcological()。
    private static final TraitCheap CHEAP = new TraitCheap(); // 设置 CHEAP 为 new TraitCheap()。
    private static final TraitCheapskate CHEAPSKATE = new TraitCheapskate(); // 设置 CHEAPSKATE 为 new TraitCheapskate()。

    /** 生成工具/部件时使用的初始配置；工具后续的实际等级保存在自身 Tag 中。 */
    public static final AbstractTraits.TraitEntry ECOLOGICAL_1 = // 设置 ECOLOGICAL_1 为 。
            new AbstractTraits.TraitEntry(ECOLOGICAL, 1); // 创建 new AbstractTraits.TraitEntry(ECOLOGICAL, 1)，作为当前对象或调用的组成部分。
    public static final AbstractTraits.TraitEntry CHEAP_1 = // 设置 CHEAP_1 为 。
            new AbstractTraits.TraitEntry(CHEAP, 1); // 创建 new AbstractTraits.TraitEntry(CHEAP, 1)，作为当前对象或调用的组成部分。
    public static final AbstractTraits.TraitEntry CHEAPSKATE_1 = // 设置 CHEAPSKATE_1 为 。
            new AbstractTraits.TraitEntry(CHEAPSKATE, 1); // 创建 new AbstractTraits.TraitEntry(CHEAPSKATE, 1)，作为当前对象或调用的组成部分。

    private TinkerTraits() { // 定义 TinkerTraits 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    /**
     * 在这里注册所有内置词条定义，与初始等级常量共用同一个定义对象。
     * 添加新词条时，在此函数中增加一行 registerTrait(...)，
     * 即可同时让遍历、反向查找和颜色查询支持该词条。
     */
    public static void registerTraits() { // 定义 registerTraits 的调用入口，参数和具体处理见下方实现。
        if (registered) { // 检查 registered；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        registered = true; // 设置 registered 为 true。

        registerTrait(ECOLOGICAL); // 调用 registerTrait，执行当前流程所需的处理。
        registerTrait(CHEAP); // 调用 registerTrait，执行当前流程所需的处理。
        registerTrait(CHEAPSKATE); // 调用 registerTrait，执行当前流程所需的处理。
    } // 结束当前作用域。

    /** 注册一个词条；同一 identifier 不能注册两次。 */
    public static <T extends AbstractTraits> T registerTrait(T trait) { // 调用 registerTrait，执行当前流程所需的处理。
        if (TRAITS_BY_NAME.containsKey(trait.identifier)) { // 检查 TRAITS_BY_NAME.containsKey(trait.identifier)；按条件决定是否执行本分支。
            throw new IllegalArgumentException("Duplicate trait identifier: " + trait.identifier); // 抛出异常并终止当前无效操作，避免继续使用不合法状态。
        } // 结束当前作用域。
        traits.add(trait); // 向集合或构建器追加条目。
        TRAITS_BY_NAME.put(trait.identifier, trait); // 按键保存对应数据。
        return trait; // 返回 trait，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 根据词条标识符反向查找已注册的词条，找不到时返回 null。 */
    public static AbstractTraits getTrait(String traitsName) { // 定义 getTrait 的调用入口，参数和具体处理见下方实现。
        registerTraits(); // 调用 registerTraits，执行当前流程所需的处理。
        return TRAITS_BY_NAME.get(traitsName); // 返回 TRAITS_BY_NAME.get(traitsName)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 根据词条标识符获取颜色；找不到时返回白色。 */
    public static int getTraitColor(String traitsName) { // 定义 getTraitColor 的调用入口，参数和具体处理见下方实现。
        AbstractTraits trait = getTrait(traitsName); // 设置 trait 为 getTrait(traitsName)。
        return trait == null ? 0xFFFFFFFF : trait.color; // 返回 trait == null ? 0xFFFFFFFF : trait.color，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 返回只读的注册表视图。 */
    public static List<AbstractTraits> getAllTraits() { // 定义 getAllTraits 的调用入口，参数和具体处理见下方实现。
        registerTraits(); // 调用 registerTraits，执行当前流程所需的处理。
        return Collections.unmodifiableList(traits); // 返回 Collections.unmodifiableList(traits)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
