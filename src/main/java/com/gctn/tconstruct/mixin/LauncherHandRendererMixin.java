/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolDefinition;
import com.gctn.tconstruct.tools.ToolRanged;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
abstract class LauncherHandRendererMixin {
    @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private Item tconstruct$useNativeCrossbowPose(ItemStack stack, Operation<Item> original) {
        // 地图检测后，原版只用这次查询判断是否进入弩的动作分支。实际渲染仍使用匠魂物品。
        return tconstruct$isCrossbow(stack) ? Items.CROSSBOW : original.call(stack);
    }

    @WrapOperation(method = {"evaluateWhichHandsToRender", "selectionUsingItemWhileHoldingBowLike", "isChargedCrossbow"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean tconstruct$selectLauncherHands(ItemStack stack, Item item, Operation<Boolean> original) {
        if (stack.getItem() instanceof AntiqueTool tool && tool.isLauncher()) {
            boolean crossbow = tool.definition().action() == ToolDefinition.Action.CROSSBOW;
            return crossbow ? item == Items.CROSSBOW : item == Items.BOW;
        }
        return original.call(stack, item);
    }

    @WrapOperation(method = "isChargedCrossbow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;isCharged(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean tconstruct$readLoadedState(ItemStack stack, Operation<Boolean> original) {
        return tconstruct$isCrossbow(stack) ? ToolRanged.isLoaded(stack) : original.call(stack);
    }

    @WrapOperation(method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CrossbowItem;isCharged(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean tconstruct$renderLoadedPose(ItemStack stack, Operation<Boolean> original) {
        return tconstruct$readLoadedState(stack, original);
    }

    @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/CrossbowItem;getChargeDuration(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int tconstruct$useMaterialDrawTime(ItemStack stack, LivingEntity player, Operation<Integer> original) {
        return tconstruct$isCrossbow(stack) && stack.getItem() instanceof AntiqueTool tool
                ? ToolRanged.drawTime(tool, stack) : original.call(stack, player);
    }

    private static boolean tconstruct$isCrossbow(ItemStack stack) {
        return stack.getItem() instanceof AntiqueTool tool && tool.definition().action() == ToolDefinition.Action.CROSSBOW;
    }
}
