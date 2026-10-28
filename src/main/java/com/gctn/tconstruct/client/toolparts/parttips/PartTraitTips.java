package com.gctn.tconstruct.client.toolparts.parttips;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.mojang.datafixers.util.Either;
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

/** 手柄仍由 ToolRodTips 展示属性；此处只负责头部和绑定结，避免重复添加。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class PartTraitTips {
    private PartTraitTips() {}

    @SubscribeEvent
    public static void gatherTraits(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        String stat;
        if (stack.is(PickaxeHead.PICKAXEHEAD.get())) {
            stat = "Head";
        } else if (stack.is(Binding.BINDING.get())) {
            stat = "Extra";
        } else {
            return;
        }
        Material material = TinkerMaterials.getMaterial(
                TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL));
        if (material == null || !material.hasStats(stat)) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> tips = new ArrayList<>();
        for (AbstractTraits.TraitEntry entry : material.getTraits(stat)) {
            tips.add(Either.left(entry.trait().getDisplayName(entry.level())));
            if (Screen.hasAltDown()) {
                tips.add(Either.left(entry.trait().getDescription()));
            }
        }
        if (!Screen.hasAltDown() && !tips.isEmpty()) {
            tips.add(Either.left(Component.translatable("tooltip.tconstruct.hold_for_traits", "Alt")
                    .withColor(0xFFAAAAAA)));
        }
        event.getTooltipElements().addAll(1, tips);
    }
}
