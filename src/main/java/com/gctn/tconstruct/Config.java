package com.gctn.tconstruct;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * 客户端与服务端共用的通用配置。
 * 模组优先级列表为后续兼容功能预留，目前不会改变配方选择结果。
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PRIORITIES = BUILDER
            .comment("A list of ModIds to decide priorities")
            .defineList("mod_priorities", List.of("tconstruct"), () -> "", obj -> obj instanceof String);

    static final ModConfigSpec SPEC = BUILDER.build();
}
