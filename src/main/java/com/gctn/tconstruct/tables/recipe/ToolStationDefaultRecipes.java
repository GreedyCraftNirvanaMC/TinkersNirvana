package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolModifiers;
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

    // 预览结果
    public static ItemStack createResult(Container container) {
        ItemStack tool = container.getItem(TOOL_SLOT);
        var special = StationProcessingRecipes.plan(container);
        if (special != null) { return special.result(); }
        if (tool.getItem() instanceof com.gctn.tconstruct.tools.PatternItem) {
            return PartBuilderRecipes.createResult(container);
        }
        if (!isTinkerTool(tool)) {
            return ItemStack.EMPTY;
        }

        var replacement = com.gctn.tconstruct.tools.ToolPartReplacement.plan(container);
        if (replacement != null) { return replacement.result(); }

        ItemStack enhancedTool = createEnhancementResult(container);
        if (!enhancedTool.isEmpty()) {
            return enhancedTool;
        }

        return createRepairResult(container);
    }

    // 确认后消耗原料
    public static void consumeInputs(Container container) {
        var special = StationProcessingRecipes.plan(container);
        if (special != null) { special.consume(container); return; }
        ItemStack tool = container.getItem(TOOL_SLOT);
        if (tool.getItem() instanceof com.gctn.tconstruct.tools.PatternItem) {
            PartBuilderRecipes.consumeInputs(container);
            return;
        }
        if (!isTinkerTool(tool)) {
            return;
        }

        var replacement = com.gctn.tconstruct.tools.ToolPartReplacement.plan(container);
        if (replacement != null) { replacement.consume(container); return; }

        ItemStack enhancedTool = createEnhancementResult(container);
        if (!enhancedTool.isEmpty()) {
            consumeEnhancementInputs(container);
            return;
        }

        RepairPlan repairPlan = createRepairPlan(container);
        if (repairPlan == null || repairPlan.isEmpty()) {
            return;
        }

        container.setItem(TOOL_SLOT, ItemStack.EMPTY);
        for (int i = 0; i < MATERIAL_SLOT_COUNT; i++) {
            if (repairPlan.repairCount[i] > 0) {
                container.removeItem(FIRST_MATERIAL_SLOT + i, repairPlan.repairCount[i]);
            }
        }
    }

    // 强化预览与提交共用同一计划。
    private static ItemStack createEnhancementResult(Container container) {
        var plan = com.gctn.tconstruct.tools.ToolModifiers.plan(container);
        return plan == null ? ItemStack.EMPTY : plan.result();
    }

    private static void consumeEnhancementInputs(Container container) {
        var plan = com.gctn.tconstruct.tools.ToolModifiers.plan(container);
        if (plan != null) { plan.consume(container); }
    }

    // 获取修复结果
    private static ItemStack createRepairResult(Container container) {
        return repairResult(container, LAST_MATERIAL_SLOT);
    }

    /** Shared by stations and the native crafting-grid sharpening-kit recipe. Slot zero is the tool. */
    public static ItemStack repairResult(Container container, int lastMaterialSlot) {
        if (lastMaterialSlot < 1 || lastMaterialSlot >= container.getContainerSize()) { return ItemStack.EMPTY; }
        RepairPlan repairPlan = createRepairPlan(container, lastMaterialSlot);
        if (repairPlan == null || repairPlan.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack repairedTool = repairPlan.resultTool.copy();
        repairedTool.setCount(1);
        return repairedTool;
    }

    // 获取修复方案(包括修复量和材料消耗情况)
    private static RepairPlan createRepairPlan(Container container) {
        return createRepairPlan(container, LAST_MATERIAL_SLOT);
    }

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
            matches.add(new RepairMatch(slot, info, input.value()));
        }

        int[] consumed = new int[lastMaterialSlot];
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
            var batch = new LinkedHashMap<RepairInfo, RepairMatch>();
            for (RepairMatch match : matches) {
                if (consumed[match.slot() - FIRST_MATERIAL_SLOT] < container.getItem(match.slot()).getCount()) {
                    batch.putIfAbsent(match.info(), match);
                }
            }
            if (batch.isEmpty()) {
                break;
            }
            // 同种材料每轮只算一次。材料属性决定基础修复量，再计入多材料加成和修复衰减。
            double amount = batch.values().stream()
                    .mapToDouble(match -> match.info().durability() * match.info().partEfficient() * match.value()).sum();
            amount *= 1 + (batch.size() - 1) / 9.0;
            amount = Math.max((int) amount * Math.min(10, durabilityFactor), tool.getMaxDamage() / 64.0);
            amount *= (1 - usedModifiers * .05) * (1 - (repairCount / 2) / 100.0);
            int healed = Math.min(tool.getDamageValue(), ToolTraits.modifyHealing(tool, (int) Math.ceil(amount), null));
            if (healed <= 0) {
                break;
            }
            for (RepairMatch match : batch.values()) {
                consumed[match.slot() - FIRST_MATERIAL_SLOT]++;
            }
            tool.setDamageValue(tool.getDamageValue() - healed);
            ToolTraits.forEach(tool, (trait, level) -> trait.onRepair(tool, level, healed));
            repairCount = Math.min(100, repairCount + 1);
        }
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

    private record RepairMatch(int slot, RepairInfo info, double value) {
    }
}
