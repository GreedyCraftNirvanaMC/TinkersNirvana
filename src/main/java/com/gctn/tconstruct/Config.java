package com.gctn.tconstruct;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue SPAWN_WITH_BOOK = BUILDER.define("general.spawnWithBook", true);
    public static final ModConfigSpec.BooleanValue RENDER_TABLE_ITEMS = BUILDER.define("client.renderInventoryInWorld", true);
    public static final ModConfigSpec.BooleanValue LIST_ALL_TABLES = BUILDER.define("client.listAllTables", true);
    public static final ModConfigSpec.BooleanValue LIST_ALL_TOOL_MATERIALS = BUILDER.define("client.listAllToolMaterials", true);
    public static final ModConfigSpec.BooleanValue LIST_ALL_PART_MATERIALS = BUILDER.define("client.listAllPartMaterials", true);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOB_HEAD_DROPS = BUILDER
            .comment("entity_id or #entity_type_tag;include_subclasses;item_id;optional_max_quantity (1..64)")
            .defineListAllowEmpty("tools.mobHeadDrops", List.of("minecraft:skeleton;true;minecraft:skeleton_skull", "minecraft:stray;true;minecraft:skeleton_skull", "minecraft:bogged;true;minecraft:skeleton_skull",
                    "minecraft:wither_skeleton;true;minecraft:wither_skeleton_skull", "minecraft:zombie;true;minecraft:zombie_head", "minecraft:player;false;minecraft:player_head", "minecraft:creeper;true;minecraft:creeper_head",
                    "minecraft:ender_dragon;true;minecraft:dragon_head", "minecraft:snow_golem;false;minecraft:carved_pumpkin", "minecraft:iron_golem;false;minecraft:carved_pumpkin", "minecraft:piglin;true;minecraft:piglin_head"),
                    () -> "minecraft:zombie;true;minecraft:zombie_head", value -> value instanceof String);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_MELTING = BUILDER
            .comment("entity_id or #entity_type_tag;include_subclasses;fluid_id;amount_per_two_damage_mB. Later matches override earlier ones.")
            .defineListAllowEmpty("smeltery.entityMelting", List.of("minecraft:blaze;true;tconstruct:blazing_blood;20", "minecraft:evoker;true;tconstruct:emerald;6", "minecraft:guardian;true;tconstruct:stone;50",
                    "minecraft:illusioner;true;tconstruct:emerald;6", "minecraft:skeleton;true;tconstruct:notmilk;20", "minecraft:skeleton_horse;true;tconstruct:notmilk;20", "minecraft:silverfish;true;tconstruct:stone;25",
                    "minecraft:slime;false;tconstruct:greenslime;25", "minecraft:snow_golem;true;minecraft:water;100", "minecraft:spider;true;tconstruct:venom;20", "minecraft:stray;true;tconstruct:notmilk;20",
                    "minecraft:bogged;true;tconstruct:notmilk;20", "minecraft:villager;true;tconstruct:emerald;6", "minecraft:iron_golem;true;tconstruct:iron;18", "minecraft:vindicator;true;tconstruct:emerald;6",
                    "minecraft:wither_skeleton;true;tconstruct:notmilk;20", "minecraft:zombified_piglin;true;tconstruct:gold;10", "tconstruct:blueslime;false;tconstruct:blueslime;25", "tconstruct:purpleslime;false;tconstruct:purpleslime;25"),
                    () -> "minecraft:zombie;false;tconstruct:blood;20", value -> value instanceof String);

    public static final ModConfigSpec.BooleanValue CHESTS_KEEP_INVENTORY = BUILDER.define("general.chestsKeepInventory", true);
    public static final ModConfigSpec.BooleanValue REUSE_STENCIL = BUILDER.define("general.reuseStencil", true);
    public static final ModConfigSpec.BooleanValue CRAFT_CASTABLE_MATERIALS = BUILDER.define("general.craftCastableMaterials", false);
    public static final ModConfigSpec.BooleanValue FLINT_RECIPE = BUILDER.define("general.addFlintRecipe", true);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CRAFTING_STATION_BLACKLIST = BUILDER
            .comment("Adjacent inventories to exclude by block ID, block entity type ID, or fully qualified class name.")
            .defineListAllowEmpty("general.craftingStationBlacklist", List.of(), () -> "minecraft:chest", value -> value instanceof String);
    public static final ModConfigSpec.BooleanValue DECONSTRUCT_TOOLS = BUILDER.define("tools.deconstructTools", true);
    public static final ModConfigSpec.BooleanValue VANILLA_TOOL_BREAKING = BUILDER.define("tools.vanillaToolBreaking", false);
    public static final ModConfigSpec.BooleanValue OLD_MATTOCK_AND_KAMA = BUILDER.define("tools.oldMattockAndKama", false);
    public static final ModConfigSpec.BooleanValue REPAIR_TOOLS_ON_ANVILS = BUILDER.define("tools.repairToolsOnAnvils", false);
    public static final ModConfigSpec.BooleanValue AUTOSMELT_FORTUNE = BUILDER.define("tools.autosmeltFortuneInteraction", false);
    public static final ModConfigSpec.IntValue PROJECTILE_DESPAWN = BUILDER.defineInRange("tools.despawnProjectile", 1200, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue GEN_SLIME_ISLANDS = BUILDER.define("world.generateSlimeIslands", true);
    public static final ModConfigSpec.BooleanValue ISLANDS_SUPERFLAT = BUILDER.define("world.generateIslandsInSuperflat", false);
    public static final ModConfigSpec.IntValue SLIME_ISLAND_RATE = BUILDER.defineInRange("world.slimeIslandRate", 730, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue MAGMA_ISLAND_RATE = BUILDER.defineInRange("world.magmaIslandRate", 100, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue ISLAND_SPAWN_INTERVAL = BUILDER.defineInRange("world.islandSlimeSpawnInterval", 40, 20, 1200);
    public static final ModConfigSpec.IntValue ISLAND_SLIME_CAP = BUILDER.defineInRange("world.islandSlimeCap", 16, 1, 64);
    public static final ModConfigSpec.IntValue SLIME_OUTSIDE_ISLAND_SECONDS = BUILDER.defineInRange("world.slimeOutsideIslandSeconds", 30, 1, 3600);
    public static final ModConfigSpec.BooleanValue GEN_SLIME_POOLS = BUILDER.define("world.generateSlimePools", false);
    public static final ModConfigSpec.IntValue SLIME_POOL_RATE = BUILDER.defineInRange("world.slimePoolRate", 30, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue MAGMA_POOL_RATE = BUILDER.defineInRange("world.magmaPoolRate", 30, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.IntValue SLIME_POOL_HEIGHT = BUILDER.defineInRange("world.slimePoolHeightMax", 64, 1, 4096);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ISLAND_DIMENSIONS = BUILDER.defineListAllowEmpty("world.slimeIslandDimensions", List.of("minecraft:the_nether", "minecraft:the_end"), () -> "minecraft:the_end", value -> value instanceof String);
    public static final ModConfigSpec.BooleanValue ISLAND_DIMENSIONS_BLACKLIST = BUILDER.define("world.slimeIslandDimensionsIsBlacklist", true);
    public static final ModConfigSpec.BooleanValue ISLANDS_SURFACE_ONLY = BUILDER.define("world.slimeIslandsOnlyGenerateInSurfaceWorlds", true);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> POOL_DIMENSIONS = BUILDER.defineListAllowEmpty("world.slimePoolDimensions", List.of("minecraft:the_nether", "minecraft:the_end"), () -> "minecraft:the_end", value -> value instanceof String);
    public static final ModConfigSpec.BooleanValue POOL_DIMENSIONS_BLACKLIST = BUILDER.define("world.slimePoolDimensionsIsBlacklist", true);
    public static final ModConfigSpec.BooleanValue POOLS_SURFACE_ONLY = BUILDER.define("world.slimePoolsOnlyGenerateInSurfaceWorlds", true);
    public static final ModConfigSpec.BooleanValue GEN_COBALT = BUILDER.define("world.genCobalt", true);
    public static final ModConfigSpec.BooleanValue GEN_ARDITE = BUILDER.define("world.genArdite", true);
    public static final ModConfigSpec.BooleanValue GEN_COPPER = BUILDER.define("world.genCopper", true);
    public static final ModConfigSpec.BooleanValue GEN_TIN = BUILDER.define("world.genTin", true);
    public static final ModConfigSpec.BooleanValue GEN_ALUMINUM = BUILDER.define("world.genAluminum", true);
    public static final ModConfigSpec.IntValue COBALT_RATE = BUILDER.defineInRange("world.cobaltRate", 20, 0, 4096);
    public static final ModConfigSpec.IntValue ARDITE_RATE = BUILDER.defineInRange("world.arditeRate", 20, 0, 4096);
    public static final ModConfigSpec.IntValue COPPER_RATE = BUILDER.defineInRange("world.copperRate", 8, 0, 4096);
    public static final ModConfigSpec.IntValue TIN_RATE = BUILDER.defineInRange("world.tinRate", 8, 0, 4096);
    public static final ModConfigSpec.IntValue ALUMINUM_RATE = BUILDER.defineInRange("world.aluminumRate", 8, 0, 4096);
    public static final ModConfigSpec.IntValue COPPER_MIN_HEIGHT = BUILDER.defineInRange("world.copperHeightMin", 20, -4096, 4096);
    public static final ModConfigSpec.IntValue COPPER_MAX_HEIGHT = BUILDER.defineInRange("world.copperHeightMax", 60, -4096, 4096);
    public static final ModConfigSpec.IntValue TIN_MIN_HEIGHT = BUILDER.defineInRange("world.tinHeightMin", 0, -4096, 4096);
    public static final ModConfigSpec.IntValue TIN_MAX_HEIGHT = BUILDER.defineInRange("world.tinHeightMax", 40, -4096, 4096);
    public static final ModConfigSpec.IntValue ALUMINUM_MIN_HEIGHT = BUILDER.defineInRange("world.aluminumHeightMin", 0, -4096, 4096);
    public static final ModConfigSpec.IntValue ALUMINUM_MAX_HEIGHT = BUILDER.defineInRange("world.aluminumHeightMax", 64, -4096, 4096);
    public static final ModConfigSpec.BooleanValue GEN_VILLAGE_STRUCTURES = BUILDER.define("world.generateVillageStructures", true);
    public static final ModConfigSpec.IntValue VILLAGE_WORKSHOP_WEIGHT = BUILDER.defineInRange("world.villageWorkshopWeight", 30, 1, 150);
    public static final ModConfigSpec.IntValue VILLAGE_SMELTERY_WEIGHT = BUILDER.defineInRange("world.villageSmelteryWeight", 9, 1, 150);
    public static final ModConfigSpec.BooleanValue LEATHER_DRYING = BUILDER.define("gadgets.addLeatherDryingRecipe", true);
    public static final ModConfigSpec.DoubleValue DRYING_TIME_MULTIPLIER = BUILDER.defineInRange("gadgets.dryingTimeMultiplier", 1.0, .00001, 1000);
    public static final ModConfigSpec.BooleanValue LIMIT_PIGGYBACK = BUILDER.define("gadgets.limitPiggybackpack", false);
    public static final ModConfigSpec.IntValue MAX_PIGGYBACK = BUILDER.defineInRange("gadgets.maxPiggybackPassengers", 3, 1, 15);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PRIORITIES = BUILDER
            .comment("A list of ModIds to decide priorities")
            .defineListAllowEmpty("mod_priorities", List.of("tconstruct"), () -> "", obj -> obj instanceof String);

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
    // 原版通透玻璃默认需要精准采集，关闭后普通破坏也会掉落玻璃。
    public static final ModConfigSpec.BooleanValue CLEAR_GLASS_SILK_TOUCH = BUILDER
            .translation("config.tconstruct.clear_glass.silk_touch")
            .define("smeltery.clearGlassSilkTouch", true);
    // 默认混色得到绿色史莱姆块；开启后保留纯绿色配方，混色改为隐藏的粉色块。
    public static final ModConfigSpec.BooleanValue MATCH_VANILLA_SLIME_BLOCK = BUILDER
            .translation("config.tconstruct.slime.match_vanilla")
            .define("general.matchVanillaSlimeblock", false);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MELTING_IGNORE = BUILDER
            .comment("Items or #item_tags ignored when deriving melting values from crafting ingredients.")
            .defineListAllowEmpty("smeltery.oredictMeltingIgnore", List.of("#minecraft:planks", "#c:rods/wooden", "minecraft:stick",
                    "minecraft:string", "minecraft:redstone", "minecraft:chest"), () -> "minecraft:stick", value -> value instanceof String);

    static final ModConfigSpec SPEC = BUILDER.build();
}
