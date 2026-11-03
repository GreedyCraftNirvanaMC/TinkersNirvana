/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityDamageAccessor {
    @Accessor("lastHurt") float tconstruct$getLastHurt();
    @Accessor("lastHurt") void tconstruct$setLastHurt(float damage);
}
