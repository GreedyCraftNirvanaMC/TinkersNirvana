package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ToolStationAssemblyRecipes {
    private static final Map<Mode, ToolAssemblyRecipe> RECIPES = new EnumMap<>(Mode.class);
    static {
        for (Mode mode : Mode.values()) {
            String id = mode == Mode.AXE ? "hatchet" : mode.name().toLowerCase(java.util.Locale.ROOT);
            var entry = AntiqueTools.get(id);
            if (entry == null) { continue; }
            if (id.equals("bolt")) {
                RECIPES.put(mode, new ToolAssemblyRecipe(new ToolAssemblyRecipe.PartRequirement[]{
                        new ToolAssemblyRecipe.PartRequirement(0, com.gctn.tconstruct.library.toolparts.AntiqueParts.get("bolt_core").item(), "Shaft"),
                        new ToolAssemblyRecipe.PartRequirement(1, com.gctn.tconstruct.library.toolparts.AntiqueParts.get("fletching").item(), "Fletching")}, materials -> net.minecraft.world.item.ItemStack.EMPTY) {
                    @Override public net.minecraft.world.item.ItemStack createResult(net.minecraft.world.Container container) {
                        var core = container.getItem(0);
                        if (!core.is(getRequiredItem(0)) || !container.getItem(1).is(getRequiredItem(1))) { return net.minecraft.world.item.ItemStack.EMPTY; }
                        for (int slot = 2; slot < 6; slot++) { if (!container.getItem(slot).isEmpty()) { return net.minecraft.world.item.ItemStack.EMPTY; } }
                        var data = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(core);
                        var shaft = com.gctn.tconstruct.library.TinkerMaterials.getMaterial(data.getString(com.gctn.tconstruct.library.utils.Tags.PART_MATERIAL));
                        var head = com.gctn.tconstruct.library.TinkerMaterials.getMaterial(data.getString("Coating"));
                        var fletching = com.gctn.tconstruct.library.TinkerMaterials.getMaterial(com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(container.getItem(1)).getString(com.gctn.tconstruct.library.utils.Tags.PART_MATERIAL));
                        return shaft == null || head == null || fletching == null ? net.minecraft.world.item.ItemStack.EMPTY : ToolBuilder.assemble(entry, List.of(shaft, head, fletching));
                    }
                });
                continue;
            }
            var parts = entry.definition().parts();
            var requirements = new ToolAssemblyRecipe.PartRequirement[parts.size()];
            for (int i = 0; i < parts.size(); i++) {
                var part = parts.get(i);
                requirements[i] = new ToolAssemblyRecipe.PartRequirement(i, part.definition().item(), part.stat());
            }
            RECIPES.put(mode, new ToolAssemblyRecipe(requirements, materials -> ToolBuilder.assemble(entry, List.of(materials))));
        }
    }
    private ToolStationAssemblyRecipes() {}
    public static ToolAssemblyRecipe get(Mode mode) { return RECIPES.get(mode); }
}
