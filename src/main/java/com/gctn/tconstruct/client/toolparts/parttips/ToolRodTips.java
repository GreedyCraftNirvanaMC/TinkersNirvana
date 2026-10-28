package com.gctn.tconstruct.client.toolparts.parttips;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.HandleMaterialStats;
import com.gctn.tconstruct.library.utils.ToolHelper;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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

import static com.gctn.tconstruct.library.toolparts.ToolRod.TOOL_ROD;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class ToolRodTips {

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(TOOL_ROD.get())) { return; }
        List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();

        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL);
        Material material = TinkerMaterials.getMaterial(materialId);
        if (material == null) { return; }
        HandleMaterialStats handleStats = material.getStats("Handle", HandleMaterialStats.class);
        if (handleStats == null) { return; }

        List<Either<FormattedText, TooltipComponent>> addedTips = new ArrayList<>();

        for (AbstractTraits trait : material.getTraits("Handle")) {
            Component traitsTip = trait.getDisplayName(trait.level);
            addedTips.add(Either.left(traitsTip));
        }
        addedTips.add(Either.left(Component.empty()));

        Component shiftTip = Component.translatable(
                "tooltip.tconstruct.hold_for_stats",
                Component.literal("Shift").withColor(0xFFFFFF55)
        ).withColor(0xFFAAAAAA);

        if (Screen.hasShiftDown()) {
            Component handleTip = Component.translatable("tooltip.tconstruct.handle")
                    .withColor(0xFFFFFFFF)
                    .withStyle(ChatFormatting.UNDERLINE);
            Component modifierTip = Component.translatable(
                    "tooltip.tconstruct.handle.modifier",
                    Component.literal(String.valueOf(handleStats.modifer)).withColor(0xFFB9B95A)
            ).withColor(0xFFAAAAAA);
            Component durabilityTip = Component.translatable(
                    "tooltip.tconstruct.part.durability",
                    Component.literal(String.valueOf(handleStats.durability)).withColor(0xFF47CC47)
            ).withColor(0xFFAAAAAA);
            addedTips.add(Either.left(handleTip));
            addedTips.add(Either.left(modifierTip));
            addedTips.add(Either.left(durabilityTip));
        } else if (Screen.hasAltDown()) {
            for (AbstractTraits trait : material.getTraits("Handle")) {
                addedTips.add(Either.left(trait.getDescription()));
            }
        } else {
            addedTips.add(Either.left(shiftTip));
            addedTips.add(Either.left(Component.translatable("tooltip.tconstruct.hold_for_traits", "Alt").withColor(0xFFAAAAAA)));
        }
        if (ToolHelper.hasCreativeTabTip(tooltip, stack)) {
            addedTips.add(Either.left(Component.empty()));
        }
        tooltip.addAll(1, addedTips);
    }

}
