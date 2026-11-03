/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.DualToolHarvest;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerOffhandHarvestMixin {
    @WrapMethod(method = "getDigSpeed")
    private float tconstruct$offhandSpeed(BlockState state, BlockPos pos, Operation<Float> original) {
        return DualToolHarvest.withTool((Player) (Object) this, state, () -> original.call(state, pos));
    }
    @WrapMethod(method = "hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z")
    private boolean tconstruct$offhandDrops(BlockState state, Level level, BlockPos pos, Operation<Boolean> original) {
        return DualToolHarvest.withTool((Player) (Object) this, state, () -> original.call(state, level, pos));
    }
}
