package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.api.TinkerContent;
import com.gctn.tconstruct.gadgets.RemainingGadgets;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipeEvents;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.recipe.PartBuilderRecipes;
import com.gctn.tconstruct.tools.PatternItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

final class RecipeCatalog {
    enum Kind {
        MELTING(SmelteryModule.CONTROLLER), CASTING_TABLE(SmelteryModule.TABLE), CASTING_BASIN(SmelteryModule.BASIN),
        ALLOYING(SmelteryModule.CONTROLLER), FUEL(SmelteryModule.TANK), DRYING(RemainingGadgets.DRYING_RACK),
        PART_BUILDING(WorkshopModule.BLOCKS.get(WorkshopModule.Kind.PART_BUILDER));

        final Supplier<? extends net.minecraft.world.level.ItemLike> station;

        Kind(Supplier<? extends net.minecraft.world.level.ItemLike> station) { this.station = station; }

        String id() { return name().toLowerCase(Locale.ROOT); }
    }

    record Ingredient(List<ItemStack> items, FluidStack fluid) {
        static Ingredient items(List<ItemStack> stacks) { return new Ingredient(stacks, FluidStack.EMPTY); }
        static Ingredient item(ItemStack stack) { return items(List.of(stack)); }
        static Ingredient fluid(FluidStack stack) { return new Ingredient(List.of(), stack.copy()); }
    }

    record Display(List<Ingredient> inputs, List<Ingredient> outputs, Component detail) {}

    private static final String TEXT = "jei.tconsnirvana.";
    private final Map<Kind, List<Display>> entries = new EnumMap<>(Kind.class);

    private RecipeCatalog() {
        for (Kind kind : Kind.values()) { entries.put(kind, new ArrayList<>()); }
    }

    static RecipeCatalog create(RecipeManager recipes, HolderLookup.Provider registries) {
        SmelteryRecipes.initialize();
        TinkerContent.installCastingRecipes();
        var catalog = new RecipeCatalog();
        var candidates = getCandidates();
        catalog.addMelting(candidates, SmelteryRecipeEvents.inferAutomaticMelting(recipes, registries));
        catalog.addCasting(candidates);
        for (var alloy : SmelteryRecipes.ALLOYS) {
            if (!alloy.enabled()) { continue; }
            catalog.add(Kind.ALLOYING, alloy.inputs().stream().map(Ingredient::fluid).toList(),
                    List.of(Ingredient.fluid(alloy.output())), Component.empty());
        }
        for (var fuel : SmelteryRecipes.FUELS) {
            FluidStack stack = new FluidStack(fuel.fluid(), fuel.amount());
            catalog.add(Kind.FUEL, List.of(Ingredient.fluid(stack)), List.of(),
                    Component.translatable(TEXT + "fuel_stats", fuel.duration(), stack.getFluidType().getTemperature(stack)));
        }
        for (var holder : recipes.getAllRecipesFor(RemainingGadgets.DRYING_TYPE.get())) {
            var recipe = holder.value();
            if (!recipe.leather() || Config.LEATHER_DRYING.get()) {
                catalog.add(Kind.DRYING, List.of(Ingredient.items(List.of(recipe.ingredient().getItems()))),
                        List.of(Ingredient.item(recipe.result().copy())), Component.translatable(TEXT + "time", recipe.duration()));
            }
        }
        catalog.addParts(candidates);
        return catalog;
    }

    List<Display> get(Kind kind) { return List.copyOf(entries.get(kind)); }

    private void add(Kind kind, List<Ingredient> inputs, List<Ingredient> outputs, Component detail) {
        entries.get(kind).add(new Display(inputs, outputs, detail));
    }

