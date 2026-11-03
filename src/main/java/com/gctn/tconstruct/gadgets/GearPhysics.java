/* SPDX-License-Identifier: LGPL-3.0-only; Antique's bounce and piggyback lifecycles. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.item.PiggybackItem;
import com.gctn.tconstruct.gadgets.item.SlimeBootsItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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
        if (entity instanceof Player player ? entity.level().isClientSide && player.isLocalPlayer() : !entity.level().isClientSide) {
            var motion = entity.getDeltaMovement(); var bounce = entity.getData(SlimeGear.BOUNCE);
            bounce.active = true; bounce.pendingY = Math.max(0, -motion.y * .9); bounce.groundTicks = 0;
            entity.setDeltaMovement(motion.x / .95, motion.y, motion.z / .95);
            if (entity.level().isClientSide || !(entity instanceof Player)) { entity.playSound(SoundEvents.SLIME_SQUISH, 1, 1); }
        }
        if (!entity.level().isClientSide && entity instanceof Player player) { entity.level().playSound(player, entity.blockPosition(), SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundSource.PLAYERS, 1, 1); }
    }
    private static void bounce(LivingEntity entity, boolean preserveHorizontal) {
        if (!entity.hasData(SlimeGear.BOUNCE)) { return; }
        BounceState state = entity.getData(SlimeGear.BOUNCE);
        if (!state.active || entity.isFallFlying()) { return; }
        var motion = entity.getDeltaMovement();
        if (!Double.isNaN(state.pendingY)) { entity.setDeltaMovement(motion.x, state.pendingY, motion.z); entity.setOnGround(false); state.pendingY = Double.NaN; }
        motion = entity.getDeltaMovement();
        if (preserveHorizontal && !entity.onGround() && (motion.x != state.lastX || motion.z != state.lastZ)) {
            entity.setDeltaMovement(motion.x / .935, motion.y, motion.z / .935); state.lastX = entity.getDeltaMovement().x; state.lastZ = entity.getDeltaMovement().z;
        }
        if (state.wasAir && entity.onGround()) { if (++state.groundTicks > 5) { entity.removeData(SlimeGear.BOUNCE); } }
        else { state.groundTicks = 0; state.wasAir = true; }
    }
    @SubscribeEvent private static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) { if (player.isLocalPlayer()) { bounce(player, true); } return; }
        carry(player);
    }
    @SubscribeEvent private static void entityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player) && !living.level().isClientSide) { bounce(living, false); }
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
            if (effect == null || effect.getAmplifier() != count - 1 || effect.getDuration() <= 300) {
                if (effect != null && effect.getAmplifier() != count - 1) { player.removeEffect(SlimeGear.CARRY); }
                player.addEffect(new MobEffectInstance(SlimeGear.CARRY, 600, count - 1, true, false));
            }
        } else { state.active = false; player.removeEffect(SlimeGear.CARRY); }
        int[] passengers = player.getPassengers().stream().mapToInt(net.minecraft.world.entity.Entity::getId).toArray();
        if (!java.util.Arrays.equals(passengers, state.passengers) && player instanceof ServerPlayer serverPlayer) { serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(player)); state.passengers = passengers; }
        if (!state.active) { player.removeData(SlimeGear.CARRY_STATE); }
    }
}
