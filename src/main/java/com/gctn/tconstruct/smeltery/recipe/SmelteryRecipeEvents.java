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
        SmelteryRecipes.AUTOMATIC_MELTING.clear();
        for (var holder : server.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof CraftingRecipe recipe)) {
                continue;
            }
            ItemStack output = recipe.getResultItem(server.registryAccess());
            if (output.isEmpty() || SmelteryRecipes.findMelting(output) != null) {
                continue;
            }
            Map<Fluid, Integer> amounts = new HashMap<>();
            boolean valid = true;
            for (var ingredient : recipe.getIngredients()) {
                ItemStack[] alternatives = ingredient.getItems();
                if (alternatives.length == 0) {
                    continue;
                }
                boolean found = false;
                for (ItemStack input : alternatives) {
                    if (ignored(input)) {
                        found = true;
                        break;
                    }
                    FluidStack material = materialValue(input);
                    if (!material.isEmpty()) {
                        amounts.merge(material.getFluid(), material.getAmount(), Integer::sum);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    valid = false;
                    break;
                }
            }
            // Ignore wood/sticks/etc., but reject recipes mixing two distinct meltable materials.
            if (valid && amounts.size() == 1) {
                var material = amounts.entrySet().iterator().next();
                int amount = material.getValue() / output.getCount();
                if (amount > 0) {
                    SmelteryRecipes.AUTOMATIC_MELTING.put(output.getItem(), SmelteryRecipes.melting(
                            stack -> stack.is(output.getItem()), material.getKey(), amount));
                }
            }
        }
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
