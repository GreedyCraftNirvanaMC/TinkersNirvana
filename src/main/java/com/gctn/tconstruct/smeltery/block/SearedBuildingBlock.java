/*
 * 根据 TinkersAntique 的焦黑台阶、楼梯组成方块行为移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block;

import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** 保留原生台阶、楼梯行为，同时将结构变化通知给所属控制器。 */
public interface SearedBuildingBlock extends EntityBlock {
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SmelteryComponentBlockEntity(pos, state);
    }

    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (world, pos, blockState, entity) -> {
            if (entity instanceof SmelteryComponentBlockEntity component) {
                component.tick();
            }
        };
    }

    static void neighborChanged(Level level, BlockPos pos, BlockPos from) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
            component.neighborChanged(from);
        }
    }

    static void removed(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
            component.onBroken();
        }
    }
}
