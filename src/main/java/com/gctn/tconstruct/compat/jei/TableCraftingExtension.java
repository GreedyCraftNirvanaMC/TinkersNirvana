package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.tables.recipe.TableMaterialRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

final class TableCraftingExtension implements ICraftingCategoryExtension<TableMaterialRecipe> {
    @Override public void setRecipe(RecipeHolder<TableMaterialRecipe> holder, IRecipeLayoutBuilder builder,
                                    ICraftingGridHelper grid, IFocusGroup focuses) {
        var level = Minecraft.getInstance().level;
        if (level == null) { return; }
        var recipe = holder.value();
        grid.createAndSetOutputs(builder, List.of(recipe.getResultItem(level.registryAccess())));
        grid.createAndSetIngredients(builder, recipe.getIngredients(), getWidth(holder), getHeight(holder));
    }

    // JEI 的默认扩展只从原生有序配方读取宽高，工作台配方需要显式提供内部模板的尺寸。
    @Override public int getWidth(RecipeHolder<TableMaterialRecipe> holder) { return holder.value().base().getWidth(); }

    @Override public int getHeight(RecipeHolder<TableMaterialRecipe> holder) { return holder.value().base().getHeight(); }
}
