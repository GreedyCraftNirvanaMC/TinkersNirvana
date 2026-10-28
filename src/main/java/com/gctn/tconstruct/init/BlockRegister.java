package com.gctn.tconstruct.init;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.block.OreBlocks;
import com.gctn.tconstruct.tables.block.TableBlocks;
import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class BlockRegister {
    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(TinkersNirvana.MODID);

    public static <T extends Block> DeferredBlock<T> registerBlockWithItem(String name, Supplier<T> block) {
        return registerBlockWithItem(name, name, block);
    }

    public static <T extends Block> DeferredBlock<T> registerBlockWithItem(String blockName, String itemName, Supplier<T> block) {
        DeferredBlock<T> registeredBlock = BLOCKS.register(blockName, block);
        ItemRegister.ITEMS.register(itemName, () -> new BlockItem(registeredBlock.get(), new Item.Properties()));
        return registeredBlock;
    }

    public static void register(IEventBus eventBus) {
        entries();
        simpleEntries();
        BLOCKS.register(eventBus);
    }

    public static Collection<DeferredBlock<?>> entries() {
        // Calling each category's entries initializes its static registrations.
        Collection<DeferredBlock<?>> entries = new ArrayList<>();
        entries.addAll(TableBlocks.entries());
        entries.addAll(SmelteryRegistration.blocks());
        return entries;
    }

    public static Collection<DeferredBlock<?>> simpleEntries() {
        Collection<DeferredBlock<?>> simpleEntries = new ArrayList<>();

        simpleEntries.addAll(OreBlocks.entries());
        return simpleEntries;
    }
}
