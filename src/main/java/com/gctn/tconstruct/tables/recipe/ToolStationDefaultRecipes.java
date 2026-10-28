package com.gctn.tconstruct.tables.recipe; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.MaterialList; // 引入 com.gctn.tconstruct.library.MaterialList，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.tools.TinkerTools; // 引入 com.gctn.tconstruct.tools.TinkerTools，供本文件使用。
import com.gctn.tconstruct.tools.TinkerTools.RepairInfo; // 引入 com.gctn.tconstruct.tools.TinkerTools.RepairInfo，供本文件使用。
import com.gctn.tconstruct.tools.traits.ToolTraits; // 引入 com.gctn.tconstruct.tools.traits.ToolTraits，供本文件使用。
import net.minecraft.world.Container; // 引入 net.minecraft.world.Container，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

import java.util.*; // 引入 java.util.*，供本文件使用。

/**
 * 在副本上规划材料修复，并记录确认时各槽位需要消耗的准确数量。
 * 只有工具认可的材料才参与修复，将同种材料拆到多个槽位不会增加材料种类加成。
 * 回调仅作用于结果副本，刷新或取消预览不能修改输入工具。
 */
public final class ToolStationDefaultRecipes { // 定义 ToolStationDefaultRecipes 的实现类型，具体职责见上方说明。
    private static final int TOOL_SLOT = 0; // 设置 TOOL_SLOT 为 0。
    private static final int FIRST_MATERIAL_SLOT = 1; // 设置 FIRST_MATERIAL_SLOT 为 1。
    private static final int LAST_MATERIAL_SLOT = 5; // 设置 LAST_MATERIAL_SLOT 为 5。
    private static final int MATERIAL_SLOT_COUNT = LAST_MATERIAL_SLOT - FIRST_MATERIAL_SLOT + 1; // 设置 MATERIAL_SLOT_COUNT 为 LAST_MATERIAL_SLOT - FIRST_MATERIAL_SLOT + 1。

