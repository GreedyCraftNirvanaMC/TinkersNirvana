package com.gctn.tconstruct.tables.block;

import com.gctn.tconstruct.init.BlockRegister;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Collection;
import java.util.List;

/**
 * 注册工具装配台，并保留已有的方块物品标识符。
 * 保持物品路径不变，使移植前后的现有物品栈和掉落引用仍能正确匹配。
 */
public class TableBlocks {
    public static final DeferredBlock<ToolStationBlock> TOOL_STATION =
        BlockRegister.registerBlockWithItem("toolstation", "blockitem/toolstation", () -> new ToolStationBlock(
            BlockBehaviour.Properties.of()
                .strength(1.0F, 5.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
        ));

    public static Collection<DeferredBlock<?>> entries() {
        return List.<DeferredBlock<?>>of(TOOL_STATION);
    }
}
