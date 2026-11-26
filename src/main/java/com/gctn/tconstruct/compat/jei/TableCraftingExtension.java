package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.tables.recipe.TableMaterialRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class TableCraftingExtension implements ICraftingCategoryExtension<TableMaterialRecipe> {
    @Override public void setRecipe(RecipeHolder<TableMaterialRecipe> holder, IRecipeLayoutBuilder builder,
                                    ICraftingGridHelper grid, IFocusGroup focuses) {
        var level = Minecraft.getInstance().level;
        if (level == null) { return; }
        var recipe = holder.value();
        var registries = level.registryAccess();
        int width = getWidth(holder);
        int height = getHeight(holder);
        List<List<ItemStack>> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        int materialIndex = -1;
        for (Ingredient ingredient : recipe.getIngredients()) {
            List<ItemStack> choices = Arrays.asList(ingredient.getItems());
            if (materialIndex < 0 && ingredient.equals(recipe.material())) {
                List<ItemStack> materialChoices = new ArrayList<>();
                for (ItemStack stack : choices) {
                    ItemStack preview = recipe.resultForMaterial(stack, registries);
                    if (!preview.isEmpty()) {
                        materialChoices.add(stack);
                        outputs.add(preview);
                    }
                }
                if (!materialChoices.isEmpty()) {
                    materialIndex = inputs.size();
                    choices = materialChoices;
                }
            }
            inputs.add(choices);
        }
        if (outputs.isEmpty()) {
            outputs = Arrays.stream(recipe.material().getItems()).map(stack -> recipe.resultForMaterial(stack, registries))
                    .filter(stack -> !stack.isEmpty()).toList();
        }
        if (outputs.isEmpty()) { outputs = List.of(recipe.getResultItem(registries)); }
        var slots = grid.createAndSetInputs(builder, inputs, width, height);
        var output = grid.createAndSetOutputs(builder, outputs);
        if (materialIndex >= 0 && inputs.get(materialIndex).size() == outputs.size()) {
            // JEI 的合成模板与产物一起换页，按决定外观的第一个原料格建立关联。
            int rowOffset = height == 1 || width == 3 && height == 2 ? 1 : 0;
            int columnOffset = width == 1 ? 1 : 0;
            int slot = 3 * (materialIndex / width + rowOffset) + materialIndex % width + columnOffset;
            builder.createFocusLink(slots.get(slot), output);
        }
    }

    // JEI 的默认扩展只从原生有序配方读取宽高，工作台配方需要显式提供内部模板的尺寸。
    @Override public int getWidth(RecipeHolder<TableMaterialRecipe> holder) { return holder.value().base().getWidth(); }

    @Override public int getHeight(RecipeHolder<TableMaterialRecipe> holder) { return holder.value().base().getHeight(); }
}
