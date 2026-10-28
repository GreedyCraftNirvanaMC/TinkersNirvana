package com.gctn.tconstruct.tables.data;

/**
 * 工具台模式的固定按钮编号，同时用于存档和菜单数据同步。
 * 新增模式时应保留已有枚举顺序；无效的存档或网络编号返回 null，交由调用者回退。
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
