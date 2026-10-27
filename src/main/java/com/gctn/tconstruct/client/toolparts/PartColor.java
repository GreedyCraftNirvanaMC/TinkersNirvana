package com.gctn.tconstruct.client.toolparts;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.library.materials.Material;
import net.minecraft.client.color.item.ItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import static com.gctn.tconstruct.library.toolparts.Binding.BINDING;
import static com.gctn.tconstruct.library.toolparts.PickaxeHead.PICKAXEHEAD;
import static com.gctn.tconstruct.library.toolparts.ToolRod.TOOL_ROD;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class PartColor {
    public static final ItemColor PART_COLOR = (stack, tintIndex) -> {
        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL);
        for (Material material : TinkerMaterials.materials) {
            if (material.identifier.equals(materialId)) {
                return material.color;
            }
        }

        return 0xFFFFFFFF;
    };

    @SubscribeEvent
    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(PART_COLOR, BINDING.get());
        event.register(PART_COLOR, PICKAXEHEAD.get());
        event.register(PART_COLOR, TOOL_ROD.get());
    }

}
