/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ToolRanged {
    static final int AMMO_DURABILITY = 10;
    private static final String CROSSBOW_LOADED = "CrossbowLoaded";
    private static final String LAUNCHER_TAG = "TinkerLauncher";
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TinkersNirvana.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<AntiqueProjectile>> PROJECTILE = ENTITIES.register("tool_projectile",
            () -> EntityType.Builder.<AntiqueProjectile>of(AntiqueProjectile::new, MobCategory.MISC)
                    .sized(.5F, .5F).clientTrackingRange(4).updateInterval(20).build("tool_projectile"));
    private ToolRanged() {}
    public static void register(IEventBus bus) { ENTITIES.register(bus); }

    public static int drawTime(AntiqueTool item, ItemStack stack) {
        float speed = ToolBuilder.savedStats(stack).getFloat("DrawSpeed");
        return (int) Math.ceil(baseDrawTime(item) / (double) Math.max(Float.MIN_NORMAL, speed));
    }

    public static float drawProgress(AntiqueTool item, ItemStack stack, int used) {
        float speed = ToolBuilder.savedStats(stack).getFloat("DrawSpeed");
        return Math.clamp(speed * used / baseDrawTime(item), 0, 1);
    }

    private static int baseDrawTime(AntiqueTool item) {
        return switch (item.definition().action()) { case LONGBOW -> 30; case CROSSBOW -> 45; default -> 12; };
    }

    private static boolean supported(AntiqueTool launcher, ItemStack stack) {
        if (stack.getItem() instanceof AntiqueTool ammo) {
            return remainingAmmo(stack) > 0 && (launcher.definition().action() == ToolDefinition.Action.CROSSBOW
                    ? ammo.definition().action() == ToolDefinition.Action.BOLT : ammo.definition().action() == ToolDefinition.Action.ARROW);
        }
        return launcher.definition().action() != ToolDefinition.Action.CROSSBOW && stack.getItem() instanceof ArrowItem;
    }

    private static ItemStack findAmmo(AntiqueTool launcher, Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (supported(launcher, stack)) { return stack; }
        }
        for (ItemStack stack : player.getInventory().items) { if (supported(launcher, stack)) { return stack; } }
        return ItemStack.EMPTY;
    }

    public static InteractionResultHolder<ItemStack> use(AntiqueTool item, Level level, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (item.definition().action() == ToolDefinition.Action.SHURIKEN) {
            if (!player.getAbilities().instabuild && remainingAmmo(tool) == 0) {
                return InteractionResultHolder.fail(tool);
            }
            if (!level.isClientSide) {
                shoot(item, tool, tool, player, 1, 1, hand, false);
            }
            return InteractionResultHolder.sidedSuccess(tool, level.isClientSide);
        }
        if (item.definition().action() == ToolDefinition.Action.CROSSBOW && isLoaded(tool)) {
            if (!level.isClientSide) {
                var legacy = tool.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
                boolean prepaid = !legacy.isEmpty();
                ItemStack ammo = prepaid ? legacy.getItems().getFirst() : findAmmo(item, player);
                if (ammo.isEmpty() && player.getAbilities().instabuild) { ammo = new ItemStack(Items.ARROW); }
                if (ammo.isEmpty() || shoot(item, tool, ammo, player, 1, 1, hand, prepaid)) {
                    setLoaded(tool, false);
                    tool.remove(DataComponents.CHARGED_PROJECTILES);
                }
            }
            return InteractionResultHolder.sidedSuccess(tool, level.isClientSide);
        }
        boolean hasAmmo = !findAmmo(item, player).isEmpty();
        var hook = EventHooks.onArrowNock(tool, level, player, hand, hasAmmo);
        if (hook != null) { return hook; }
        if (!hasAmmo && !player.getAbilities().instabuild) { return InteractionResultHolder.fail(tool); }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(tool);
    }

    public static boolean isLoaded(ItemStack tool) {
        return TinkerDataComponents.getMaterialTag(tool).getBoolean(CROSSBOW_LOADED)
                || !tool.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty();
    }

    private static void setLoaded(ItemStack tool, boolean loaded) {
        var data = TinkerDataComponents.getMaterialTag(tool);
        if (loaded) {
            data.putBoolean(CROSSBOW_LOADED, true);
        } else {
            data.remove(CROSSBOW_LOADED);
            data.remove("LoadedAmmo");
        }
        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
    }

    public static void release(AntiqueTool item, ItemStack tool, Level level, LivingEntity entity, int used) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (item.definition().action() == ToolDefinition.Action.CROSSBOW) {
            if (!level.isClientSide && drawProgress(item, tool, used) >= 1 && !isLoaded(tool)) {
                // 怀古的上弦只保存就绪状态，发射时才从当时的背包消耗弩箭。
                setLoaded(tool, true);
                level.playSound(null, player.blockPosition(), ToolSounds.CROSSBOW_RELOAD.get(), SoundSource.PLAYERS, 1.5F, .9F + level.random.nextFloat() * .1F);
            }
            return;
        }
        ItemStack ammo = findAmmo(item, player);
        if (ammo.isEmpty() && !player.getAbilities().instabuild) { return; }
        used = EventHooks.onArrowLoose(tool, level, player, used, !ammo.isEmpty());
        if (used < 5 || level.isClientSide) {
            return;
        }
        // 怀古以实际拉弓五刻为下限。长弓在五刻后的低速箭仍会发射。
        if (ammo.isEmpty()) { ammo = new ItemStack(Items.ARROW); }
        float progress = drawProgress(item, tool, used);
        float power = BowItem.getPowerForTime((int) (progress * 20)) * progress;
        shoot(item, tool, ammo, player, power, progress, player.getUsedItemHand(), false);
    }

    private static int remainingAmmo(ItemStack ammo) {
        return Math.max(0, ammo.getMaxDamage() - ammo.getDamageValue()) / AMMO_DURABILITY;
    }

    private static ItemStack pickupFor(ItemStack ammo) {
        ItemStack pickup = ammo.copyWithCount(1);
        if (ammo.getItem() instanceof AntiqueTool) { pickup.setDamageValue(Math.max(0, pickup.getMaxDamage() - AMMO_DURABILITY)); }
        return pickup;
    }

    private static boolean consumeAmmo(ItemStack ammo, Player player) {
        if (player.getAbilities().instabuild) {
            return false;
        }
        if (!(ammo.getItem() instanceof AntiqueTool item)) {
            ammo.shrink(1);
            return true;
        }
        int before = remainingAmmo(ammo);
        EquipmentSlot slot = ammo == player.getOffhandItem() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
        item.damage(ammo, AMMO_DURABILITY, player, slot);
        int after = remainingAmmo(ammo);
        if (after == 0 && !ammo.isEmpty()) {
            ammo.setDamageValue(ammo.getMaxDamage());
        }
        return after < before;
    }

    private static boolean shoot(AntiqueTool launcher, ItemStack tool, ItemStack ammo, Player player, float power, float progress,
                                 InteractionHand hand, boolean prepaid) {
        // 在扣除耐久和运行词条前保留快照，命中时不再依赖玩家当前手持物。
        ItemStack firedFrom = tool.copyWithCount(1);
        boolean crossbow = launcher.definition().action() == ToolDefinition.Action.CROSSBOW;
        boolean shuriken = launcher.definition().action() == ToolDefinition.Action.SHURIKEN;
        ItemStack pickup = prepaid ? ammo.copyWithCount(1) : pickupFor(ammo);
        ItemStack traits = ammo.getItem() instanceof AntiqueTool ? ammo.copyWithCount(1) : tool.copy();
        if (prepaid) {
            var loaded = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(tool).getCompound("LoadedAmmo");
            ItemStack original = ItemStack.parseOptional(player.registryAccess(), loaded);
            if (!original.isEmpty() && original.getItem() instanceof AntiqueTool) { traits = original; }
        }
        boolean splits = !shuriken && ammo.getItem() instanceof AntiqueTool
                && com.gctn.tconstruct.tools.traits.AntiqueTraits.has(traits, "splitting");
        boolean endspeed = ammo.getItem() instanceof AntiqueTool
                && com.gctn.tconstruct.tools.traits.AntiqueTraits.has(traits, "endspeed");
        if (ammo.getItem() instanceof AntiqueTool && tool != ammo) {
            var tag = com.gctn.tconstruct.init.TinkerDataComponents.getTraitsTag(traits);
            var entries = com.gctn.tconstruct.tools.traits.ToolTraits.getTraits(tool).stream()
                    .map(entry -> new com.gctn.tconstruct.tools.traits.AbstractTraits.TraitEntry(entry.trait(), entry.level())).toList();
            com.gctn.tconstruct.tools.traits.ToolTraits.addTraits(tag, entries);
            traits.set(com.gctn.tconstruct.init.TinkerDataComponents.TINKER_TRAITS.get(), net.minecraft.world.item.component.CustomData.of(tag));
            var material = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(traits);
            var modifiers = material.getCompound("Modifiers").copy();
            var launcherModifiers = ToolModifiers.data(tool);
            for (String id : launcherModifiers.getAllKeys()) { modifiers.putInt(id, Math.max(modifiers.getInt(id), launcherModifiers.getInt(id))); }
            material.put("Modifiers", modifiers);
            traits.set(com.gctn.tconstruct.init.TinkerDataComponents.TINKER_MATERIAL.get(), net.minecraft.world.item.component.CustomData.of(material));
        }
        AbstractArrow arrow = pickup.getItem() instanceof ArrowItem vanilla
                ? vanilla.createArrow(player.level(), pickup, player, firedFrom)
                : new AntiqueProjectile(player.level(), player, pickup, traits, firedFrom);
        saveLauncher(arrow, firedFrom);
        arrow.getPersistentData().put("TinkerTool", traits.save(player.registryAccess()));
        float range = shuriken ? 1 : Math.max(.1F, ToolBuilder.savedStats(tool).getFloat("Range"));
        float speed = switch (launcher.definition().action()) {
            case SHURIKEN -> 2.1F;
            case LONGBOW -> 5.5F;
            case CROSSBOW -> 7F;
            default -> 3F;
        };
        float velocity = power * speed * range;
        float inaccuracy = switch (launcher.definition().action()) {
            case SHURIKEN, CROSSBOW -> 0F;
            case LONGBOW -> 1.2F;
            default -> 1F;
        };
        if (endspeed) {
            inaccuracy *= 2F / 3F;
        }
        if (ammo.getItem() instanceof AntiqueTool && !shuriken) {
            float accuracy = Math.max(.1F, ToolBuilder.savedStats(traits).getFloat("Accuracy"));
            inaccuracy = Math.max(0, inaccuracy - (1 - 1 / accuracy) * velocity / 2);
        }
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity, inaccuracy);
        if (arrow instanceof AntiqueProjectile) {
            float strength = progress * progress;
            double damage = player.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                    + ToolBuilder.savedStats(pickup).getFloat("Attack");
            if (!shuriken) {
                float bonus = crossbow ? 3F : launcher.definition().action() == ToolDefinition.Action.LONGBOW ? 2.5F : 0F;
                float multiplier = crossbow ? 1.3F : launcher.definition().action() == ToolDefinition.Action.LONGBOW ? 1.25F : .8F;
                damage = (damage + bonus * strength + ToolBuilder.savedStats(tool).getFloat("ProjectileDamage")) * multiplier;
            }
            arrow.setBaseDamage(Math.max(0, damage * strength) / Math.max(.001, arrow.getDeltaMovement().length()));
        }
        arrow.setCritArrow(!shuriken && progress >= 1);
        double launchDamage = arrow.getBaseDamage();
        double launchVelocity = arrow.getDeltaMovement().length();
        com.gctn.tconstruct.tools.traits.ToolTraits.forEach(traits, (trait, level) -> trait.onProjectileLaunch(traitsFor(arrow), level, arrow));
        if (arrow instanceof AntiqueProjectile projectile) { projectile.recordLaunchSpeed(); }
        if (!player.level().addFreshEntity(arrow)) {
            return false;
        }
        // 词条可以免除耐久消耗。只允许回收确实扣除过的弹药，避免凭空增加弹药。
        boolean paid = prepaid || consumeAmmo(ammo, player);
        arrow.pickup = player.getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY
                : paid ? AbstractArrow.Pickup.ALLOWED : AbstractArrow.Pickup.DISALLOWED;
        if (!shuriken && !player.getAbilities().instabuild) {
            launcher.damage(tool, 1, player, LivingEntity.getSlotForHand(hand));
        }
        if (splits && player.getRandom().nextBoolean()) {
            var extra = new AntiqueProjectile(player.level(), player, pickup, traits, firedFrom);
            extra.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity, inaccuracy + 3);
            extra.setBaseDamage(launchDamage * launchVelocity / Math.max(.001, extra.getDeltaMovement().length()));
            extra.setCritArrow(arrow.isCritArrow());
            com.gctn.tconstruct.tools.traits.ToolTraits.forEach(traits, (trait, level) -> trait.onProjectileLaunch(extra.traitTool(), level, extra));
            extra.recordLaunchSpeed();
            extra.pickup = AbstractArrow.Pickup.DISALLOWED;
            player.level().addFreshEntity(extra);
        }
        if (!shuriken) { player.awardStat(Stats.ITEM_USED.get(launcher)); }
        float random = player.getRandom().nextFloat();
        float pitch = crossbow ? .5F + random * .1F : 1 / (random * .4F + 1.2F) + (shuriken ? power : velocity) * .5F;
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT,
                shuriken ? SoundSource.PLAYERS : SoundSource.NEUTRAL, 1, pitch);
        return true;
    }

    static void saveLauncher(AbstractArrow arrow, ItemStack launcher) {
        // NeoForge 持久化数据独立于原版可被附魔清空的 weapon 字段，随实体存档保存。
        if (!launcher.isEmpty()) { arrow.getPersistentData().put(LAUNCHER_TAG, launcher.save(arrow.registryAccess())); }
    }

    /** 服务端读取发射器快照；返回独立副本。旧投射物缺少记录时返回空，不从弹药或手持物猜测。 */
    public static ItemStack launcherFor(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof AbstractArrow && entity.getPersistentData().contains(LAUNCHER_TAG)) {
            return ItemStack.parseOptional(entity.registryAccess(), entity.getPersistentData().getCompound(LAUNCHER_TAG));
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack traitsFor(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof AntiqueProjectile arrow) { return arrow.traitTool(); }
        if (entity instanceof AbstractArrow && entity.getPersistentData().contains("TinkerTool")) {
            return ItemStack.parseOptional(entity.registryAccess(), entity.getPersistentData().getCompound("TinkerTool"));
        }
        return ItemStack.EMPTY;
    }
}
