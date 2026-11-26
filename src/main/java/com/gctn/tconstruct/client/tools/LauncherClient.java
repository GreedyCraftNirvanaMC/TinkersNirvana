/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.client.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolRanged;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class LauncherClient {
    private LauncherClient() {}

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return !entity.swinging && ToolRanged.isLoaded(stack) ? HumanoidModel.ArmPose.CROSSBOW_HOLD : null;
            }
        }, AntiqueTools.entries().get("crossbow").item().get());
    }
}
