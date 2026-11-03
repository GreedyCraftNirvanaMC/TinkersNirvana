/* SPDX-License-Identifier: LGPL-3.0-only; Antique's configurable beheading drops. */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.ConfiguredEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class HeadDrops {
    private record Entry(Predicate<LivingEntity> entity, Function<LivingEntity, ItemStack> output) {}
    private static final List<Entry> REGISTERED = new ArrayList<>();
    private static List<Entry> configured = List.of();
    private static List<? extends String> snapshot;
    private HeadDrops() {}
    public static void register(Predicate<LivingEntity> entity, Function<LivingEntity, ItemStack> output) {
        REGISTERED.add(new Entry(Objects.requireNonNull(entity), Objects.requireNonNull(output)));
    }
    private static void configure(ServerLevel level) {
        var values = Config.MOB_HEAD_DROPS.get(); if (values.equals(snapshot)) { return; }
        var parsed = new ArrayList<Entry>();
        for (String value : values) {
            try {
                String[] fields = value.split(";", -1);
                if (fields.length < 3 || fields.length > 4) { throw new IllegalArgumentException("Expected entity;subtypes;item[;max_quantity]"); }
                ResourceLocation id = itemId(fields[2]); int count = fields.length == 4 ? Integer.parseInt(fields[3]) : 1;
                if (count < 1 || count > 64) { throw new IllegalArgumentException("Head quantity must be 1..64"); }
                if (!BuiltInRegistries.ITEM.containsKey(id)) { continue; }
                ItemStack output = new ItemStack(BuiltInRegistries.ITEM.get(id), count);
                if (output.isEmpty()) { continue; }
                parsed.add(new Entry(ConfiguredEntities.matcher(fields[0], fields[1], level), entity -> output.copy()));
            } catch (RuntimeException error) { TinkersNirvana.LOGGER.error("Invalid mobHeadDrops entry {}: {}", value, error.getMessage()); }
        }
        configured = List.copyOf(parsed); snapshot = List.copyOf(values);
    }
    private static ResourceLocation itemId(String name) {
        // Existing 1.12 configuration files can retain vanilla skull metadata during migration.
        if (name.startsWith("minecraft:skull:")) {
            String[] ids = {"skeleton_skull", "wither_skeleton_skull", "zombie_head", "player_head", "creeper_head", "dragon_head"};
            int meta = Integer.parseInt(name.substring("minecraft:skull:".length()));
            if (meta < 0 || meta >= ids.length) { throw new IllegalArgumentException("Invalid skull metadata"); }
            return ResourceLocation.withDefaultNamespace(ids[meta]);
        }
        return ResourceLocation.parse(name);
    }
    public static ItemStack random(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) { return ItemStack.EMPTY; }
        configure(level); List<ItemStack> choices = new ArrayList<>();
        for (var entries : List.of(configured, REGISTERED)) { for (Entry entry : entries) { if (entry.entity.test(entity)) { ItemStack stack = entry.output.apply(entity); if (stack != null && !stack.isEmpty()) { choices.add(stack.copy()); } } } }
        if (choices.isEmpty()) { return ItemStack.EMPTY; }
        ItemStack head = choices.get(entity.getRandom().nextInt(choices.size())); head.setCount(1 + entity.getRandom().nextInt(head.getCount()));
        if (entity instanceof Player player && head.is(Items.PLAYER_HEAD)) { head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile())); }
        return head;
    }
    private static boolean chance(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) { return false; }
        ItemStack tool = ToolRanged.traitsFor(source.getDirectEntity()); if (tool.isEmpty()) { tool = attacker.getMainHandItem(); }
        if (!(tool.getItem() instanceof com.gctn.tconstruct.tools.tools.AntiqueTool item) || TinkerTools.isBroken(tool)) { return false; }
        int level = ToolModifiers.level(tool, "beheading") + (item.definition().action() == ToolDefinition.Action.CLEAVER ? 2 : 0);
        return level > 0 && victim.getRandom().nextInt(10) < level;
    }
    public static void add(LivingDropsEvent event) {
        if (!chance(event.getEntity(), event.getSource())) { return; }
        ItemStack head = random(event.getEntity());
        if (head.isEmpty() || !(event.getEntity() instanceof Player) && event.getDrops().stream().anyMatch(drop -> ItemStack.isSameItemSameComponents(drop.getItem(), head))) { return; }
        var entity = event.getEntity(); var drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), head); drop.setDefaultPickUpDelay(); event.getDrops().add(drop);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) private static void keepInventory(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide && player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && chance(player, event.getSource())) {
            ItemStack head = random(player); if (!head.isEmpty()) { player.drop(head, true); }
        }
    }
}
