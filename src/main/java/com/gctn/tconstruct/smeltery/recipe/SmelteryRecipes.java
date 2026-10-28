package com.gctn.tconstruct.smeltery.recipe; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.crafting.RecipeHolder; // 引入 net.minecraft.world.item.crafting.RecipeHolder，供本文件使用。
import net.minecraft.world.item.crafting.RecipeManager; // 引入 net.minecraft.world.item.crafting.RecipeManager，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.neoforged.neoforge.fluids.FluidStack; // 引入 net.neoforged.neoforge.fluids.FluidStack，供本文件使用。

import java.util.*; // 引入 java.util.*，供本文件使用。

/**
 * 供冶炼炉库存与浇铸方块共用的、支持重载的配方查询。
 * 按操作类型建立索引，依赖物品栈具体状态的原料仍使用原生匹配逻辑。
 * Minecraft 替换配方映射时重建索引，即使重载前后的配方数量相同也能识别。
 */
public final class SmelteryRecipes { // 定义 SmelteryRecipes 的实现类型，具体职责见上方说明。
    // 使用弱引用键，使退出世界后的旧世界和配方管理器可以释放。单人内置
    // 服务器同时存在客户端与服务端线程，因此需要对缓存的创建和读取加锁。
    private static final Map<RecipeManager, Index> INDEXES = new WeakHashMap<>(); // 向当前声明或调用传入 private static final Map<RecipeManager, Index> INDEXES = new WeakHashMap<>()。

    private SmelteryRecipes() {} // 定义 SmelteryRecipes 的调用入口；此处无需额外处理。

    public static List<RecipeHolder<SmelteryRecipe>> all(Level level) { // 定义 all 的调用入口，参数和具体处理见下方实现。
        return index(level).all(); // 返回 index(level).all()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static Index index(Level level) { // 定义 index 的调用入口，参数和具体处理见下方实现。
        return index(level.getRecipeManager()); // 返回 index(level.getRecipeManager())，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static synchronized Index index(RecipeManager manager) { // 定义 index 的调用入口，参数和具体处理见下方实现。
        Collection<RecipeHolder<?>> snapshot = manager.getRecipes(); // 设置 snapshot 为 manager.getRecipes()。
        Index index = INDEXES.get(manager); // 设置 index 为 INDEXES.get(manager)。
        // Minecraft 1.21.1 返回的是不可变映射的值视图，
        // 该映射会被 apply 或 replaceRecipes 替换；比较视图对象的身份即可检测
        // 配方数量不变的重载，无需每刻复制或扫描全部配方。
        if (index == null || index.snapshot() != snapshot) { // 检查 index == null || index.snapshot() != snapshot；按条件决定是否执行本分支。
            var all = manager.getAllRecipesFor(SmelteryRegistration.RECIPE_TYPE.get()); // 设置 all 为 manager.getAllRecipesFor(SmelteryRegistration.RECIPE_TYPE.get())。
            Map<SmelteryRecipe.Operation, List<SmelteryRecipe>> grouped = new EnumMap<>(SmelteryRecipe.Operation.class); // 向当前声明或调用传入 Map<SmelteryRecipe.Operation, List<SmelteryRecipe>> grouped = new EnumMap<>(SmelteryRecipe.Operation.class)。
            for (var operation : SmelteryRecipe.Operation.values()) { // 遍历 var operation : SmelteryRecipe.Operation.values()，逐项执行当前处理。
                grouped.put(operation, all.stream().map(RecipeHolder::value) // 按键保存对应数据。
                        .filter(recipe -> recipe.operation() == operation).toList()); // 按条件筛选条目，只保留符合要求的数据。
            } // 结束当前作用域。
            index = new Index(snapshot, List.copyOf(all), Map.copyOf(grouped)); // 设置 index 为 new Index(snapshot, List.copyOf(all), Map.copyOf(grouped))。
            INDEXES.put(manager, index); // 按键保存对应数据。
        } // 结束当前作用域。
        return index; // 返回 index，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static SmelteryRecipe melting(Level level, ItemStack stack) { // 定义 melting 的调用入口，参数和具体处理见下方实现。
        if (stack.isEmpty()) return null; // 检查 stack.isEmpty()；按条件决定是否执行本分支。
        for (var recipe : index(level).byOperation().get(SmelteryRecipe.Operation.MELTING)) { // 遍历 var recipe : index(level).byOperation().get(SmelteryRecipe.Operation.MELTING)，逐项执行当前处理。
            // 不能只按物品标识符缓存匹配结果，因为原料条件可能依赖物品栈组件。
            if (recipe.ingredient().test(stack)) return recipe; // 检查 recipe.ingredient().test(stack)；按条件决定是否执行本分支。
        } // 结束当前作用域。
        return null; // 返回 null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static SmelteryRecipe casting(Level level, boolean basin, ItemStack cast, FluidStack fluid) { // 定义 casting 的调用入口，参数和具体处理见下方实现。
        var operation = basin ? SmelteryRecipe.Operation.CASTING_BASIN : SmelteryRecipe.Operation.CASTING_TABLE; // 设置 operation 为 basin ? SmelteryRecipe.Operation.CASTING_BASIN : SmelteryRecipe.Operation.CASTING_TABLE。
        for (var recipe : index(level).byOperation().get(operation)) { // 遍历 var recipe : index(level).byOperation().get(operation)，逐项执行当前处理。
            if (recipe.matchesCast(cast) // 检查 recipe.matchesCast(cast)；按条件决定是否执行本分支。
                    && FluidStack.isSameFluidSameComponents(recipe.fluids().getFirst(), fluid)) return recipe; // 调用 isSameFluidSameComponents，执行当前流程所需的处理。
        } // 结束当前作用域。
        return null; // 返回 null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private record Index(Collection<RecipeHolder<?>> snapshot, List<RecipeHolder<SmelteryRecipe>> all, // 定义 Index 的数据记录，具体职责见上方说明。
                         Map<SmelteryRecipe.Operation, List<SmelteryRecipe>> byOperation) {} // 在 SmelteryRecipes 中补充当前声明或表达式的 Map<SmelteryRecipe.Operation, List<SmelteryRecipe>> byOperation) {} 部分。
} // 结束当前作用域。
