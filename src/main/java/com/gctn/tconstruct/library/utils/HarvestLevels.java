package com.gctn.tconstruct.library.utils; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.resources.ResourceLocation; // 引入 net.minecraft.resources.ResourceLocation，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.network.chat.MutableComponent; // 引入 net.minecraft.network.chat.MutableComponent，供本文件使用。
import net.minecraft.tags.BlockTags; // 引入 net.minecraft.tags.BlockTags，供本文件使用。
import net.minecraft.world.level.block.Block; // 引入 net.minecraft.world.level.block.Block，供本文件使用。
import net.minecraft.tags.TagKey; // 引入 net.minecraft.tags.TagKey，供本文件使用。

/**
 * 将 Antique 的数字采掘等级映射到 Minecraft 1.21.1 的 incorrect-for-tool 方块标签。
 * 等级名称表示可采掘的材料，而不是工具自身的材料。
 * 钴保持为独立的自定义等级，高于原版钻石与下界合金工具的采掘门槛。
 */
public class HarvestLevels { // 定义 HarvestLevels 的实现类型，具体职责见上方说明。

    public static final int STONE = 0; // 设置 STONE 为 0。
    public static final int IRON = 1; // 设置 IRON 为 1。
    public static final int DIAMOND = 2; // 设置 DIAMOND 为 2。
    public static final int OBSIDIAN = 3; // 设置 OBSIDIAN 为 3。
    public static final int COBALT = 4; // 设置 COBALT 为 4。

    private HarvestLevels() { // 定义 HarvestLevels 的调用入口，参数和具体处理见下方实现。
    } // 工具类不允许创建实例。

    public static Component getHarvestLevelName(int harvestLevel) { // 定义 getHarvestLevelName 的调用入口，参数和具体处理见下方实现。
        MutableComponent name = switch (harvestLevel) { // 设置 name 为 switch (harvestLevel) {。
            case STONE -> Component.translatable("ui.mininglevel.stone") // 定义 case STONE 分支，调用 translatable，执行当前流程所需的处理。
                    .withColor(TinkerMaterials.stone.getColor()); // 调用 withColor，执行当前流程所需的处理。
            case IRON -> Component.translatable("ui.mininglevel.iron") // 定义 case IRON 分支，调用 translatable，执行当前流程所需的处理。
                    .withColor(TinkerMaterials.iron.getColor()); // 调用 withColor，执行当前流程所需的处理。
            case DIAMOND -> Component.translatable("ui.mininglevel.diamond") // 定义 case DIAMOND 分支，调用 translatable，执行当前流程所需的处理。
                    .withColor(0x55FFFF); // 调用 withColor，执行当前流程所需的处理。
            case OBSIDIAN -> Component.translatable("ui.mininglevel.obsidian") // 定义 case OBSIDIAN 分支，调用 translatable，执行当前流程所需的处理。
                    .withColor(TinkerMaterials.obsidian.getColor()); // 调用 withColor，执行当前流程所需的处理。
            case COBALT -> Component.translatable("ui.mininglevel.cobalt") // 定义 case COBALT 分支，调用 translatable，执行当前流程所需的处理。
                    .withColor(TinkerMaterials.cobalt.getColor()); // 调用 withColor，执行当前流程所需的处理。
            default -> Component.literal(String.valueOf(harvestLevel)); // 定义 default 分支，调用 literal，执行当前流程所需的处理。
        }; // 结束当前作用域并完成本次表达式或声明。
        return name; // 返回 name，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static TagKey<Block> getIncorrectBlocksForDrops(int harvestLevel) { // 定义 getIncorrectBlocksForDrops 的调用入口，参数和具体处理见下方实现。
        if (harvestLevel <= HarvestLevels.STONE) { // 检查 harvestLevel <= HarvestLevels.STONE；按条件决定是否执行本分支。
            return BlockTags.INCORRECT_FOR_WOODEN_TOOL; // 返回 BlockTags.INCORRECT_FOR_WOODEN_TOOL，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (harvestLevel == HarvestLevels.IRON) { // 检查 harvestLevel == HarvestLevels.IRON；按条件决定是否执行本分支。
            return BlockTags.INCORRECT_FOR_STONE_TOOL; // 返回 BlockTags.INCORRECT_FOR_STONE_TOOL，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (harvestLevel == HarvestLevels.DIAMOND) { // 检查 harvestLevel == HarvestLevels.DIAMOND；按条件决定是否执行本分支。
            return BlockTags.INCORRECT_FOR_IRON_TOOL; // 返回 BlockTags.INCORRECT_FOR_IRON_TOOL，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        if (harvestLevel < HarvestLevels.COBALT) { // 检查 harvestLevel < HarvestLevels.COBALT；按条件决定是否执行本分支。
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; // 返回 BlockTags.INCORRECT_FOR_DIAMOND_TOOL，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        return TagKey.create(Registries.BLOCK, // 返回 TagKey.create(Registries.BLOCK,，将当前分支的结果交给调用者。
                ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "incorrect_for_cobalt_tool")); // 调用 fromNamespaceAndPath，执行当前流程所需的处理。
    } // 结束当前作用域。
} // 结束当前作用域。
