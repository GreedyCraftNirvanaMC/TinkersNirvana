/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.gadgets.SlimeChannels;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the removed 1.12 isEntityInsideMaterial hook with NeoForge's fluid-type state. */
@Mixin(Entity.class)
public abstract class EntityChannelFluidMixin {
    @Shadow protected abstract void setFluidTypeHeight(FluidType type, double height);
    @Shadow private FluidType forgeFluidTypeOnEyes;
    @Inject(method = "updateFluidHeightAndDoFluidPushing()V", at = @At("RETURN"))
    private void tconstruct$channelHeight(CallbackInfo callback) {
        Entity entity = (Entity) (Object) this;
        double height = SlimeChannels.waterHeight(entity);
        if (height > 0) { FluidType water = NeoForgeMod.WATER_TYPE.value(); setFluidTypeHeight(water, Math.max(entity.getFluidTypeHeight(water), height)); }
    }
    @Inject(method = "updateFluidOnEyes", at = @At("TAIL"))
    private void tconstruct$channelEyes(CallbackInfo callback) {
        if (SlimeChannels.wetEyes((Entity) (Object) this)) { forgeFluidTypeOnEyes = NeoForgeMod.WATER_TYPE.value(); }
    }
}
