package com.gctn.tconstruct.library.utils;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.TagKey;

public class HarvestLevels {

    public static final int STONE = 0;
    public static final int IRON = 1;
    public static final int DIAMOND = 2;
    public static final int OBSIDIAN = 3;
    public static final int COBALT = 4;

    private HarvestLevels() {
    } // non-instantiable

    public static Component getHarvestLevelName(int harvestLevel) {
        MutableComponent name = switch (harvestLevel) {
            case STONE -> Component.translatable("ui.mininglevel.stone")
                    .withColor(TinkerMaterials.stone.getColor());
            case IRON -> Component.translatable("ui.mininglevel.iron")
                    .withColor(TinkerMaterials.iron.getColor());
            case DIAMOND -> Component.translatable("ui.mininglevel.diamond")
                    .withColor(0x55FFFF);
            case OBSIDIAN -> Component.translatable("ui.mininglevel.obsidian")
                    .withColor(TinkerMaterials.obsidian.getColor());
            case COBALT -> Component.translatable("ui.mininglevel.cobalt")
                    .withColor(TinkerMaterials.cobalt.getColor());
            default -> Component.literal(String.valueOf(harvestLevel));
        };
        return name;
    }

    public static TagKey<Block> getIncorrectBlocksForDrops(int harvestLevel) {
        if (harvestLevel <= HarvestLevels.STONE) {
            return BlockTags.INCORRECT_FOR_WOODEN_TOOL;
        }
        if (harvestLevel == HarvestLevels.IRON) {
            return BlockTags.INCORRECT_FOR_STONE_TOOL;
        }
        if (harvestLevel == HarvestLevels.DIAMOND) {
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }
        if (harvestLevel < HarvestLevels.COBALT) {
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        }
        return TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "incorrect_for_cobalt_tool"));
    }
}
