/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's TinkerWorld. */
package com.gctn.tconstruct.world;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.world.block.*;
import com.gctn.tconstruct.world.feature.SlimeTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class WorldModule {
    public static final String[] DIRT_COLORS = {"green", "blue", "purple", "magma"};
    public static final String[] FOLIAGE_COLORS = {"blue", "purple", "orange"};
    public static final Map<String, DeferredBlock<SlimeDirtBlock>> DIRT = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<SlimeGrassBlock>> GRASS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<SlimeLeavesBlock>> LEAVES = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<SlimePlantBlock>> PLANTS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<SlimeSaplingBlock>> SAPLINGS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<SlimeVineBlock>> VINES = new LinkedHashMap<>();
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TinkersNirvana.MODID);
    private static final DeferredRegister<net.minecraft.world.level.levelgen.placement.PlacementModifierType<?>> PLACEMENT = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TinkersNirvana.MODID);
    public static final java.util.function.Supplier<net.minecraft.world.level.levelgen.placement.PlacementModifierType<com.gctn.tconstruct.world.feature.WorldgenFilter>> GENERATION_FILTER = PLACEMENT.register("antique_worldgen", () -> () -> com.gctn.tconstruct.world.feature.WorldgenFilter.CODEC);
    public static final java.util.function.Supplier<net.minecraft.world.level.levelgen.placement.PlacementModifierType<com.gctn.tconstruct.world.feature.AntiqueHeightPlacement>> HEIGHT_PLACEMENT = PLACEMENT.register("antique_height", () -> () -> com.gctn.tconstruct.world.feature.AntiqueHeightPlacement.CODEC);
    private static final DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TinkersNirvana.MODID);
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<IslandBounds>> ISLAND_BOUNDS = ATTACHMENTS.register("slime_islands", () -> net.neoforged.neoforge.attachment.AttachmentType.builder(() -> IslandBounds.EMPTY).serialize(IslandBounds.CODEC).build());
    static {
        FEATURES.register("slime_tree", () -> new SlimeTreeFeature());
        FEATURES.register("slime_island", () -> new com.gctn.tconstruct.world.feature.SlimeIslandFeature(false));
        FEATURES.register("magma_island", () -> new com.gctn.tconstruct.world.feature.SlimeIslandFeature(true));
        FEATURES.register("slime_pool", () -> new com.gctn.tconstruct.world.feature.SlimePoolFeature(false));
        FEATURES.register("magma_pool", () -> new com.gctn.tconstruct.world.feature.SlimePoolFeature(true));
        for (String dirt : DIRT_COLORS) {
            DIRT.put(dirt, BlockRegister.registerBlockWithItem("slime_dirt_" + dirt, () -> new SlimeDirtBlock(
                    BlockBehaviour.Properties.of().strength(.55f).sound(SoundType.SLIME_BLOCK))));
        }
        for (String foliage : FOLIAGE_COLORS) {
            for (String dirt : new String[]{"vanilla", "green", "blue", "purple", "magma"}) {
                GRASS.put(foliage + "_" + dirt, BlockRegister.registerBlockWithItem("slime_grass_" + foliage + "_" + dirt, () -> new SlimeGrassBlock(foliage, dirt,
                        BlockBehaviour.Properties.of().strength(.65f).friction(.65f).sound(SoundType.GRASS).randomTicks())));
            }
            LEAVES.put(foliage, BlockRegister.registerBlockWithItem("slime_leaves_" + foliage, () -> new SlimeLeavesBlock(
                    BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_LEAVES).strength(.3f))));
            for (boolean fern : new boolean[]{false, true}) {
                String type = fern ? "fern" : "tall_grass";
                PLANTS.put(foliage + "_" + type, BlockRegister.registerBlockWithItem("slime_" + type + "_" + foliage,
                        () -> new SlimePlantBlock(BlockBehaviour.Properties.of().noCollission().instabreak().replaceable().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XYZ))));
            }
            SAPLINGS.put(foliage, BlockRegister.registerBlockWithItem("slime_sapling_" + foliage, () -> new SlimeSaplingBlock(tree(foliage),
                    BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks().sound(SoundType.GRASS))));
        }
        for (String foliage : new String[]{"blue", "purple"}) {
            for (int stage = 2; stage >= 0; stage--) {
                int index = stage;
                String name = "slime_vine_" + foliage + (stage == 0 ? "" : stage == 1 ? "_mid" : "_end");
                VINES.put(foliage + stage, BlockRegister.registerBlockWithItem(name, () -> new SlimeVineBlock(foliage, index,
                        BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.VINE))));
            }
        }
    }
    private WorldModule() {}
    public static ResourceKey<ConfiguredFeature<?, ?>> treeKey(String foliage) { return ResourceKey.create(Registries.CONFIGURED_FEATURE, id("slime_tree_" + foliage)); }
    private static TreeGrower tree(String foliage) { return new TreeGrower(id("slime_tree_" + foliage).toString(), Optional.empty(), Optional.of(treeKey(foliage)), Optional.empty()); }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, path); }
    public static void register(IEventBus bus) { FEATURES.register(bus); PLACEMENT.register(bus); ATTACHMENTS.register(bus); }
    public static boolean soil(net.minecraft.world.level.block.state.BlockState state) { return state.getBlock() instanceof SlimeDirtBlock || state.getBlock() instanceof SlimeGrassBlock; }
    public static Block dirt(String color) { return color.equals("vanilla") ? net.minecraft.world.level.block.Blocks.DIRT : DIRT.get(color).get(); }
}
