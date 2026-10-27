package com.gctn.tconstruct.tools.traits;

import net.minecraft.network.chat.Component;

public abstract class AbstractTraits {
    public final String identifier;
    public final int maxLevel;
    public int level;
    public final int color;
    // -1 代表非强化升级
    public int strengthenNumber;
    public AbstractTraits(String identifier, int maxLevel, int strengthenNumber, int color) {
        this.identifier = identifier;
        this.maxLevel = maxLevel;
        this.strengthenNumber = strengthenNumber;
        this.color = color;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public abstract int addLevel(int level);

    public abstract int mergeLevel(int level);

    public String getTranslatedName() {
        return Component.translatable("traits." + this.identifier + ".name").getString();
    }

    public record TraitPosPair(String pos, AbstractTraits... traits) {
        public static TraitPosPair of(String pos, AbstractTraits... traits) {
            return new TraitPosPair(pos, traits);
        }
    };
}
