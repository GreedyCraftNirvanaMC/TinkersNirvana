/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.soil.AntiqueSoilBlock;
import com.gctn.tconstruct.world.WorldModule;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class SlimeJumpEvents {
    private SlimeJumpEvents() {}

    @SubscribeEvent
    private static void jump(LivingEvent.LivingJumpEvent event) {
        var entity = event.getEntity();
        var surface = entity.level().getBlockState(entity.getOnPos());
        var block = surface.getBlock();
        float boost;
        if (block instanceof CongealedSlimeBlock) {
            boost = .25F;
        } else if (block instanceof AntiqueSoilBlock soil && soil.isSlimyMud()) {
            boost = .15F;
        } else if (WorldModule.soil(surface)) {
            boost = .06F;
        } else {
            return;
        }
        // 怀古在原版跳跃速度算完后追加固定竖直速度，保留药水和冲刺的原有计算。
        entity.setDeltaMovement(entity.getDeltaMovement().add(0, boost, 0));
        entity.playSound(SoundEvents.SLIME_SQUISH, .5F + boost, 1F);
    }
}
