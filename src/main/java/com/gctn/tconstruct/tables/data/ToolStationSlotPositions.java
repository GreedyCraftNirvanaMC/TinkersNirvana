package com.gctn.tconstruct.tables.data;

/** 所有模式共用六个物理槽，切换模式只改变输入约束，避免重复同步同一物品。 */
public final class ToolStationSlotPositions {
    public static final int INPUT_SLOT_COUNT = 6;
    private static final SlotPosition CENTER = new SlotPosition(32, 41);
    private static final SlotPosition LOWER_LEFT = new SlotPosition(14, 61);
    private static final SlotPosition LEFT = new SlotPosition(10, 36);
    private static final SlotPosition TOP = new SlotPosition(32, 18);
    private static final SlotPosition RIGHT = new SlotPosition(54, 36);
    private static final SlotPosition LOWER_RIGHT = new SlotPosition(50, 61);

    // Indexed by recipe slot, including inactive slots that may still contain items.
    private static final SlotPosition[] REPAIR = {CENTER, LOWER_LEFT, LEFT, TOP, RIGHT, LOWER_RIGHT};
    private static final SlotPosition[] PICKAXE = layout(15, 60, 53, 22, 33, 42);
    private static final SlotPosition[] SHOVEL = layout(33, 42, 51, 24, 13, 62);
    private static final SlotPosition[] HAND_AXE = layout(22, 53, 31, 22, 51, 34);
    private static final SlotPosition[] HAMMER = layout(21, 52, 44, 29, 57, 48, 25, 16);
    private static final SlotPosition[] EXCAVATOR = layout(25, 46, 45, 26, 25, 26, 7, 62);
    private static final SlotPosition[] LUMBERAXE = layout(32, 46, 33, 22, 53, 38, 13, 62);
    private static final SlotPosition[] SCYTHE = layout(17, 54, 36, 19, 56, 29, 37, 47);
    private static final SlotPosition[] SWORD = layout(12, 62, 48, 26, 30, 44);
    private static final SlotPosition[] RAPIER = layout(52, 62, 18, 26, 32, 44);
    private static final SlotPosition[] FRYPAN = layout(12, 62, 34, 36);
    private static final SlotPosition[] BATTLESIGN = layout(27, 60, 27, 34);
    private static final SlotPosition[] CLEAVER = layout(9, 64, 25, 36, 47, 30, 33, 58);
    private static final SlotPosition[] SHORTBOW = layout(36, 23, 14, 45, 38, 47);
    private static final SlotPosition[] LONGBOW = layout(44, 19, 10, 53, 17, 26, 38, 47);
    private static final SlotPosition[] CROSSBOW = layout(38, 47, 44, 19, 14, 23, 18, 51);
    private static final SlotPosition[] ARROW = layout(32, 41, 50, 23, 14, 59);
    private static final SlotPosition[] BOLT = layout(40, 37, 20, 53);
    private static final SlotPosition[] SHURIKEN = layout(20, 29, 44, 29, 44, 53, 20, 53);

    private ToolStationSlotPositions() {}
    public static boolean isInputSlotActive(Mode mode, int slot) {
        if (slot < 0 || slot >= INPUT_SLOT_COUNT) { return false; }
        if (mode == Mode.DEFAULT || mode == Mode.DISASSEMBLE) { return true; }
        var recipe = com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes.get(mode);
        return recipe != null && recipe.getRequiredItem(slot) != null;
    }
    public static int getActiveInputSlotCount(Mode mode) {
        int count = 0;
        for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) { if (isInputSlotActive(mode, slot)) { count++; } }
        return count;
    }
    public static SlotPosition getInputSlotPosition(Mode mode, int slot) {
        return isInputSlotActive(mode, slot) ? getDisplayInputSlotPosition(mode, slot) : null;
    }
    public static SlotPosition getDisplayInputSlotPosition(Mode mode, int slot) {
        SlotPosition[] layout = switch (mode) {
            case PICKAXE -> PICKAXE;
            case SHOVEL -> SHOVEL;
            case AXE, MATTOCK, KAMA -> HAND_AXE;
            case HAMMER -> HAMMER;
            case EXCAVATOR -> EXCAVATOR;
            case LUMBERAXE -> LUMBERAXE;
            case SCYTHE -> SCYTHE;
            case BROADSWORD, LONGSWORD -> SWORD;
            case RAPIER -> RAPIER;
            case FRYPAN -> FRYPAN;
            case BATTLESIGN -> BATTLESIGN;
            case CLEAVER -> CLEAVER;
            case SHORTBOW -> SHORTBOW;
            case LONGBOW -> LONGBOW;
            case CROSSBOW -> CROSSBOW;
            case ARROW -> ARROW;
            case BOLT -> BOLT;
            case SHURIKEN -> SHURIKEN;
            case DEFAULT, DISASSEMBLE, DEBUG -> REPAIR;
        };
        return slot < layout.length ? layout[slot] : REPAIR[slot];
    }

    private static SlotPosition[] layout(int... positions) {
        SlotPosition[] result = new SlotPosition[positions.length / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = new SlotPosition(positions[2 * i] - 1, positions[2 * i + 1] - 1);
        }
        return result;
    }
    public record SlotPosition(int x, int y) {
        public int slotX() { return x + 1; }
        public int slotY() { return y + 1; }
    }
}
