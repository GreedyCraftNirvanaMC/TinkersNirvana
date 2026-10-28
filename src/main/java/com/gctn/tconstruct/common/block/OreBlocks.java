package com.gctn.tconstruct.common.block;

import com.gctn.tconstruct.init.BlockRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Collection;
import java.util.List;

/**
 * 按 Antique 的硬度和正确工具掉落要求注册钴矿石。
 * 采掘等级限制通过数据标签定义，由原版掉落与工具判定统一执行。
 */
public class OreBlocks {
    public static final DeferredBlock<Block> COBALT_ORE =
            BlockRegister.registerBlockWithItem("cobalt_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(10.0F, 15.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
            ));

    public static Collection<DeferredBlock<?>> entries() {
        return List.of(
                COBALT_ORE
        );
    }
}
