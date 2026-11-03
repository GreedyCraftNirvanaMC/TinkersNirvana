/* SPDX-License-Identifier: LGPL-3.0-only; Antique's secret food and station upgrades. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.gadgets.item.MomsSpaghettiItem;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tables.recipe.StationProcessingRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.HashSet;
import java.util.function.Supplier;

public final class Spaghetti {
    public static final Supplier<Item> HARD = ItemRegister.ITEMS.register("spaghetti_hard", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SOGGY = ItemRegister.ITEMS.register("spaghetti_soggy", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> COLD = ItemRegister.ITEMS.register("spaghetti_cold", () -> new Item(new Item.Properties()));
    public static final Supplier<MomsSpaghettiItem> MOMS = ItemRegister.ITEMS.register("moms_spaghetti", MomsSpaghettiItem::new);
    private static final TagKey<Item> MEAT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/cooked_meat"));
    private Spaghetti() {}
    public static void init() { StationProcessingRecipes.register("tconstruct:spaghetti", stack -> stack.is(MOMS.get()), Spaghetti::plan); }
    private static StationProcessingRecipes.Plan plan(Container input) {
        ItemStack result = input.getItem(0).copyWithCount(1);
        int[] counts = new int[6]; counts[0] = 1;
        var byproducts = new java.util.ArrayList<ItemStack>();
        int seasoning = MomsSpaghettiItem.seasoning(result), occupied = 0, sauceSlot = -1;
        boolean wheat = result.isDamaged();
        var meats = new HashSet<Item>();
        for (int i = 1; i < 6; i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) { continue; }
            occupied++; wheat &= stack.is(Items.WHEAT);
            if (stack.is(Items.BEETROOT_SOUP)) { sauceSlot = i; }
            if (stack.is(MEAT) && meats.add(stack.getItem()) && meats.size() <= 3) { counts[i] = 1; }
        }
        if (occupied == 0) { return null; }
        if (wheat) {
            int damage = result.getDamageValue();
            for (int i = 1; i < 6; i++) { counts[i] = Math.min(damage, input.getItem(i).getCount()); damage -= counts[i]; }
            result.setDamageValue(damage);
        } else if (seasoning == 0 && occupied == 1 && sauceSlot > 0) {
            counts[sauceSlot] = 1; addModifier(result, "spaghetti_sauce");
            if (input.getItem(sauceSlot).getCraftingRemainingItem().isEmpty()) { byproducts.add(new ItemStack(Items.BOWL)); }
        } else if (seasoning == 1 && meats.size() >= 3) {
            for (int i = 1; i < 6; i++) { if (!input.getItem(i).isEmpty() && !input.getItem(i).is(MEAT)) { return null; } }
            addModifier(result, "spaghetti_meat");
        } else { return null; }
        return new StationProcessingRecipes.Plan(result, counts, byproducts);
    }
    private static void addModifier(ItemStack stack, String id) {
        var data = TinkerDataComponents.getMaterialTag(stack); var modifiers = data.getCompound("Modifiers"); modifiers.putInt(id, 1); data.put("Modifiers", modifiers);
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
    }
}
