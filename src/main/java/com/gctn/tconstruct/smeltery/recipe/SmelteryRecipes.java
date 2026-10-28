package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public final class SmelteryRecipes {
    private SmelteryRecipes() {}

    public static List<RecipeHolder<SmelteryRecipe>> all(Level level) {
        return level.getRecipeManager().getAllRecipesFor(SmelteryRegistration.RECIPE_TYPE.get());
    }

    public static SmelteryRecipe melting(Level level, ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (var holder : all(level)) {
            var recipe = holder.value();
            if (recipe.operation() == SmelteryRecipe.Operation.MELTING && recipe.ingredient().test(stack)) return recipe;
        }
        return null;
    }

    public static SmelteryRecipe casting(Level level, boolean basin, ItemStack cast, FluidStack fluid) {
        var operation = basin ? SmelteryRecipe.Operation.CASTING_BASIN : SmelteryRecipe.Operation.CASTING_TABLE;
        for (var holder : all(level)) {
            var recipe = holder.value();
            if (recipe.operation() == operation && recipe.matchesCast(cast)
                    && FluidStack.isSameFluidSameComponents(recipe.fluids().getFirst(), fluid)) return recipe;
        }
        return null;
    }
}
