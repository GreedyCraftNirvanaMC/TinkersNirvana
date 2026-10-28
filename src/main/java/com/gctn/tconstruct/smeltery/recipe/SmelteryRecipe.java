package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Data-pack definition for melting, alloying, table casting or basin casting.
 * The codec validates operation-specific inputs/outputs and bounds processing times before machine code uses them.
 * Mutable stacks are copied at construction and item results are copied when assembled.
 */
public record SmelteryRecipe(Operation operation, Ingredient ingredient, List<FluidStack> fluids,
                             FluidStack fluidResult, ItemStack result, int temperature, int time,
                             boolean consumeInput) implements Recipe<SingleRecipeInput> {
    public enum Operation implements StringRepresentable {
        MELTING, ALLOY, CASTING_TABLE, CASTING_BASIN;
        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);
        @Override public String getSerializedName() { return name(); }
    }

    public static final MapCodec<SmelteryRecipe> CODEC = RecordCodecBuilder.<SmelteryRecipe>mapCodec(instance -> instance.group(
            Operation.CODEC.fieldOf("operation").forGetter(SmelteryRecipe::operation),
            Ingredient.CODEC.optionalFieldOf("ingredient", Ingredient.EMPTY).forGetter(SmelteryRecipe::ingredient),
            FluidStack.CODEC.listOf().optionalFieldOf("fluids", List.of()).forGetter(SmelteryRecipe::fluids),
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid_result", FluidStack.EMPTY).forGetter(SmelteryRecipe::fluidResult),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(SmelteryRecipe::result),
            Codec.intRange(0, 10000).optionalFieldOf("temperature", 0).forGetter(SmelteryRecipe::temperature),
            Codec.intRange(1, 72000).optionalFieldOf("time", 100).forGetter(SmelteryRecipe::time),
            Codec.BOOL.optionalFieldOf("consume_input", false).forGetter(SmelteryRecipe::consumeInput)
    ).apply(instance, SmelteryRecipe::new)).flatXmap(SmelteryRecipe::validate, DataResult::success);

    public SmelteryRecipe {
        fluids = fluids.stream().map(FluidStack::copy).toList();
        fluidResult = fluidResult.copy();
        result = result.copy();
    }

    private static DataResult<SmelteryRecipe> validate(SmelteryRecipe recipe) {
        boolean valid = switch (recipe.operation) {
            case MELTING -> !recipe.ingredient.isEmpty() && !recipe.fluidResult.isEmpty();
            case ALLOY -> !recipe.fluids.isEmpty() && !recipe.fluidResult.isEmpty();
            case CASTING_TABLE, CASTING_BASIN -> recipe.fluids.size() == 1 && !recipe.result.isEmpty();
        };
        return valid ? DataResult.success(recipe) : DataResult.error(() -> "Incomplete smeltery recipe: " + recipe.operation);
    }

    public boolean matchesCast(ItemStack cast) {
        return ingredient.isEmpty() ? cast.isEmpty() : ingredient.test(cast);
    }

    @Override public boolean matches(SingleRecipeInput input, Level level) { return ingredient.test(input.item()); }
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return result.copy(); }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return false; }
    @Override public boolean isSpecial() { return true; }
    @Override public RecipeType<?> getType() { return SmelteryRegistration.RECIPE_TYPE.get(); }
    @Override public RecipeSerializer<?> getSerializer() { return SmelteryRegistration.RECIPE_SERIALIZER.get(); }

    public static class Serializer implements RecipeSerializer<SmelteryRecipe> {
        @Override public MapCodec<SmelteryRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> streamCodec() {
            return ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        }
    }
}
