/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.DualToolHarvest;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerOffhandHarvestMixin {
    @Shadow @Final protected ServerPlayer player;
    @WrapMethod(method = "destroyBlock")
    private boolean tconstruct$offhandBreak(BlockPos pos, Operation<Boolean> original) { return DualToolHarvest.withTool(player, pos, () -> original.call(pos)); }
}
