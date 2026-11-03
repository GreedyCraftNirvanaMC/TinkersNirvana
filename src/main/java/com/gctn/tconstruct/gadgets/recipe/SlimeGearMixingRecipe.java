/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's shaped_fallback recipes. */
package com.gctn.tconstruct.gadgets.recipe;

import com.gctn.tconstruct.common.slime.SlimeModule;
import com.gctn.tconstruct.gadgets.SlimeGear;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Map;

/** Uniform known slime colors use their normal recipes; mixed/unknown colors make hidden pink gear. */
public class SlimeGearMixingRecipe extends CustomRecipe {
    private final boolean boots;
    private final ShapedRecipePattern pattern;
    public SlimeGearMixingRecipe(CraftingBookCategory category, boolean boots) {
        super(category); this.boots = boots;
        var balls = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "slime_balls")));
        var blocks = Ingredient.of(TagKey.create(Registries.ITEM, SlimeGear.id("congealed_slime")));
        pattern = boots ? ShapedRecipePattern.of(Map.of('A', balls, 'B', blocks), "A A", "B B")
                : ShapedRecipePattern.of(Map.of('S', Ingredient.of(Items.STRING), 'C', blocks, 'B', balls), "SCS", "B B", " B ");
    }
    @Override public boolean matches(CraftingInput input, Level level) {
        if (!pattern.matches(input)) { return false; }
        for (String color : new String[]{"green", "blue", "purple", "blood", "magma"}) {
            int matching = 0;
            for (ItemStack stack : input.items()) { if (stack.is(SlimeModule.ballTag(color)) || stack.is(SlimeModule.CONGEALED.get(color).get().asItem())) { matching++; } }
            if (matching == 4) { return false; }
        }
        return true;
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return new ItemStack(boots ? SlimeGear.BOOTS.get("pink").get() : SlimeGear.SLINGS.get("pink").get()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= (boots ? 2 : 3); }
    @Override public RecipeSerializer<?> getSerializer() { return boots ? SlimeGear.MIX_BOOTS.get() : SlimeGear.MIX_SLING.get(); }
}
