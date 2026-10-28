package com.gctn.tconstruct.tables.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.init.BlockRegister; // 引入 com.gctn.tconstruct.init.BlockRegister，供本文件使用。

import net.minecraft.world.level.block.SoundType; // 引入 net.minecraft.world.level.block.SoundType，供本文件使用。
import net.minecraft.world.level.block.state.BlockBehaviour; // 引入 net.minecraft.world.level.block.state.BlockBehaviour，供本文件使用。
import net.neoforged.neoforge.registries.DeferredBlock; // 引入 net.neoforged.neoforge.registries.DeferredBlock，供本文件使用。

import java.util.Collection; // 引入 java.util.Collection，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 注册工具装配台，并保留已有的方块物品标识符。
 * 保持物品路径不变，使移植前后的现有物品栈和掉落引用仍能正确匹配。
 */
public class TableBlocks { // 定义 TableBlocks 的实现类型，具体职责见上方说明。
    public static final DeferredBlock<ToolStationBlock> TOOL_STATION = // 设置 TOOL_STATION 为 。
        BlockRegister.registerBlockWithItem("toolstation", "blockitem/toolstation", () -> new ToolStationBlock( // 定义回调或映射规则，在对应条目被处理时执行。
            BlockBehaviour.Properties.of() // 调用 of，执行当前流程所需的处理。
                .strength(1.0F, 5.0F) // 调用 strength，执行当前流程所需的处理。
                .sound(SoundType.WOOD) // 调用 sound，执行当前流程所需的处理。
                .noOcclusion() // 调用 noOcclusion，执行当前流程所需的处理。
        )); // 结束当前参数列表或表达式并完成本次调用。

    public static Collection<DeferredBlock<?>> entries() { // 定义 entries 的调用入口，参数和具体处理见下方实现。
        return List.<DeferredBlock<?>>of(TOOL_STATION); // 返回 List.<DeferredBlock<?>>of(TOOL_STATION)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
