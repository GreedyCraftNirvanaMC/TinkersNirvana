package com.gctn.tconstruct.client.tools.tooltips; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.stats.HeadMaterialStats; // 引入 com.gctn.tconstruct.library.stats.HeadMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.utils.HarvestLevels; // 引入 com.gctn.tconstruct.library.utils.HarvestLevels，供本文件使用。
import com.gctn.tconstruct.library.utils.ToolHelper; // 引入 com.gctn.tconstruct.library.utils.ToolHelper，供本文件使用。
import com.gctn.tconstruct.tools.TinkerTools; // 引入 com.gctn.tconstruct.tools.TinkerTools，供本文件使用。
import com.gctn.tconstruct.tools.traits.ToolTraits; // 引入 com.gctn.tconstruct.tools.traits.ToolTraits，供本文件使用。
import com.mojang.datafixers.util.Either; // 引入 com.mojang.datafixers.util.Either，供本文件使用。
import net.minecraft.client.gui.screens.Screen; // 引入 net.minecraft.client.gui.screens.Screen，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.core.component.DataComponents; // 引入 net.minecraft.core.component.DataComponents，供本文件使用。
import net.minecraft.world.entity.EquipmentSlot; // 引入 net.minecraft.world.entity.EquipmentSlot，供本文件使用。
import net.minecraft.world.entity.ai.attributes.Attributes; // 引入 net.minecraft.world.entity.ai.attributes.Attributes，供本文件使用。
import net.minecraft.world.item.component.ItemAttributeModifiers; // 引入 net.minecraft.world.item.component.ItemAttributeModifiers，供本文件使用。
import net.minecraft.world.level.block.Blocks; // 引入 net.minecraft.world.level.block.Blocks，供本文件使用。
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

import static com.gctn.tconstruct.tools.tools.Pickaxe.PICKAXE; // 引入 com.gctn.tconstruct.tools.tools.Pickaxe.PICKAXE，供本文件使用。

