/*
 * 根据 TinkersAntique 的 GuiUtil 储液提示移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.client;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 两种储液界面共用单位规则，按铸造配方识别锭、粒、宝石和方块。 */
public final class FluidTankTooltip {
    private FluidTankTooltip() {
    }

    public static void amount(List<Component> text, int amount, FluidStack fluid, boolean shift) {
        if (!shift) {
            List<Unit> units = new ArrayList<>();
            if (fluid.isEmpty()) {
                units.add(new Unit(144, "ingot"));
            } else {
                for (var recipe : SmelteryRecipes.CASTING) {
                    if (recipe.fluid() != fluid.getFluid()) {
                        continue;
                    }
                    if (recipe.basin() && recipe.cast().test(ItemStack.EMPTY)) {
                        units.add(new Unit(recipe.amount(), "block"));
                    } else if (!recipe.basin()) {
                        for (String cast : new String[]{"ingot", "nugget", "gem"}) {
                            if (recipe.cast().test(new ItemStack(SmelteryModule.item("cast_" + cast)))) {
                                units.add(new Unit(recipe.amount(), cast));
                            }
                        }
                    }
                }
            }
            for (Unit unit : units.stream().distinct().sorted(Comparator.comparingInt(Unit::amount).reversed()).toList()) {
                amount = append(text, amount, unit.amount, unit.key);
            }
        }
        amount = append(text, amount, 1_000_000, "kilobucket");
        amount = append(text, amount, 1000, "bucket");
        append(text, amount, 1, "millibucket");
    }

    private static int append(List<Component> text, int amount, int size, String unit) {
        if (amount >= size) {
            text.add(Component.translatable("gui.smeltery.liquid.amount", amount / size,
                    Component.translatable("gui.smeltery.liquid." + unit)).withStyle(ChatFormatting.GRAY));
        }
        return amount % size;
    }

    private record Unit(int amount, String key) {
    }
}
