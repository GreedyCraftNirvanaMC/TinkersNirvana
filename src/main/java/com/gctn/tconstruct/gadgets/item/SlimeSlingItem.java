/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's ItemSlimeSling. */
package com.gctn.tconstruct.gadgets.item;

import com.gctn.tconstruct.gadgets.GearNetwork;
import com.gctn.tconstruct.gadgets.SlimeGear;
import com.gctn.tconstruct.tools.ToolSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class SlimeSlingItem extends Item {
    public final String color;
    public SlimeSlingItem(String color) { super(new Properties().stacksTo(1)); this.color = color; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) { player.startUsingItem(hand); return InteractionResultHolder.consume(player.getItemInHand(hand)); }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!(entity instanceof ServerPlayer player) || !player.onGround() || getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getType() != HitResult.Type.BLOCK) { return; }
        float charge = (getUseDuration(stack, player) - remaining) / 20f;
        float strength = Math.min(6, (charge * charge + charge * 2) / 3 * 4);
        var look = player.getLookAngle().normalize();
        var motion = player.getDeltaMovement().add(-look.x * strength, -look.y * strength / 3, -look.z * strength);
        player.setDeltaMovement(motion);
        // The native velocity packet clips at 3.9 blocks/tick; a full sling charge reaches six.
        PacketDistributor.sendToPlayer(player, new GearNetwork.Velocity(motion));
        level.playSound(null, player.blockPosition(), ToolSounds.SLIMESLING.get(), SoundSource.PLAYERS, 1, 1);
    }
}
