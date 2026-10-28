package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tools.tools.Pickaxe;

import java.util.Map;

/**
 * 将已实现的工具台模式映射到准确的组装要求。
 * 目前只有镐具备组装配方，其他已显示的模式暂不生成结果。
 */
public final class ToolStationAssemblyRecipes {
    private static final Map<Mode, ToolAssemblyRecipe> RECIPES = Map.of(
            Mode.PICKAXE, new ToolAssemblyRecipe(
                    new ToolAssemblyRecipe.PartRequirement[] {
                            new ToolAssemblyRecipe.PartRequirement(0, ToolRod.TOOL_ROD, "Handle"),
                            new ToolAssemblyRecipe.PartRequirement(1, PickaxeHead.PICKAXEHEAD, "Head"),
                            new ToolAssemblyRecipe.PartRequirement(2, Binding.BINDING, "Extra")
                    },
                    materials -> Pickaxe.initPickaxe(materials[0], materials[1], materials[2]))
    );

    public static ToolAssemblyRecipe get(Mode mode) {
        return RECIPES.get(mode);
    }
}
