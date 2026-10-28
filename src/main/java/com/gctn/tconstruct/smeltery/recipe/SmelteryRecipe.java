package com.gctn.tconstruct.smeltery.recipe; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import com.mojang.serialization.Codec; // 引入 com.mojang.serialization.Codec，供本文件使用。
import com.mojang.serialization.MapCodec; // 引入 com.mojang.serialization.MapCodec，供本文件使用。
import com.mojang.serialization.DataResult; // 引入 com.mojang.serialization.DataResult，供本文件使用。
import com.mojang.serialization.codecs.RecordCodecBuilder; // 引入 com.mojang.serialization.codecs.RecordCodecBuilder，供本文件使用。
import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.network.RegistryFriendlyByteBuf; // 引入 net.minecraft.network.RegistryFriendlyByteBuf，供本文件使用。
import net.minecraft.network.codec.ByteBufCodecs; // 引入 net.minecraft.network.codec.ByteBufCodecs，供本文件使用。
import net.minecraft.network.codec.StreamCodec; // 引入 net.minecraft.network.codec.StreamCodec，供本文件使用。
import net.minecraft.util.StringRepresentable; // 引入 net.minecraft.util.StringRepresentable，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.crafting.*; // 引入 net.minecraft.world.item.crafting.*，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 数据包中的熔炼、合金、铸件台浇铸或铸造盆浇铸配方定义。
 * 编解码器在机器使用配方前校验各操作所需的输入输出，并限制处理时长。
 * 构造时复制可变的物品栈和流体栈，创建物品结果时也返回副本。
 */
