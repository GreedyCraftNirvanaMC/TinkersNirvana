package com.gctn.tconstruct.tools.traits;

/**
 * 采用等级相加规则的词条基类，区别于仅取最高等级的普通词条。
 * 默认情况下，每种不同的初始强度只贡献一次，与 Antique 的可叠加词条行为一致。
 */
public abstract class LeveledTraits extends AbstractTraits {
    protected LeveledTraits(String identifier, int maxLevel, int strengthenNumber, int color) {
        super(identifier, maxLevel, strengthenNumber, color);
    }

    /** 覆盖为 true 时，每个部件的等级都会累加，包括相同强度。 */
    public boolean stacksRepeatedLevels() { return false; }

    @Override
    public int addLevel(int currentLevel, int amount) {
        return (int) Math.clamp((long) currentLevel + amount, 1L, maxLevel);
    }
}
