package com.gctn.tconstruct.library.stats; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 标识 Head、Handle、Extra 等材料属性类别。
 * Material 提供带类型检查的查询，缺少属性或属性类型不匹配时无需进行不安全的强制转换。
 */
public abstract class AbstractMaterialStats { // 定义 AbstractMaterialStats 的实现类型，具体职责见上方说明。
    public final String identifier; // 声明 identifier，保存后续流程使用的状态或依赖。
    public AbstractMaterialStats(String identifier) { // 定义 AbstractMaterialStats 的调用入口，参数和具体处理见下方实现。
        this.identifier = identifier; // 设置 this.identifier 为 identifier。
    } // 结束当前作用域。
} // 结束当前作用域。
