/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ToolRanged {
    private static final String LAUNCHER_TAG = "TinkerLauncher";
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TinkersNirvana.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<AntiqueProjectile>> PROJECTILE = ENTITIES.register("tool_projectile",
            () -> EntityType.Builder.<AntiqueProjectile>of(AntiqueProjectile::new, MobCategory.MISC)
                    .sized(.5F, .5F).clientTrackingRange(4).updateInterval(20).build("tool_projectile"));
    private ToolRanged() {}
    public static void register(IEventBus bus) { ENTITIES.register(bus); }

    public static int drawTime(AntiqueTool item, ItemStack stack) {
        float speed = ToolBuilder.savedStats(stack).getFloat("DrawSpeed");
        int base = switch (item.definition().action()) { case LONGBOW -> 30; case CROSSBOW -> 45; default -> 12; };
        return Math.max(3, Math.round(base / Math.max(.1F, speed)));
    }

    private static boolean supported(AntiqueTool launcher, ItemStack stack) {
        if (stack.getItem() instanceof AntiqueTool ammo) {
            return !TinkerTools.isBroken(stack) && (launcher.definition().action() == ToolDefinition.Action.CROSSBOW
                    ? ammo.definition().action() == ToolDefinition.Action.BOLT : ammo.definition().action() == ToolDefinition.Action.ARROW);
        }
        return stack.getItem() instanceof ArrowItem;
    }

    private static ItemStack findAmmo(AntiqueTool launcher, Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (supported(launcher, stack)) { return stack; }
        }
        for (ItemStack stack : player.getInventory().items) { if (supported(launcher, stack)) { return stack; } }
        return player.getAbilities().instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
    }

    public static InteractionResultHolder<ItemStack> use(AntiqueTool item, Level level, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (item.definition().action() == ToolDefinition.Action.SHURIKEN) {
            if (!level.isClientSide) {
                shoot(item, tool, tool, player, 1, hand);
                player.getCooldowns().addCooldown(tool.getItem(), 5);
            }
            return InteractionResultHolder.sidedSuccess(tool, level.isClientSide);
        }
        ChargedProjectiles loaded = tool.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (item.definition().action() == ToolDefinition.Action.CROSSBOW && !loaded.isEmpty()) {
            if (!level.isClientSide) {
                if (shoot(item, tool, loaded.getItems().getFirst(), player, 1, hand)) {
                    tool.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
                    var data = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(tool);
                    data.remove("LoadedAmmo");
                    tool.set(com.gctn.tconstruct.init.TinkerDataComponents.TINKER_MATERIAL.get(), net.minecraft.world.item.component.CustomData.of(data));
                }
            }
            return InteractionResultHolder.sidedSuccess(tool, level.isClientSide);
        }
        boolean hasAmmo = !findAmmo(item, player).isEmpty();
        var hook = EventHooks.onArrowNock(tool, level, player, hand, hasAmmo);
        if (hook != null) { return hook; }
        if (!hasAmmo) { return InteractionResultHolder.fail(tool); }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(tool);
    }

    public static void charge(AntiqueTool item, ItemStack tool, Level level, LivingEntity entity, int used) {
        if (used >= drawTime(item, tool) && entity instanceof Player player && !level.isClientSide
                && tool.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty()) {
            ItemStack ammo = findAmmo(item, player);
            if (!ammo.isEmpty()) {
                var data = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(tool);
                data.put("LoadedAmmo", ammo.copyWithCount(1).save(level.registryAccess()));
                tool.set(com.gctn.tconstruct.init.TinkerDataComponents.TINKER_MATERIAL.get(), net.minecraft.world.item.component.CustomData.of(data));
                tool.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(pickupFor(ammo)));
                consumeAmmo(ammo, player);
                level.playSound(null, player.blockPosition(), ToolSounds.CROSSBOW_RELOAD.get(), SoundSource.PLAYERS, 1, 1);
                player.stopUsingItem();
            }
        }
    }

    public static void release(AntiqueTool item, ItemStack tool, Level level, LivingEntity entity, int used) {
        if (!(entity instanceof Player player) || item.definition().action() == ToolDefinition.Action.CROSSBOW) { return; }
        ItemStack ammo = findAmmo(item, player);
        used = EventHooks.onArrowLoose(tool, level, player, used, !ammo.isEmpty());
        if (used < 0 || ammo.isEmpty()) { return; }
        float power = BowItem.getPowerForTime(Math.round(used * 20F / drawTime(item, tool)));
        if (power < .1F || level.isClientSide) { return; }
        shoot(item, tool, ammo, player, power, player.getUsedItemHand());
    }

    private static ItemStack pickupFor(ItemStack ammo) {
        ItemStack pickup = ammo.copyWithCount(1);
        if (ammo.getItem() instanceof AntiqueTool) { pickup.setDamageValue(Math.max(0, pickup.getMaxDamage() - 10)); }
        return pickup;
    }

    private static void consumeAmmo(ItemStack ammo, Player player) {
        if (player.getAbilities().instabuild) { return; }
        if (ammo.getItem() instanceof AntiqueTool item) { item.damage(ammo, 10, player, EquipmentSlot.MAINHAND); }
        else { ammo.shrink(1); }
    }

    private static boolean shoot(AntiqueTool launcher, ItemStack tool, ItemStack ammo, Player player, float power, InteractionHand hand) {
        // 在扣除耐久和运行词条前保留快照，命中时不再依赖玩家当前手持物。
        ItemStack firedFrom = tool.copyWithCount(1);
        boolean crossbow = launcher.definition().action() == ToolDefinition.Action.CROSSBOW;
        boolean shuriken = launcher.definition().action() == ToolDefinition.Action.SHURIKEN;
        ItemStack pickup = crossbow ? ammo.copyWithCount(1) : pickupFor(ammo);
        ItemStack traits = ammo.getItem() instanceof AntiqueTool ? ammo.copyWithCount(1) : tool.copy();
        if (crossbow) {
            var loaded = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(tool).getCompound("LoadedAmmo");
            ItemStack original = ItemStack.parseOptional(player.registryAccess(), loaded);
            if (!original.isEmpty() && original.getItem() instanceof AntiqueTool) { traits = original; }
        }
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
        float accuracy = Math.max(.1F, ToolBuilder.savedStats(traits).getFloat("Accuracy"));
        float velocity = power * (crossbow ? 3.15F : 3F) * range;
        float damage = shuriken ? ToolBuilder.savedStats(tool).getFloat("Attack")
                : (crossbow ? 3 : launcher.definition().action() == ToolDefinition.Action.LONGBOW ? 2.5F : 2)
                    + ToolBuilder.savedStats(tool).getFloat("ProjectileDamage")
                    + (pickup.getItem() instanceof AntiqueTool ? ToolBuilder.savedStats(pickup).getFloat("Attack") : 0);
        arrow.setBaseDamage(Math.max(0, damage) / Math.max(.1F, velocity));
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity, 1 / accuracy);
        arrow.setCritArrow(power >= 1);
        arrow.pickup = player.getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY : AbstractArrow.Pickup.ALLOWED;
        com.gctn.tconstruct.tools.traits.ToolTraits.forEach(traits, (trait, level) -> trait.onProjectileLaunch(traitsFor(arrow), level, arrow));
        if (!player.level().addFreshEntity(arrow)) { return false; }
        if (!crossbow) { consumeAmmo(ammo, player); }
        if (!shuriken) { launcher.damage(tool, 1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND); }
        if (com.gctn.tconstruct.tools.traits.AntiqueTraits.has(traits, "splitting") && player.getRandom().nextBoolean()) {
            var extra = new AntiqueProjectile(player.level(), player, pickup, traits, firedFrom);
            extra.setBaseDamage(arrow.getBaseDamage());
            extra.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity, 3 / accuracy);
            extra.pickup = AbstractArrow.Pickup.DISALLOWED;
            player.level().addFreshEntity(extra);
        }
        player.level().playSound(null, player.blockPosition(), crossbow ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1, 1 / (player.getRandom().nextFloat() * .4F + 1.2F) + power * .5F);
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
