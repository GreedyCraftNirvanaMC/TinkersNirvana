package com.gctn.tconstruct; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.neoforged.neoforge.common.ModConfigSpec; // 引入 net.neoforged.neoforge.common.ModConfigSpec，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 客户端与服务端共用的通用配置。
 * 模组优先级列表为后续兼容功能预留，目前不会改变配方选择结果。
 */
public class Config { // 定义 Config 的实现类型，具体职责见上方说明。
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder(); // 设置 BUILDER 为 new ModConfigSpec.Builder()。

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PRIORITIES = BUILDER // 在 Config 中补充当前声明或表达式的 public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PRIORITIES = BUILDER 部分。
            .comment("A list of ModIds to decide priorities") // 调用 comment，执行当前流程所需的处理。
            .defineList("mod_priorities", List.of("tconstruct"), () -> "", obj -> obj instanceof String); // 调用 defineList，执行当前流程所需的处理。

    static final ModConfigSpec SPEC = BUILDER.build(); // 设置 SPEC 为 BUILDER.build()。
} // 结束当前作用域。
