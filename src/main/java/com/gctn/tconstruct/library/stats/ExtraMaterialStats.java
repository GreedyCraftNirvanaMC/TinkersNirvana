package com.gctn.tconstruct.library.stats; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 额外部件提供的固定耐久，在手柄系数生效前与头部耐久相加。
 * 绑定结不决定挖掘速度，也不决定工具可以使用哪种材料修复。
 */
public class ExtraMaterialStats extends AbstractMaterialStats { // 定义 ExtraMaterialStats 的实现类型，具体职责见上方说明。
    public final int durability; // 声明 durability，保存后续流程使用的状态或依赖。
    public ExtraMaterialStats(int durability) { // 定义 ExtraMaterialStats 的调用入口，参数和具体处理见下方实现。
        super("Extra"); // 向当前声明或调用传入 super("Extra")。
        this.durability = durability; // 设置 this.durability 为 durability。
    } // 结束当前作用域。
} // 结束当前作用域。
