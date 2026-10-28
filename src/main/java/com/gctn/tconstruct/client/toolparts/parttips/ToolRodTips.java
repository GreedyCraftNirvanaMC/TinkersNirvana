package com.gctn.tconstruct.client.toolparts.parttips; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.utils.Tags; // 引入 com.gctn.tconstruct.library.utils.Tags，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.stats.HandleMaterialStats; // 引入 com.gctn.tconstruct.library.stats.HandleMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.utils.ToolHelper; // 引入 com.gctn.tconstruct.library.utils.ToolHelper，供本文件使用。
import com.gctn.tconstruct.tools.traits.AbstractTraits; // 引入 com.gctn.tconstruct.tools.traits.AbstractTraits，供本文件使用。
import com.mojang.datafixers.util.Either; // 引入 com.mojang.datafixers.util.Either，供本文件使用。
import net.minecraft.ChatFormatting; // 引入 net.minecraft.ChatFormatting，供本文件使用。
import net.minecraft.client.gui.screens.Screen; // 引入 net.minecraft.client.gui.screens.Screen，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.network.chat.FormattedText; // 引入 net.minecraft.network.chat.FormattedText，供本文件使用。
import net.minecraft.world.inventory.tooltip.TooltipComponent; // 引入 net.minecraft.world.inventory.tooltip.TooltipComponent，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.client.event.RenderTooltipEvent; // 引入 net.neoforged.neoforge.client.event.RenderTooltipEvent，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

import static com.gctn.tconstruct.library.toolparts.ToolRod.TOOL_ROD; // 引入 com.gctn.tconstruct.library.toolparts.ToolRod.TOOL_ROD，供本文件使用。

/**
 * 显示手柄词条，并根据 Shift 或 Alt 展示耐久属性或词条说明。
 * 提示构建只读取数据，展示存档材料前先通过带类型检查的查询取得属性。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public class ToolRodTips { // 定义 ToolRodTips 的实现类型，具体职责见上方说明。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) { // 定义 onGatherTooltipComponents 的调用入口，参数和具体处理见下方实现。
        ItemStack stack = event.getItemStack(); // 设置 stack 为 event.getItemStack()。
        if (!stack.is(TOOL_ROD.get())) { return; } // 检查 !stack.is(TOOL_ROD.get())；按条件决定是否执行本分支。
        List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements(); // 调用 getTooltipElements，执行当前流程所需的处理。

        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL); // 设置 materialId 为 TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL)。
        Material material = TinkerMaterials.getMaterial(materialId); // 设置 material 为 TinkerMaterials.getMaterial(materialId)。
        if (material == null) { return; } // 检查 material == null；按条件决定是否执行本分支。
        HandleMaterialStats handleStats = material.getStats("Handle", HandleMaterialStats.class); // 设置 handleStats 为 material.getStats("Handle", HandleMaterialStats.class)。
        if (handleStats == null) { return; } // 检查 handleStats == null；按条件决定是否执行本分支。

        List<Either<FormattedText, TooltipComponent>> addedTips = new ArrayList<>(); // 向当前声明或调用传入 List<Either<FormattedText, TooltipComponent>> addedTips = new ArrayList<>()。

        for (AbstractTraits.TraitEntry entry : material.getTraits("Handle")) { // 遍历 AbstractTraits.TraitEntry entry : material.getTraits("Handle")，逐项执行当前处理。
            addedTips.add(Either.left(entry.trait().getDisplayName(entry.level()))); // 向集合或构建器追加条目。
        } // 结束当前作用域。
        addedTips.add(Either.left(Component.empty())); // 向集合或构建器追加条目。

        Component shiftTip = Component.translatable( // 设置 shiftTip 为 Component.translatable(。
                "tooltip.tconstruct.hold_for_stats", // 向当前声明或调用传入 "tooltip.tconstruct.hold_for_stats"。
                Component.literal("Shift").withColor(0xFFFFFF55) // 调用 literal，执行当前流程所需的处理。
        ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。

        if (Screen.hasShiftDown()) { // 检查 Screen.hasShiftDown()；按条件决定是否执行本分支。
            Component handleTip = Component.translatable("tooltip.tconstruct.handle") // 设置 handleTip 为 Component.translatable("tooltip.tconstruct.handle")。
                    .withColor(0xFFFFFFFF) // 调用 withColor，执行当前流程所需的处理。
                    .withStyle(ChatFormatting.UNDERLINE); // 调用 withStyle，执行当前流程所需的处理。
            Component modifierTip = Component.translatable( // 设置 modifierTip 为 Component.translatable(。
                    "tooltip.tconstruct.handle.modifier", // 向当前声明或调用传入 "tooltip.tconstruct.handle.modifier"。
                    Component.literal(String.valueOf(handleStats.modifer)).withColor(0xFFB9B95A) // 调用 literal，执行当前流程所需的处理。
            ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
            Component durabilityTip = Component.translatable( // 设置 durabilityTip 为 Component.translatable(。
                    "tooltip.tconstruct.part.durability", // 向当前声明或调用传入 "tooltip.tconstruct.part.durability"。
                    Component.literal(String.valueOf(handleStats.durability)).withColor(0xFF47CC47) // 调用 literal，执行当前流程所需的处理。
            ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
            addedTips.add(Either.left(handleTip)); // 向集合或构建器追加条目。
            addedTips.add(Either.left(modifierTip)); // 向集合或构建器追加条目。
            addedTips.add(Either.left(durabilityTip)); // 向集合或构建器追加条目。
        } else if (Screen.hasAltDown()) { // 前一条件不成立时继续检查本分支条件。
            for (AbstractTraits.TraitEntry entry : material.getTraits("Handle")) { // 遍历 AbstractTraits.TraitEntry entry : material.getTraits("Handle")，逐项执行当前处理。
                addedTips.add(Either.left(entry.trait().getDescription())); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } else { // 处理前述条件均不满足时的替代分支。
            addedTips.add(Either.left(shiftTip)); // 向集合或构建器追加条目。
            if (!material.getTraits("Handle").isEmpty()) { // 检查 !material.getTraits("Handle").isEmpty()；按条件决定是否执行本分支。
                addedTips.add(Either.left(Component.translatable("tooltip.tconstruct.hold_for_traits", "Alt") // 向集合或构建器追加条目。
                        .withColor(0xFFAAAAAA))); // 调用 withColor，执行当前流程所需的处理。
            } // 结束当前作用域。
        } // 结束当前作用域。
        if (ToolHelper.hasCreativeTabTip(tooltip, stack)) { // 检查 ToolHelper.hasCreativeTabTip(tooltip, stack)；按条件决定是否执行本分支。
            addedTips.add(Either.left(Component.empty())); // 向集合或构建器追加条目。
        } // 结束当前作用域。
        tooltip.addAll(1, addedTips); // 合并当前批次的全部条目。
    } // 结束当前作用域。

} // 结束当前作用域。
