/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class ToolCombat {
    private static final ThreadLocal<Boolean> SWEEPING = ThreadLocal.withInitial(() -> false);
    private ToolCombat() {}

    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) { return; }
        ItemStack projectileTool = ToolRanged.traitsFor(event.getSource().getDirectEntity());
        if (projectileTool.isEmpty() && !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                && !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)) { return; }
        ItemStack tool = projectileTool.isEmpty() ? attacker.getMainHandItem() : projectileTool;
        if (!(tool.getItem() instanceof AntiqueTool item) || TinkerTools.isBroken(tool)) { return; }
        if (event.getSource().getDirectEntity() != attacker && projectileTool.isEmpty()) { return; }
        float original = event.getAmount();
        float damage = original;
        for (ToolTraits.Entry entry : ToolTraits.getTraits(tool)) {
            damage = entry.trait().modifyAttack(tool, entry.level(), attacker, event.getEntity(), original, damage);
        }
        if (item.definition().action() == ToolDefinition.Action.HAMMER && event.getEntity().getType().is(EntityTypeTags.UNDEAD)) {
            damage += 3 + attacker.getRandom().nextInt(4);
        }
        if (item.definition().action() == ToolDefinition.Action.RAPIER) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> reduction / 2);
            event.setInvulnerabilityTicks(4);
        }
        event.setAmount(Math.max(0, ToolModifiers.modifyAttack(tool, attacker, event.getEntity(), damage)));
        if (com.gctn.tconstruct.tools.traits.AntiqueTraits.has(tool, "squeaky")) {
            attacker.level().playSound(null, event.getEntity().blockPosition(), ToolSounds.SQUEAK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
        }
    }

    public static void afterHit(AntiqueTool item, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (item.definition().action() == ToolDefinition.Action.FRYPAN) {
            target.knockback(1, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            attacker.level().playSound(null, attacker.blockPosition(), ToolSounds.FRYPAN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1);
        }
        if (item.definition().action() == ToolDefinition.Action.SCYTHE && !SWEEPING.get()) {
            SWEEPING.set(true);
            try {
                for (LivingEntity other : attacker.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.5))) {
                    if (other == attacker || other == target || attacker.isAlliedTo(other)
                            || (attacker instanceof Player player && other instanceof Player victim && !player.canHarmPlayer(victim))) { continue; }
                    other.hurt(attacker.damageSources().mobAttack(attacker), ToolBuilder.savedStats(stack).getFloat("Attack"));
                }
            } finally { SWEEPING.set(false); }
        }
        ToolModifiers.afterHit(stack, target, attacker);
    }

    public static void release(AntiqueTool item, ItemStack stack, Player player, int used) {
        if (player.isFallFlying()) { return; }
        if (item.definition().action() == ToolDefinition.Action.LONGSWORD && used > 5) {
            Vec3 look = player.getLookAngle();
            float speed = Math.min(.925F, .05F * used);
            player.setDeltaMovement(look.x * speed, player.getDeltaMovement().y + Math.min(.56F, .02F * used + .2F), look.z * speed);
            player.hurtMarked = true;
            player.causeFoodExhaustion(.2F);
            player.getCooldowns().addCooldown(stack.getItem(), 3);
        } else if (item.definition().action() == ToolDefinition.Action.RAPIER && player.onGround()) {
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(-look.x * .5, .32, -look.z * .5);
            player.hurtMarked = true;
            player.getCooldowns().addCooldown(stack.getItem(), 4);
        } else if (item.definition().action() == ToolDefinition.Action.FRYPAN && player.getMainHandItem() == stack) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(3.2));
            var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player, eye, end,
                    player.getBoundingBox().expandTowards(look.scale(3.2)).inflate(1), entity -> entity instanceof LivingEntity && entity.isPickable(), 3.2 * 3.2);
            if (hit == null || !(hit.getEntity() instanceof LivingEntity target) || player.isAlliedTo(target)
                    || (target instanceof Player victim && !player.canHarmPlayer(victim))) { return; }
            float progress = Math.min(1, used / 30F);
            var attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "frypan_charge");
            if (attribute == null) { return; }
            float before = target.getHealth() + target.getAbsorptionAmount();
            attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, progress * 5,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
            try { player.attack(target); }
            finally { attribute.removeModifier(id); }
            if (target.getHealth() + target.getAbsorptionAmount() < before) {
                float strength = .1F + 2.5F * progress * progress;
                target.setDeltaMovement(target.getDeltaMovement().add(look.x * strength, look.y / 3 * strength + .1F + .4F * progress, look.z * strength));
                target.hurtMarked = true;
            }
        }
    }
}
