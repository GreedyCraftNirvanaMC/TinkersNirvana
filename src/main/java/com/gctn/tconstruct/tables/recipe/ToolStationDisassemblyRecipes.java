package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.Material;
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

        if (!(stack.getItem() instanceof com.gctn.tconstruct.tools.tools.AntiqueTool item)) { return PartList.EMPTY; }
        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack);
        if (item.definition().action() == com.gctn.tconstruct.tools.ToolDefinition.Action.BOLT) {
            Material shaft = getMaterial(materials, "Shaft", "Shaft");
            Material head = getMaterial(materials, "Head", "Head");
            Material fletching = getMaterial(materials, "Fletching", "Fletching");
            if (shaft == null || head == null || fletching == null) { return PartList.EMPTY; }
            ItemStack core = com.gctn.tconstruct.library.toolparts.AntiqueParts.get("bolt_core").create(shaft);
            var data = TinkerDataComponents.getMaterialTag(core);
            data.putString("Coating", head.identifier);
            core.set(TinkerDataComponents.TINKER_MATERIAL.get(), net.minecraft.world.item.component.CustomData.of(data));
            return new PartList(new ItemStack[]{core, com.gctn.tconstruct.library.toolparts.AntiqueParts.get("fletching").create(fletching)});
        }
        java.util.List<ItemStack> parts = new java.util.ArrayList<>();
        for (var part : item.definition().parts()) {
            Material material = getMaterial(materials, part.key(), part.stat());
            if (material == null) { return PartList.EMPTY; }
            ItemStack result = part.definition().create(material);
            if (result.isEmpty()) { return PartList.EMPTY; }
            parts.add(result);
        }
        return new PartList(parts.toArray(ItemStack[]::new));
    }

    private static boolean isFullDurabilityTool(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() == 0;
    }

    private static Material getMaterial(CompoundTag materials, String materialKey, String requiredStat) {
        String materialId = materials.getString(materialKey);
        for (Material material : TinkerMaterials.materials) {
            if (material.identifier.equals(materialId)
                    && material.getStats() != null
                    && material.getStats().containsKey(requiredStat)) {
                return material;
            }
        }
        return null;
    }

    public record PartList(ItemStack[] parts) {
        public static final PartList EMPTY = new PartList(new ItemStack[0]);

        public boolean isEmpty() {
            return this.parts.length == 0;
        }
    }
}
