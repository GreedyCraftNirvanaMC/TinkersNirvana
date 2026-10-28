package com.gctn.tconstruct.client.toolparts.parttips; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.toolparts.Binding; // 引入 com.gctn.tconstruct.library.toolparts.Binding，供本文件使用。
import com.gctn.tconstruct.library.toolparts.PickaxeHead; // 引入 com.gctn.tconstruct.library.toolparts.PickaxeHead，供本文件使用。
import com.gctn.tconstruct.library.utils.Tags; // 引入 com.gctn.tconstruct.library.utils.Tags，供本文件使用。
import com.gctn.tconstruct.tools.traits.AbstractTraits; // 引入 com.gctn.tconstruct.tools.traits.AbstractTraits，供本文件使用。
import com.mojang.datafixers.util.Either; // 引入 com.mojang.datafixers.util.Either，供本文件使用。
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

/**
 * 在客户端显示镐头和绑定结的词条名称，按 Alt 时额外显示说明。
 * 手柄提示由 ToolRodTips 单独处理，避免重复添加同一词条。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public final class PartTraitTips { // 定义 PartTraitTips 的实现类型，具体职责见上方说明。
    private PartTraitTips() {} // 定义 PartTraitTips 的调用入口；此处无需额外处理。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void gatherTraits(RenderTooltipEvent.GatherComponents event) { // 定义 gatherTraits 的调用入口，参数和具体处理见下方实现。
        ItemStack stack = event.getItemStack(); // 设置 stack 为 event.getItemStack()。
        String stat; // 在 PartTraitTips 中补充当前声明或表达式的 String stat 部分。
        if (stack.is(PickaxeHead.PICKAXEHEAD.get())) { // 检查 stack.is(PickaxeHead.PICKAXEHEAD.get())；按条件决定是否执行本分支。
            stat = "Head"; // 设置 stat 为 "Head"。
        } else if (stack.is(Binding.BINDING.get())) { // 前一条件不成立时继续检查本分支条件。
            stat = "Extra"; // 设置 stat 为 "Extra"。
        } else { // 处理前述条件均不满足时的替代分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        Material material = TinkerMaterials.getMaterial( // 设置 material 为 TinkerMaterials.getMaterial(。
                TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL)); // 取得材料数据副本。
        if (material == null || !material.hasStats(stat)) { // 检查 material == null || !material.hasStats(stat)；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        List<Either<FormattedText, TooltipComponent>> tips = new ArrayList<>(); // 向当前声明或调用传入 List<Either<FormattedText, TooltipComponent>> tips = new ArrayList<>()。
        for (AbstractTraits.TraitEntry entry : material.getTraits(stat)) { // 遍历 AbstractTraits.TraitEntry entry : material.getTraits(stat)，逐项执行当前处理。
            tips.add(Either.left(entry.trait().getDisplayName(entry.level()))); // 向集合或构建器追加条目。
            if (Screen.hasAltDown()) { // 检查 Screen.hasAltDown()；按条件决定是否执行本分支。
                tips.add(Either.left(entry.trait().getDescription())); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } // 结束当前作用域。
        if (!Screen.hasAltDown() && !tips.isEmpty()) { // 检查 !Screen.hasAltDown() && !tips.isEmpty()；按条件决定是否执行本分支。
            tips.add(Either.left(Component.translatable("tooltip.tconstruct.hold_for_traits", "Alt") // 向集合或构建器追加条目。
                    .withColor(0xFFAAAAAA))); // 调用 withColor，执行当前流程所需的处理。
        } // 结束当前作用域。
        event.getTooltipElements().addAll(1, tips); // 合并当前批次的全部条目。
    } // 结束当前作用域。
} // 结束当前作用域。
