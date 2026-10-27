package com.gctn.tconstruct.tools.traits;

/** Opt-in stacking of distinct strengths, as in Antique; repeated parts do not add levels. */
public abstract class LeveledTraits extends AbstractTraits {
    protected LeveledTraits(String identifier, int maxLevel, int level, int strengthenNumber, int color) {
        super(identifier, maxLevel, level, strengthenNumber, color);
    }

    @Override
    public int addLevel(int existingLevel) {
        return (int) Math.clamp((long) existingLevel + level, 1L, maxLevel);
    }
}
