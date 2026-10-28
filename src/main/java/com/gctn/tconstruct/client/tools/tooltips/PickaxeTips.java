package com.gctn.tconstruct.client.tools.tooltips;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.HeadMaterialStats;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.library.utils.ToolHelper;
import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.ArrayList;
import java.util.List;

import static com.gctn.tconstruct.tools.tools.Pickaxe.PICKAXE;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class PickaxeTips {
    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(PICKAXE.get())) { return; }
        List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();

        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack);
        List<Either<FormattedText, TooltipComponent>> upperTips = new ArrayList<>();

        String headMaterialId = materials.getString("Head");
        Material headMaterial = TinkerMaterials.getMaterial(headMaterialId);
        String handleMaterialId = materials.getString("Handle");
        Material handleMaterial = TinkerMaterials.getMaterial(handleMaterialId);
        String bindingMaterialId = materials.getString("Binding");
        Material bindingMaterial = TinkerMaterials.getMaterial(bindingMaterialId);
        if (headMaterial == null || handleMaterial == null || bindingMaterial == null) { return; }

        HeadMaterialStats headStats = headMaterial.getStats("Head", HeadMaterialStats.class);
        if (headStats == null) { return; }

        for (ToolTraits.Entry entry : ToolTraits.getTraits(stack)) {
            upperTips.add(Either.left(entry.trait().getDisplayName(entry.level())));
        }
        upperTips.add(Either.left(Component.empty()));

        if (Screen.hasShiftDown()) {
            Component durabilityTip = TinkerTools.getDurabilityTip(stack);
            Component miningLevelTip = Component.translatable("tooltip.tconstruct.tool.mininglevel",
                    HarvestLevels.getHarvestLevelName(headStats.harvestLevel)
            ).withColor(0xFFAAAAAA);
            Component miningSpeedTip = Component.translatable("tooltip.tconstruct.tool.miningspeed",
                    Component.literal(String.valueOf(headStats.miningspeed)).withColor(0xFF78A0CD)
            ).withColor(0xFFAAAAAA);
            Component attackTip = Component.translatable("tooltip.tconstruct.tool.attack",
                    Component.literal(String.valueOf(headStats.attack)).withColor(0xFFD76464)
            ).withColor(0xFFAAAAAA);
            Component modifiersTip = Component.translatable("tooltip.tconstruct.tool.modifiers",
                    ((TinkerTools) stack.getItem()).getAvailableModifierSlots(stack)
            ).withColor(0xFFAAAAAA);
            upperTips.add(Either.left(durabilityTip));
            upperTips.add(Either.left(miningLevelTip));
            upperTips.add(Either.left(miningSpeedTip));
            upperTips.add(Either.left(attackTip));
            upperTips.add(Either.left(modifiersTip));
        } else if (Screen.hasControlDown()) {
            // TODO 按下Ctrl时显示详细信息
        } else if (Screen.hasAltDown()) {
            for (ToolTraits.Entry entry : ToolTraits.getTraits(stack)) {
                upperTips.add(Either.left(entry.trait().getDescription()));
            }
        } else {
            upperTips.add(Either.left(TinkerTools.shiftTip));
            upperTips.add(Either.left(TinkerTools.controlTip));
            upperTips.add(Either.left(Component.translatable("tooltip.tconstruct.hold_for_traits", "Alt")
                    .withColor(0xFFAAAAAA)));
        }
        if (ToolHelper.hasCreativeTabTip(tooltip, stack)) {
            upperTips.add(Either.left(Component.empty()));
        }
        tooltip.addAll(1, upperTips);
    }
}
