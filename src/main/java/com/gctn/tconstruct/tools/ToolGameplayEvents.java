/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.traits.AntiqueTraits;
import com.gctn.tconstruct.tools.traits.TinkerEffects;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class ToolGameplayEvents {
    private static final String SOULBOUND = "tconstruct:soulbound_items";
    private ToolGameplayEvents() {}

    @SubscribeEvent
    public static void drops(BlockDropsEvent event) {
        ItemStack tool = event.getTool();
        if (!(tool.getItem() instanceof TinkerTools) || TinkerTools.isBroken(tool)) { return; }
        ToolTraits.forEach(tool, (trait, level) -> trait.onBlockDrops(tool, level, event));
        if (ToolModifiers.level(tool, "autosmelt") > 0 && !AntiqueTraits.has(tool, "autosmelt")) { ToolModifiers.smeltDrops(event); }
        int blasting = ToolModifiers.level(tool, "blasting");
        if (blasting > 0 && !event.getLevel().random.nextBoolean()) { event.getDrops().clear(); }
        if (tool.getItem() instanceof AntiqueTool item && (item.definition().action() == ToolDefinition.Action.KAMA || item.definition().action() == ToolDefinition.Action.SCYTHE)
                && event.getState().is(net.minecraft.tags.BlockTags.LEAVES)) {
            event.getDrops().clear();
            event.getDrops().add(new ItemEntity(event.getLevel(), event.getPos().getX() + .5, event.getPos().getY() + .5, event.getPos().getZ() + .5,
                    new ItemStack(event.getState().getBlock())));
        }
        ToolHarvest.reserveSeed(event);
    }

    @SubscribeEvent
    public static void projectileHit(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if (event.getSource().getDirectEntity() instanceof AntiqueProjectile || event.getNewDamage() <= 0
                || !(event.getSource().getEntity() instanceof LivingEntity attacker)) { return; }
        ItemStack tool = ToolRanged.traitsFor(event.getSource().getDirectEntity());
        if (!tool.isEmpty()) {
            ToolTraits.forEach(tool, (trait, level) -> trait.afterHit(tool, level, event.getEntity(), attacker));
            ToolModifiers.afterHit(tool, event.getEntity(), attacker);
        }
    }

    @SubscribeEvent
    public static void blasting(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        int level = ToolModifiers.level(tool, "blasting");
        if (level > 0 && !TinkerTools.isBroken(tool)) {
            var pos = event.getPosition().orElse(event.getEntity().blockPosition());
            float hardness = event.getState().getDestroySpeed(event.getEntity().level(), pos);
            if (hardness > 0) { event.setNewSpeed(Math.max(event.getNewSpeed(), ToolBuilder.savedStats(tool).getFloat("MiningSpeed") * hardness / (level > 2 ? 1.1F : level > 1 ? 1.5F : 2))); }
        }
    }

    @SubscribeEvent
    public static void defense(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack tool = entity.isUsingItem() ? entity.getUseItem() : entity.getMainHandItem();
        if (tool.getItem() instanceof TinkerTools && !TinkerTools.isBroken(tool)) {
            ToolTraits.forEach(tool, (trait, level) -> trait.onDamageTaken(tool, level, event));
        }
    }

    @SubscribeEvent
    public static void heavy(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof TinkerTools && !TinkerTools.isBroken(event.getItemStack())
                && AntiqueTraits.has(event.getItemStack(), "heavy")) {
            event.addModifier(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "heavy"), 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND);
        }
    }

    @SubscribeEvent
    public static void teleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntity() instanceof LivingEntity entity && entity.hasEffect(TinkerEffects.ENDERFERENCE)) { event.setCanceled(true); }
    }

    @SubscribeEvent
    public static void experience(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player != null && AntiqueTraits.has(player.getMainHandItem(), "established") && !TinkerTools.isBroken(player.getMainHandItem())) {
            int xp = event.getDroppedExperience();
            event.setDroppedExperience(xp == 0 ? (player.getRandom().nextFloat() < .03F ? 1 : 0) : 1 + Math.round(xp * 1.25F + player.getRandom().nextFloat() * .25F));
        }
    }

    @SubscribeEvent
    public static void beheadingAndSoulbound(LivingDropsEvent event) {
        if (event.getEntity().getType() == net.minecraft.world.entity.EntityType.WITHER_SKELETON
                && event.getSource().getEntity() instanceof Player player && player.getRandom().nextFloat() < .07F) {
            event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(ToolSupplies.item("necrotic_bone"))));
        }
        if (event.getEntity() instanceof Player player) {
            ListTag kept = new ListTag();
            event.getDrops().removeIf(drop -> {
                if (drop.getItem().getItem() instanceof TinkerTools && ToolModifiers.level(drop.getItem(), "soulbound") > 0) {
                    kept.add(drop.getItem().save(player.registryAccess()));
                    return true;
                }
                return false;
            });
            if (!kept.isEmpty()) { player.getPersistentData().put(SOULBOUND, kept); }
        }
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) { return; }
        ItemStack tool = event.getSource().getDirectEntity() instanceof AntiqueProjectile arrow ? arrow.traitTool() : attacker.getMainHandItem();
        if (!(tool.getItem() instanceof AntiqueTool item) || TinkerTools.isBroken(tool)) { return; }
        int level = ToolModifiers.level(tool, "beheading") + (item.definition().action() == ToolDefinition.Action.CLEAVER ? 2 : 0);
        if (level == 0 || attacker.getRandom().nextFloat() >= level * .1F) { return; }
        var type = event.getEntity().getType();
        var head = type == net.minecraft.world.entity.EntityType.ZOMBIE ? Items.ZOMBIE_HEAD
                : type == net.minecraft.world.entity.EntityType.SKELETON ? Items.SKELETON_SKULL
                : type == net.minecraft.world.entity.EntityType.WITHER_SKELETON ? Items.WITHER_SKELETON_SKULL
                : type == net.minecraft.world.entity.EntityType.CREEPER ? Items.CREEPER_HEAD
                : type == net.minecraft.world.entity.EntityType.PIGLIN ? Items.PIGLIN_HEAD : null;
        if (head != null) { event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(head))); }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal().getPersistentData().contains(SOULBOUND)) {
            event.getEntity().getPersistentData().put(SOULBOUND, event.getOriginal().getPersistentData().getList(SOULBOUND, Tag.TAG_COMPOUND).copy());
            event.getOriginal().getPersistentData().remove(SOULBOUND);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        ListTag items = player.getPersistentData().getList(SOULBOUND, Tag.TAG_COMPOUND);
        player.getPersistentData().remove(SOULBOUND);
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(player.registryAccess(), items.getCompound(i));
            if (!stack.isEmpty() && !player.addItem(stack)) { player.drop(stack, false); }
        }
    }

    @SubscribeEvent
    public static void mending(PlayerXpEvent.XpChange event) {
        if (event.getAmount() <= 0) { return; }
        int remaining = event.getAmount();
        for (ItemStack tool : new ItemStack[]{event.getEntity().getMainHandItem(), event.getEntity().getOffhandItem()}) {
            int moss = ToolModifiers.level(tool, "mending_moss");
            if (moss == 0 || !(tool.getItem() instanceof TinkerTools)) { continue; }
            CompoundTag data = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(tool);
            int capacity = 100 * (int) Math.pow(3, moss - 1);
            int stored = Math.min(remaining, Math.max(0, capacity - data.getInt("MossXP")));
            if (stored > 0) {
                data.putInt("MossXP", data.getInt("MossXP") + stored);
                tool.set(com.gctn.tconstruct.init.TinkerDataComponents.TINKER_MATERIAL.get(), net.minecraft.world.item.component.CustomData.of(data));
                remaining -= stored;
            }
        }
        event.setAmount(remaining);
    }

    @SubscribeEvent
    public static void offhand(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == net.minecraft.world.InteractionHand.OFF_HAND
                && event.getEntity().getMainHandItem().getItem() instanceof AntiqueTool item && item.definition().action() == ToolDefinition.Action.CLEAVER) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void reflect(net.neoforged.neoforge.event.entity.ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit)
                || !(hit.getEntity() instanceof Player player) || !player.isBlocking()) { return; }
        ItemStack stack = player.getUseItem();
        if (!(stack.getItem() instanceof AntiqueTool item) || item.definition().action() != ToolDefinition.Action.BATTLESIGN || TinkerTools.isBroken(stack)) { return; }
        var projectile = event.getProjectile();
        if (projectile.level().isClientSide || projectile.getOwner() == player || projectile.getDeltaMovement().lengthSqr() < .001) { return; }
        var look = player.getLookAngle();
        if (-look.dot(projectile.getDeltaMovement().normalize()) < .1) { return; }
        float speed = (float) projectile.getDeltaMovement().length() + .2F;
        projectile.deflect((entity, deflector, random) -> entity.shoot(look.x, look.y, look.z, speed, 0), player, player, true);
        item.damage(stack, Math.max(1, (int) Math.ceil(speed)), player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void shield(net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent event) {
        ItemStack stack = event.getEntity().getUseItem();
        if (!(stack.getItem() instanceof AntiqueTool item) || item.definition().action() != ToolDefinition.Action.BATTLESIGN
                || TinkerTools.isBroken(stack) || !event.getBlocked()) { return; }
        float amount = event.getOriginalBlockedDamage();
        event.setShieldDamage(0);
        if (!event.getDamageSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            event.setBlockedDamage(amount * .65F);
            if (event.getDamageSource().getEntity() instanceof LivingEntity attacker && attacker != event.getEntity()
                    && !event.getDamageSource().is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
                attacker.hurt(event.getEntity().damageSources().thorns(event.getEntity()), amount * .35F);
            }
        }
        item.damage(stack, Math.max(1, Math.round(amount * .75F)), event.getEntity(), LivingEntity.getSlotForHand(event.getEntity().getUsedItemHand()));
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void moss(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ToolSupplies.item("moss"))
                || event.getLevel().getBlockState(event.getPos()).getEnchantPowerBonus(event.getLevel(), event.getPos()) < 1) { return; }
        Player player = event.getEntity();
        if (event.getLevel().isClientSide) { return; }
        if (player.experienceLevel < 5 && !player.getAbilities().instabuild) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.tconstruct.moss_levels"), true);
        } else {
            if (!player.getAbilities().instabuild) { event.getItemStack().shrink(1); player.giveExperienceLevels(-5); }
            ItemStack result = new ItemStack(ToolSupplies.item("mending_moss"));
            if (!player.addItem(result)) { player.drop(result, false); }
            player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
        }
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }
}
