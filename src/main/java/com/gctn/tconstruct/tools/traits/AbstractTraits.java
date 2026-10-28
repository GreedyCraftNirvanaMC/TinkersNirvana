package com.gctn.tconstruct.tools.traits;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * 共享且不可变的词条定义，保存标识符、等级上限和显示信息。
 * TraitEntry 保存材料贡献的初始等级，实际工具等级与运行状态保存在物品栈组件中。
 * 普通词条合并时取最高等级，LeveledTraits 则显式启用等级相加规则。
 */
public abstract class AbstractTraits implements ITraits {
    public final String identifier;
    public final int maxLevel;
    public final int color;
    // -1 代表非强化升级
    public final int strengthenNumber;
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
        var name = Component.translatable("traits." + identifier + ".name");
        return (maxLevel > 1 ? Component.translatable("tooltip.tconstruct.trait.level", name, toolLevel) : name)
                .withColor(color);
    }

    public Component getDescription() {
        return Component.translatable("traits." + identifier + ".description").withColor(color);
    }

    public String getTranslatedName() {
        return Component.translatable("traits." + this.identifier + ".name").getString();
    }

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