/**
 * 显示组装工具的词条，并根据按键选择展示整体属性或各部件信息。
 * 速度和攻击值读取物品栈的实际组件，可能与当前材料默认值不同。
 * 词条说明和部件贡献分别从整体与部件角度展示同一把工具。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public class PickaxeTips { // 定义 PickaxeTips 的实现类型，具体职责见上方说明。
    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) { // 定义 onGatherTooltipComponents 的调用入口，参数和具体处理见下方实现。
        ItemStack stack = event.getItemStack(); // 设置 stack 为 event.getItemStack()。
        if (!stack.is(PICKAXE.get())) { return; } // 检查 !stack.is(PICKAXE.get())；按条件决定是否执行本分支。
        List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements(); // 调用 getTooltipElements，执行当前流程所需的处理。

        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack); // 设置 materials 为 TinkerDataComponents.getMaterialTag(stack)。
        List<Either<FormattedText, TooltipComponent>> upperTips = new ArrayList<>(); // 向当前声明或调用传入 List<Either<FormattedText, TooltipComponent>> upperTips = new ArrayList<>()。

        String headMaterialId = materials.getString("Head"); // 设置 headMaterialId 为 materials.getString("Head")。
        Material headMaterial = TinkerMaterials.getMaterial(headMaterialId); // 设置 headMaterial 为 TinkerMaterials.getMaterial(headMaterialId)。
        String handleMaterialId = materials.getString("Handle"); // 设置 handleMaterialId 为 materials.getString("Handle")。
        Material handleMaterial = TinkerMaterials.getMaterial(handleMaterialId); // 设置 handleMaterial 为 TinkerMaterials.getMaterial(handleMaterialId)。
        String bindingMaterialId = materials.getString("Binding"); // 设置 bindingMaterialId 为 materials.getString("Binding")。
        Material bindingMaterial = TinkerMaterials.getMaterial(bindingMaterialId); // 设置 bindingMaterial 为 TinkerMaterials.getMaterial(bindingMaterialId)。
        if (headMaterial == null || handleMaterial == null || bindingMaterial == null) { return; } // 检查 headMaterial == null || handleMaterial == null || bindingMaterial == null；按条件决定是否执行本分支。

        HeadMaterialStats headStats = headMaterial.getStats("Head", HeadMaterialStats.class); // 设置 headStats 为 headMaterial.getStats("Head", HeadMaterialStats.class)。
        if (headStats == null) { return; } // 检查 headStats == null；按条件决定是否执行本分支。

        for (ToolTraits.Entry entry : ToolTraits.getTraits(stack)) { // 遍历 ToolTraits.Entry entry : ToolTraits.getTraits(stack)，逐项执行当前处理。
            upperTips.add(Either.left(entry.trait().getDisplayName(entry.level()))); // 向集合或构建器追加条目。
        } // 结束当前作用域。
        upperTips.add(Either.left(Component.empty())); // 向集合或构建器追加条目。

        if (Screen.hasShiftDown()) { // 检查 Screen.hasShiftDown()；按条件决定是否执行本分支。
            Component durabilityTip = TinkerTools.getDurabilityTip(stack); // 设置 durabilityTip 为 TinkerTools.getDurabilityTip(stack)。
            Component miningLevelTip = Component.translatable("tooltip.tconstruct.tool.mininglevel", // 设置 miningLevelTip 为 Component.translatable("tooltip.tconstruct.tool.mininglevel",。
                    HarvestLevels.getHarvestLevelName(headStats.harvestLevel) // 调用 getHarvestLevelName，执行当前流程所需的处理。
            ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
            Component miningSpeedTip = Component.translatable("tooltip.tconstruct.tool.miningspeed", // 设置 miningSpeedTip 为 Component.translatable("tooltip.tconstruct.tool.miningspeed",。
                    Component.literal(String.valueOf(stack.getDestroySpeed(Blocks.STONE.defaultBlockState()))).withColor(0xFF78A0CD) // 调用 literal，执行当前流程所需的处理。
            ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
            Component attackTip = Component.translatable("tooltip.tconstruct.tool.attack", // 设置 attackTip 为 Component.translatable("tooltip.tconstruct.tool.attack",。
                    Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(getAttackBonus(stack))).withColor(0xFFD76464) // 调用 literal，执行当前流程所需的处理。
            ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
            Component modifiersTip = Component.translatable("tooltip.tconstruct.tool.modifiers", // 设置 modifiersTip 为 Component.translatable("tooltip.tconstruct.tool.modifiers",。
                    ((TinkerTools) stack.getItem()).getAvailableModifierSlots(stack) // 调用 getItem，执行当前流程所需的处理。
            ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
            upperTips.add(Either.left(durabilityTip)); // 向集合或构建器追加条目。
            upperTips.add(Either.left(miningLevelTip)); // 向集合或构建器追加条目。
            upperTips.add(Either.left(miningSpeedTip)); // 向集合或构建器追加条目。
            upperTips.add(Either.left(attackTip)); // 向集合或构建器追加条目。
            upperTips.add(Either.left(modifiersTip)); // 向集合或构建器追加条目。
        } else if (Screen.hasControlDown()) { // 前一条件不成立时继续检查本分支条件。
            addPartInfo(upperTips, handleMaterial, "Handle", "handle"); // 调用 addPartInfo，执行当前流程所需的处理。
            addPartInfo(upperTips, headMaterial, "Head", "head"); // 调用 addPartInfo，执行当前流程所需的处理。
            addPartInfo(upperTips, bindingMaterial, "Extra", "binding"); // 调用 addPartInfo，执行当前流程所需的处理。
        } else if (Screen.hasAltDown()) { // 前一条件不成立时继续检查本分支条件。
            for (ToolTraits.Entry entry : ToolTraits.getTraits(stack)) { // 遍历 ToolTraits.Entry entry : ToolTraits.getTraits(stack)，逐项执行当前处理。
                upperTips.add(Either.left(entry.trait().getDescription())); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } else { // 处理前述条件均不满足时的替代分支。
            upperTips.add(Either.left(TinkerTools.shiftTip)); // 向集合或构建器追加条目。
            upperTips.add(Either.left(TinkerTools.controlTip)); // 向集合或构建器追加条目。
            upperTips.add(Either.left(Component.translatable("tooltip.tconstruct.hold_for_traits", "Alt") // 向集合或构建器追加条目。
                    .withColor(0xFFAAAAAA))); // 调用 withColor，执行当前流程所需的处理。
        } // 结束当前作用域。
        if (ToolHelper.hasCreativeTabTip(tooltip, stack)) { // 检查 ToolHelper.hasCreativeTabTip(tooltip, stack)；按条件决定是否执行本分支。
            upperTips.add(Either.left(Component.empty())); // 向集合或构建器追加条目。
        } // 结束当前作用域。
        tooltip.addAll(1, upperTips); // 合并当前批次的全部条目。
    } // 结束当前作用域。
    private static double getAttackBonus(ItemStack stack) { // 定义 getAttackBonus 的调用入口，参数和具体处理见下方实现。
        if (TinkerTools.isBroken(stack)) { // 检查 TinkerTools.isBroken(stack)；按条件决定是否执行本分支。
            return 0; // 返回 0，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        ItemAttributeModifiers attributes = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY); // 设置 attributes 为 stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)。
        return new ItemAttributeModifiers(attributes.modifiers().stream() // 返回 new ItemAttributeModifiers(attributes.modifiers().stream()，将当前分支的结果交给调用者。
                .filter(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE)).toList(), false) // 按条件筛选条目，只保留符合要求的数据。
                .compute(0, EquipmentSlot.MAINHAND); // 调用 compute，执行当前流程所需的处理。
    } // 结束当前作用域。

    private static void addPartInfo(List<Either<FormattedText, TooltipComponent>> tips, // 定义 addPartInfo 的调用入口，参数和具体处理见下方实现。
                                    Material material, String stat, String label) { // 在 PickaxeTips 中补充当前声明或表达式的 Material material, String stat, String label) { 部分。
        tips.add(Either.left(Component.translatable("tooltip.tconstruct.tool.part", // 向集合或构建器追加条目。
                Component.translatable("tooltip.tconstruct." + label), // 调用 translatable，执行当前流程所需的处理。
                Component.translatable("material." + material.identifier + ".name")).withColor(material.color))); // 调用 translatable，执行当前流程所需的处理。
        for (var trait : material.getTraits(stat)) { // 遍历 var trait : material.getTraits(stat)，逐项执行当前处理。
            tips.add(Either.left(trait.trait().getDisplayName(trait.level()))); // 向集合或构建器追加条目。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
