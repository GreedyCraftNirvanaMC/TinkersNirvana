package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import com.gctn.tconstruct.tools.tools.Pickaxe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public final class ToolStationDisassemblyRecipes {
    private ToolStationDisassemblyRecipes() {
    }

    public static boolean canDisassemble(ItemStack stack) {
        return !createParts(stack).isEmpty();
    }

    public static PartList createParts(ItemStack stack) {
        if (!isFullDurabilityTool(stack)) {
            return PartList.EMPTY;
        }

        return switch (stack.getItem()) {
            case net.minecraft.world.item.Item item when item == Pickaxe.PICKAXE.get() -> createPickaxeParts(stack);
            default -> PartList.EMPTY;
        };
    }

    private static boolean isFullDurabilityTool(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() == 0;
    }

    private static PartList createPickaxeParts(ItemStack stack) {
        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack);
        Material handle = getMaterial(materials, "Handle", "Handle");
        Material head = getMaterial(materials, "Head", "Head");
        Material binding = getMaterial(materials, "Binding", "Extra");
        if (handle == null || head == null || binding == null) {
            return PartList.EMPTY;
        }

        return new PartList(new ItemStack[] {
                ToolRod.getPart(handle),
                PickaxeHead.getColoredPart(head),
                Binding.getColoredPart(binding)
        });
    }

    private static Material getMaterial(CompoundTag materials, String materialKey, String requiredStat) {
        String materialId = materials.getString(materialKey);
        Material material = TinkerMaterials.getMaterial(materialId);
        return material != null && material.hasStats(requiredStat) ? material : null;
    }

    public record PartList(ItemStack[] parts) {
        public static final PartList EMPTY = new PartList(new ItemStack[0]);

        public boolean isEmpty() {
            return this.parts.length == 0;
        }
    }
}
