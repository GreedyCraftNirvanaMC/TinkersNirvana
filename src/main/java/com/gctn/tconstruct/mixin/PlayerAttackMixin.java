/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolCombat;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @WrapMethod(method = "attack")
    private void tconstruct$scytheAttack(Entity target, Operation<Void> original) {
        ToolCombat.attack((Player) (Object) this, target, () -> original.call(target));
    }
}
