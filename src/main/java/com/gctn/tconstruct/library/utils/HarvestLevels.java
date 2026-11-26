package com.gctn.tconstruct.library.utils;

import com.gctn.tconstruct.library.TinkerMaterials;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.component.Tool;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.holdersets.AndHolderSet;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HarvestLevels {

    public static final int STONE = 0;
    public static final int IRON = 1;
    public static final int DIAMOND = 2;
    public static final int OBSIDIAN = 3;
    public static final int COBALT = 4;
    private static final Map<ToolTier, Tool.Rule> RESTRICTED_RULES = new ConcurrentHashMap<>();

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
        if (harvestLevel == HarvestLevels.OBSIDIAN) {
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        }
        // 钴级与下界合金共用采掘门槛，涵盖默认四级的钴和阿迪特矿石。
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }

    public static Tool.Rule restrictToolToTier(TagKey<Block> toolBlocks, int harvestLevel) {
        var key = new ToolTier(toolBlocks, getIncorrectBlocksForDrops(harvestLevel));
        // 每组标签只注册一次重载监听。交集由 NeoForge 在数据包重载后重新计算。
        return RESTRICTED_RULES.computeIfAbsent(key, tier -> new Tool.Rule(new AndHolderSet<>(
                BuiltInRegistries.BLOCK.getOrCreateTag(tier.toolBlocks()),
                BuiltInRegistries.BLOCK.getOrCreateTag(tier.incorrectBlocks())), Optional.of(1F), Optional.of(false)));
    }

    private record ToolTier(TagKey<Block> toolBlocks, TagKey<Block> incorrectBlocks) {}
}
