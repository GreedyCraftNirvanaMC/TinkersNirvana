package com.gctn.tconstruct;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PRIORITIES = BUILDER
            .comment("A list of ModIds to decide priorities")
            .defineList("mod_priorities", List.of("tconstruct"), () -> "", obj -> obj instanceof String);

    // Preserve Antique's defaults while allowing the same Smeltery tuning on NeoForge.
    public static final ModConfigSpec.IntValue SMELTERY_HEAT_TICK_RATE = BUILDER
            .defineInRange("smeltery.heatItemsTickrateSmeltery", 4, 1, 20);
    // 焦黑熔炉保持原版独立的加热间隔，避免与冶炼炉的配置互相影响。
    public static final ModConfigSpec.IntValue SEARED_FURNACE_HEAT_TICK_RATE = BUILDER
            .translation("config.tconstruct.seared_furnace.heat_tick_rate")
            .defineInRange("smeltery.heatItemsTickrateSearedFurnace", 4, 1, 20);
    public static final ModConfigSpec.IntValue LIQUID_TRANSFER_RATE = BUILDER
            .defineInRange("smeltery.liquidTransferRate", 6, 1, 144);
    public static final ModConfigSpec.DoubleValue ORE_TO_INGOT_RATIO = BUILDER
            .defineInRange("smeltery.oreToIngotRatio", 2.0, 0.1, 100.0);
    public static final ModConfigSpec.BooleanValue OBSIDIAN_ALLOY = BUILDER.define("smeltery.obsidianAlloy", true);
    public static final ModConfigSpec.BooleanValue CLAY_CASTS = BUILDER.define("smeltery.claycasts", true);
    public static final ModConfigSpec.BooleanValue ALLOW_BRICK_CASTING = BUILDER.define("smeltery.allowBrickCasting", true);
    public static final ModConfigSpec.BooleanValue STEEL_ALLOY = BUILDER.define("smeltery.steelAlloy", true);
    public static final ModConfigSpec.BooleanValue DRAIN_GASEOUS_FLUIDS = BUILDER.define("smeltery.drainGaseousFluids", true);
    public static final ModConfigSpec.IntValue MIN_FLUID_HEIGHT = BUILDER.defineInRange("smeltery.minFluidHeight", 3, 0, 52);
    public static final ModConfigSpec.IntValue MAX_SMELTERY_ITEM_RENDERS = BUILDER.defineInRange("smeltery.maxSmelteryItemRenders", -1, -1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue TEMPERATURE_CELSIUS = BUILDER.define("smeltery.temperatureCelsius", true);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MELTING_IGNORE = BUILDER
            .comment("Items or #item_tags ignored when deriving melting values from crafting ingredients.")
            .defineList("smeltery.oredictMeltingIgnore", List.of("#minecraft:planks", "#c:rods/wooden", "minecraft:stick",
                    "minecraft:string", "minecraft:redstone", "minecraft:chest"), () -> "minecraft:stick", value -> value instanceof String);

    static final ModConfigSpec SPEC = BUILDER.build();
}
