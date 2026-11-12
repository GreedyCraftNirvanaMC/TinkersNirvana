/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.SmelteryTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** The original quantities and temperature/cooling rules, independent of a recipe's item source. */
public final class SmelteryRecipes {
    public static final int INGOT = 144;
    public static final int NUGGET = 16;
    public static final int BLOCK = 1296;
    public static final int GEM = 666;
    public static final List<Melting> MELTING = new ArrayList<>();
    public static final List<Casting> CASTING = new ArrayList<>();
    public static final List<Alloy> ALLOYS = new ArrayList<>();
    public static final List<Fuel> FUELS = new ArrayList<>();
    public static final List<EntityMelting> ENTITY_MELTING = new ArrayList<>();
    public static final java.util.Map<Item, Melting> AUTOMATIC_MELTING = new java.util.HashMap<>();
    private static boolean initialized;

    private SmelteryRecipes() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        CASTING.add(new Casting(false, stack -> stack.is(com.gctn.tconstruct.gadgets.Spaghetti.HARD.get()), Fluids.WATER, 3000,
                new ItemStack(com.gctn.tconstruct.gadgets.Spaghetti.SOGGY.get()), 18000, true, false));
        FUELS.add(new Fuel(Fluids.LAVA, 50, 100));
        FUELS.add(new Fuel(SmelteryFluids.get("blazing_blood"), 50, 150));
        for (String name : SmelteryFluids.ENTRIES.keySet()) {
            Fluid fluid = SmelteryFluids.get(name);
            if (List.of("blood", "venom", "blazing_blood", "greenslime", "blueslime", "purpleslime", "stone", "dirt", "clay", "notmilk", "obsidian", "glass", "emerald", "diamond").contains(name)) {
                continue;
            }
            // Common tags let this port use already-installed materials without depending on their mods.
            for (var form : List.of(new Form("ingots", INGOT, "ingot"), new Form("nuggets", NUGGET, "nugget"),
                    new Form("storage_blocks", BLOCK, "block"), new Form("ores", INGOT * 2, "ore"),
                    new Form("raw_materials", INGOT * 2, "raw"), new Form("dusts", INGOT, "dust"),
                    new Form("plates", INGOT, "plate"), new Form("gears", INGOT * 4, "gear"))) {
                Predicate<ItemStack> matches = stack -> stack.is(TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("c", form.tag + "/" + name))) || isNamed(stack, name, form.suffix);
                MELTING.add(new Melting(matches, new FluidStack(fluid, form.amount), temperature(fluid, form.amount), form.suffix.equals("ore") || form.suffix.equals("raw")));
                if (List.of("ingot", "nugget", "block", "plate", "gear").contains(form.suffix)) {
                    ItemStack output = preferred(name, form.suffix);
                    if (!output.isEmpty()) {
                        String cast = form.suffix.equals("block") ? "" : "cast_" + form.suffix;
                        CASTING.add(casting(form.suffix.equals("block"), castPredicate(cast), fluid, form.amount, output, false, false));
                    }
                }
            }
        }
        addMelting(Items.ICE, Fluids.WATER, 1000, 305);
        addMelting(Items.PACKED_ICE, Fluids.WATER, 2000, 310);
        addMelting(Items.SNOW_BLOCK, Fluids.WATER, 1000, 305);
        addMelting(Items.SNOWBALL, Fluids.WATER, 125, 301);
        addMelting(Items.ROTTEN_FLESH, SmelteryFluids.get("blood"), 40);
        addMelting(Items.SPIDER_EYE, SmelteryFluids.get("venom"), 40);
        addMelting(Items.PUFFERFISH, SmelteryFluids.get("venom"), 40);
        addMelting(Items.SLIME_BALL, SmelteryFluids.get("greenslime"), 250);
        addMelting(Items.SLIME_BLOCK, SmelteryFluids.get("greenslime"), 2250);
        CASTING.add(new Casting(false, ItemStack::isEmpty, SmelteryFluids.get("greenslime"), 250, new ItemStack(Items.SLIME_BALL), 50, false, false));
        // 保留原版仅对绿、蓝、紫和血液注册的熔炼与浇铸，不把熔岩或粉色史莱姆误作流体。
        for (String color : new String[]{"green", "blue", "purple", "blood"}) {
            String fluidName = switch (color) {
                case "green" -> "greenslime";
                case "blue" -> "blueslime";
                case "purple" -> "purpleslime";
                default -> "blood";
            };
            var fluid = SmelteryFluids.get(fluidName);
            var congealed = com.gctn.tconstruct.common.slime.SlimeModule.CONGEALED.get(color).get();
            addMelting(congealed.asItem(), fluid, 1000);
            CASTING.add(new Casting(true, ItemStack::isEmpty, fluid, 1000, new ItemStack(congealed), 100, false, false));
            if (!color.equals("green")) {
                var ball = com.gctn.tconstruct.common.slime.SlimeModule.ball(color);
                addMelting(ball, fluid, 250);
                CASTING.add(new Casting(false, ItemStack::isEmpty, fluid, 250, new ItemStack(ball), 50, false, false));
                if (!color.equals("blood")) {
                    addMelting(com.gctn.tconstruct.common.slime.SlimeModule.fullBlock(color).asItem(), fluid, 2250);
                }
            }
        }
        special("obsidian", Items.OBSIDIAN, 288, "", true, -1);
        addMelting(Items.CLAY_BALL, SmelteryFluids.get("clay"), 144);
        addMelting(Items.CLAY, SmelteryFluids.get("clay"), 576);
        CASTING.add(casting(false, stack -> com.gctn.tconstruct.Config.ALLOW_BRICK_CASTING.get() && castPredicate("cast_ingot").test(stack),
                SmelteryFluids.get("clay"), 144, new ItemStack(Items.BRICK), false, false));
        CASTING.add(casting(true, ItemStack::isEmpty, SmelteryFluids.get("clay"), 576, new ItemStack(Items.TERRACOTTA), false, false));
        CASTING.add(new Casting(true, stack -> stack.is(net.minecraft.tags.ItemTags.TERRACOTTA) && !stack.is(Items.TERRACOTTA),
                Fluids.WATER, 250, new ItemStack(Items.TERRACOTTA), 150, true, false));
        special("diamond", Items.DIAMOND, GEM, "cast_gem", false, -1);
        special("diamond", Items.DIAMOND_BLOCK, GEM * 9, "", true, -1);
        special("emerald", Items.EMERALD, GEM, "cast_gem", false, -1);
        special("emerald", Items.EMERALD_BLOCK, GEM * 9, "", true, -1);
        for (String gem : new String[]{"diamond", "emerald"}) {
            MELTING.add(new Melting(stack -> stack.is(TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", "ores/" + gem))), new FluidStack(SmelteryFluids.get(gem), GEM * 2),
                    temperature(SmelteryFluids.get(gem), GEM * 2), true));
        }
        MELTING.add(melting(stack -> stack.is(net.minecraft.tags.ItemTags.SAND), SmelteryFluids.get("glass"), 1000));
        // 保留未加载标签时可查询的基础配方，随后用公共标签覆盖世界内的普通染色玻璃。
        addMelting(Items.GLASS, SmelteryFluids.get("glass"), 1000);
        addMelting(SmelteryModule.CLEAR_GLASS.get().asItem(), SmelteryFluids.get("glass"), 1000);
        // 现代 cheap 标签对应原版普通及染色玻璃，不包含新增的遮光玻璃。
        MELTING.add(melting(stack -> stack.is(TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "glass_blocks/cheap"))), SmelteryFluids.get("glass"), 1000));
        CASTING.add(new Casting(true, ItemStack::isEmpty, SmelteryFluids.get("glass"), 1000, new ItemStack(SmelteryModule.CLEAR_GLASS.get()), 120, false, false));
        special("glass", Items.GLASS_PANE, 375, "", false, 50);
        special("notmilk", Items.BONE, 144, "", false, 50);
        special("notmilk", Items.BONE_BLOCK, 432, "", true, 100);
        addMelting(Items.BONE_MEAL, SmelteryFluids.get("notmilk"), 48);
        MELTING.add(new Melting(stack -> stack.is(Items.DIRT) || stack.is(Items.COARSE_DIRT), new FluidStack(SmelteryFluids.get("dirt"), 144),
                temperature(SmelteryFluids.get("dirt"), 576)));
        addMelting(SmelteryModule.item("mud_brick"), SmelteryFluids.get("dirt"), 144);
        addMelting(SmelteryModule.MUD_BRICKS.get().asItem(), SmelteryFluids.get("dirt"), 576);
        CASTING.add(casting(false, castPredicate("cast_ingot"), SmelteryFluids.get("dirt"), 144,
                new ItemStack(SmelteryModule.item("mud_brick")), false, false));
        CASTING.add(new Casting(true, stack -> stack.is(net.minecraft.tags.ItemTags.PLANKS), Fluids.LAVA, 250,
                new ItemStack(SmelteryModule.LAVAWOOD.get()), 100, true, false));
        CASTING.add(casting(true, stack -> stack.is(Items.SAND), SmelteryFluids.get("blood"), 10, new ItemStack(Items.RED_SAND), true, false));
        CASTING.add(casting(false, stack -> com.gctn.tconstruct.Config.STEEL_ALLOY.get() && stack.is(Items.IRON_INGOT),
                SmelteryFluids.get("blazing_blood"), 100, new ItemStack(SmelteryModule.item("steel_ingot")), true, false));
        CASTING.add(casting(false, stack -> com.gctn.tconstruct.Config.STEEL_ALLOY.get() && stack.is(Items.IRON_BLOCK),
                SmelteryFluids.get("blazing_blood"), 900, preferred("steel", "block"), true, false));
        for (String type : new String[]{"stone", "cobble", "paver", "brick", "brick_cracked", "brick_fancy", "brick_square",
                "brick_triangle", "brick_small", "road", "tile", "creeper"}) {
            addMelting(SmelteryModule.BLOCKS.get(type.equals("brick") ? "seared_bricks" : "seared_" + type).get().asItem(), SmelteryFluids.get("stone"), 288);
        }
        addMelting(SmelteryModule.item("seared_brick"), SmelteryFluids.get("stone"), 72);
        addMelting(SmelteryModule.LADDER.get().asItem(), SmelteryFluids.get("stone"), 288);
        MELTING.add(new Melting(stack -> stack.is(Items.COBBLESTONE) || stack.is(Items.STONE),
                new FluidStack(SmelteryFluids.get("stone"), 72), temperature(SmelteryFluids.get("stone"), 288)));
        MELTING.add(new Melting(stack -> stack.is(SmelteryModule.item("grout")),
                new FluidStack(SmelteryFluids.get("stone"), 72), temperature(SmelteryFluids.get("stone"), 24)));
        CASTING.add(casting(false, castPredicate("cast_ingot"), SmelteryFluids.get("stone"), 72,
                new ItemStack(SmelteryModule.item("seared_brick")), false, false));
        CASTING.add(casting(true, ItemStack::isEmpty, SmelteryFluids.get("stone"), 288,
                new ItemStack(SmelteryModule.BLOCKS.get("seared_stone").get()), false, false));
        CASTING.add(casting(true, stack -> stack.is(Items.COBBLESTONE), SmelteryFluids.get("stone"), 216,
                new ItemStack(SmelteryModule.BLOCKS.get("seared_cobble").get()), true, false));
        CASTING.add(casting(true, stack -> stack.is(TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "glass_blocks"))), SmelteryFluids.get("stone"), 288,
                new ItemStack(SmelteryModule.GLASS.get()), true, true));
        addMelting(Items.IRON_HORSE_ARMOR, SmelteryFluids.get("iron"), INGOT * 4);
        addMelting(Items.GOLDEN_HORSE_ARMOR, SmelteryFluids.get("gold"), INGOT * 4);
        addMelting(Items.RAIL, SmelteryFluids.get("iron"), INGOT * 6 / 16);
        addMelting(Items.ACTIVATOR_RAIL, SmelteryFluids.get("iron"), INGOT);
        addMelting(Items.DETECTOR_RAIL, SmelteryFluids.get("iron"), INGOT);
        addMelting(Items.POWERED_RAIL, SmelteryFluids.get("gold"), INGOT);
        addMetalEquipment("iron", "iron"); addMetalEquipment("golden", "gold"); addMetalEquipment("chainmail", "iron");
        for (String fluid : new String[]{"gold", "alubrass", "brass"}) {
            int amount = fluid.equals("gold") ? 288 : 144;
            for (String form : new String[]{"ingot", "nugget", "gem"}) {
                Predicate<ItemStack> pattern = switch (form) {
                    case "gem" -> stack -> stack.is(Items.DIAMOND) || stack.is(Items.EMERALD);
                    case "nugget" -> stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_nugget")
                            || stack.getTags().anyMatch(tag -> tag.location().getPath().startsWith("nuggets/"));
                    default -> stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_ingot")
                            || stack.getTags().anyMatch(tag -> tag.location().getPath().startsWith("ingots/")) || stack.is(Items.BRICK)
                            || stack.is(Items.NETHER_BRICK) || stack.is(SmelteryModule.item("seared_brick"));
                };
                CASTING.add(casting(false, pattern, SmelteryFluids.get(fluid), amount,
                        new ItemStack(SmelteryModule.item("cast_" + form)), true, true));
            }
            CASTING.add(casting(false, ItemStack::isEmpty, SmelteryFluids.get(fluid), amount,
                    new ItemStack(SmelteryModule.item("cast_blank")), false, false));
        }
        for (var material : TinkerMaterials.materials) {
            String name = material.identifier;
            if (!material.isCastable() || !SmelteryFluids.ENTRIES.containsKey(name)) { continue; }
            for (var part : com.gctn.tconstruct.library.toolparts.AntiqueParts.entries().values()) {
                if (part.external()) { continue; }
                if (!java.util.Set.of("Head", "Handle", "Extra", "Bow").contains(part.stat()) || !material.hasStats(part.stat())) { continue; }
                ItemStack output = part.create(material);
                int cost = Math.round(part.cost() * INGOT);
                MELTING.add(melting(stack -> stack.is(output.getItem()) && TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL).equals(name), SmelteryFluids.get(name), cost));
                CASTING.add(casting(false, castPredicate("cast_" + part.id()), SmelteryFluids.get(name), cost, output, false, false));
                CASTING.add(casting(false, stack -> com.gctn.tconstruct.Config.CLAY_CASTS.get() && stack.is(SmelteryModule.item("clay_cast_" + part.id())),
                        SmelteryFluids.get(name), cost, output, true, false));
            }
            // 弩箭核心必须先有箭杆，再包覆可浇筑材料。
            for (var shaft : TinkerMaterials.materials) {
                if (!shaft.hasStats("Shaft")) { continue; }
                ItemStack core = com.gctn.tconstruct.library.toolparts.AntiqueParts.get("bolt_core").create(shaft);
                var data = TinkerDataComponents.getMaterialTag(core);
                data.putString("Coating", name);
                core.set(TinkerDataComponents.TINKER_MATERIAL.get(), net.minecraft.world.item.component.CustomData.of(data));
                CASTING.add(casting(false, stack -> stack.is(com.gctn.tconstruct.library.toolparts.AntiqueParts.get("arrow_shaft").item().get())
                                && TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL).equals(shaft.identifier),
                        SmelteryFluids.get(name), INGOT * 2, core, true, false));
            }
        }
        for (var part : com.gctn.tconstruct.library.toolparts.AntiqueParts.entries().values()) {
            if (part.external()) { continue; }
            if (!java.util.Set.of("Head", "Handle", "Extra", "Bow").contains(part.stat())) { continue; }
            Item item = part.item().get();
            for (String fluid : new String[]{"gold", "alubrass", "brass"}) {
                CASTING.add(casting(false, stack -> stack.is(item), SmelteryFluids.get(fluid), fluid.equals("gold") ? 288 : 144,
                        new ItemStack(SmelteryModule.item("cast_" + part.id())), true, true));
            }
            CASTING.add(casting(false, stack -> com.gctn.tconstruct.Config.CLAY_CASTS.get() && stack.is(item),
                    SmelteryFluids.get("clay"), 288, new ItemStack(SmelteryModule.item("clay_cast_" + part.id())), true, true));
        }
        CASTING.add(casting(false, stack -> stack.is(Items.BONE), SmelteryFluids.get("blood"), 250,
                new ItemStack(com.gctn.tconstruct.tools.ToolSupplies.item("bloodbone")), true, false));
        CASTING.add(casting(false, stack -> stack.is(Items.BONE), SmelteryFluids.get("venom"), 250,
                new ItemStack(com.gctn.tconstruct.tools.ToolSupplies.item("venomous_bone")), true, false));
        CASTING.add(new Casting(false, stack -> stack.is(com.gctn.tconstruct.tools.ToolSupplies.item("necrotic_bone")), SmelteryFluids.get("blazing_blood"), 250,
                new ItemStack(com.gctn.tconstruct.tools.ToolSupplies.item("blazing_bone")), 12, true, false));
        alloy("obsidian", 36, new FluidStack(Fluids.WATER, 125), new FluidStack(Fluids.LAVA, 125));
        alloy("clay", 144, fluid("stone", 72), fluid("dirt", 144), new FluidStack(Fluids.WATER, 250));
        alloy("pigiron", 144, fluid("iron", 144), fluid("blood", 40), fluid("clay", 72));
        alloy("knightslime", 72, fluid("iron", 72), fluid("purpleslime", 125), fluid("stone", 144));
        alloy("manyullyn", 2, fluid("cobalt", 2), fluid("ardite", 2));
        alloy("bronze", 4, fluid("copper", 3), fluid("tin", 1));
        alloy("electrum", 2, fluid("gold", 1), fluid("silver", 1));
        alloy("alubrass", 4, fluid("copper", 1), fluid("aluminum", 3));
        alloy("brass", 3, fluid("copper", 2), fluid("zinc", 1));
        alloy("alumite", 144, fluid("aluminum", 144), fluid("iron", 144), fluid("obsidian", 288));
    }

    public static Melting findMelting(ItemStack stack) {
        initialize();
        // Later registrations override built-ins, matching integrations' ability to replace recipes.
        for (int i = MELTING.size() - 1; i >= 0; i--) {
            if (MELTING.get(i).input.test(stack)) {
                Melting recipe = MELTING.get(i);
                if (recipe.ore) {
                    int amount = (int) (recipe.output.getAmount() / 2.0 * com.gctn.tconstruct.Config.ORE_TO_INGOT_RATIO.get());
                    return melting(recipe.input, recipe.output.getFluid(), amount);
                }
                return recipe;
            }
        }
        return AUTOMATIC_MELTING.get(stack.getItem());
    }

    public static Fuel fuel(FluidStack stack) {
        initialize();
        return FUELS.stream().filter(fuel -> stack.is(fuel.fluid) && stack.getAmount() >= fuel.amount).findFirst().orElse(null);
    }

    public static FluidStack entityMelting(net.minecraft.world.entity.LivingEntity entity) {
        initialize();
        for (int i = ENTITY_MELTING.size() - 1; i >= 0; i--) {
            if (ENTITY_MELTING.get(i).input.test(entity)) {
                return ENTITY_MELTING.get(i).output.copy();
            }
        }
        FluidStack configured = ConfiguredEntityMelting.find(entity);
        if (!configured.isEmpty()) { return configured; }
        return fluid("blood", 20);
    }

    public static Casting findCasting(boolean basin, ItemStack cast, Fluid fluid) {
        initialize();
        if (!basin && cast.is(Items.BUCKET) && fluid.getBucket() != Items.AIR) {
            return casting(false, stack -> stack.is(Items.BUCKET), fluid, 1000, new ItemStack(fluid.getBucket()), true, false);
        }
        for (int i = CASTING.size() - 1; i >= 0; i--) {
            Casting recipe = CASTING.get(i);
            if (recipe.basin == basin && recipe.fluid == fluid && recipe.cast.test(cast)) {
                return recipe;
            }
        }
        if (!basin && (fluid == SmelteryFluids.get("gold") || fluid == SmelteryFluids.get("alubrass") || fluid == SmelteryFluids.get("brass"))) {
            for (String form : new String[]{"plate", "gear"}) {
                if (BuiltInRegistries.ITEM.getKey(cast.getItem()).getPath().endsWith("_" + form)
                        || cast.getTags().anyMatch(tag -> tag.location().getPath().startsWith(form + "s/"))) {
                    return casting(false, stack -> true, fluid, fluid == SmelteryFluids.get("gold") ? 288 : 144,
                            new ItemStack(SmelteryModule.item("cast_" + form)), true, true);
                }
            }
        }
        String material = SmelteryFluids.ENTRIES.values().stream().filter(entry -> entry.source.get() == fluid)
                .map(entry -> entry.name).findFirst().orElse("");
        for (String form : new String[]{"ingot", "nugget", "block", "plate", "gear"}) {
            if (basin != form.equals("block") || !(basin ? cast.isEmpty() : cast.is(SmelteryModule.item("cast_" + form)))) {
                continue;
            }
            ItemStack result = preferred(material, form);
            if (!result.isEmpty()) {
                int amount = form.equals("block") ? BLOCK : form.equals("nugget") ? NUGGET : form.equals("gear") ? INGOT * 4 : INGOT;
                return casting(basin, stack -> true, fluid, amount, result, false, false);
            }
        }
        return null;
    }

    private static boolean isNamed(ItemStack stack, String material, String suffix) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getPath().equals(material + "_" + suffix) || id.getPath().equals("deepslate_" + material + "_" + suffix)
                || suffix.equals("raw") && id.getPath().equals("raw_" + material);
    }

    private static ItemStack preferred(String material, String suffix) {
        for (String namespace : new String[]{"minecraft", TinkersNirvana.MODID}) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, material + "_" + suffix));
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        String tag = switch (suffix) {
            case "ingot" -> "ingots";
            case "nugget" -> "nuggets";
            case "block" -> "storage_blocks";
            case "plate" -> "plates";
            case "gear" -> "gears";
            default -> "";
        };
        if (!tag.isEmpty()) {
            var items = BuiltInRegistries.ITEM.getTag(TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("c", tag + "/" + material)));
            if (items.isPresent() && items.get().size() > 0) {
                return new ItemStack(items.get().get(0).value());
            }
        }
        return ItemStack.EMPTY;
    }

    private static void addMetalEquipment(String prefix, String fluid) {
        String[] names = {"helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"};
        int[] amounts = {5, 8, 7, 4, 2, 3, 3, 1, 2};
        for (int i = 0; i < names.length; i++) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(prefix + "_" + names[i]));
            if (item != Items.AIR) {
                addMelting(item, SmelteryFluids.get(fluid), amounts[i] * INGOT);
            }
        }
    }

    private static void special(String fluid, Item item, int amount, String cast, boolean basin, int time) {
        addMelting(item, SmelteryFluids.get(fluid), amount);
        Casting recipe = casting(basin, castPredicate(cast), SmelteryFluids.get(fluid), amount, new ItemStack(item), false, false);
        CASTING.add(time < 0 ? recipe : new Casting(basin, recipe.cast, recipe.fluid, amount, recipe.output, time, false, false));
    }

    private static Predicate<ItemStack> castPredicate(String name) {
        return name.isEmpty() ? ItemStack::isEmpty : stack -> stack.is(SmelteryModule.item(name));
    }

    private static void addMelting(Item item, Fluid fluid, int amount) {
        MELTING.add(melting(stack -> stack.is(item), fluid, amount));
    }

    private static void addMelting(Item item, Fluid fluid, int amount, int temperature) {
        MELTING.add(new Melting(stack -> stack.is(item), new FluidStack(fluid, amount), temperature));
    }

    public static Melting melting(Predicate<ItemStack> input, Fluid fluid, int amount) {
        return new Melting(input, new FluidStack(fluid, amount), temperature(fluid, amount));
    }

    public static int temperature(Fluid fluid, int amount) {
        return 300 + (int) (Math.pow((double) amount / BLOCK, 0.31546487678) * Math.max(0, fluid.getFluidType().getTemperature() - 300));
    }

    public static Casting casting(boolean basin, Predicate<ItemStack> cast, Fluid fluid, int amount, ItemStack output, boolean consume, boolean swap) {
        return new Casting(basin, cast, fluid, amount, output, 24 + (fluid.getFluidType().getTemperature() - 300) * amount / 1600, consume, swap);
    }

    private static FluidStack fluid(String name, int amount) {
        return new FluidStack(SmelteryFluids.get(name), amount);
    }

    private static void alloy(String name, int amount, FluidStack... inputs) {
        ALLOYS.add(new Alloy(fluid(name, amount), List.of(inputs)));
    }

    public record Melting(Predicate<ItemStack> input, FluidStack output, int temperature, boolean ore) {
        public Melting(Predicate<ItemStack> input, FluidStack output, int temperature) {
            this(input, output, temperature, false);
        }
        public int heat() {
            return Math.max(5, temperature - 300);
        }
    }

    public record Casting(boolean basin, Predicate<ItemStack> cast, Fluid fluid, int amount, ItemStack output,
                          int time, boolean consumeCast, boolean switchOutputs) {
    }

    public record Fuel(Fluid fluid, int amount, int duration) {
    }

    public record EntityMelting(Predicate<net.minecraft.world.entity.LivingEntity> input, FluidStack output) {
    }

    public record Alloy(FluidStack output, List<FluidStack> inputs) {
        public Alloy {
            if (output.isEmpty() || inputs.isEmpty() || inputs.stream().anyMatch(FluidStack::isEmpty)) {
                throw new IllegalArgumentException("An alloy requires nonempty input and output fluids");
            }
            output = output.copy();
            List<FluidStack> merged = new ArrayList<>();
            // Merge duplicate ingredients before simulation, so the eventual drain remains atomic.
            for (FluidStack input : inputs) {
                FluidStack existing = merged.stream().filter(fluid -> FluidStack.isSameFluidSameComponents(fluid, input)).findFirst().orElse(null);
                if (existing == null) {
                    merged.add(input.copy());
                } else {
                    existing.setAmount(Math.addExact(existing.getAmount(), input.getAmount()));
                }
            }
            inputs = List.copyOf(merged);
        }

        public int matches(SmelteryTank tank) {
            int count = Integer.MAX_VALUE;
            for (FluidStack input : inputs) {
                int amount = tank.fluids().stream().filter(fluid -> FluidStack.isSameFluidSameComponents(fluid, input))
                        .mapToInt(FluidStack::getAmount).sum();
                count = Math.min(count, amount / input.getAmount());
            }
            return count;
        }
    }

    private record Form(String tag, int amount, String suffix) {
    }
}
