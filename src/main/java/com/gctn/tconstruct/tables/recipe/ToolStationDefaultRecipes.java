package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.MaterialList;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import com.gctn.tconstruct.tools.TinkerTools.RepairInfo;
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
        if (!isTinkerTool(tool)) {
            return ItemStack.EMPTY;
        }

        ItemStack enhancedTool = createEnhancementResult(container);
        if (!enhancedTool.isEmpty()) {
            return enhancedTool;
        }

        return createRepairResult(container);
    }

    // 确认后消耗原料
    public static void consumeInputs(Container container) {
        ItemStack tool = container.getItem(TOOL_SLOT);
        if (!isTinkerTool(tool)) {
            return;
        }

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

    // TODO 匹配强化配方
    private static ItemStack createEnhancementResult(Container container) {
        return ItemStack.EMPTY;
    }

    private static void consumeEnhancementInputs(Container container) {
    }

    // 获取修复结果
    private static ItemStack createRepairResult(Container container) {
        ItemStack tool = container.getItem(TOOL_SLOT);
        RepairPlan repairPlan = createRepairPlan(container);
        if (repairPlan == null || repairPlan.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack repairedTool = repairPlan.resultTool.copy();
        repairedTool.setCount(1);
        return repairedTool;
    }

    // 获取修复方案(包括修复量和材料消耗情况)
    private static RepairPlan createRepairPlan(Container container) {

        ItemStack tool = container.getItem(TOOL_SLOT);
        if (!isRepairableTool(tool)) {
            return null;
        }
        tool = tool.copy();
        int remainingDamage = tool.getDamageValue();
        int[] totalRepairAmount = new int[MATERIAL_SLOT_COUNT];

        while (remainingDamage > 0) {
            Collection<RepairMatch> repairMatches = findRepairMatches(container, tool, totalRepairAmount);
            if (repairMatches.isEmpty()) {
                break;
            }

            int previousDamage = remainingDamage;
            long distinctMaterials = repairMatches.stream().map(RepairMatch::materialId).distinct().count();
            double matchEfficiency = 1 + (distinctMaterials - 1) / 9.0;
            for (RepairMatch repairMatch : repairMatches) {
                if (remainingDamage == 0) {
                    break;
                }
                int repairAmount = getRepairAmount(tool, repairMatch.durability, repairMatch.partEfficient, repairMatch.matEfficient, matchEfficiency);
                if (repairAmount <= 0) {
                    continue;
                }
                int slot = repairMatch.slot() - FIRST_MATERIAL_SLOT;
                totalRepairAmount[slot] += 1;
                remainingDamage = Math.max(0, remainingDamage - repairAmount);
                tool.setDamageValue(remainingDamage);
            }
            if (remainingDamage == previousDamage) {
                break;
            }
        }

        return new RepairPlan(totalRepairAmount, tool);
    }

    // 获取修复槽位和对应材料信息
    private static List<RepairMatch> findRepairMatches(Container container, ItemStack tool, int[] totalRepairAmount) {
        List<RepairMatch> repairMatches = new ArrayList<>();
        TinkerTools tinkerTool = (TinkerTools) tool.getItem();
        Set<String> checkedMaterialIds = new HashSet<>();
        List<RepairInfo> repairInfos = tinkerTool.getRepairInfo(tool);

        for (RepairInfo repairInfo : repairInfos) {
            Material material = repairInfo.material();
            if (material == null || material.identifier == null || repairInfo.partEfficient() <= 0.0F) {
                continue;
            }
            // 每种材料只计算一次
            if (!checkedMaterialIds.add(material.identifier)) {
                continue;
            }

            List<RepairMatch> repairMatch = findInputMatch(container, repairInfo, totalRepairAmount);
            if (!repairMatch.isEmpty()) {
                repairMatches.addAll(repairMatch);
            }
        }

        return repairMatches;
    }

    // 获取可用于修复的槽位和材料
    private static List<RepairMatch> findInputMatch(Container container, RepairInfo repairInfo, int[] totalRepairAmount) {
        Material material = repairInfo.material();
        float partEfficient = repairInfo.partEfficient();

        List<RepairMatch> repairMatches = new ArrayList<>();
        for (int slot = FIRST_MATERIAL_SLOT; slot <= LAST_MATERIAL_SLOT; slot++) {
            ItemStack materialStack = container.getItem(slot);
            if (materialStack.isEmpty()) {
                continue;
            }
            if(materialStack.getCount() <= totalRepairAmount[slot - FIRST_MATERIAL_SLOT]) {
                continue;
            }
            double matEfficient = MaterialList.getMaterialValue(material, materialStack);
            if (matEfficient > 0.0) {
                repairMatches.add(new RepairMatch(slot, material.identifier, repairInfo.durability(), partEfficient, matEfficient));
            }
        }

        return repairMatches;
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

    private static int getRepairAmount(ItemStack tool, int materialDurability, float partEfficient, double repairEfficient, double matchEfficient) {
        int base = Math.max(1, (int) Math.ceil(materialDurability * partEfficient * repairEfficient * matchEfficient));
        ToolTraits.forEach(tool, (trait, level) -> trait.onRepair(tool, level, base));
        return ToolTraits.modifyHealing(tool, base, null);
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

    private record RepairMatch(int slot, String materialId, int durability, float partEfficient, double matEfficient) {
    }
}