    private ToolStationDefaultRecipes() { // 定义 ToolStationDefaultRecipes 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    // 预览结果
    public static ItemStack createResult(Container container) { // 定义 createResult 的调用入口，参数和具体处理见下方实现。
        ItemStack tool = container.getItem(TOOL_SLOT); // 设置 tool 为 container.getItem(TOOL_SLOT)。
        if (!isTinkerTool(tool)) { // 检查 !isTinkerTool(tool)；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        ItemStack enhancedTool = createEnhancementResult(container); // 设置 enhancedTool 为 createEnhancementResult(container)。
        if (!enhancedTool.isEmpty()) { // 检查 !enhancedTool.isEmpty()；按条件决定是否执行本分支。
            return enhancedTool; // 返回 enhancedTool，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        return createRepairResult(container); // 返回 createRepairResult(container)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 确认后消耗原料
    public static void consumeInputs(Container container) { // 定义 consumeInputs 的调用入口，参数和具体处理见下方实现。
        ItemStack tool = container.getItem(TOOL_SLOT); // 设置 tool 为 container.getItem(TOOL_SLOT)。
        if (!isTinkerTool(tool)) { // 检查 !isTinkerTool(tool)；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        ItemStack enhancedTool = createEnhancementResult(container); // 设置 enhancedTool 为 createEnhancementResult(container)。
        if (!enhancedTool.isEmpty()) { // 检查 !enhancedTool.isEmpty()；按条件决定是否执行本分支。
            consumeEnhancementInputs(container); // 调用 consumeEnhancementInputs，执行当前流程所需的处理。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        RepairPlan repairPlan = createRepairPlan(container); // 设置 repairPlan 为 createRepairPlan(container)。
        if (repairPlan == null || repairPlan.isEmpty()) { // 检查 repairPlan == null || repairPlan.isEmpty()；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        container.setItem(TOOL_SLOT, ItemStack.EMPTY); // 更新指定物品槽位。
        for (int i = 0; i < MATERIAL_SLOT_COUNT; i++) { // 遍历 int i = 0; i < MATERIAL_SLOT_COUNT; i++，逐项执行当前处理。
            if (repairPlan.repairCount[i] > 0) { // 检查 repairPlan.repairCount[i] > 0；按条件决定是否执行本分支。
                container.removeItem(FIRST_MATERIAL_SLOT + i, repairPlan.repairCount[i]); // 从指定槽位移除已确认的物品数量。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。

    // TODO 匹配强化配方
    private static ItemStack createEnhancementResult(Container container) { // 定义 createEnhancementResult 的调用入口，参数和具体处理见下方实现。
        return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static void consumeEnhancementInputs(Container container) { // 定义 consumeEnhancementInputs 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    // 获取修复结果
    private static ItemStack createRepairResult(Container container) { // 定义 createRepairResult 的调用入口，参数和具体处理见下方实现。
        RepairPlan repairPlan = createRepairPlan(container); // 设置 repairPlan 为 createRepairPlan(container)。
        if (repairPlan == null || repairPlan.isEmpty()) { // 检查 repairPlan == null || repairPlan.isEmpty()；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        ItemStack repairedTool = repairPlan.resultTool.copy(); // 设置 repairedTool 为 repairPlan.resultTool.copy()。
        repairedTool.setCount(1); // 设置物品栈数量。
        return repairedTool; // 返回 repairedTool，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 获取修复方案(包括修复量和材料消耗情况)
    private static RepairPlan createRepairPlan(Container container) { // 定义 createRepairPlan 的调用入口，参数和具体处理见下方实现。

        ItemStack sourceTool = container.getItem(TOOL_SLOT); // 设置 sourceTool 为 container.getItem(TOOL_SLOT)。
        if (!isRepairableTool(sourceTool)) { // 检查 !isRepairableTool(sourceTool)；按条件决定是否执行本分支。
            return null; // 返回 null，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        // 所有词条计算只作用于副本，预览不能修改输入工具。
        ItemStack tool = sourceTool.copy(); // 设置 tool 为 sourceTool.copy()。
        int remainingDamage = tool.getDamageValue(); // 设置 remainingDamage 为 tool.getDamageValue()。
        int[] totalRepairAmount = new int[MATERIAL_SLOT_COUNT]; // 设置 totalRepairAmount 为 new int[MATERIAL_SLOT_COUNT]。

        // 只记录模拟消耗数量，不减少真实原料，
        // 使预览、校验和确认消耗都能重现同一套确定性方案。
        while (remainingDamage > 0) { // 在 remainingDamage > 0 成立时继续处理。
            Collection<RepairMatch> repairMatches = findRepairMatches(container, tool, totalRepairAmount); // 设置 repairMatches 为 findRepairMatches(container, tool, totalRepairAmount)。
            if (repairMatches.isEmpty()) { // 检查 repairMatches.isEmpty()；按条件决定是否执行本分支。
                break; // 结束当前循环或分支。
            } // 结束当前作用域。

            int previousDamage = remainingDamage; // 设置 previousDamage 为 remainingDamage。
            // 按材料种类计数，而不是按槽位计数，避免将同一堆原料分散到多个
            // 输入槽后获得额外修复效率。
            long distinctMaterials = repairMatches.stream().map(RepairMatch::materialId).distinct().count(); // 设置 distinctMaterials 为 repairMatches.stream().map(RepairMatch::materialId).distinct().count()。
            double matchEfficiency = 1 + (distinctMaterials - 1) / 9.0; // 设置 matchEfficiency 为 1 + (distinctMaterials - 1) / 9.0。
            for (RepairMatch repairMatch : repairMatches) { // 遍历 RepairMatch repairMatch : repairMatches，逐项执行当前处理。
                if (remainingDamage == 0) { // 检查 remainingDamage == 0；按条件决定是否执行本分支。
                    break; // 结束当前循环或分支。
                } // 结束当前作用域。
                int repairAmount = getRepairAmount(tool, repairMatch.durability, repairMatch.partEfficient, repairMatch.matEfficient, matchEfficiency); // 设置 repairAmount 为 getRepairAmount(tool, repairMatch.durability, repairMatch.partEfficient, repairMatch.matEfficient, matchEff…。
                if (repairAmount <= 0) { // 检查 repairAmount <= 0；按条件决定是否执行本分支。
                    continue; // 跳过当前条目，继续处理下一条。
                } // 结束当前作用域。
                int healedAmount = Math.min(remainingDamage, repairAmount); // 设置 healedAmount 为 Math.min(remainingDamage, repairAmount)。
                int slot = repairMatch.slot() - FIRST_MATERIAL_SLOT; // 设置 slot 为 repairMatch.slot() - FIRST_MATERIAL_SLOT。
                totalRepairAmount[slot] += 1; // 将当前计算结果累积到 totalRepairAmount[slot]，保持本轮状态一致。
                remainingDamage -= healedAmount; // 将当前计算结果累积到 remainingDamage，保持本轮状态一致。
                tool.setDamageValue(remainingDamage); // 写入当前工具的耐久损耗。
                ToolTraits.forEach(tool, (trait, level) -> trait.onRepair(tool, level, healedAmount)); // 定义回调或映射规则，在对应条目被处理时执行。
            } // 结束当前作用域。
            if (remainingDamage == previousDamage) { // 检查 remainingDamage == previousDamage；按条件决定是否执行本分支。
                break; // 结束当前循环或分支。
            } // 结束当前作用域。
        } // 结束当前作用域。

        return new RepairPlan(totalRepairAmount, tool); // 返回 new RepairPlan(totalRepairAmount, tool)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 获取修复槽位和对应材料信息
    private static List<RepairMatch> findRepairMatches(Container container, ItemStack tool, int[] totalRepairAmount) { // 定义 findRepairMatches 的调用入口，参数和具体处理见下方实现。
        List<RepairMatch> repairMatches = new ArrayList<>(); // 设置 repairMatches 为 new ArrayList<>()。
        TinkerTools tinkerTool = (TinkerTools) tool.getItem(); // 设置 tinkerTool 为 (TinkerTools) tool.getItem()。
        Set<String> checkedMaterialIds = new HashSet<>(); // 设置 checkedMaterialIds 为 new HashSet<>()。
        List<RepairInfo> repairInfos = tinkerTool.getRepairInfo(tool); // 设置 repairInfos 为 tinkerTool.getRepairInfo(tool)。

        for (RepairInfo repairInfo : repairInfos) { // 遍历 RepairInfo repairInfo : repairInfos，逐项执行当前处理。
            Material material = repairInfo.material(); // 设置 material 为 repairInfo.material()。
            if (material == null || material.identifier == null || repairInfo.partEfficient() <= 0.0F) { // 检查 material == null || material.identifier == null || repairInfo.partEfficient() <= 0.0F；按条件决定是否执行本分支。
                continue; // 跳过当前条目，继续处理下一条。
            } // 结束当前作用域。
            // 每种材料只计算一次
            if (!checkedMaterialIds.add(material.identifier)) { // 检查 !checkedMaterialIds.add(material.identifier)；按条件决定是否执行本分支。
                continue; // 跳过当前条目，继续处理下一条。
            } // 结束当前作用域。

            List<RepairMatch> repairMatch = findInputMatch(container, repairInfo, totalRepairAmount); // 设置 repairMatch 为 findInputMatch(container, repairInfo, totalRepairAmount)。
            if (!repairMatch.isEmpty()) { // 检查 !repairMatch.isEmpty()；按条件决定是否执行本分支。
                repairMatches.addAll(repairMatch); // 合并当前批次的全部条目。
            } // 结束当前作用域。
        } // 结束当前作用域。

        return repairMatches; // 返回 repairMatches，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 获取可用于修复的槽位和材料
    private static List<RepairMatch> findInputMatch(Container container, RepairInfo repairInfo, int[] totalRepairAmount) { // 定义 findInputMatch 的调用入口，参数和具体处理见下方实现。
        Material material = repairInfo.material(); // 设置 material 为 repairInfo.material()。
        float partEfficient = repairInfo.partEfficient(); // 设置 partEfficient 为 repairInfo.partEfficient()。

        List<RepairMatch> repairMatches = new ArrayList<>(); // 设置 repairMatches 为 new ArrayList<>()。
        for (int slot = FIRST_MATERIAL_SLOT; slot <= LAST_MATERIAL_SLOT; slot++) { // 遍历 int slot = FIRST_MATERIAL_SLOT; slot <= LAST_MATERIAL_SLOT; slot++，逐项执行当前处理。
            ItemStack materialStack = container.getItem(slot); // 设置 materialStack 为 container.getItem(slot)。
            if (materialStack.isEmpty()) { // 检查 materialStack.isEmpty()；按条件决定是否执行本分支。
                continue; // 跳过当前条目，继续处理下一条。
            } // 结束当前作用域。
            if(materialStack.getCount() <= totalRepairAmount[slot - FIRST_MATERIAL_SLOT]) { // 检查 materialStack.getCount() <= totalRepairAmount[slot - FIRST_MATERIAL_SLOT]；按条件决定是否执行本分支。
                continue; // 跳过当前条目，继续处理下一条。
            } // 结束当前作用域。
            double matEfficient = MaterialList.getMaterialValue(material, materialStack); // 设置 matEfficient 为 MaterialList.getMaterialValue(material, materialStack)。
            if (matEfficient > 0.0) { // 检查 matEfficient > 0.0；按条件决定是否执行本分支。
                repairMatches.add(new RepairMatch(slot, material.identifier, repairInfo.durability(), partEfficient, matEfficient)); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } // 结束当前作用域。

        return repairMatches; // 返回 repairMatches，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static boolean isTinkerTool(ItemStack stack) { // 定义 isTinkerTool 的调用入口，参数和具体处理见下方实现。
        return stack.getItem() instanceof TinkerTools; // 返回 stack.getItem() instanceof TinkerTools，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static boolean isRepairableTool(ItemStack stack) { // 定义 isRepairableTool 的调用入口，参数和具体处理见下方实现。
        return isTinkerTool(stack) // 返回 isTinkerTool(stack)，将当前分支的结果交给调用者。
                && stack.isDamageableItem() // 调用 isDamageableItem，执行当前流程所需的处理。
                && stack.getMaxDamage() > 0 // 调用 getMaxDamage，执行当前流程所需的处理。
                && stack.getDamageValue() > 0; // 调用 getDamageValue，执行当前流程所需的处理。
    } // 结束当前作用域。

    private static int getRepairAmount(ItemStack tool, int materialDurability, float partEfficient, double repairEfficient, double matchEfficient) { // 定义 getRepairAmount 的调用入口，参数和具体处理见下方实现。
        int base = Math.max(1, (int) Math.ceil(materialDurability * partEfficient * repairEfficient * matchEfficient)); // 设置 base 为 Math.max(1, (int) Math.ceil(materialDurability * partEfficient * repairEfficient * matchEfficient))。
        // 此处只计算恢复量，onRepair 在 createRepairPlan 中应用
        // 实际恢复量之后仅调用一次，实际恢复量可能受到剩余损耗上限限制。
        return ToolTraits.modifyHealing(tool, base, null); // 返回 ToolTraits.modifyHealing(tool, base, null)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private record RepairPlan(int[] repairCount, ItemStack resultTool) { // 定义 RepairPlan 的数据记录，具体职责见上方说明。
        boolean isEmpty() { // 调用 isEmpty，执行当前流程所需的处理。
            for (int count : repairCount) { // 遍历 int count : repairCount，逐项执行当前处理。
                if (count > 0) { // 检查 count > 0；按条件决定是否执行本分支。
                    return false; // 返回 false，将当前分支的结果交给调用者。
                } // 结束当前作用域。
            } // 结束当前作用域。
            return true; // 返回 true，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。

    private record RepairMatch(int slot, String materialId, int durability, float partEfficient, double matEfficient) { // 定义 RepairMatch 的数据记录，具体职责见上方说明。
    } // 结束当前作用域。
} // 结束当前作用域。
