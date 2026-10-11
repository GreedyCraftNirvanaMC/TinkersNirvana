package com.gctn.tconstruct.tools.traits;

public abstract class AbstractTraits {
    public final String identifier;
    public final int maxLevel;
    public int level;
    public AbstractTraits(String identifier, int maxLevel,int level) {
        this.identifier = identifier;
        this.maxLevel = maxLevel;
        this.level = level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int addLevel(int level) {
        this.level += level;
        return level;
    }

}
