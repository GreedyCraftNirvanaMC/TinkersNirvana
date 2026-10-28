package com.gctn.tconstruct.client.tools.toolcolors;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.Material;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import static com.gctn.tconstruct.tools.tools.Pickaxe.PICKAXE;

/**
 * 将镐模型的三个图层映射到存档中的手柄、头部和绑定结材料。
 * 材料数据缺失或无法识别时安全回退，不修改或迁移物品栈数据。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public class PickaxeColor {
    public static final ItemColor PICKAXE_COLOR = (stack, tintIndex) -> {
        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack);
        if (!materials.contains("Handle", Tag.TAG_STRING)
                || !materials.contains("Head", Tag.TAG_STRING)
                || !materials.contains("Binding", Tag.TAG_STRING)) {
            return 0xFFFFFFFF;
        }

        return switch (tintIndex) {
            case 0 -> // layer0 → Handle（手柄）
                    getMaterialColor(materials.getString("Handle"));
            case 1 -> // layer1 → Head（头部）
                    getMaterialColor(materials.getString("Head"));
            case 2 -> // layer2 → Binding（绑定）
                    getMaterialColor(materials.getString("Binding"));
            default -> // 异常索引，返回默认色
                    0xFFFFFFFF;
        };
    };

    /**
     * 方法：根据材质ID获取颜色
     * @param materialId 材质标识符（如"iron"、"diamond"）
     * @return 材质对应的颜色，匹配不到返回默认白色
     */
    private static int getMaterialColor(String materialId) {
        Material material = TinkerMaterials.getMaterial(materialId);
        return material == null ? 0xFFFFFFFF : material.color;
    }

    @SubscribeEvent
    public static void registerPickaxeColors(RegisterColorHandlersEvent.Item event) {
        event.register(PICKAXE_COLOR, PICKAXE.get());
    }
}
