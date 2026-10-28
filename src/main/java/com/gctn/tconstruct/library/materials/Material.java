package com.gctn.tconstruct.library.materials;

import com.gctn.tconstruct.library.stats.AbstractMaterialStats;
import com.gctn.tconstruct.tools.traits.AbstractTraits;

import java.util.*;

public class Material {
    public final String identifier;
    public final int color;
    public final boolean hidden;

    public Map<String, AbstractMaterialStats> stats = new HashMap<>();
    private final Map<String, List<AbstractTraits>> traits = new HashMap<>();

    // 是否可以直接使用部件加工台(默认false)
    protected boolean craftable = false;

    // 是否可以使用浇筑方式合成(默认false)
    protected boolean castable = false;

    private boolean isHidden;

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
            List<AbstractTraits> list = this.traits.computeIfAbsent(traitPair.pos(), k -> new ArrayList<>());
            for (AbstractTraits trait : traitPair.traits()) {
                if (list.stream().anyMatch(existing -> existing.identifier.equals(trait.identifier))) {
                    throw new IllegalArgumentException("Duplicate material trait: " + trait.identifier);
                }
                list.add(trait);
            }
        }
    }

    // 获取词条属性
    public List<AbstractTraits> getTraits(String pos) {
        return List.copyOf(traits.getOrDefault(pos, traits.getOrDefault(null, List.of())));
    }

    public int getColor() { return color; }

}
