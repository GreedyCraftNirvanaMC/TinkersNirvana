/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's ItemPiggybackPack. */
package com.gctn.tconstruct.gadgets.item;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.gadgets.SlimeGear;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PiggybackItem extends ArmorItem {
    public PiggybackItem() { super(SlimeGear.PIGGYBACK_ARMOR, Type.CHESTPLATE, new Properties().stacksTo(16)); }
    @Override public ItemAttributeModifiers getDefaultAttributeModifiers() { return ItemAttributeModifiers.EMPTY; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { return InteractionResultHolder.pass(player.getItemInHand(hand)); }
    public static List<Entity> chain(Entity base) {
        List<Entity> entities = new ArrayList<>(); Entity current = base;
        while (current != null && entities.size() <= 16) {
            if (entities.contains(current) || current.getPassengers().size() > 1) { return List.of(); }
            entities.add(current); current = current.getFirstPassenger();
        }
        return current == null ? entities : List.of();
    }
    private InteractionResult reject(Player player, String reason) { player.displayClientMessage(Component.translatable("message.tconstruct.piggyback." + reason), true); return InteractionResult.FAIL; }
    @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) { return InteractionResult.SUCCESS; }
        if (Config.LIMIT_PIGGYBACK.get() && !(target instanceof Player) && !(target instanceof Mob mob && mob.canHaveALeashAttachedToIt())) { return reject(player, "cannot_pick_up"); }
        var carried = chain(player); var picked = chain(target);
        if (carried.isEmpty() || picked.isEmpty() || carried.contains(target) || picked.contains(player) || player.getVehicle() == target) { return reject(player, "cannot_pick_up"); }
        int count = carried.size() - 1 + picked.size();
        if (count > Config.MAX_PIGGYBACK.get() || carried.stream().skip(1).filter(Player.class::isInstance).count() + picked.stream().filter(Player.class::isInstance).count() > 1) { return reject(player, "full"); }
        ItemStack equipped = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean pack = equipped.getItem() == this;
        int needed = Math.max(0, count - (pack ? equipped.getCount() : 0));
        if (stack.getCount() < needed || !pack && player.getInventory().getFreeSlot() < 0) { return reject(player, "inventory_full"); }
        if (!target.startRiding(carried.getLast(), true)) { return reject(player, "cannot_pick_up"); }
        if (!pack) {
            player.setItemSlot(EquipmentSlot.CHEST, stack.split(needed));
            if (!equipped.isEmpty()) { player.getInventory().placeItemBackInInventory(equipped); }
        } else { stack.shrink(needed); equipped.grow(needed); }
        player.getData(SlimeGear.CARRY_STATE).active = true;
        if (player instanceof ServerPlayer serverPlayer) { serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetPassengersPacket(player)); }
        return InteractionResult.CONSUME;
    }
}
