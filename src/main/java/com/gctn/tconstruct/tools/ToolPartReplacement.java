/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique ToolBuilder.tryReplaceToolParts. */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Pure replacement preview; material traits are rebuilt while independent runtime data survives. */
public final class ToolPartReplacement {
    private ToolPartReplacement() {}
    public static ToolModifiers.Plan plan(Container container) {
        ItemStack source = container.getItem(0);
        if (!(source.getItem() instanceof AntiqueTool item)) { return null; }
        var definition = item.definition();
        CompoundTag original = TinkerDataComponents.getMaterialTag(source);
        List<Material> materials = new ArrayList<>();
        for (var required : definition.parts()) {
            Material material = TinkerMaterials.getMaterial(original.getString(required.key()));
            if (material == null) { return null; }
            materials.add(material);
        }
        ItemStack oldBase = ToolBuilder.assemble(source.getItem(), definition, materials);
        if (oldBase.isEmpty()) { return null; }
        HashSet<Integer> assigned = new HashSet<>();
        int[] counts = new int[6];
        boolean changed = false;
        for (int slot = 1; slot < 6; slot++) {
            ItemStack input = container.getItem(slot);
            if (input.isEmpty()) { continue; }
            var part = AntiqueParts.get(input);
            if (part == null) { return null; }
            CompoundTag partData = TinkerDataComponents.getMaterialTag(input);
            Material replacement = TinkerMaterials.getMaterial(partData.getString(Tags.PART_MATERIAL));
            if (replacement == null) { return null; }
            if (part.id().equals("bolt_core") && definition.action() == ToolDefinition.Action.BOLT) {
                Material coating = TinkerMaterials.getMaterial(partData.getString("Coating"));
                if (coating == null || assigned.contains(0) || assigned.contains(1)
                        || (materials.get(0) == replacement && materials.get(1) == coating)) { return null; }
                materials.set(0, replacement); materials.set(1, coating); assigned.add(0); assigned.add(1);
            } else {
                int candidate = -1;
                for (int index = 0; index < definition.parts().size(); index++) {
                    var required = definition.parts().get(index);
                    if (required.part().equals(part.id()) && materials.get(index) != replacement && !assigned.contains(index)) {
                        candidate = index;
                        if (slot - 1 <= index) { break; }
                    }
                }
                if (candidate < 0) { return null; }
                materials.set(candidate, replacement); assigned.add(candidate);
            }
            counts[slot] = 1; changed = true;
        }
        if (!changed) { return null; }
        ItemStack newBase = ToolBuilder.assemble(source.getItem(), definition, materials);
        if (newBase.isEmpty()) { return null; }
        ItemStack result = source.copyWithCount(1);
        CompoundTag materialData = original.copy();
        for (int index = 0; index < materials.size(); index++) { materialData.putString(definition.parts().get(index).key(), materials.get(index).identifier); }
        CompoundTag oldTraits = TinkerDataComponents.getTraitsTag(oldBase);
        CompoundTag newTraits = TinkerDataComponents.getTraitsTag(newBase);
        CompoundTag embossed = materialData.getCompound("EmbossedTraits");
        ToolTraits.addTraits(newTraits, ToolTraits.entriesFrom(embossed));
        CompoundTag actual = TinkerDataComponents.getTraitsTag(source);
        boolean legacyEmbossing = materialData.contains("Embossed") && !materialData.contains("EmbossedTraits");
        for (String id : oldTraits.getAllKeys()) {
            // Older saves did not retain embossing provenance; preserve their stored trait snapshot.
            if (!newTraits.contains(id) && !legacyEmbossing) { actual.remove(id); }
        }
        for (String id : newTraits.getAllKeys()) {
            CompoundTag state = actual.getCompound(id).copy();
            state.merge(newTraits.getCompound(id));
            if (!newTraits.getCompound(id).contains("AppliedLevels")) { state.remove("AppliedLevels"); }
            actual.put(id, state);
        }
        result.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(materialData));
        result.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(actual));
        if (assigned.stream().anyMatch(index -> java.util.Set.of("Head", "Bow").contains(definition.parts().get(index).stat()))
                && materialData.contains("FortifiedTier") && ToolBuilder.calculate(result, definition).harvestLevel >= materialData.getInt("FortifiedTier")) {
            materialData.remove("FortifiedTier"); materialData.remove("FortifiedMaterial"); result.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(materialData));
        }
        if (!ToolModifiers.isCompatible(result)) { return null; }
        result.setDamageValue(0);
        ToolBuilder.rebuild(result, definition);
        if (source.getDamageValue() >= result.getMaxDamage()) { return null; }
        result.setDamageValue(source.getDamageValue());
        result.set(DataComponents.ITEM_NAME, newBase.get(DataComponents.ITEM_NAME));
        return new ToolModifiers.Plan(result, counts);
    }
}
