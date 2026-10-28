package com.gctn.tconstruct.tables.data; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 工具台模式的固定按钮编号，同时用于存档和菜单数据同步。
 * 新增模式时应保留已有枚举顺序；无效的存档或网络编号返回 null，交由调用者回退。
 */
public enum Mode { // 定义 Mode 的固定枚举取值，具体职责见上方说明。
    DEFAULT, // 向当前声明或调用传入 DEFAULT。
    DISASSEMBLE, // 向当前声明或调用传入 DISASSEMBLE。
    PICKAXE, // 向当前声明或调用传入 PICKAXE。
    SHOVEL, // 向当前声明或调用传入 SHOVEL。
    AXE, // 向当前声明或调用传入 AXE。
    DEBUG; // 在 Mode 中补充当前声明或表达式的 DEBUG 部分。

    public int getButtonId() { // 定义 getButtonId 的调用入口，参数和具体处理见下方实现。
        return this.ordinal(); // 返回 this.ordinal()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static Mode byButtonId(int id) { // 定义 byButtonId 的调用入口，参数和具体处理见下方实现。
        Mode[] modes = values(); // 设置 modes 为 values()。
        if (id < 0 || id >= modes.length) { // 检查 id < 0 || id >= modes.length；按条件决定是否执行本分支。
            return null; // 返回 null，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return modes[id]; // 返回 modes[id]，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
