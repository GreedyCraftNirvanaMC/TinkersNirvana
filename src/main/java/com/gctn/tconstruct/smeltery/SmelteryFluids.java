/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SmelteryFluids {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TinkersNirvana.MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TinkersNirvana.MODID);
    public static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    static {
        // Temperatures and colors come from Antique's TinkerFluids, including its 300 K baseline.
        add("iron", 769, 0xa81212); add("gold", 532, 0xf6d609); add("copper", 542, 0xed9f07);
        add("cobalt", 950, 0x2882d4); add("ardite", 860, 0xd14210); add("manyullyn", 1000, 0xa15cf8);
        add("pigiron", 600, 0xef9e9b); add("knightslime", 520, 0xf18ff0);
        add("alubrass", 500, 0xf0d467); add("alumite", 900, 0xffa7e9); add("brass", 470, 0xede38b);
        add("tin", 350, 0xc1cddc); add("bronze", 475, 0xe3bd68); add("zinc", 375, 0xd3efe8);
        add("lead", 400, 0x4d4968); add("nickel", 727, 0xc8d683); add("silver", 480, 0xd1ecf6);
        add("electrum", 500, 0xe8db49); add("steel", 681, 0xa7a7a7); add("aluminum", 330, 0xefe0d5);
        add("stone", 800, 0x777777); add("obsidian", 1000, 0x2c0d59); add("clay", 700, 0xc67453);
        add("dirt", 500, 0xa68564); add("notmilk", 800, 0xcbc6a5); add("emerald", 999, 0x58e78e);
        add("diamond", 999, 0x8cf4e2); add("glass", 625, 0xc0f5fe); add("blood", 336, 0x540000);
        add("venom", 336, 0xb9b566); add("blazing_blood", 1800, 0xff6600);
        add("greenslime", 370, 0x82c873); add("blueslime", 370, 0x74c8c7); add("purpleslime", 370, 0xc873c8);
    }

    private SmelteryFluids() {
    }

    private static void add(String name, int temperature, int color) {
        Entry entry = new Entry(name, temperature, color);
        ENTRIES.put(name, entry);
        entry.type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create().temperature(temperature)
                .density(2000).viscosity(6000).lightLevel(temperature >= 500 ? 10 : 0)
                .descriptionId("fluid.tconstruct." + name)));
        entry.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(entry.properties()));
        entry.flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(entry.properties()));
        entry.block = BlockRegister.BLOCKS.register("molten_" + name, () -> new LiquidBlock(entry.source.get(),
                BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)));
        entry.bucket = ItemRegister.ITEMS.register(name + "_bucket", () -> new BucketItem(entry.source.get(),
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, path);
    }

    public static Fluid get(String name) {
        return ENTRIES.get(name).source.get();
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        FLUIDS.register(bus);
    }

    public static final class Entry {
        public final String name;
        public final int temperature;
        public final int color;
        public Supplier<FluidType> type;
        public Supplier<FlowingFluid> source;
        public Supplier<FlowingFluid> flowing;
        public Supplier<LiquidBlock> block;
        public Supplier<Item> bucket;

        private Entry(String name, int temperature, int color) {
            this.name = name;
            this.temperature = temperature;
            this.color = color;
        }

        private BaseFlowingFluid.Properties properties() {
            return new BaseFlowingFluid.Properties(type, source, flowing).bucket(bucket).block(block).tickRate(30);
        }
    }
}
