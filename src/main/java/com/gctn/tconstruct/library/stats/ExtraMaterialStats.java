package com.gctn.tconstruct.library.stats;

/**
 * Extra-part durability added to head durability before the handle multiplier.
 * It is a flat contribution; bindings do not determine mining speed or repair material.
 */
public class ExtraMaterialStats extends AbstractMaterialStats {
    public final int durability;
    public ExtraMaterialStats(int durability) {
        super("Extra");
        this.durability = durability;
    }
}
