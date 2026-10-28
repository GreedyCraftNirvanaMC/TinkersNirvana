package com.gctn.tconstruct.library.materials; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.stats.AbstractMaterialStats; // 引入 com.gctn.tconstruct.library.stats.AbstractMaterialStats，供本文件使用。
import com.gctn.tconstruct.tools.traits.AbstractTraits; // 引入 com.gctn.tconstruct.tools.traits.AbstractTraits，供本文件使用。

import java.util.*; // 引入 java.util.*，供本文件使用。

/**
 * 共享的材料定义，保存各部位属性和材料初始赋予的词条。
 * 定义在初始化阶段填充；耐久损耗、实际词条等级和冷却等运行状态保存在物品栈中。
 * 指定部位的词条列表会完整替换默认列表，也允许用空列表显式取消默认词条。
 */
public class Material { // 定义 Material 的实现类型，具体职责见上方说明。
    public final String identifier; // 声明 identifier，保存后续流程使用的状态或依赖。
    public final int color; // 声明 color，保存后续流程使用的状态或依赖。
    public final boolean hidden; // 声明 hidden，保存后续流程使用的状态或依赖。

    public Map<String, AbstractMaterialStats> stats = new HashMap<>(); // 向当前声明或调用传入 public Map<String, AbstractMaterialStats> stats = new HashMap<>()。
    private final Map<String, List<AbstractTraits.TraitEntry>> traits = new HashMap<>(); // 向当前声明或调用传入 private final Map<String, List<AbstractTraits.TraitEntry>> traits = new HashMap<>()。

    // 是否可以直接使用部件加工台(默认false)
    protected boolean craftable = false; // 设置 craftable 为 false。

    // 是否可以使用浇筑方式合成(默认false)
    protected boolean castable = false; // 设置 castable 为 false。

    // 参数不全的默认初始化
    public Material(String identifier, int color) { this(identifier, color, false); } // 定义 Material 的调用入口；调用 this，执行当前流程所需的处理。
    public Material(String identifier) { this(identifier, 0xFFFFFF); } // 定义 Material 的调用入口；调用 this，执行当前流程所需的处理。

    public Material(String identifier, int color, boolean hidden) { // 定义 Material 的调用入口，参数和具体处理见下方实现。
        this.identifier = identifier; // 设置 this.identifier 为 identifier。
        this.color = color; // 设置 this.color 为 color。
        this.hidden = hidden; // 设置 this.hidden 为 hidden。
    } // 结束当前作用域。

    // 设置合成方式
    public Material setCraftable(boolean craftable) { // 定义 setCraftable 的调用入口，参数和具体处理见下方实现。
        this.craftable = craftable; // 设置 this.craftable 为 craftable。
        return this; // 返回 this，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    public Material setCastable(boolean castable) { // 定义 setCastable 的调用入口，参数和具体处理见下方实现。
        this.castable = castable; // 设置 this.castable 为 castable。
        return this; // 返回 this，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isHidden() { // 定义 isHidden 的调用入口，参数和具体处理见下方实现。
        return hidden; // 返回 hidden，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isCraftable() { // 定义 isCraftable 的调用入口，参数和具体处理见下方实现。
        return craftable; // 返回 craftable，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean isCastable() { // 定义 isCastable 的调用入口，参数和具体处理见下方实现。
        return castable; // 返回 castable，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 添加材料属性
    public void addMaterialStats(AbstractMaterialStats... stats) { // 定义 addMaterialStats 的调用入口，参数和具体处理见下方实现。
        for (AbstractMaterialStats stat : stats) { // 遍历 AbstractMaterialStats stat : stats，逐项执行当前处理。
            this.stats.put(stat.identifier, stat); // 按键保存对应数据。
        } // 结束当前作用域。
    } // 结束当前作用域。

    // 获取材料属性
    public Map<String, AbstractMaterialStats> getStats() { // 调用 getStats，执行当前流程所需的处理。
        return stats; // 返回 stats，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean hasStats(String identifier) { // 定义 hasStats 的调用入口，参数和具体处理见下方实现。
        return stats.containsKey(identifier); // 返回 stats.containsKey(identifier)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public <T extends AbstractMaterialStats> T getStats(String identifier, Class<T> statClass) { // 调用 getStats，执行当前流程所需的处理。
        AbstractMaterialStats stat = stats.get(identifier); // 设置 stat 为 stats.get(identifier)。
        return statClass.isInstance(stat) ? statClass.cast(stat) : null; // 返回 statClass.isInstance(stat) ? statClass.cast(stat) : null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 添加词条属性
    public void addMaterialTraits(AbstractTraits.TraitPosPair... traitPairs) { // 定义 addMaterialTraits 的调用入口，参数和具体处理见下方实现。
        for (AbstractTraits.TraitPosPair traitPair : traitPairs) { // 遍历 AbstractTraits.TraitPosPair traitPair : traitPairs，逐项执行当前处理。
            List<AbstractTraits.TraitEntry> list = this.traits.computeIfAbsent(traitPair.pos(), k -> new ArrayList<>()); // 设置 list 为 this.traits.computeIfAbsent(traitPair.pos(), k -> new ArrayList<>())。
            for (AbstractTraits.TraitEntry entry : traitPair.traits()) { // 遍历 AbstractTraits.TraitEntry entry : traitPair.traits()，逐项执行当前处理。
                if (list.stream().anyMatch(existing -> existing.trait().identifier.equals(entry.trait().identifier))) { // 检查 list.stream().anyMatch(existing -> existing.trait().identifier.equals(entry.trait().identifier))；按条件决定是否执行本分支。
                    throw new IllegalArgumentException("Duplicate material trait: " + entry.trait().identifier); // 抛出异常并终止当前无效操作，避免继续使用不合法状态。
                } // 结束当前作用域。
                list.add(entry); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。

    // 获取词条属性
    public List<AbstractTraits.TraitEntry> getTraits(String pos) { // 定义 getTraits 的调用入口，参数和具体处理见下方实现。
        // null 位置表示默认配置；指定位置的配置整体替换默认配置，而不是追加。
        return List.copyOf(traits.getOrDefault(pos, traits.getOrDefault(null, List.of()))); // 返回 List.copyOf(traits.getOrDefault(pos, traits.getOrDefault(null, List.of())))，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public int getColor() { return color; } // 定义 getColor 的调用入口；返回 color，将当前分支的结果交给调用者。

} // 结束当前作用域。
