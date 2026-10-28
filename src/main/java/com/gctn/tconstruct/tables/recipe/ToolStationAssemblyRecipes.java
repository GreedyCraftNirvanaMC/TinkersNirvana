package com.gctn.tconstruct.tables.recipe; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.toolparts.Binding; // 引入 com.gctn.tconstruct.library.toolparts.Binding，供本文件使用。
import com.gctn.tconstruct.library.toolparts.PickaxeHead; // 引入 com.gctn.tconstruct.library.toolparts.PickaxeHead，供本文件使用。
import com.gctn.tconstruct.library.toolparts.ToolRod; // 引入 com.gctn.tconstruct.library.toolparts.ToolRod，供本文件使用。
import com.gctn.tconstruct.tables.data.Mode; // 引入 com.gctn.tconstruct.tables.data.Mode，供本文件使用。
import com.gctn.tconstruct.tools.tools.Pickaxe; // 引入 com.gctn.tconstruct.tools.tools.Pickaxe，供本文件使用。

import java.util.Map; // 引入 java.util.Map，供本文件使用。

/**
 * 将已实现的工具台模式映射到准确的组装要求。
 * 目前只有镐具备组装配方，其他已显示的模式暂不生成结果。
 */
public final class ToolStationAssemblyRecipes { // 定义 ToolStationAssemblyRecipes 的实现类型，具体职责见上方说明。
    private static final Map<Mode, ToolAssemblyRecipe> RECIPES = Map.of( // 调用 of，执行当前流程所需的处理。
            Mode.PICKAXE, new ToolAssemblyRecipe( // 在 ToolStationAssemblyRecipes 中补充当前声明或表达式的 Mode.PICKAXE, new ToolAssemblyRecipe( 部分。
                    new ToolAssemblyRecipe.PartRequirement[] { // 创建 new ToolAssemblyRecipe.PartRequirement[] {，作为当前对象或调用的组成部分。
                            new ToolAssemblyRecipe.PartRequirement(0, ToolRod.TOOL_ROD, "Handle"), // 创建 new ToolAssemblyRecipe.PartRequirement(0, ToolRod.TOOL_ROD, "Handle"),，作为当前对象或调用的组成部分。
                            new ToolAssemblyRecipe.PartRequirement(1, PickaxeHead.PICKAXEHEAD, "Head"), // 创建 new ToolAssemblyRecipe.PartRequirement(1, PickaxeHead.PICKAXEHEAD, "Head"),，作为当前对象或调用的组成部分。
                            new ToolAssemblyRecipe.PartRequirement(2, Binding.BINDING, "Extra") // 创建 new ToolAssemblyRecipe.PartRequirement(2, Binding.BINDING, "Extra")，作为当前对象或调用的组成部分。
                    }, // 结束当前作用域。
                    materials -> Pickaxe.initPickaxe(materials[0], materials[1], materials[2])) // 定义回调或映射规则，在对应条目被处理时执行。
    ); // 结束当前参数列表或表达式并完成本次调用。

    public static ToolAssemblyRecipe get(Mode mode) { // 定义 get 的调用入口，参数和具体处理见下方实现。
        return RECIPES.get(mode); // 返回 RECIPES.get(mode)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
