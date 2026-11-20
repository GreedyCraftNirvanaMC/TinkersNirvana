package com.gctn.tconstruct.api;

import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/** 查询当前服务端的冶炼和浇铸规则，返回独立副本。 */
public final class SmelteryApi {
    private SmelteryApi() {}

    /** 返回单件输入的熔炼产物。配方、标签和矿物倍率以当前服务端为准。 */
    public static FluidStack meltingOutput(ItemStack input) {
        if (input.isEmpty()) { return FluidStack.EMPTY; }
        var recipe = SmelteryRecipes.findMelting(input);
        return recipe == null ? FluidStack.EMPTY : recipe.output().copy();
    }

    /** 使用给定流体量和铸模查询产物，不消耗输入；无配方或流体不足时返回空物品。 */
    public static ItemStack castingOutput(boolean basin, ItemStack cast, FluidStack fluid) {
        if (fluid.isEmpty()) { return ItemStack.EMPTY; }
        var recipe = SmelteryRecipes.findCasting(basin, cast, fluid.getFluid());
        return recipe == null || fluid.getAmount() < recipe.amount() ? ItemStack.EMPTY : recipe.output().copy();
    }
}
