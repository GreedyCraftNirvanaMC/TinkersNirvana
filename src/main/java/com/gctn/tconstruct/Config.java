package com.gctn.tconstruct;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Common configuration shared by both logical sides.
 * The mod-priority list reserves an integration preference; it currently does not alter recipe selection.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PRIORITIES = BUILDER
            .comment("A list of ModIds to decide priorities")
            .defineList("mod_priorities", List.of("tconstruct"), () -> "", obj -> obj instanceof String);

    static final ModConfigSpec SPEC = BUILDER.build();
}
