/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
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
        if (returnShards) { return planSingle(container, part); }
        Material material = null;
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
        int outputCount = Math.round(value / part.cost());
        if (outputCount < 1 || outputCount > 64 || Math.abs(value - part.cost() * outputCount) > 0.001F) { return null; }
        result.setCount(outputCount);
        return new Plan(result, counts);
    }

    private static Plan planSingle(Container container, AntiqueParts.Definition part) {
        record Input(int slot, int count, MaterialInputs.Match match) {}
        var inputs = new java.util.ArrayList<Input>();
        for (int slot = 1; slot < 6; slot++) {
            ItemStack stack = container.getItem(slot);
            var match = MaterialInputs.match(stack);
            if (match != null && match.material().isCraftable()) {
                inputs.add(new Input(slot, stack.getCount(), match));
            }
        }
        // Mantle 优先消耗价值高的匹配项，并让不能用于当前部件的材料留在原槽。
        inputs.sort(java.util.Comparator.comparingDouble((Input input) -> input.match().value()).reversed());
        for (Material material : TinkerMaterials.materials) {
            float value = 0;
            int[] counts = new int[6];
            for (Input input : inputs) {
                if (input.match().material() != material) { continue; }
                while (counts[input.slot()] < input.count() && value + .0001F < part.cost()) {
                    value += input.match().value();
                    counts[input.slot()]++;
                }
            }
            if (value + .0001F < part.cost()) { continue; }
            ItemStack result = part.create(material);
            if (result.isEmpty()) { continue; }
            // 怀古只返还完整碎片。少于半份的尾料不影响部件产出。
            int shards = (int) ((value - part.cost() + .0001F) * 2);
            if (shards == 0) { return new Plan(result, counts); }
            ItemStack remainder = AntiqueParts.get("shard").create(material);
            ItemStack existing = container.getItem(6);
            if (remainder.isEmpty() || shards > remainder.getMaxStackSize() || !existing.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(existing, remainder) || existing.getCount() + shards > existing.getMaxStackSize())) {
                return null;
            }
            remainder.setCount(shards);
            return new Plan(result, counts, remainder);
        }
        return null;
    }

    public static ItemStack createResult(Container container) {
        Plan plan = plan(container);
        return plan == null ? ItemStack.EMPTY : plan.result();
    }

    public static void consumeInputs(Container container) {
        Plan plan = plan(container);
        if (plan != null) { plan.consume(container); }
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
