package com.gctn.tconstruct.tables.data;

public enum Mode {
    DEFAULT,
    DISASSEMBLE,
    PICKAXE,
    SHOVEL,
    AXE,
    DEBUG,
    MATTOCK, KAMA, HAMMER, EXCAVATOR, LUMBERAXE, SCYTHE,
    BROADSWORD, LONGSWORD, RAPIER, FRYPAN, BATTLESIGN, CLEAVER,
    SHORTBOW, LONGBOW, CROSSBOW, ARROW, BOLT, SHURIKEN;

    public int getButtonId() {
        return this.ordinal();
    }

    public boolean requiresForge() {
        return switch (this) {
            case HAMMER, EXCAVATOR, LUMBERAXE, SCYTHE, CLEAVER, LONGBOW, CROSSBOW, BOLT, SHURIKEN -> true;
            default -> false;
        };
    }

    public static Mode byButtonId(int id) {
        Mode[] modes = values();
        if (id < 0 || id >= modes.length) {
            return null;
        }
        return modes[id];
    }
}
