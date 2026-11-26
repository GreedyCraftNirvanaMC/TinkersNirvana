/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.AntiqueTraits;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class ToolCombat {
    private static final ThreadLocal<Entity> CHARGED_SCYTHE_TARGET = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SWEEPING = ThreadLocal.withInitial(() -> false);
    private ToolCombat() {}

    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) { return; }
        ItemStack projectileTool = ToolRanged.traitsFor(event.getSource().getDirectEntity());
        if (projectileTool.isEmpty() && !isMeleeDamage(event.getSource())) { return; }
        ItemStack tool = projectileTool.isEmpty() ? meleeTool(attacker) : projectileTool;
        if (!(tool.getItem() instanceof AntiqueTool item) || TinkerTools.isBroken(tool)) { return; }
        if (event.getSource().getDirectEntity() != attacker && projectileTool.isEmpty()) { return; }
        float strength = projectileTool.isEmpty() && attacker instanceof Player player ? player.getAttackStrengthScale(.5F) : 1F;
        float cooldown = .2F + strength * strength * .8F;
        // 原版进入伤害事件时已经乘了冷却系数；怀古先计算词条和递减伤害，再乘回冷却。
        float original = event.getAmount() / cooldown;
        float damage = original;
        for (ToolTraits.Entry entry : ToolTraits.getTraits(tool)) {
            damage = entry.trait().modifyAttack(tool, entry.level(), attacker, event.getEntity(), original, damage);
        }
        if (item.definition().action() == ToolDefinition.Action.HAMMER && event.getEntity().getType().is(EntityTypeTags.UNDEAD)) {
            damage += 3 + attacker.getRandom().nextInt(4);
        }
        boolean rapier = item.definition().action() == ToolDefinition.Action.RAPIER;
        boolean bolt = item.definition().action() == ToolDefinition.Action.BOLT && !projectileTool.isEmpty()
                && !event.getSource().is(AntiqueProjectile.ENDERFERENCE_DAMAGE);
        if (rapier || bolt) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> reduction / 2);
        }
        if (rapier) {
            event.setInvulnerabilityTicks(4);
        }
        damage = ToolModifiers.modifyAttack(tool, attacker, event.getEntity(), damage);
        event.setAmount(Math.max(0, applyDamageCutoff(damage, item.definition().action()) * cooldown));
        if (com.gctn.tconstruct.tools.traits.AntiqueTraits.has(tool, "squeaky", "modifyAttack")) {
            attacker.level().playSound(null, event.getEntity().blockPosition(), ToolSounds.SQUEAK.get(), net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
        }
    }

    public static float applyDamageCutoff(float damage, ToolDefinition.Action action) {
        float cutoff = switch (action) {
            case RAPIER -> 13F;
            case LONGSWORD -> 18F;
            case CLEAVER -> 25F;
            default -> 15F;
        };
        float total = 0F;
        float multiplier = 1F;
        while (damage > cutoff && multiplier > .001F) {
            total += cutoff * multiplier;
            damage -= cutoff;
            multiplier *= .9F;
        }
        return total + damage * multiplier;
    }

    public static void afterHit(AntiqueTool item, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (item.definition().action() == ToolDefinition.Action.FRYPAN) {
            attacker.level().playSound(null, attacker.blockPosition(), ToolSounds.FRYPAN.get(), net.minecraft.sounds.SoundSource.PLAYERS, 2, 1);
        }
        if (attacker instanceof Player player && target == CHARGED_SCYTHE_TARGET.get()
                && item.definition().action() == ToolDefinition.Action.SCYTHE && !SWEEPING.get()) {
            sweep(item, stack, target, player);
        }
        if (!(attacker instanceof Player)) { ToolModifiers.applyExtraKnockback(stack, target, attacker); }
        ToolModifiers.afterHit(stack, target, attacker);
    }

    public static void beforeHit(Player player, Entity target) {
        ItemStack stack = meleeTool(player);
        if (!player.level().isClientSide && target instanceof LivingEntity living
                && stack.getItem() instanceof AntiqueTool && !TinkerTools.isBroken(stack)) {
            AntiqueTraits.shockingHit(stack, player, living);
        }
    }

    public static ItemStack meleeTool(LivingEntity attacker) {
        ItemStack held = attacker.getMainHandItem();
        return held.getItem() instanceof AntiqueTool item && item.isAmmo() ? ItemStack.EMPTY : held;
    }

    public static float hurtKnockbackScale(DamageSource source) {
        if (!isMeleeDamage(source) || !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
            return 0;
        }
        ItemStack stack = meleeTool(attacker);
        if (!(stack.getItem() instanceof AntiqueTool tool) || TinkerTools.isBroken(stack)) {
            return 0;
        }
        return switch (tool.definition().action()) {
            case FRYPAN -> 2F;
            case LUMBERAXE -> 1.5F;
            case AXE -> 1.3F;
            case MATTOCK -> 1.1F;
            case RAPIER -> .6F;
            default -> 1F;
        };
    }

    private static boolean isMeleeDamage(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK);
    }

    public static void attack(Player player, Entity target, Runnable original) {
        if (player.level().isClientSide || !(player.getMainHandItem().getItem() instanceof AntiqueTool tool)
                || tool.definition().action() != ToolDefinition.Action.SCYTHE || player.getAttackStrengthScale(.5F) <= .9F) {
            original.run();
            return;
        }
        // 原版攻击在命中回调之前重置攻击冷却，因此在入口保存本次蓄满的目标。
        Entity previous = CHARGED_SCYTHE_TARGET.get();
        CHARGED_SCYTHE_TARGET.set(target);
        try {
            original.run();
        } finally {
            if (previous == null) {
                CHARGED_SCYTHE_TARGET.remove();
            } else {
                CHARGED_SCYTHE_TARGET.set(previous);
            }
        }
    }

    private static void sweep(AntiqueTool item, ItemStack stack, LivingEntity target, Player attacker) {
        boolean wide = ToolModifiers.level(stack, "harvestwidth") > 0;
        boolean tall = ToolModifiers.level(stack, "harvestheight") > 0;
        int width = wide ? 2 : 1;
        int height = tall ? 2 : 1;
        float damage = (float) attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        attacker.level().playSound(null, attacker.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1, 1);
        attacker.sweepAttack();
        SWEEPING.set(true);
        try {
            for (LivingEntity other : attacker.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(width, height, width))) {
                if (TinkerTools.isBroken(stack) || stack.isEmpty()) {
                    break;
                }
                if (other == attacker || other == target || !other.isAttackable() || attacker.isAlliedTo(other)
                        || ((wide || tall) && other.distanceToSqr(target) > 9)
                        || (other instanceof Player victim && !attacker.canHarmPlayer(victim))) {
                    continue;
                }
                beforeHit(attacker, other);
                if (other.hurt(attacker.damageSources().playerAttack(attacker), damage)) {
                    item.postHurtEnemy(stack, other, attacker);
                }
            }
        } finally {
            SWEEPING.remove();
        }
    }

    public static void release(AntiqueTool item, ItemStack stack, Player player, int used) {
        if (item.definition().action() == ToolDefinition.Action.LONGSWORD && used > 5 && !player.isFallFlying()) {
            Vec3 look = player.getLookAngle();
            float speed = Math.min(.925F, .05F * used);
            player.setDeltaMovement(look.x * speed, player.getDeltaMovement().y + Math.min(.56F, .02F * used + .2F), look.z * speed);
            player.hurtMarked = true;
            player.causeFoodExhaustion(.2F);
            player.getCooldowns().addCooldown(stack.getItem(), 3);
        } else if (item.definition().action() == ToolDefinition.Action.RAPIER && player.onGround() && !player.isFallFlying()) {
            Vec3 look = player.getLookAngle();
            player.setDeltaMovement(-look.x * .5, player.getDeltaMovement().y + .32, -look.z * .5);
            player.hurtMarked = true;
            player.causeFoodExhaustion(.1F);
            player.getCooldowns().addCooldown(stack.getItem(), 4);
        } else if (item.definition().action() == ToolDefinition.Action.FRYPAN && player.getMainHandItem() == stack) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            HitResult wall = player.pick(3.2, 1, false);
            Vec3 end = wall.getType() == HitResult.Type.BLOCK ? wall.getLocation() : eye.add(look.scale(3.2));
            var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player, eye, end,
                    player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1),
                    entity -> entity instanceof LivingEntity && entity.isPickable(), eye.distanceToSqr(end));
            if (hit == null || !(hit.getEntity() instanceof LivingEntity target) || player.isAlliedTo(target)
                    || (target instanceof Player victim && !player.canHarmPlayer(victim))) { return; }
            float progress = Math.min(1, used / 30F);
            var attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            var id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "frypan_charge");
            if (attribute == null) { return; }
            float before = target.getHealth() + target.getAbsorptionAmount();
            attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, progress * 5,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
            // 拉满时只在结算这一次命中期间点燃目标，让致死一击按燃烧状态计算掉落。
            boolean chargedIgnition = progress >= 1 && !target.isOnFire();
            if (chargedIgnition) {
                target.igniteForSeconds(1);
            }
            try {
                player.attack(target);
            } finally {
                attribute.removeModifier(id);
                if (chargedIgnition) {
                    target.clearFire();
                }
            }
            if (target.getHealth() + target.getAbsorptionAmount() < before) {
                float strength = .1F + 2.5F * progress * progress;
                target.setDeltaMovement(target.getDeltaMovement().add(look.x * strength, look.y / 3 * strength + .1F + .4F * progress, look.z * strength));
                target.hurtMarked = true;
                player.level().playSound(null, player.blockPosition(), ToolSounds.FRYPAN.get(), player.getSoundSource(),
                        1.5F, .6F + .2F * player.getRandom().nextFloat());
            }
        }
    }
}
