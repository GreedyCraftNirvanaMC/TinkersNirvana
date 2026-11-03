/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.DualToolHarvest;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientOffhandHarvestMixin {
    @Shadow @Final private Minecraft minecraft;
    @WrapMethod(method = "destroyBlock")
    private boolean tconstruct$offhandBreak(BlockPos pos, Operation<Boolean> original) { return DualToolHarvest.withTool(minecraft.player, pos, () -> original.call(pos)); }
}
