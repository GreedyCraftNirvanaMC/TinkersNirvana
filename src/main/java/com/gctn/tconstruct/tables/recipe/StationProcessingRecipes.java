/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.tables.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/** Extension point for repairable/upgradeable non-tool items. Planners must never mutate inputs. */
public final class StationProcessingRecipes {
    private record Handler(Predicate<ItemStack> item, Function<Container, Plan> planner) {}
    private static final Map<String, Handler> HANDLERS = new LinkedHashMap<>();
    private StationProcessingRecipes() {}
    public static void register(String id, Predicate<ItemStack> item, Function<Container, Plan> planner) {
        if (id == null || item == null || planner == null || HANDLERS.putIfAbsent(id, new Handler(item, planner)) != null) { throw new IllegalArgumentException("Invalid or duplicate station handler: " + id); }
    }
    public static boolean supports(ItemStack stack) { return HANDLERS.values().stream().anyMatch(handler -> handler.item.test(stack)); }
    public static Plan plan(Container input) {
        for (Handler handler : HANDLERS.values()) { if (handler.item.test(input.getItem(0))) { Plan plan = handler.planner.apply(input); if (plan != null) { return plan; } } }
        return null;
    }
    public record Plan(ItemStack result, int[] consumed, java.util.List<ItemStack> byproducts) {
        public Plan(ItemStack result, int[] consumed) { this(result, consumed, java.util.List.of()); }
        public Plan {
            result = result.copy(); consumed = consumed.clone();
            byproducts = byproducts.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
            if (result.isEmpty() || consumed.length != 6 || consumed[0] != 1) { throw new IllegalArgumentException("Station plan requires one source and six input counts"); }
            for (int count : consumed) { if (count < 0) { throw new IllegalArgumentException("Negative station ingredient count"); } }
        }
        @Override public ItemStack result() { return result.copy(); }
        @Override public int[] consumed() { return consumed.clone(); }
        @Override public java.util.List<ItemStack> byproducts() { return byproducts.stream().map(ItemStack::copy).toList(); }
        /** Real stations drop overflow in-world; standalone containers return it to their caller. */
        public java.util.List<ItemStack> consume(Container input) {
            var remainders = new java.util.ArrayList<ItemStack>();
            var overflow = new java.util.ArrayList<ItemStack>();
            for (int slot = 0; slot < consumed.length; slot++) {
                ItemStack ingredient = input.getItem(slot);
                if (ingredient.getCount() < consumed[slot]) { throw new IllegalStateException("Station inputs changed before commit"); }
            }
            for (int slot = 0; slot < consumed.length; slot++) {
                if (consumed[slot] == 0) { continue; }
                ItemStack ingredient = input.getItem(slot).copyWithCount(1);
                input.removeItem(slot, consumed[slot]);
                for (int count = 0; count < consumed[slot]; count++) {
                    ItemStack remainder = ingredient.getCraftingRemainingItem();
                    if (!remainder.isEmpty()) { remainders.add(remainder); }
                }
            }
            remainders.addAll(byproducts());
            for (ItemStack remainder : remainders) {
                for (int target = 0; target < consumed.length && !remainder.isEmpty(); target++) {
                    ItemStack current = input.getItem(target);
                    int limit = Math.min(input.getMaxStackSize(), remainder.getMaxStackSize());
                    if (current.isEmpty()) { input.setItem(target, remainder.split(Math.min(limit, remainder.getCount()))); }
                    else if (ItemStack.isSameItemSameComponents(current, remainder)) {
                        int moved = Math.min(remainder.getCount(), Math.max(0, limit - current.getCount()));
                        current.grow(moved); remainder.shrink(moved);
                    }
                }
                if (!remainder.isEmpty()) {
                    if (input instanceof BlockEntity block && block.getLevel() != null) { Containers.dropItemStack(block.getLevel(), block.getBlockPos().getX() + .5, block.getBlockPos().getY() + 1, block.getBlockPos().getZ() + .5, remainder); }
                    else { overflow.add(remainder); }
                }
            }
            return java.util.List.copyOf(overflow);
        }
    }
}
