package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;

/**
 * 供冶炼炉库存与浇铸方块共用的、支持重载的配方查询。
 * 按操作类型建立索引，依赖物品栈具体状态的原料仍使用原生匹配逻辑。
 * Minecraft 替换配方映射时重建索引，即使重载前后的配方数量相同也能识别。
 */
public final class SmelteryRecipes {
    // 使用弱引用键，使退出世界后的旧世界和配方管理器可以释放。单人内置
    // 服务器同时存在客户端与服务端线程，因此需要对缓存的创建和读取加锁。
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
        // Minecraft 1.21.1 返回的是不可变映射的值视图，
        // 该映射会被 apply 或 replaceRecipes 替换；比较视图对象的身份即可检测
        // 配方数量不变的重载，无需每刻复制或扫描全部配方。
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
            // 不能只按物品标识符缓存匹配结果，因为原料条件可能依赖物品栈组件。
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
