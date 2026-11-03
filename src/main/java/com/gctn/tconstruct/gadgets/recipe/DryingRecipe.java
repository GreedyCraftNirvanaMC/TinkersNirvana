/* SPDX-License-Identifier: LGPL-3.0-only; Antique's drying recipes using the native recipe manager. */
package com.gctn.tconstruct.gadgets.recipe;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.gadgets.RemainingGadgets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Datapacks and KubeJS ServerEvents.recipes can add ingredient/result/time recipes without Java hooks. */
public record DryingRecipe(Ingredient ingredient, ItemStack result, int time, boolean leather) implements Recipe<SingleRecipeInput> {
    @Override public boolean matches(SingleRecipeInput input, Level level) { return (!leather || Config.LEATHER_DRYING.get()) && ingredient.test(input.item()); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result; }
    @Override public NonNullList<Ingredient> getIngredients() { return NonNullList.of(Ingredient.EMPTY, ingredient); }
    @Override public RecipeSerializer<?> getSerializer() { return RemainingGadgets.DRYING_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return RemainingGadgets.DRYING_TYPE.get(); }
    public static class Serializer implements RecipeSerializer<DryingRecipe> {
        private static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(DryingRecipe::ingredient), ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DryingRecipe::result),
                Codec.intRange(1, 72000000).fieldOf("time").forGetter(DryingRecipe::time), Codec.BOOL.optionalFieldOf("leather", false).forGetter(DryingRecipe::leather)).apply(instance, DryingRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM = StreamCodec.of((buffer, recipe) -> {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient); ItemStack.STREAM_CODEC.encode(buffer, recipe.result); buffer.writeVarInt(recipe.time); buffer.writeBoolean(recipe.leather);
        }, buffer -> new DryingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readVarInt(), buffer.readBoolean()));
        @Override public MapCodec<DryingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> streamCodec() { return STREAM; }
    }
}
