/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class ToolChargeMovementMixin {
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=0.2"), require = 2, allow = 2)
    private float tconstruct$chargeMovement(float original) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        var stack = player.getUseItem();
        if (!(stack.getItem() instanceof AntiqueTool tool) || AntiqueTool.isBroken(stack)) {
            return original;
        }
        // 只调整使用物品时的前进与横移输入，保留原版的潜行、冲刺和载具规则。
        float legacy = switch (tool.definition().action()) {
            case SHORTBOW -> .5F;
            case CROSSBOW -> .195F;
            case FRYPAN -> .7F;
            case LONGSWORD -> .9F;
            default -> .2F;
        };
        return original * (legacy * 5F);
    }
}
