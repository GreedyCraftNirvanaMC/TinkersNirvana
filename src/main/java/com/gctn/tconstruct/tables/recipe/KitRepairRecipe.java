/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's RepairRecipe. */
package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tools.TinkerTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class KitRepairRecipe extends CustomRecipe {
    public KitRepairRecipe(CraftingBookCategory category) { super(category); }
    @Override public boolean matches(CraftingInput input, Level level) { return !repair(input).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return repair(input); }
    private ItemStack repair(CraftingInput input) {
        SimpleContainer station = new SimpleContainer(input.size() + 1); int slot = 1;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) { continue; }
            if (stack.getItem() instanceof TinkerTools) { if (!station.getItem(0).isEmpty()) { return ItemStack.EMPTY; } station.setItem(0, stack.copyWithCount(1)); }
            else if (stack.is(AntiqueParts.get("sharpening_kit").item().get())) { station.setItem(slot++, stack.copyWithCount(1)); }
            else { return ItemStack.EMPTY; }
        }
        return slot <= 1 ? ItemStack.EMPTY : ToolStationDefaultRecipes.repairResult(station, slot - 1);
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public RecipeSerializer<?> getSerializer() { return WorkshopModule.KIT_REPAIR.get(); }
}
