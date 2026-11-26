package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolModifiers;
import com.gctn.tconstruct.tools.ToolPartReplacement;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.TinkerTools.RepairInfo;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class ToolStationDefaultRecipes {
    private static final int TOOL_SLOT = 0;
    private static final int FIRST_MATERIAL_SLOT = 1;
    private static final int LAST_MATERIAL_SLOT = 5;
    private static final int MATERIAL_SLOT_COUNT = LAST_MATERIAL_SLOT - FIRST_MATERIAL_SLOT + 1;

    private ToolStationDefaultRecipes() {
    }

    public static ItemStack createResult(Container container) {
        return apply(container, false);
    }

    public static void consumeInputs(Container container) {
        apply(container, true);
    }

    private static ItemStack apply(Container container, boolean consume) {
        var special = StationProcessingRecipes.plan(container);
        if (special != null) {
            if (consume) { special.consume(container); }
            return special.result();
        }
        ItemStack tool = container.getItem(TOOL_SLOT);
        if (tool.getItem() instanceof com.gctn.tconstruct.tools.PatternItem) {
            ItemStack result = PartBuilderRecipes.createResult(container);
            if (consume && !result.isEmpty()) { PartBuilderRecipes.consumeInputs(container); }
            return result;
        }
        if (!isTinkerTool(tool)) { return ItemStack.EMPTY; }

        // 怀古先修复，再换件和强化。磨制套件与燧石同时适用时，预览和扣料必须选中同一种操作。
        RepairPlan repair = createRepairPlan(container, LAST_MATERIAL_SLOT);
        if (repair != null && !repair.isEmpty()) {
            if (consume) {
                container.setItem(TOOL_SLOT, ItemStack.EMPTY);
                for (int i = 0; i < MATERIAL_SLOT_COUNT; i++) {
                    if (repair.repairCount[i] > 0) {
                        container.removeItem(FIRST_MATERIAL_SLOT + i, repair.repairCount[i]);
                    }
                }
            }
            return repair.resultTool;
        }
        var replacement = ToolPartReplacement.plan(container);
        if (replacement != null) {
            if (consume) { replacement.consume(container); }
            return replacement.result();
        }
        var enhancement = ToolModifiers.plan(container);
        if (enhancement == null) { return ItemStack.EMPTY; }
        if (consume) { enhancement.consume(container); }
        return enhancement.result();
    }

    /** Shared by stations and the native crafting-grid sharpening-kit recipe. Slot zero is the tool. */
    public static ItemStack repairResult(Container container, int lastMaterialSlot) {
        if (lastMaterialSlot < 1 || lastMaterialSlot >= container.getContainerSize()) { return ItemStack.EMPTY; }
        RepairPlan repairPlan = createRepairPlan(container, lastMaterialSlot);
        if (repairPlan == null || repairPlan.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return repairPlan.resultTool;
    }

    // 获取修复方案(包括修复量和材料消耗情况)
    private static RepairPlan createRepairPlan(Container container, int lastMaterialSlot) {

        ItemStack sourceTool = container.getItem(TOOL_SLOT);
        if (!isRepairableTool(sourceTool)) {
            return null;
        }
        ItemStack tool = sourceTool.copy();
        List<RepairMatch> matches = new ArrayList<>();
        List<RepairInfo> materials = ((TinkerTools) tool.getItem()).getRepairInfo(tool);
        for (int slot = FIRST_MATERIAL_SLOT; slot <= lastMaterialSlot; slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            var toolPart = AntiqueParts.get(stack);
            boolean kit = toolPart != null && toolPart.id().equals("sharpening_kit");
            if (toolPart != null && !kit && !toolPart.id().equals("shard")) { return null; }
            var input = MaterialInputs.match(stack);
            if (input == null) {
                return null;
            }
            RepairInfo info = materials.stream()
                    .filter(part -> input.material() == part.material() && part.partEfficient() > 0)
                    .findFirst().orElse(null);
            if (info == null) {
                return null;
            }
            matches.add(new RepairMatch(slot, info, input.value(), kit));
        }

        int[] consumed = new int[lastMaterialSlot];
        // 怀古逐个可修理部件检查第一个修理包。原料可单独匹配，额外的修理包槽位必须也能匹配。
        var kits = matches.stream().filter(RepairMatch::kit).toList();
        int kitIndex = 0;
        for (RepairInfo info : materials) {
            if (kitIndex >= kits.size()) { break; }
            RepairMatch kit = kits.get(kitIndex);
            if (kit.info().material() == info.material()) {
                if (++consumed[kit.slot() - FIRST_MATERIAL_SLOT] >= container.getItem(kit.slot()).getCount()) { kitIndex++; }
            }
        }
        for (RepairMatch kit : kits) {
            if (consumed[kit.slot() - FIRST_MATERIAL_SLOT] == 0) { return null; }
        }
        Arrays.fill(consumed, 0);
        List<RepairInfo> repairParts = materials.stream().filter(info -> info.partEfficient() > 0)
                .filter(info -> matches.stream().anyMatch(match -> match.info().equals(info))).distinct().toList();
        var data = TinkerDataComponents.getMaterialTag(tool);
        int repairCount = Math.clamp(data.getInt("RepairCount"), 0, 100);
        int baseDurability = ToolBuilder.savedStats(tool).getInt("BaseDurability");
        if (baseDurability <= 0 && tool.getItem() instanceof AntiqueTool antique) {
            var stats = ToolBuilder.calculate(tool, antique.definition());
            ToolTraits.forEach(tool, (trait, level) -> trait.modifyStats(tool, level, stats));
            baseDurability = ToolTraits.modifyDurability(tool, stats.durability);
        }
        double durabilityFactor = tool.getMaxDamage() / (double) (baseDurability > 0 ? baseDurability : tool.getMaxDamage());
        int usedModifiers = Math.min(3, ToolModifiers.usedSlots(tool));

        while (tool.isDamaged()) {
            List<RepairMatch> batch = new ArrayList<>();
            var raw = new HashMap<RepairInfo, RepairMatch>();
            var availableKits = new ArrayDeque<RepairMatch>();
            for (RepairMatch match : matches) {
                if (consumed[match.slot() - FIRST_MATERIAL_SLOT] >= container.getItem(match.slot()).getCount()) { continue; }
                if (match.kit()) { availableKits.addLast(match); }
                else { raw.merge(match.info(), match, (first, second) -> first.value() >= second.value() ? first : second); }
            }
            float amount = 0;
            int rawMaterials = 0;
            for (RepairInfo info : repairParts) {
                RepairMatch kit = availableKits.peekFirst();
                if (kit != null && kit.info().equals(info)) {
                    batch.add(kit);
                    amount += (int) (info.durability() * kit.value()) * info.partEfficient();
                    if (consumed[kit.slot() - FIRST_MATERIAL_SLOT] + 1 >= container.getItem(kit.slot()).getCount()) {
                        availableKits.removeFirst();
                    }
                }
                RepairMatch ingredient = raw.get(info);
                if (ingredient != null) {
                    batch.add(ingredient);
                    amount += info.durability() * ingredient.value() * info.partEfficient();
                    rawMaterials++;
                }
            }
            // 修理包单独计入修复量，多材料倍率只按普通原料的种数计算。
            int baseRepair = (int) (amount * (1 + (rawMaterials - 1) / 9F));
            if (baseRepair <= 0) { break; }
            double adjusted = Math.max(baseRepair * Math.min(10, durabilityFactor), tool.getMaxDamage() / 64.0);
            adjusted *= (1 - usedModifiers * .05) * (1 - (repairCount / 2) / 100.0);
            int healed = Math.min(tool.getDamageValue(), ToolTraits.modifyHealing(tool, (int) Math.ceil(adjusted), null));
            if (healed <= 0) { break; }
            for (RepairMatch match : batch) { consumed[match.slot() - FIRST_MATERIAL_SLOT]++; }
            tool.setDamageValue(tool.getDamageValue() - healed);
            ToolTraits.forEach(tool, (trait, level) -> trait.onRepair(tool, level, healed));
            repairCount = Math.min(100, repairCount + 1);
        }
        tool.setCount(1);
        data = TinkerDataComponents.getMaterialTag(tool);
        data.putInt("RepairCount", repairCount);
        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
        return new RepairPlan(consumed, tool);
    }

    private static boolean isTinkerTool(ItemStack stack) {
        return stack.getItem() instanceof TinkerTools;
    }

    private static boolean isRepairableTool(ItemStack stack) {
        return isTinkerTool(stack)
                && stack.isDamageableItem()
                && stack.getMaxDamage() > 0
                && stack.getDamageValue() > 0;
    }

    private record RepairPlan(int[] repairCount, ItemStack resultTool) {
        boolean isEmpty() {
            for (int count : repairCount) {
                if (count > 0) {
                    return false;
                }
            }
            return true;
        }
    }

    private record RepairMatch(int slot, RepairInfo info, float value, boolean kit) {
    }
}
