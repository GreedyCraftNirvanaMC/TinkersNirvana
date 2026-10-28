package com.gctn.tconstruct.library.stats; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 手柄提供耐久倍率，以及在乘算完成后加上的固定耐久。
 * Antique 允许固定耐久为负值，例如石制手柄；最终工具耐久会限制在有效范围内。
 */
public class HandleMaterialStats extends AbstractMaterialStats { // 定义 HandleMaterialStats 的实现类型，具体职责见上方说明。
    public final int durability; // 声明 durability，保存后续流程使用的状态或依赖。
    public final float modifer; // 声明 modifer，保存后续流程使用的状态或依赖。
    public HandleMaterialStats(float modifer, int durability) { // 定义 HandleMaterialStats 的调用入口，参数和具体处理见下方实现。
        super("Handle"); // 向当前声明或调用传入 super("Handle")。
        this.modifer = modifer; // 设置 this.modifer 为 modifer。
        this.durability = durability; // 设置 this.durability 为 durability。
    } // 结束当前作用域。
} // 结束当前作用域。
