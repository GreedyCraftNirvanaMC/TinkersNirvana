package com.gctn.tconstruct.tools.traits;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/** 启动期间配置的共享词条定义；材料赋予等级与工具运行状态均不存放在定义中。 */
public abstract class AbstractTraits implements ITraits {
    public final String identifier;
    public int maxLevel;
    public int color;
    // -1 代表非强化升级
    public int strengthenNumber;
    public AbstractTraits(String identifier, int maxLevel, int strengthenNumber, int color) {
        if (identifier == null || identifier.isBlank() || maxLevel < 1) {
            throw new IllegalArgumentException("Invalid trait definition: " + identifier);
        }
        this.identifier = identifier;
        this.maxLevel = maxLevel;
        this.strengthenNumber = strengthenNumber;
        this.color = color;
    }

    /** 根据原等级和新增等级计算结果，不保存工具的等级状态。 */
    public int addLevel(int currentLevel, int amount) {
        return Math.clamp(Math.max(currentLevel, amount), 1, maxLevel);
    }

    /** 根据原等级和待合并等级计算结果，不保存工具的等级状态。 */
    public int mergeLevel(int currentLevel, int incomingLevel) {
        return addLevel(currentLevel, incomingLevel);
    }

    /** 优先级越高越早执行；同优先级按 identifier 排序。 */
    public int getPriority() { return 100; }

    public Component getDisplayName(int toolLevel) {
        var name = displayName();
        return (maxLevel > 1 ? Component.translatable("tooltip.tconstruct.trait.level", name, toolLevel) : name).copy()
                .withColor(color);
    }

    public Component getDescription() {
        return com.gctn.tconstruct.api.ContentNames.translated(com.gctn.tconstruct.api.ContentNames.key("traits", identifier, "description"),
                Component.translatableWithFallback("traits." + identifier + ".description", "")).copy().withColor(color);
    }

    public String getTranslatedName() {
        return displayName().getString();
    }

    public Component getNameComponent() { return displayName(); }

    protected Component displayName() {
        return com.gctn.tconstruct.api.ContentNames.translated(com.gctn.tconstruct.api.ContentNames.key("traits", identifier, "name"),
                Component.translatableWithFallback("traits." + identifier + ".name", identifier));
    }

    /** Native rules outside lifecycle callbacks are controlled by the rules hook. */
    public boolean usesNativeRules() { return true; }
    public boolean usesNativeHook(String hook) { return true; }

    /** 材料赋予的词条及初始等级；等级不属于共享的词条定义。 */
    public record TraitEntry(AbstractTraits trait, int level) {
        public TraitEntry {
            Objects.requireNonNull(trait, "trait");
            if (level < 1 || level > trait.maxLevel) {
                throw new IllegalArgumentException("Invalid initial level for " + trait.identifier + ": " + level);
            }
        }
    }

    public record TraitPosPair(String pos, TraitEntry... traits) {
        public static TraitPosPair of(String pos, TraitEntry... traits) {
            return new TraitPosPair(pos, traits);
        }
    }
}
