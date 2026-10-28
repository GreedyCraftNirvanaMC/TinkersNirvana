package com.gctn.tconstruct.common.block; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.init.BlockRegister; // 引入 com.gctn.tconstruct.init.BlockRegister，供本文件使用。
import net.minecraft.world.level.block.Block; // 引入 net.minecraft.world.level.block.Block，供本文件使用。
import net.minecraft.world.level.block.SoundType; // 引入 net.minecraft.world.level.block.SoundType，供本文件使用。
import net.minecraft.world.level.block.state.BlockBehaviour; // 引入 net.minecraft.world.level.block.state.BlockBehaviour，供本文件使用。
import net.neoforged.neoforge.registries.DeferredBlock; // 引入 net.neoforged.neoforge.registries.DeferredBlock，供本文件使用。

import java.util.Collection; // 引入 java.util.Collection，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 按 Antique 的硬度和正确工具掉落要求注册钴矿石。
 * 采掘等级限制通过数据标签定义，由原版掉落与工具判定统一执行。
 */
public class OreBlocks { // 定义 OreBlocks 的实现类型，具体职责见上方说明。
    public static final DeferredBlock<Block> COBALT_ORE = // 设置 COBALT_ORE 为 。
            BlockRegister.registerBlockWithItem("cobalt_ore", () -> new Block( // 定义回调或映射规则，在对应条目被处理时执行。
                    BlockBehaviour.Properties.of() // 调用 of，执行当前流程所需的处理。
                            .strength(10.0F, 15.0F) // 调用 strength，执行当前流程所需的处理。
                            .sound(SoundType.STONE) // 调用 sound，执行当前流程所需的处理。
                            .requiresCorrectToolForDrops() // 调用 requiresCorrectToolForDrops，执行当前流程所需的处理。
            )); // 结束当前参数列表或表达式并完成本次调用。

    public static Collection<DeferredBlock<?>> entries() { // 定义 entries 的调用入口，参数和具体处理见下方实现。
        return List.of( // 返回 List.of(，将当前分支的结果交给调用者。
                COBALT_ORE // 在 OreBlocks 中补充当前声明或表达式的 COBALT_ORE 部分。
        ); // 结束当前参数列表或表达式并完成本次调用。
    } // 结束当前作用域。
} // 结束当前作用域。
