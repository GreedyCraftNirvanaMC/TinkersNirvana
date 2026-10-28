package com.gctn.tconstruct.library.toolparts;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.materials.MaterialValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Collection;
import java.util.List;

/**
 * 只为可见且具有头部属性的材料生成镐头变体。
 * 根据保存的材料给共享模型着色，无需为每种材料分别注册物品。
 */
public class PickaxeHead extends ToolPart {
    public PickaxeHead(int cost) {
        super(cost);
    }

    public static final DeferredItem<Item> PICKAXEHEAD = ITEMS.register("parts/pickaxe_head",
            () -> new PickaxeHead(MaterialValue.VALUE_Ingot * 2));

    /** 显式触发本类的静态注册条目初始化。 */
    public static void init() {
    }

    public static void getAllColoredParts(Collection<ItemStack> coloredParts) {
        List<Material> materials = TinkerMaterials.materials;
        for (Material material : materials) {
            if (material.isHidden() || !material.hasStats("Head")) { continue; }
            ItemStack stack = getColoredPart(material);
            coloredParts.add(stack);
        }
    }

    public static ItemStack getColoredPart(Material material) {
        ItemStack stack = new ItemStack(PICKAXEHEAD.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(Tags.PART_MATERIAL, material.identifier);
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_name",
                Component.translatable("material." + material.identifier + ".name"),
                Component.translatable("item.tconstruct.pickaxe_head.name")));
        return stack;
    }

}
