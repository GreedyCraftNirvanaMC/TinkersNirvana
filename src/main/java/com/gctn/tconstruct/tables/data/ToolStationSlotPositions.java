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
    private static final SlotPosition[] HANDHELD = {LOWER_LEFT, TOP, CENTER, LEFT, RIGHT, LOWER_RIGHT};
    private static final SlotPosition[] SIDE_HEAD = {LOWER_LEFT, TOP, RIGHT, CENTER, LEFT, LOWER_RIGHT};
    private static final SlotPosition[] HAMMER = {LOWER_LEFT, TOP, LEFT, RIGHT, CENTER, LOWER_RIGHT};
    private static final SlotPosition[] SCYTHE = {LOWER_LEFT, TOP, CENTER, LOWER_RIGHT, LEFT, RIGHT};
    private static final SlotPosition[] SHORTBOW = {TOP, LOWER_LEFT, CENTER, LEFT, RIGHT, LOWER_RIGHT};
    private static final SlotPosition[] LONGBOW = {TOP, LOWER_LEFT, LEFT, CENTER, RIGHT, LOWER_RIGHT};
    private static final SlotPosition[] CROSSBOW = {LOWER_LEFT, TOP, LEFT, CENTER, RIGHT, LOWER_RIGHT};
    private static final SlotPosition[] ARROW = {CENTER, TOP, LOWER_LEFT, LEFT, RIGHT, LOWER_RIGHT};
    private static final SlotPosition[] SHURIKEN = {LEFT, TOP, RIGHT, CENTER, LOWER_LEFT, LOWER_RIGHT};

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
            case PICKAXE, SHOVEL, AXE, KAMA, BROADSWORD, LONGSWORD, RAPIER, FRYPAN, BATTLESIGN -> HANDHELD;
            case MATTOCK, EXCAVATOR, LUMBERAXE, CLEAVER -> SIDE_HEAD;
            case HAMMER -> HAMMER;
            case SCYTHE -> SCYTHE;
            case SHORTBOW -> SHORTBOW;
            case LONGBOW, BOLT -> LONGBOW;
            case CROSSBOW -> CROSSBOW;
            case ARROW -> ARROW;
            case SHURIKEN -> SHURIKEN;
            case DEFAULT, DISASSEMBLE, DEBUG -> REPAIR;
        };
        return layout[slot];
    }
    public record SlotPosition(int x, int y) {
        public int slotX() { return x + 1; }
        public int slotY() { return y + 1; }
    }
}
