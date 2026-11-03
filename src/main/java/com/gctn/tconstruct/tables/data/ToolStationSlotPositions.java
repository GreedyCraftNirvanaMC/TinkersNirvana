package com.gctn.tconstruct.tables.data;

/** 所有模式共用六个物理槽，切换模式只改变输入约束，避免重复同步同一物品。 */
public final class ToolStationSlotPositions {
    public static final int INPUT_SLOT_COUNT = 6;
    private static final SlotPosition[] INPUT_SLOTS = {
            new SlotPosition(32, 41), new SlotPosition(14, 61), new SlotPosition(10, 36),
            new SlotPosition(32, 18), new SlotPosition(54, 36), new SlotPosition(50, 61)
    };
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
        return isInputSlotActive(mode, slot) ? INPUT_SLOTS[slot] : null;
    }
    public static SlotPosition getDisplayInputSlotPosition(Mode mode, int slot) {
        return INPUT_SLOTS[slot];
    }
    public record SlotPosition(int x, int y) {
        public int slotX() { return x + 1; }
        public int slotY() { return y + 1; }
    }
}
