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

public class ToolRod extends ToolPart {
    private final String materialTag = "";

    public ToolRod(int cost) {
        super(cost);
    }

    public static final DeferredItem<Item> TOOL_ROD = ITEMS.register("parts/tool_rod",
            () -> new ToolRod(MaterialValue.VALUE_Ingot));

    /** Forces this class's static registration entry to be initialized. */
    public static void init() {
    }

    public static void getAllParts(Collection<ItemStack> coloredParts) {
        List<Material> materials = TinkerMaterials.materials;
        for (Material material : materials) {
            if (material.isHidden() || !material.hasStats("Handle")) { continue; }
            ItemStack stack = getPart(material);
            coloredParts.add(stack);
        }
    }

    public static ItemStack getPart(Material material) {
        ItemStack stack = new ItemStack(TOOL_ROD.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(Tags.PART_MATERIAL, material.identifier);
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_name",
                Component.translatable("material." + material.identifier + ".name"),
                Component.translatable("item.tconstruct.tool_rod.name")));
        return stack;
    }

}
