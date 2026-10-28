package com.gctn.tconstruct.tables.data;

/**
 * Stable station button IDs stored in saves and synchronized through menu data slots.
 * Keep the existing enum order when adding modes; invalid saved/network IDs return null for caller fallback.
 */
public enum Mode {
    DEFAULT,
    DISASSEMBLE,
    PICKAXE,
    SHOVEL,
    AXE,
    DEBUG;

    public int getButtonId() {
        return this.ordinal();
    }

    public static Mode byButtonId(int id) {
        Mode[] modes = values();
        if (id < 0 || id >= modes.length) {
            return null;
        }
        return modes[id];
    }
}
