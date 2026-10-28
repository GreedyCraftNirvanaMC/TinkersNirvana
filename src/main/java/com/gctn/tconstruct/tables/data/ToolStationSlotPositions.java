package com.gctn.tconstruct.tables.data;

/**
 * Shared coordinates and active-slot masks for station modes.
 * Menus and screens use one layout definition so clickable slots match their rendered backgrounds.
 * Inactive inputs containing real items remain visible so changing modes cannot trap inventory.
 */
public final class ToolStationSlotPositions {
    public static final int INPUT_SLOT_COUNT = 6;

    private static final int SLOT_ITEM_OFFSET = 1;

    private static final SlotPosition[] ALL_INPUT_SLOTS = {
            new SlotPosition(32, 41),
            new SlotPosition(14, 61),
            new SlotPosition(10, 36),
            new SlotPosition(32, 18),
            new SlotPosition(54, 36),
            new SlotPosition(50, 61)
    };

    private static final SlotPosition[] PICKAXE_INPUT_SLOTS = {
            new SlotPosition(14, 59),
            new SlotPosition(52, 21),
            new SlotPosition(32, 41),
            null,
            null,
            null
    };

    private static final SlotPosition[] SHOVEL_INPUT_SLOTS = {
            new SlotPosition(32, 41),
            new SlotPosition(50, 23),
            new SlotPosition(12, 61),
            null,
            null,
            null
    };

    private static final SlotPosition[] AXE_INPUT_SLOTS = {
            new SlotPosition(22, 51),
            new SlotPosition(30, 20),
            new SlotPosition(50, 32),
            null,
            null,
            null
    };

    private static final SlotPosition[] DEBUG_INPUT_SLOTS = {
            new SlotPosition(14, 59),
            new SlotPosition(32, 41),
            new SlotPosition(50, 23),
            new SlotPosition(50, 59),
            null,
            null
    };

    private static final SlotPosition[] HIDDEN_INPUT_SLOTS = {
            new SlotPosition(74, 61),
            new SlotPosition(92, 61),
            new SlotPosition(110, 61),
            new SlotPosition(74, 61),
            new SlotPosition(92, 61),
            new SlotPosition(110, 61)
    };

    private ToolStationSlotPositions() {
    }

    public static boolean isInputSlotActive(Mode mode, int inputSlot) {
        return getInputSlotPosition(mode, inputSlot) != null;
    }

    public static int getActiveInputSlotCount(Mode mode) {
        int activeSlots = 0;
        for (SlotPosition position : getInputSlots(mode)) {
            if (position != null) {
                activeSlots++;
            }
        }
        return activeSlots;
    }

    public static SlotPosition getInputSlotPosition(Mode mode, int inputSlot) {
        if (inputSlot < 0 || inputSlot >= INPUT_SLOT_COUNT) {
            return null;
        }
        return getInputSlots(mode)[inputSlot];
    }

    public static SlotPosition getDisplayInputSlotPosition(Mode mode, int inputSlot) {
        SlotPosition activePosition = getInputSlotPosition(mode, inputSlot);
        return activePosition == null ? HIDDEN_INPUT_SLOTS[inputSlot] : activePosition;
    }

    private static SlotPosition[] getInputSlots(Mode mode) {
        return switch (mode) {
            case DEFAULT, DISASSEMBLE -> ALL_INPUT_SLOTS;
            case PICKAXE -> PICKAXE_INPUT_SLOTS;
            case SHOVEL -> SHOVEL_INPUT_SLOTS;
            case AXE -> AXE_INPUT_SLOTS;
            case DEBUG -> DEBUG_INPUT_SLOTS;
        };
    }

    public record SlotPosition(int x, int y) {
        public int slotX() {
            return this.x + SLOT_ITEM_OFFSET;
        }

        public int slotY() {
            return this.y + SLOT_ITEM_OFFSET;
        }
    }
}