public record SmelteryRecipe(Operation operation, Ingredient ingredient, List<FluidStack> fluids, // 定义 SmelteryRecipe 的数据记录，具体职责见上方说明。
                             FluidStack fluidResult, ItemStack result, int temperature, int time, // 向当前声明或调用传入 FluidStack fluidResult, ItemStack result, int temperature, int time。
                             boolean consumeInput) implements Recipe<SingleRecipeInput> { // 在 SmelteryRecipe 中补充当前声明或表达式的 boolean consumeInput) implements Recipe<SingleRecipeInput> { 部分。
    public enum Operation implements StringRepresentable { // 定义 Operation 的固定枚举取值，具体职责见上方说明。
        MELTING, ALLOY, CASTING_TABLE, CASTING_BASIN; // 在 SmelteryRecipe 中补充当前声明或表达式的 MELTING, ALLOY, CASTING_TABLE, CASTING_BASIN 部分。
        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values); // 设置 CODEC 为 StringRepresentable.fromEnum(Operation::values)。
        @Override public String getSerializedName() { return name(); } // 实现父类或接口约定；定义 getSerializedName 的调用入口；返回 name()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static final MapCodec<SmelteryRecipe> CODEC = RecordCodecBuilder.<SmelteryRecipe>mapCodec(instance -> instance.group( // 设置 CODEC 为 RecordCodecBuilder.<SmelteryRecipe>mapCodec(instance -> instance.group(。
            Operation.CODEC.fieldOf("operation").forGetter(SmelteryRecipe::operation), // 调用 fieldOf，执行当前流程所需的处理。
            Ingredient.CODEC.optionalFieldOf("ingredient", Ingredient.EMPTY).forGetter(SmelteryRecipe::ingredient), // 调用 optionalFieldOf，执行当前流程所需的处理。
            FluidStack.CODEC.listOf().optionalFieldOf("fluids", List.of()).forGetter(SmelteryRecipe::fluids), // 调用 listOf，执行当前流程所需的处理。
            FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid_result", FluidStack.EMPTY).forGetter(SmelteryRecipe::fluidResult), // 调用 optionalFieldOf，执行当前流程所需的处理。
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(SmelteryRecipe::result), // 调用 optionalFieldOf，执行当前流程所需的处理。
            Codec.intRange(0, 10000).optionalFieldOf("temperature", 0).forGetter(SmelteryRecipe::temperature), // 调用 intRange，执行当前流程所需的处理。
            Codec.intRange(1, 72000).optionalFieldOf("time", 100).forGetter(SmelteryRecipe::time), // 调用 intRange，执行当前流程所需的处理。
            Codec.BOOL.optionalFieldOf("consume_input", false).forGetter(SmelteryRecipe::consumeInput) // 调用 optionalFieldOf，执行当前流程所需的处理。
    ).apply(instance, SmelteryRecipe::new)).flatXmap(SmelteryRecipe::validate, DataResult::success); // 调用 apply，执行当前流程所需的处理。

    public SmelteryRecipe { // 在 SmelteryRecipe 中补充当前声明或表达式的 public SmelteryRecipe { 部分。
        fluids = fluids.stream().map(FluidStack::copy).toList(); // 设置 fluids 为 fluids.stream().map(FluidStack::copy).toList()。
        fluidResult = fluidResult.copy(); // 设置 fluidResult 为 fluidResult.copy()。
        result = result.copy(); // 设置 result 为 result.copy()。
    } // 结束当前作用域。

    private static DataResult<SmelteryRecipe> validate(SmelteryRecipe recipe) { // 定义 validate 的调用入口，参数和具体处理见下方实现。
        boolean valid = switch (recipe.operation) { // 设置 valid 为 switch (recipe.operation) {。
            case MELTING -> !recipe.ingredient.isEmpty() && !recipe.fluidResult.isEmpty(); // 定义 case MELTING 分支，调用 isEmpty，执行当前流程所需的处理。
            case ALLOY -> !recipe.fluids.isEmpty() && !recipe.fluidResult.isEmpty(); // 定义 case ALLOY 分支，调用 isEmpty，执行当前流程所需的处理。
            case CASTING_TABLE, CASTING_BASIN -> recipe.fluids.size() == 1 && !recipe.result.isEmpty(); // 定义 case CASTING_TABLE, CASTING_BASIN 分支，调用 size，执行当前流程所需的处理。
        }; // 结束当前作用域并完成本次表达式或声明。
        return valid ? DataResult.success(recipe) : DataResult.error(() -> "Incomplete smeltery recipe: " + recipe.operation); // 返回 valid ? DataResult.success(recipe) : DataResult.error(() -> "Incomplete smeltery recipe: " + recipe.operation)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public boolean matchesCast(ItemStack cast) { // 定义 matchesCast 的调用入口，参数和具体处理见下方实现。
        return ingredient.isEmpty() ? cast.isEmpty() : ingredient.test(cast); // 返回 ingredient.isEmpty() ? cast.isEmpty() : ingredient.test(cast)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override public boolean matches(SingleRecipeInput input, Level level) { return ingredient.test(input.item()); } // 实现父类或接口约定；定义 matches 的调用入口；返回 ingredient.test(input.item())，将当前分支的结果交给调用者。
    @Override public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) { return result.copy(); } // 实现父类或接口约定；定义 assemble 的调用入口；返回 result.copy()，将当前分支的结果交给调用者。
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); } // 实现父类或接口约定；定义 getResultItem 的调用入口；返回 result.copy()，将当前分支的结果交给调用者。
    @Override public boolean canCraftInDimensions(int width, int height) { return false; } // 实现父类或接口约定；定义 canCraftInDimensions 的调用入口；返回 false，将当前分支的结果交给调用者。
    @Override public boolean isSpecial() { return true; } // 实现父类或接口约定；定义 isSpecial 的调用入口；返回 true，将当前分支的结果交给调用者。
    @Override public RecipeType<?> getType() { return SmelteryRegistration.RECIPE_TYPE.get(); } // 实现父类或接口约定；定义 getType 的调用入口；返回 SmelteryRegistration.RECIPE_TYPE.get()，将当前分支的结果交给调用者。
    @Override public RecipeSerializer<?> getSerializer() { return SmelteryRegistration.RECIPE_SERIALIZER.get(); } // 实现父类或接口约定；定义 getSerializer 的调用入口；返回 SmelteryRegistration.RECIPE_SERIALIZER.get()，将当前分支的结果交给调用者。

    public static class Serializer implements RecipeSerializer<SmelteryRecipe> { // 定义 Serializer 的实现类型，具体职责见上方说明。
        @Override public MapCodec<SmelteryRecipe> codec() { return CODEC; } // 实现父类或接口约定；定义 codec 的调用入口；返回 CODEC，将当前分支的结果交给调用者。
        @Override public StreamCodec<RegistryFriendlyByteBuf, SmelteryRecipe> streamCodec() { // 实现父类或接口约定；调用 streamCodec，执行当前流程所需的处理。
            return ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()); // 返回 ByteBufCodecs.fromCodecWithRegistries(CODEC.codec())，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