    private static List<ItemStack> getCandidates() {
        Map<Item, List<ItemStack>> parts = new HashMap<>();
        for (var part : AntiqueParts.entries().values()) {
            for (Material material : TinkerMaterials.materials) {
                if (!material.isHidden() && (material.isCraftable() || material.isCastable())) {
                    ItemStack stack = part.create(material);
                    if (!stack.isEmpty()) { parts.computeIfAbsent(stack.getItem(), item -> new ArrayList<>()).add(stack); }
                }
            }
        }

        List<ItemStack> candidates = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) { continue; }
            var variants = parts.get(item);
            if (variants == null) {
                ItemStack stack = item.getDefaultInstance();
                if (!stack.isEmpty()) { candidates.add(stack); }
            } else {
                candidates.addAll(variants);
            }
        }
        return candidates;
    }

    private record MeltingKey(Fluid fluid, int amount, DataComponentPatch patch, int temperature) {}

    private void addMelting(List<ItemStack> candidates, Map<Item, SmelteryRecipes.Melting> automatic) {
        Map<MeltingKey, List<ItemStack>> groups = new LinkedHashMap<>();
        for (ItemStack stack : candidates) {
            var recipe = SmelteryRecipes.findMelting(stack, automatic);
            if (recipe == null || recipe.output().isEmpty()) { continue; }
            var output = recipe.output();
            var key = new MeltingKey(output.getFluid(), output.getAmount(), output.getComponentsPatch(), recipe.temperature());
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(stack);
        }
        groups.forEach((key, inputs) -> add(Kind.MELTING, List.of(Ingredient.items(inputs)),
                List.of(Ingredient.fluid(new FluidStack(BuiltInRegistries.FLUID.wrapAsHolder(key.fluid()), key.amount(), key.patch()))),
                Component.translatable(TEXT + "temperature", key.temperature())));
    }

    private record CastQuery(boolean basin, Fluid fluid, ItemStack cast) {}

    private record CastingKey(boolean basin, Fluid fluid, int amount, int time, boolean consume,
                              boolean hasCast, Item output, int outputCount, DataComponentPatch components) {}

    private void addCasting(List<ItemStack> candidates) {
        Map<CastingKey, List<ItemStack>> groups = new LinkedHashMap<>();
        Set<CastQuery> shown = new HashSet<>();
        // 先用已注册的谓词找候选，再查询实际生效规则，避免展示被后注册配方覆盖的产物。
        for (var recipe : SmelteryRecipes.CASTING) {
            if (recipe.cast().test(ItemStack.EMPTY)) {
                addCast(groups, shown, recipe.basin(), recipe.fluid(), ItemStack.EMPTY);
            }
            for (ItemStack cast : candidates) {
                if (recipe.cast().test(cast)) {
                    addCast(groups, shown, recipe.basin(), recipe.fluid(), cast);
                }
            }
        }
        Map<Item, ItemStack> byItem = new HashMap<>();
        for (ItemStack candidate : candidates) { byItem.putIfAbsent(candidate.getItem(), candidate); }
        ItemStack bucket = byItem.get(Items.BUCKET);
        var standardCasts = List.of("ingot", "nugget", "plate", "gear").stream()
                .map(form -> byItem.get(SmelteryModule.item("cast_" + form))).toList();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (!fluid.defaultFluidState().isSource()) { continue; }
            addCast(groups, shown, false, fluid, bucket);
            addCast(groups, shown, true, fluid, ItemStack.EMPTY);
            for (ItemStack cast : standardCasts) { addCast(groups, shown, false, fluid, cast); }
        }

        groups.forEach((key, casts) -> {
            List<Ingredient> inputs = new ArrayList<>();
            inputs.add(Ingredient.fluid(new FluidStack(key.fluid(), key.amount())));
            if (key.hasCast()) { inputs.add(Ingredient.items(casts)); }
            Component detail = Component.translatable(TEXT + (key.hasCast() && key.consume() ? "time_consumed" : "time"), key.time());
            ItemStack result = new ItemStack(key.output(), key.outputCount());
            result.applyComponents(key.components());
            add(key.basin() ? Kind.CASTING_BASIN : Kind.CASTING_TABLE, inputs, List.of(Ingredient.item(result)), detail);
        });
    }

    private static void addCast(Map<CastingKey, List<ItemStack>> groups, Set<CastQuery> shown,
                                boolean basin, Fluid fluid, ItemStack cast) {
        if (cast == null || !shown.add(new CastQuery(basin, fluid, cast))) { return; }
        var rule = SmelteryRecipes.findCasting(basin, cast, fluid);
        if (rule == null || rule.amount() <= 0 || rule.output().isEmpty()) { return; }
        var output = rule.output();
        var key = new CastingKey(basin, fluid, rule.amount(), rule.time(), rule.consumeCast(), !cast.isEmpty(),
                output.getItem(), output.getCount(), output.getComponentsPatch());
        groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(cast);
    }

    private record PartYield(int count, int shards) {}

    private void addParts(List<ItemStack> candidates) {
        Map<Material, List<ItemStack>> inputs = new LinkedHashMap<>();
        for (ItemStack stack : candidates) {
            var match = MaterialInputs.match(stack);
            if (match != null && match.material().isCraftable() && !match.material().isHidden()) {
                inputs.computeIfAbsent(match.material(), ignored -> new ArrayList<>()).add(stack);
            }
        }

        for (var part : AntiqueParts.entries().values()) {
            ItemStack pattern = new ItemStack(PatternItem.PATTERN.get());
            CompoundTag data = new CompoundTag();
            data.putString("Pattern", part.id());
            pattern.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
            SimpleContainer inventory = new SimpleContainer(7);
            inventory.setItem(0, pattern);
            for (var stacks : inputs.values()) {
                Map<PartYield, List<ItemStack>> groups = new LinkedHashMap<>();
                Map<PartYield, PartBuilderRecipes.Plan> plans = new HashMap<>();
                for (ItemStack stack : stacks) {
                    inventory.setItem(1, stack.copyWithCount(stack.getMaxStackSize()));
                    var plan = PartBuilderRecipes.planWithRemainder(inventory);
                    if (plan == null) { continue; }
                    var key = new PartYield(plan.counts()[1], plan.remainder().getCount());
                    groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(stack.copyWithCount(key.count()));
                    plans.putIfAbsent(key, plan);
                }

                groups.forEach((key, choices) -> {
                    var plan = plans.get(key);
                    List<Ingredient> outputs = new ArrayList<>();
                    outputs.add(Ingredient.item(plan.result()));
                    if (!plan.remainder().isEmpty()) { outputs.add(Ingredient.item(plan.remainder())); }
                    add(Kind.PART_BUILDING, List.of(Ingredient.item(pattern), Ingredient.items(choices)), outputs, Component.empty());
                });
            }
        }
    }
}
