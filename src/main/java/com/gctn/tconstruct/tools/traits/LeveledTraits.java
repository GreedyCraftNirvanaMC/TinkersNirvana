package com.gctn.tconstruct.tools.traits;

/**
 * Trait base for additive levels rather than maximum-only merging.
 * By default each distinct contributed strength counts once, matching Antique leveled-trait behavior.
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
