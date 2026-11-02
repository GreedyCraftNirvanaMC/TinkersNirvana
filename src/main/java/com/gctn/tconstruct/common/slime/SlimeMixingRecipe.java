/*
 * 根据 TinkersAntique 的 ShapedFallbackRecipe 配方选择行为移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** 同色配方优先，九个任意颜色的史莱姆球则使用原版可配置的混合结果。 */
public final class SlimeMixingRecipe extends CustomRecipe {
    public static final TagKey<Item> SLIMEBALLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "slime_balls"));

    public SlimeMixingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) {
            return false;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (!input.getItem(slot).is(SLIMEBALLS)) {
                return false;
            }
        }
        for (String color : new String[]{"blue", "purple", "blood", "magma", Config.MATCH_VANILLA_SLIME_BLOCK.get() ? "green" : "pink"}) {
            // 使用颜色标签判断同色，不依赖第三方史莱姆球的物品身份。
            var tag = SlimeModule.ballTag(color);
            boolean uniform = true;
            for (int slot = 0; slot < 9; slot++) {
                uniform &= input.getItem(slot).is(tag);
            }
            if (uniform) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(Config.MATCH_VANILLA_SLIME_BLOCK.get() ? SlimeModule.FULL_BLOCKS.get("pink").get().asItem() : Items.SLIME_BLOCK);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SlimeModule.MIXING.get();
    }
}
