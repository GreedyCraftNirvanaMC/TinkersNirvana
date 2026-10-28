package com.gctn.tconstruct.tools.traits;

import net.minecraft.network.chat.Component;

public abstract class AbstractTraits implements ITraits{
    public final String identifier;
    public final int maxLevel;
    public final int color;
    // -1 代表非强化升级
    public int strengthenNumber;
    public AbstractTraits(String identifier, int maxLevel, int strengthenNumber, int color) {
        this.identifier = identifier;
        this.maxLevel = maxLevel;
        this.strengthenNumber = strengthenNumber;
        this.color = color;
    }

    /** 根据原等级和新增等级计算结果，不保存工具的等级状态。 */
    public abstract int addLevel(int currentLevel, int amount);

    /** 根据原等级和待合并等级计算结果，不保存工具的等级状态。 */
    public abstract int mergeLevel(int currentLevel, int incomingLevel);

    public String getTranslatedName() {
        return Component.translatable("traits." + this.identifier + ".name").getString();
    }

    /** 材料赋予的词条及初始等级；等级不属于共享的词条定义。 */
    public record TraitEntry(AbstractTraits trait, int level) {
    }

    public record TraitPosPair(String pos, TraitEntry... traits) {
        public static TraitPosPair of(String pos, TraitEntry... traits) {
            return new TraitPosPair(pos, traits);
        }
    };
}
