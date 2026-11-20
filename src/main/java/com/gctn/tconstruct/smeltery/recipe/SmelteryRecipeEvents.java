/*
 * Crafting-to-melting inference adapted from TinkersAntique, Slime Knights and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class SmelteryRecipeEvents {
    private SmelteryRecipeEvents() {
    }

    @SubscribeEvent
    private static void start(ServerAboutToStartEvent event) {
        rebuild(event.getServer());
    }

    @SubscribeEvent
    private static void reload(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            rebuild(event.getPlayerList().getServer());
        }
    }

    private static void rebuild(MinecraftServer server) {
        SmelteryRecipes.initialize();
        com.gctn.tconstruct.api.TinkerContent.installCastingRecipes();
        SmelteryRecipes.AUTOMATIC_MELTING.clear();
        Map<Item, FluidStack> inferred = new HashMap<>();
        for (var holder : server.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe)) {
                continue;
            }
            ItemStack output = recipe.getResultItem(server.registryAccess());
            if (output.isEmpty() || !output.getComponentsPatch().isEmpty()
                    || SmelteryRecipes.findMelting(output) != null || recipe.isIncomplete()) {
                continue;
            }
            FluidStack value = infer(recipe, output.getCount());
            // 产物没有配方来源。同一物品的所有合成途径必须给出相同的回收量。
            inferred.merge(output.getItem(), value, (previous, next) ->
                    FluidStack.matches(previous, next) ? previous : FluidStack.EMPTY);
        }
        inferred.forEach((item, value) -> {
            if (value.isEmpty()) { return; }
            ItemStack plain = new ItemStack(item);
            SmelteryRecipes.AUTOMATIC_MELTING.put(item, SmelteryRecipes.melting(
                    stack -> ItemStack.isSameItemSameComponents(plain, stack), value.getFluid(), value.getAmount()));
        });
    }

    private static FluidStack infer(CraftingRecipe recipe, int outputCount) {
        Fluid fluid = null;
        int amount = 0;
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) { continue; }
            FluidStack slot = null;
            for (ItemStack input : ingredient.getItems()) {
                boolean ignore = ignored(input);
                FluidStack value = ignore ? FluidStack.EMPTY : materialValue(input);
                // 标签可以匹配多种材料；按首个备选项计价会让低价原料变成另一种流体。
                if (!ignore && value.isEmpty() || slot != null && !FluidStack.matches(slot, value)) {
                    return FluidStack.EMPTY;
                }
                slot = value;
            }
            if (slot == null) { return FluidStack.EMPTY; }
            if (!slot.isEmpty()) {
                if (fluid != null && fluid != slot.getFluid()) { return FluidStack.EMPTY; }
                fluid = slot.getFluid();
                amount += slot.getAmount();
            }
        }
        return fluid != null && amount >= outputCount ? new FluidStack(fluid, amount / outputCount) : FluidStack.EMPTY;
    }

    private static boolean ignored(ItemStack stack) {
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String entry : Config.MELTING_IGNORE.get()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (id != null && (entry.startsWith("#") ? stack.is(TagKey.create(Registries.ITEM, id)) : item.equals(id))) {
                return true;
            }
        }
        return false;
    }

    private static FluidStack materialValue(ItemStack stack) {
        String item = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (stack.is(net.minecraft.world.item.Items.CLAY_BALL)) {
            return new FluidStack(SmelteryFluids.get("clay"), 144);
        }
        if (stack.is(net.minecraft.world.item.Items.CLAY)) {
            return new FluidStack(SmelteryFluids.get("clay"), 576);
        }
        if (stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "glass_blocks")))) {
            return new FluidStack(SmelteryFluids.get("glass"), 1000);
        }
        if (stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "glass_panes")))) {
            return new FluidStack(SmelteryFluids.get("glass"), 375);
        }
        String[] forms = {"ingot", "nugget", "block", "plate", "gear", "dust"};
        String[] tags = {"ingots", "nuggets", "storage_blocks", "plates", "gears", "dusts"};
        int[] amounts = {144, 16, 1296, 144, 576, 144};
        for (var material : SmelteryFluids.ENTRIES.values()) {
            if (material.name.equals("diamond") || material.name.equals("emerald")) {
                if (item.equals(material.name) || stack.is(TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("c", "gems/" + material.name)))) {
                    return new FluidStack(material.source.get(), SmelteryRecipes.GEM);
                }
                if (item.equals(material.name + "_block")) {
                    return new FluidStack(material.source.get(), SmelteryRecipes.GEM * 9);
                }
            }
            for (int i = 0; i < forms.length; i++) {
                if (item.equals(material.name + "_" + forms[i]) || stack.is(TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("c", tags[i] + "/" + material.name)))) {
                    return new FluidStack(material.source.get(), amounts[i]);
                }
            }
        }
        return FluidStack.EMPTY;
    }
}
