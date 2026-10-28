package com.gctn.tconstruct.library.stats;

/**
 * 标识 Head、Handle、Extra 等材料属性类别。
 * Material 提供带类型检查的查询，缺少属性或属性类型不匹配时无需进行不安全的强制转换。
 */
public abstract class AbstractMaterialStats {
    public final String identifier;
    public AbstractMaterialStats(String identifier) {
        this.identifier = identifier;
    }
}
