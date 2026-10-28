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
 * 创建镐组装与拆解使用的绑定结部件。
 * 绑定结材料提供额外耐久和词条，采掘行为由头部材料决定。
 */
public class Binding extends ToolPart {
    public Binding(int cost) {
        super(cost);
    }

    public static final DeferredItem<Item> BINDING = ITEMS.register("parts/binding",
            () -> new Binding(MaterialValue.VALUE_Ingot));

    /** 显式触发本类的静态注册条目初始化。 */
    public static void init() {
    }

    public static void getAllColoredParts(Collection<ItemStack> coloredParts) {
        List<Material> materials = TinkerMaterials.materials;
        for (Material material : materials) {
            if (material.hasStats("Extra") && !material.isHidden()) {
                coloredParts.add(getColoredPart(material));
            }
        }
    }

    public static ItemStack getColoredPart(Material material) {
        ItemStack stack = new ItemStack(BINDING.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(Tags.PART_MATERIAL, material.identifier);
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_name",
                Component.translatable("material." + material.identifier + ".name"),
                Component.translatable("item.tconstruct.binding.name")));
        return stack;
    }

}
