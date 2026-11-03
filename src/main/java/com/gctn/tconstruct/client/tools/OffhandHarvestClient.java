/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.client.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.DualToolHarvest;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class OffhandHarvestClient {
    private OffhandHarvestClient() {}
    @SubscribeEvent(priority = EventPriority.LOWEST) private static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        var minecraft = Minecraft.getInstance();
        if (!event.isCanceled() && event.isAttack() && event.shouldSwingHand() && minecraft.player != null && minecraft.level != null
                && minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && DualToolHarvest.shouldUseOffhand(minecraft.player, minecraft.level.getBlockState(hit.getBlockPos()))) {
            event.setSwingHand(false); minecraft.player.swing(InteractionHand.OFF_HAND);
        }
    }
}
