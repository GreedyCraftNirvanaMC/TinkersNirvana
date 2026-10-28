package com.gctn.tconstruct.common.block;

import com.gctn.tconstruct.init.BlockRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Collection;
import java.util.List;

/**
 * Registers the cobalt ore block with Antique hardness and correct-tool drop requirements.
 * Mining tier restrictions live in data tags so native loot/tool rules enforce them consistently.
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
