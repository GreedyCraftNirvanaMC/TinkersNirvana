package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

/**
 * Reload-aware recipe lookup shared by smeltery inventories and casting blocks.
 * Recipes are indexed by operation; stack-sensitive ingredients still run their native matching logic.
 * The index is replaced when Minecraft replaces its recipe map, including same-size data-pack reloads.
 */
public final class SmelteryRecipes {
    // Weak keys release old worlds/managers after disconnect. An integrated
    // server has both client and server threads, so cache publication is locked.
    private static final Map<RecipeManager, Index> INDEXES = new WeakHashMap<>();

    private SmelteryRecipes() {}

    public static List<RecipeHolder<SmelteryRecipe>> all(Level level) {
        return index(level).all();
    }

    private static Index index(Level level) {
        return index(level.getRecipeManager());
    }

    private static synchronized Index index(RecipeManager manager) {
        Collection<RecipeHolder<?>> snapshot = manager.getRecipes();
        Index index = INDEXES.get(manager);
        // In Minecraft 1.21.1 this is the values view of an immutable map that is
        // replaced by apply/replaceRecipes. Comparing its identity detects even
        // same-size reloads without copying/scanning every recipe on every tick.
        if (index == null || index.snapshot() != snapshot) {
            var all = manager.getAllRecipesFor(SmelteryRegistration.RECIPE_TYPE.get());
            Map<SmelteryRecipe.Operation, List<SmelteryRecipe>> grouped = new EnumMap<>(SmelteryRecipe.Operation.class);
            for (var operation : SmelteryRecipe.Operation.values()) {
                grouped.put(operation, all.stream().map(RecipeHolder::value)
                        .filter(recipe -> recipe.operation() == operation).toList());
            }
            index = new Index(snapshot, List.copyOf(all), Map.copyOf(grouped));
            INDEXES.put(manager, index);
        }
        return index;
    }

    public static SmelteryRecipe melting(Level level, ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (var recipe : index(level).byOperation().get(SmelteryRecipe.Operation.MELTING)) {
            // Do not cache by item ID: ingredients may depend on stack components.
            if (recipe.ingredient().test(stack)) return recipe;
        }
        return null;
    }

    public static SmelteryRecipe casting(Level level, boolean basin, ItemStack cast, FluidStack fluid) {
        var operation = basin ? SmelteryRecipe.Operation.CASTING_BASIN : SmelteryRecipe.Operation.CASTING_TABLE;
        for (var recipe : index(level).byOperation().get(operation)) {
            if (recipe.matchesCast(cast)
                    && FluidStack.isSameFluidSameComponents(recipe.fluids().getFirst(), fluid)) return recipe;
        }
        return null;
    }

    private record Index(Collection<RecipeHolder<?>> snapshot, List<RecipeHolder<SmelteryRecipe>> all,
                         Map<SmelteryRecipe.Operation, List<SmelteryRecipe>> byOperation) {}
}
