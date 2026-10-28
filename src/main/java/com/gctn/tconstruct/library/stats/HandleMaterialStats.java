package com.gctn.tconstruct.library.stats;

/**
 * Handle durability multiplier plus a flat contribution applied after multiplication.
 * Negative flat values are valid in Antique (for example stone); final tool durability is clamped.
 */
public class HandleMaterialStats extends AbstractMaterialStats {
    public final int durability;
    public final float modifer;
    public HandleMaterialStats(float modifer, int durability) {
        super("Handle");
        this.modifer = modifer;
        this.durability = durability;
    }
}
