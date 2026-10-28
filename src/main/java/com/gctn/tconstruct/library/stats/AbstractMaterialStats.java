package com.gctn.tconstruct.library.stats;

/**
 * Identifies one family of material stats such as Head, Handle or Extra.
 * Typed lookup on Material distinguishes missing or incompatible stats without a blind cast.
 */
public abstract class AbstractMaterialStats {
    public final String identifier;
    public AbstractMaterialStats(String identifier) {
        this.identifier = identifier;
    }
}
