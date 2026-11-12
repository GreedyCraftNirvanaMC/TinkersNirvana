/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.tools.PatternItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** 复用现有组装台的六输入和预览事务，模板不消耗，完整分配投入材料。 */
public final class PartBuilderRecipes {
    private PartBuilderRecipes() {}

    public static Plan plan(Container container) {
        return plan(container, false);
    }

    public static Plan planWithRemainder(Container container) {
        return plan(container, true);
    }

    private static Plan plan(Container container, boolean returnShards) {
        if (!(container.getItem(0).getItem() instanceof PatternItem) || PatternItem.isBlank(container.getItem(0))) { return null; }
        var part = PatternItem.selected(container.getItem(0));
        if (part.id().equals("bolt_core")) { return null; }
        com.gctn.tconstruct.library.materials.Material material = null;
        float value = 0;
        int[] counts = new int[6];
        for (int slot = 1; slot < 6; slot++) {
            ItemStack input = container.getItem(slot);
            if (input.isEmpty()) { continue; }
            var match = MaterialInputs.match(input);
            if (match == null || !match.material().isCraftable() || !part.acceptsMaterial(match.material())) { return null; }
            if (material != null && material != match.material()) { return null; }
            material = match.material();
            while (counts[slot] < input.getCount() && value + 0.0001F < part.cost()) {
                value += match.value();
                counts[slot]++;
            }
        }
        // 不接受无法完整分配的高价值原料，避免悄悄吞掉多余材料。
        if (material == null) { return null; }
        ItemStack result = part.create(material);
        if (result.isEmpty()) { return null; }
        if (returnShards) {
            if (value + .0001F < part.cost()) { return null; }
            float surplus = value - part.cost();
            int shards = Math.round(surplus * 2);
            if (shards > 64 || Math.abs(surplus - shards * .5F) > .001F) { return null; }
            ItemStack remainder = ItemStack.EMPTY;
            if (shards > 0) {
                remainder = com.gctn.tconstruct.library.toolparts.AntiqueParts.get("shard").create(material);
                if (remainder.isEmpty()) { return null; }
                remainder.setCount(shards);
                ItemStack existing = container.getItem(6);
                if (!existing.isEmpty() && (!ItemStack.isSameItemSameComponents(existing, remainder) || existing.getCount() + shards > existing.getMaxStackSize())) { return null; }
            }
            return new Plan(result, counts, remainder);
        }
        int outputCount = Math.round(value / part.cost());
        if (outputCount < 1 || outputCount > 64 || Math.abs(value - part.cost() * outputCount) > 0.001F) { return null; }
        result.setCount(outputCount);
        return new Plan(result, counts);
    }

    public static ItemStack createResult(Container container) {
        Plan plan = plan(container);
        return plan == null ? ItemStack.EMPTY : plan.result();
    }

    public static void consumeInputs(Container container) {
        Plan plan = plan(container);
        if (plan != null) {
            for (int slot = 1; slot < 6; slot++) { container.removeItem(slot, plan.counts()[slot]); }
        }
    }
    public record Plan(ItemStack result, int[] counts, ItemStack remainder) {
        public Plan(ItemStack result, int[] counts) { this(result, counts, ItemStack.EMPTY); }
        public void consume(Container container) {
            for (int slot = 1; slot < 6; slot++) { container.removeItem(slot, counts[slot]); }
            if (!remainder.isEmpty()) {
                ItemStack existing = container.getItem(6);
                ItemStack output = remainder.copy();
                if (!existing.isEmpty()) { output.grow(existing.getCount()); }
                container.setItem(6, output);
            }
        }
    }
}
