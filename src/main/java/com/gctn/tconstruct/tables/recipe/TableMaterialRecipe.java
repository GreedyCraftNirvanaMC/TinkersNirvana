/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's TableRecipeFactory. */
package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Delegates matching, recipes, and the recipe book to the native shaped recipe. */
public record TableMaterialRecipe(ShapedRecipe base, Ingredient material) implements CraftingRecipe {
    private BlockState material(CraftingInput input) {
        for (ItemStack stack : input.items()) {
            if (!material.test(stack)) { continue; }
            if (!(stack.getItem() instanceof BlockItem item)) { continue; }
            return stack.getOrDefault(TinkerDataComponents.TABLE_MATERIAL.get(), item.getBlock().defaultBlockState());
        }
        return null;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return base.matches(input, level) && material(input) != null; }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack output = base.assemble(input, registries); BlockState material = material(input);
        if (material != null) { output.set(TinkerDataComponents.TABLE_MATERIAL.get(), material); }
        return output;
    }
    @Override public RecipeSerializer<?> getSerializer() { return WorkshopModule.TABLE_RECIPE.get(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return base.canCraftInDimensions(width, height); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack output = base.getResultItem(registries).copy();
        for (ItemStack stack : material.getItems()) {
            if (stack.getItem() instanceof BlockItem item) { output.set(TinkerDataComponents.TABLE_MATERIAL.get(), stack.getOrDefault(TinkerDataComponents.TABLE_MATERIAL.get(), item.getBlock().defaultBlockState())); break; }
        }
        return output;
    }
    @Override public NonNullList<Ingredient> getIngredients() { return base.getIngredients(); }
    @Override public CraftingBookCategory category() { return base.category(); }
    @Override public String getGroup() { return base.getGroup(); }
    @Override public boolean showNotification() { return base.showNotification(); }
    public static class Serializer implements RecipeSerializer<TableMaterialRecipe> {
        private static final MapCodec<TableMaterialRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ShapedRecipe.Serializer.CODEC.forGetter(TableMaterialRecipe::base), Ingredient.CODEC.fieldOf("material").forGetter(TableMaterialRecipe::material)).apply(instance, TableMaterialRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, TableMaterialRecipe> STREAM = StreamCodec.composite(
                ShapedRecipe.Serializer.STREAM_CODEC, TableMaterialRecipe::base, Ingredient.CONTENTS_STREAM_CODEC, TableMaterialRecipe::material, TableMaterialRecipe::new);
        @Override public MapCodec<TableMaterialRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, TableMaterialRecipe> streamCodec() { return STREAM; }
    }
}
