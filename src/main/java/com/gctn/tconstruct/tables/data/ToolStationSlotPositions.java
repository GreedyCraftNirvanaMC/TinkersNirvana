package com.gctn.tconstruct.tables.data; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 集中定义各工具台模式的槽位坐标和启用规则。
 * 菜单与界面使用同一份布局，保证实际点击区域与绘制的槽位背景一致。
 * 未启用但仍存有真实物品的输入槽保持可见，避免切换模式后物品无法取回。
 */
public final class ToolStationSlotPositions { // 定义 ToolStationSlotPositions 的实现类型，具体职责见上方说明。
    public static final int INPUT_SLOT_COUNT = 6; // 设置 INPUT_SLOT_COUNT 为 6。

    private static final int SLOT_ITEM_OFFSET = 1; // 设置 SLOT_ITEM_OFFSET 为 1。

    private static final SlotPosition[] ALL_INPUT_SLOTS = { // 设置 ALL_INPUT_SLOTS 为 {。
            new SlotPosition(32, 41), // 创建 new SlotPosition(32, 41),，作为当前对象或调用的组成部分。
            new SlotPosition(14, 61), // 创建 new SlotPosition(14, 61),，作为当前对象或调用的组成部分。
            new SlotPosition(10, 36), // 创建 new SlotPosition(10, 36),，作为当前对象或调用的组成部分。
            new SlotPosition(32, 18), // 创建 new SlotPosition(32, 18),，作为当前对象或调用的组成部分。
            new SlotPosition(54, 36), // 创建 new SlotPosition(54, 36),，作为当前对象或调用的组成部分。
            new SlotPosition(50, 61) // 创建 new SlotPosition(50, 61)，作为当前对象或调用的组成部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private static final SlotPosition[] PICKAXE_INPUT_SLOTS = { // 设置 PICKAXE_INPUT_SLOTS 为 {。
            new SlotPosition(14, 59), // 创建 new SlotPosition(14, 59),，作为当前对象或调用的组成部分。
            new SlotPosition(52, 21), // 创建 new SlotPosition(52, 21),，作为当前对象或调用的组成部分。
            new SlotPosition(32, 41), // 创建 new SlotPosition(32, 41),，作为当前对象或调用的组成部分。
            null, // 向当前声明或调用传入 null。
            null, // 向当前声明或调用传入 null。
            null // 在 ToolStationSlotPositions 中补充当前声明或表达式的 null 部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private static final SlotPosition[] SHOVEL_INPUT_SLOTS = { // 设置 SHOVEL_INPUT_SLOTS 为 {。
            new SlotPosition(32, 41), // 创建 new SlotPosition(32, 41),，作为当前对象或调用的组成部分。
            new SlotPosition(50, 23), // 创建 new SlotPosition(50, 23),，作为当前对象或调用的组成部分。
            new SlotPosition(12, 61), // 创建 new SlotPosition(12, 61),，作为当前对象或调用的组成部分。
            null, // 向当前声明或调用传入 null。
            null, // 向当前声明或调用传入 null。
            null // 在 ToolStationSlotPositions 中补充当前声明或表达式的 null 部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private static final SlotPosition[] AXE_INPUT_SLOTS = { // 设置 AXE_INPUT_SLOTS 为 {。
            new SlotPosition(22, 51), // 创建 new SlotPosition(22, 51),，作为当前对象或调用的组成部分。
            new SlotPosition(30, 20), // 创建 new SlotPosition(30, 20),，作为当前对象或调用的组成部分。
            new SlotPosition(50, 32), // 创建 new SlotPosition(50, 32),，作为当前对象或调用的组成部分。
            null, // 向当前声明或调用传入 null。
            null, // 向当前声明或调用传入 null。
            null // 在 ToolStationSlotPositions 中补充当前声明或表达式的 null 部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private static final SlotPosition[] DEBUG_INPUT_SLOTS = { // 设置 DEBUG_INPUT_SLOTS 为 {。
            new SlotPosition(14, 59), // 创建 new SlotPosition(14, 59),，作为当前对象或调用的组成部分。
            new SlotPosition(32, 41), // 创建 new SlotPosition(32, 41),，作为当前对象或调用的组成部分。
            new SlotPosition(50, 23), // 创建 new SlotPosition(50, 23),，作为当前对象或调用的组成部分。
            new SlotPosition(50, 59), // 创建 new SlotPosition(50, 59),，作为当前对象或调用的组成部分。
            null, // 向当前声明或调用传入 null。
            null // 在 ToolStationSlotPositions 中补充当前声明或表达式的 null 部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private static final SlotPosition[] HIDDEN_INPUT_SLOTS = { // 设置 HIDDEN_INPUT_SLOTS 为 {。
            new SlotPosition(74, 61), // 创建 new SlotPosition(74, 61),，作为当前对象或调用的组成部分。
            new SlotPosition(92, 61), // 创建 new SlotPosition(92, 61),，作为当前对象或调用的组成部分。
            new SlotPosition(110, 61), // 创建 new SlotPosition(110, 61),，作为当前对象或调用的组成部分。
            new SlotPosition(74, 61), // 创建 new SlotPosition(74, 61),，作为当前对象或调用的组成部分。
            new SlotPosition(92, 61), // 创建 new SlotPosition(92, 61),，作为当前对象或调用的组成部分。
            new SlotPosition(110, 61) // 创建 new SlotPosition(110, 61)，作为当前对象或调用的组成部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private ToolStationSlotPositions() { // 定义 ToolStationSlotPositions 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    public static boolean isInputSlotActive(Mode mode, int inputSlot) { // 定义 isInputSlotActive 的调用入口，参数和具体处理见下方实现。
        return getInputSlotPosition(mode, inputSlot) != null; // 返回 getInputSlotPosition(mode, inputSlot) != null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static int getActiveInputSlotCount(Mode mode) { // 定义 getActiveInputSlotCount 的调用入口，参数和具体处理见下方实现。
        int activeSlots = 0; // 设置 activeSlots 为 0。
        for (SlotPosition position : getInputSlots(mode)) { // 遍历 SlotPosition position : getInputSlots(mode)，逐项执行当前处理。
            if (position != null) { // 检查 position != null；按条件决定是否执行本分支。
                activeSlots++; // 更新 activeSlots++ 对应的计数，供下一步判定使用。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return activeSlots; // 返回 activeSlots，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static SlotPosition getInputSlotPosition(Mode mode, int inputSlot) { // 定义 getInputSlotPosition 的调用入口，参数和具体处理见下方实现。
        if (inputSlot < 0 || inputSlot >= INPUT_SLOT_COUNT) { // 检查 inputSlot < 0 || inputSlot >= INPUT_SLOT_COUNT；按条件决定是否执行本分支。
            return null; // 返回 null，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return getInputSlots(mode)[inputSlot]; // 返回 getInputSlots(mode)[inputSlot]，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static SlotPosition getDisplayInputSlotPosition(Mode mode, int inputSlot) { // 定义 getDisplayInputSlotPosition 的调用入口，参数和具体处理见下方实现。
        SlotPosition activePosition = getInputSlotPosition(mode, inputSlot); // 设置 activePosition 为 getInputSlotPosition(mode, inputSlot)。
        return activePosition == null ? HIDDEN_INPUT_SLOTS[inputSlot] : activePosition; // 返回 activePosition == null ? HIDDEN_INPUT_SLOTS[inputSlot] : activePosition，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static SlotPosition[] getInputSlots(Mode mode) { // 定义 getInputSlots 的调用入口，参数和具体处理见下方实现。
        return switch (mode) { // 返回 switch (mode) {，将当前分支的结果交给调用者。
            case DEFAULT, DISASSEMBLE -> ALL_INPUT_SLOTS; // 定义 case DEFAULT, DISASSEMBLE 分支，产生此情况对应的结果。
            case PICKAXE -> PICKAXE_INPUT_SLOTS; // 定义 case PICKAXE 分支，产生此情况对应的结果。
            case SHOVEL -> SHOVEL_INPUT_SLOTS; // 定义 case SHOVEL 分支，产生此情况对应的结果。
            case AXE -> AXE_INPUT_SLOTS; // 定义 case AXE 分支，产生此情况对应的结果。
            case DEBUG -> DEBUG_INPUT_SLOTS; // 定义 case DEBUG 分支，产生此情况对应的结果。
        }; // 结束当前作用域并完成本次表达式或声明。
    } // 结束当前作用域。

    public record SlotPosition(int x, int y) { // 定义 SlotPosition 的数据记录，具体职责见上方说明。
        public int slotX() { // 定义 slotX 的调用入口，参数和具体处理见下方实现。
            return this.x + SLOT_ITEM_OFFSET; // 返回 this.x + SLOT_ITEM_OFFSET，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        public int slotY() { // 定义 slotY 的调用入口，参数和具体处理见下方实现。
            return this.y + SLOT_ITEM_OFFSET; // 返回 this.y + SLOT_ITEM_OFFSET，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
