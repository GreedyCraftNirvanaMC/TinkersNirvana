package com.gctn.tconstruct.common.block;

import com.gctn.tconstruct.init.BlockRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Collection;
import java.util.List;

public class OreBlocks {
    public static final DeferredBlock<Block> COPPER_ORE = commonOre("copper");
    public static final DeferredBlock<Block> TIN_ORE = commonOre("tin");
    public static final DeferredBlock<Block> ALUMINUM_ORE = commonOre("aluminum");
    // 旧版 setHardness 同时保证抗爆强度至少等于硬度，下界矿石的两项数值均为十。
    public static final DeferredBlock<Block> ARDITE_ORE =
            BlockRegister.registerBlockWithItem("ardite_ore", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(10, 10).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> COBALT_ORE =
            BlockRegister.registerBlockWithItem("cobalt_ore", () -> new Block(
                    BlockBehaviour.Properties.of()
                            .strength(25.0F, 15.0F)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
            ));

    public static Collection<DeferredBlock<?>> entries() {
        return List.of(
                COBALT_ORE, ARDITE_ORE, COPPER_ORE, TIN_ORE, ALUMINUM_ORE
        );
    }

    private static DeferredBlock<Block> commonOre(String metal) {
        // Antique 通过方法返回硬度三，但未设置爆炸抗性，不能沿用原生矿石的抗性。
        return BlockRegister.registerBlockWithItem(metal + "_ore", () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(3, 0).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    }
}
