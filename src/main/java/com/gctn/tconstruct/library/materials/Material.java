package com.gctn.tconstruct.library.materials;

import com.gctn.tconstruct.library.stats.AbstractMaterialStats;
import com.gctn.tconstruct.tools.traits.AbstractTraits;

import java.util.*;

/**
 * 共享的材料定义，保存各部位属性和材料初始赋予的词条。
 * 定义在初始化阶段填充；耐久损耗、实际词条等级和冷却等运行状态保存在物品栈中。
 * 指定部位的词条列表会完整替换默认列表，也允许用空列表显式取消默认词条。
 */
public class Material {
    public final String identifier;
    public final int color;
    public final boolean hidden;

    public Map<String, AbstractMaterialStats> stats = new HashMap<>();
    private final Map<String, List<AbstractTraits.TraitEntry>> traits = new HashMap<>();

    // 是否可以直接使用部件加工台(默认false)
    protected boolean craftable = false;

    // 是否可以使用浇筑方式合成(默认false)
    protected boolean castable = false;

    // 参数不全的默认初始化
    public Material(String identifier, int color) { this(identifier, color, false); }
    public Material(String identifier) { this(identifier, 0xFFFFFF); }

    public Material(String identifier, int color, boolean hidden) {
        this.identifier = identifier;
        this.color = color;
        this.hidden = hidden;
    }

    // 设置合成方式
    public Material setCraftable(boolean craftable) {
        this.craftable = craftable;
        return this;
    }
    public Material setCastable(boolean castable) {
        this.castable = castable;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isCraftable() {
        return craftable;
    }

    public boolean isCastable() {
        return castable;
    }

    // 添加材料属性
    public void addMaterialStats(AbstractMaterialStats... stats) {
        for (AbstractMaterialStats stat : stats) {
            this.stats.put(stat.identifier, stat);
        }
    }

    // 获取材料属性
    public Map<String, AbstractMaterialStats> getStats() {
        return stats;
    }

    public boolean hasStats(String identifier) {
        return stats.containsKey(identifier);
    }

    public <T extends AbstractMaterialStats> T getStats(String identifier, Class<T> statClass) {
        AbstractMaterialStats stat = stats.get(identifier);
        return statClass.isInstance(stat) ? statClass.cast(stat) : null;
    }

    // 添加词条属性
    public void addMaterialTraits(AbstractTraits.TraitPosPair... traitPairs) {
        for (AbstractTraits.TraitPosPair traitPair : traitPairs) {
            List<AbstractTraits.TraitEntry> list = this.traits.computeIfAbsent(traitPair.pos(), k -> new ArrayList<>());
            for (AbstractTraits.TraitEntry entry : traitPair.traits()) {
                if (list.stream().anyMatch(existing -> existing.trait().identifier.equals(entry.trait().identifier))) {
                    throw new IllegalArgumentException("Duplicate material trait: " + entry.trait().identifier);
                }
                list.add(entry);
            }
        }
    }

    // 获取词条属性
    public List<AbstractTraits.TraitEntry> getTraits(String pos) {
        // null 位置表示默认配置；指定位置的配置整体替换默认配置，而不是追加。
        return List.copyOf(traits.getOrDefault(pos, traits.getOrDefault(null, List.of())));
    }

    public int getColor() { return color; }

}
