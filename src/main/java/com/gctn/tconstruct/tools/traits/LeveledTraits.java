package com.gctn.tconstruct.tools.traits;

/** 可叠加词条，默认按 Antique 规则：每种不同的初始强度只贡献一次。 */
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
