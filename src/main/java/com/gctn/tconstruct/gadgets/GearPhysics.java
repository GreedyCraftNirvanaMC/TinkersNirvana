/* SPDX-License-Identifier: LGPL-3.0-only; Antique's bounce and piggyback lifecycles. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.item.PiggybackItem;
import com.gctn.tconstruct.gadgets.item.SlimeBootsItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class GearPhysics {
    public static class BounceState { public boolean active, wasAir; public int groundTicks; public double pendingY = Double.NaN; double lastX, lastZ; }
    public static class CarryState { public boolean active; int[] passengers = new int[0]; }
    private GearPhysics() {}
    @SubscribeEvent private static void fall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof SlimeBootsItem)) { return; }
        if (entity.isShiftKeyDown()) { event.setDamageMultiplier(.2f); return; }
        if (event.getDistance() <= 2) { return; }
        event.setDamageMultiplier(0); event.setCanceled(true); entity.fallDistance = 0;
        if (entity instanceof Player player) {
            // 怀古只给真实玩家启动反弹。其余实体穿靴子也免受跌落伤害。
            if (player.isLocalPlayer() && player.level().isClientSide) {
                var motion = player.getDeltaMovement();
                var bounce = player.getData(SlimeGear.BOUNCE);
                bounce.active = true;
                bounce.pendingY = -motion.y * .9;
                bounce.groundTicks = 0;
                player.setDeltaMovement(motion.x / .95, motion.y, motion.z / .95);
                player.playSound(SoundEvents.SLIME_SQUISH, 1, 1);
            } else if (!player.level().isClientSide) {
                player.level().playSound(player, player.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1, 1);
            }
        } else if (!entity.level().isClientSide) {
            entity.playSound(SoundEvents.SLIME_SQUISH, 1, 1);
        }
    }
    private static void bounce(Player entity) {
        if (!entity.hasData(SlimeGear.BOUNCE)) { return; }
        BounceState state = entity.getData(SlimeGear.BOUNCE);
        if (!state.active || entity.isFallFlying()) { return; }
        var motion = entity.getDeltaMovement();
        if (!Double.isNaN(state.pendingY)) { entity.setDeltaMovement(motion.x, state.pendingY, motion.z); entity.setOnGround(false); state.pendingY = Double.NaN; }
        motion = entity.getDeltaMovement();
        if (!entity.onGround() && (motion.x != state.lastX || motion.z != state.lastZ)) {
            entity.setDeltaMovement(motion.x / .935, motion.y, motion.z / .935); state.lastX = entity.getDeltaMovement().x; state.lastZ = entity.getDeltaMovement().z;
        }
        if (state.wasAir && entity.onGround()) { if (++state.groundTicks > 6) { entity.removeData(SlimeGear.BOUNCE); } }
        else { state.groundTicks = 0; state.wasAir = true; }
    }
    @SubscribeEvent private static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) { if (player.isLocalPlayer()) { bounce(player); } return; }
        carry(player);
    }
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST) private static void interact(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof PiggybackItem pack && event.getTarget() instanceof LivingEntity target) {
            InteractionResult result = pack.interactLivingEntity(event.getItemStack(), event.getEntity(), target, event.getHand());
            event.setCancellationResult(result); event.setCanceled(true);
        }
    }
    private static void carry(Player player) {
        var equipped = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean pack = equipped.getItem() instanceof PiggybackItem;
        boolean active = player.hasData(SlimeGear.CARRY_STATE) && player.getData(SlimeGear.CARRY_STATE).active || player.hasEffect(SlimeGear.CARRY);
        if (!active && !(pack && player.isVehicle())) { return; }
        CarryState state = player.getData(SlimeGear.CARRY_STATE);
        int max = pack ? Math.min(com.gctn.tconstruct.Config.MAX_PIGGYBACK.get(), equipped.getCount()) : 0;
        var chain = PiggybackItem.chain(player);
        if (chain.isEmpty()) { for (var passenger : java.util.List.copyOf(player.getPassengers())) { passenger.stopRiding(); } }
        else { for (int i = chain.size() - 1; i > max; i--) { chain.get(i).stopRiding(); } }
        int count = Math.max(0, PiggybackItem.chain(player).size() - 1);
        if (count > 0) {
            state.active = true;
            var effect = player.getEffect(SlimeGear.CARRY);
            if (effect == null || effect.getAmplifier() != count - 1 || effect.getDuration() <= 300 || !effect.showIcon()) {
                if (effect != null && effect.getAmplifier() != count - 1) { player.removeEffect(SlimeGear.CARRY); }
                // 怀古的背负效果不产生粒子，但会在 HUD 和物品栏显示对应乘客数量的图标。
                player.addEffect(new MobEffectInstance(SlimeGear.CARRY, 600, count - 1, true, false, true));
            }
        } else { state.active = false; player.removeEffect(SlimeGear.CARRY); }
        int[] passengers = player.getPassengers().stream().mapToInt(net.minecraft.world.entity.Entity::getId).toArray();
        if (!java.util.Arrays.equals(passengers, state.passengers) && player instanceof ServerPlayer serverPlayer) { serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(player)); state.passengers = passengers; }
        if (!state.active) { player.removeData(SlimeGear.CARRY_STATE); }
    }
}
