/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolDefinition;
import com.gctn.tconstruct.tools.ToolRanged;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnimationUtils.class)
abstract class CrossbowChargeAnimationMixin {
    @WrapOperation(method = "animateCrossbowCharge", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/CrossbowItem;getChargeDuration(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private static int tconstruct$useMaterialDrawTime(ItemStack stack, LivingEntity entity, Operation<Integer> original) {
        return stack.getItem() instanceof AntiqueTool tool && tool.definition().action() == ToolDefinition.Action.CROSSBOW
                ? ToolRanged.drawTime(tool, stack) : original.call(stack, entity);
    }
}
