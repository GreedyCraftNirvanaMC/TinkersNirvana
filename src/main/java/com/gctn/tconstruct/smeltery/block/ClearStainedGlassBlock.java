/*
 * 根据 TinkersAntique 的 BlockClearStainedGlass 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 染色通透玻璃保留原版调色板，仅与同色同类方块连接。 */
public class ClearStainedGlassBlock extends ClearGlassBlock implements BeaconBeamBlock {
    public static final MapCodec<ClearStainedGlassBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DyeColor.CODEC.fieldOf("color").forGetter(ClearStainedGlassBlock::getColor), propertiesCodec())
            .apply(instance, ClearStainedGlassBlock::new));
    // 不能直接使用现代染料 RGB；原版的玻璃和信标颜色与现代羊毛调色板不同。
    private static final int[] COLORS = {0xffffff, 0xd87f33, 0xb24cd8, 0x6699d8, 0xe5e533, 0x7fcc19, 0xf27fa5, 0x4c4c4c,
            0x999999, 0x4c7f99, 0x7f3fb2, 0x334cb2, 0x664c33, 0x667f33, 0x993333, 0x191919};
    private final DyeColor color;

    public ClearStainedGlassBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    protected MapCodec<ClearStainedGlassBlock> codec() {
        return CODEC;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    public int argb() {
        return 0xff000000 | COLORS[color.getId()];
    }

    @Override
    public Integer getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        return argb();
    }
}
