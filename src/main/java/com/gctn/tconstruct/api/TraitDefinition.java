package com.gctn.tconstruct.api;

import com.gctn.tconstruct.tools.ToolStats;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.gctn.tconstruct.tools.traits.LeveledTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.*;
import java.util.function.Consumer;

/** Script and Java addons share the same editable definition and dispatcher. */
public final class TraitDefinition extends LeveledTraits {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Set<String> HOOKS = Set.of(
            "modifyStats", "modifyAttack", "modifyDurability", "onToolDamage", "onToolHeal", "onRepair",
            "miningSpeed", "beforeBlockBreak", "afterBlockBreak", "onBlockDrops", "onDamageTaken",
            "inventoryTick", "afterHit", "afterDamage", "onProjectileLaunch", "onProjectileTick", "modifyModifierSlots", "onExperienceDrop", "onProjectileHitBlock", "rules");
    private final AbstractTraits base;
    private final Map<String, List<Consumer<TraitContext>>> handlers = new HashMap<>();
    private final Set<String> replaced = new HashSet<>();
    private final Set<Consumer<TraitContext>> failed = Collections.newSetFromMap(new IdentityHashMap<>());
    private Component name;
    private Component description;
    private String translationKey;
    private String descriptionKey;
    private int priority;
    private String stacking;

    public TraitDefinition(String id) { this(id, null); }

    public TraitDefinition(String id, AbstractTraits base) {
        super(id, base == null ? 1 : base.maxLevel, base == null ? -1 : base.strengthenNumber, base == null ? 0xffffff : base.color);
        this.base = base;
        this.priority = base == null ? 100 : base.getPriority();
    }

    public TraitDefinition displayName(Component text) { name = text; return this; }
    public TraitDefinition description(Component text) { description = text; return this; }
    public TraitDefinition translationKey(String key) { translationKey = key; return this; }
    public TraitDefinition descriptionKey(String key) { descriptionKey = key; return this; }
    public TraitDefinition color(int value) { color = value; return this; }
    public TraitDefinition maxLevel(int value) {
        if (value < 1) { throw new IllegalArgumentException("maxLevel must be positive: " + identifier); }
        maxLevel = value;
        return this;
    }
    public TraitDefinition priority(int value) { priority = value; return this; }
    public TraitDefinition stacking(String mode) {
        if (!Set.of("max", "sum", "distinct").contains(mode)) { throw new IllegalArgumentException("Unknown stacking mode: " + mode); }
        stacking = mode;
        return this;
    }

    private static void checkHook(String hook) {
        if (!HOOKS.contains(hook)) { throw new IllegalArgumentException("Unknown trait hook: " + hook); }
    }

    public TraitDefinition clearHandler(String hook) {
        checkHook(hook);
        replaced.add(hook);
        handlers.remove(hook);
        return this;
    }
    public TraitDefinition replaceHandler(String hook, Consumer<TraitContext> handler) {
        clearHandler(hook);
        return appendHandler(hook, handler);
    }
    public TraitDefinition appendHandler(String hook, Consumer<TraitContext> handler) {
        checkHook(hook);
        if (hook.equals("rules")) { throw new IllegalArgumentException("Use clearHandler('rules') to disable native special rules"); }
        handlers.computeIfAbsent(hook, ignored -> new ArrayList<>()).add(Objects.requireNonNull(handler));
        return this;
    }
    public TraitDefinition clearHandlers() {
        replaced.addAll(HOOKS);
        handlers.clear();
        return this;
    }

    @Override public boolean usesNativeHook(String hook) { return nativeHook(hook) && base.usesNativeHook(hook); }
    @Override public boolean usesNativeRules() { return base != null && !replaced.contains("rules") && base.usesNativeRules(); }
    @Override public int getPriority() { return priority; }
    private String effectiveStacking() {
        if (stacking != null) { return stacking; }
        if (base instanceof TraitDefinition definition) { return definition.effectiveStacking(); }
        return base instanceof LeveledTraits leveled ? (leveled.stacksRepeatedLevels() ? "sum" : "distinct") : "max";
    }

    @Override public boolean stacksRepeatedLevels() { return !effectiveStacking().equals("distinct"); }
    @Override public int addLevel(int current, int incoming) {
        if (stacking == null && base != null) {
            if (maxLevel == base.maxLevel) { return Math.clamp(base.addLevel(current, incoming), 1, maxLevel); }
            return effectiveStacking().equals("max")
                    ? Math.clamp(Math.max(current, incoming), 1, maxLevel)
                    : (int) Math.clamp((long) current + incoming, 1, maxLevel);
        }
        return stacking == null || stacking.equals("max") ? Math.clamp(Math.max(current, incoming), 1, maxLevel)
                : (int) Math.clamp((long) current + incoming, 1, maxLevel);
    }
    @Override public int mergeLevel(int current, int incoming) { return addLevel(current, incoming); }

    @Override protected Component displayName() {
        Component fallback = ContentNames.translated("traits." + identifier + ".name", name != null ? name : base != null ? base.getNameComponent() : Component.literal(identifier));
        String key = translationKey == null ? ContentNames.key("traits", identifier, "name") : translationKey;
        return ContentNames.translated(key, fallback);
    }
    @Override public Component getDescription() {
        Component fallback = ContentNames.translated("traits." + identifier + ".description",
                description != null ? description : base != null ? base.getDescription().copy().setStyle(net.minecraft.network.chat.Style.EMPTY) : Component.empty());
        String key = descriptionKey == null ? ContentNames.key("traits", identifier, "description") : descriptionKey;
        return ContentNames.translated(key, fallback).copy().withColor(color);
    }

    private boolean nativeHook(String hook) { return base != null && !replaced.contains(hook); }

    private void run(String hook, TraitContext context) {
        for (Consumer<TraitContext> handler : handlers.getOrDefault(hook, List.of())) {
            if (failed.contains(handler)) { continue; }
            int amount = context.amount;
            float damage = context.damage;
            ToolStats originalStats = context.stats;
            ToolStats stats = originalStats == null ? null : originalStats.copy();
            CompoundTag data = context.data == null ? null : context.data.copy();
            try {
                handler.accept(context);
                if (!Float.isFinite(context.damage)) { throw new IllegalArgumentException("Non-finite damage"); }
                if (context.stats != null) { context.stats.validate(); }
                if (data != null && context.data == null) { throw new IllegalArgumentException("Trait data cannot be null"); }
            } catch (RuntimeException exception) {
                failed.add(handler);
                context.amount = amount;
                context.damage = damage;
                if (stats != null) { originalStats.copyFrom(stats); context.stats = originalStats; }
                context.data = data;
                LOGGER.error("Disabled failing callback for trait {} hook {} until restart", identifier, hook, exception);
            }
        }
    }

    @Override
    public int modifyModifierSlots(ItemStack tool, int level, int slots) {
        if (nativeHook("modifyModifierSlots")) { slots = base.modifyModifierSlots(tool, level, slots); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.originalAmount = slots;
        ctx.amount = slots;
        run("modifyModifierSlots", ctx);
        return ctx.amount;
    }

    @Override
    public void onExperienceDrop(ItemStack tool, int level, net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent event) {
        if (nativeHook("onExperienceDrop")) { base.onExperienceDrop(tool, level, event); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.event = event;
        ctx.entity = event.getEntity();
        run("onExperienceDrop", ctx);
    }

    @Override
    public void onProjectileHitBlock(ItemStack tool, int level, AbstractArrow projectile, net.minecraft.world.phys.BlockHitResult hit) {
        if (nativeHook("onProjectileHitBlock")) { base.onProjectileHitBlock(tool, level, projectile, hit); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.projectile = projectile;
        ctx.world = projectile.level();
        ctx.pos = hit.getBlockPos();
        ctx.event = hit;
        run("onProjectileHitBlock", ctx);
    }

    @Override
    public void modifyStats(ItemStack tool, int level, ToolStats stats) {
        if (nativeHook("modifyStats")) { base.modifyStats(tool, level, stats); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.stats = stats;
        run("modifyStats", ctx);
    }

    @Override
    public float modifyAttack(ItemStack tool, int level, LivingEntity attacker, LivingEntity target, float original, float damage) {
        if (nativeHook("modifyAttack")) { damage = base.modifyAttack(tool, level, attacker, target, original, damage); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.attacker = attacker;
        ctx.target = target;
        ctx.originalDamage = original;
        ctx.damage = damage;
        run("modifyAttack", ctx);
        return ctx.damage;
    }

    @Override
    public int modifyDurability(ItemStack tool, int level, int durability) {
        if (nativeHook("modifyDurability")) { durability = base.modifyDurability(tool, level, durability); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.originalAmount = durability;
        ctx.amount = durability;
        run("modifyDurability", ctx);
        return Math.max(1, ctx.amount);
    }

    @Override
    public int onToolDamage(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
        if (nativeHook("onToolDamage")) { amount = base.onToolDamage(tool, level, original, amount, entity); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.originalAmount = original;
        ctx.amount = amount;
        ctx.entity = entity;
        run("onToolDamage", ctx);
        return Math.max(0, ctx.amount);
    }

    @Override
    public int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
        if (nativeHook("onToolHeal")) { amount = base.onToolHeal(tool, level, original, amount, entity); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.originalAmount = original;
        ctx.amount = amount;
        ctx.entity = entity;
        run("onToolHeal", ctx);
        return Math.max(0, ctx.amount);
    }

    @Override
    public void onRepair(ItemStack tool, int level, int healedAmount) {
        if (nativeHook("onRepair")) { base.onRepair(tool, level, healedAmount); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.amount = healedAmount;
        run("onRepair", ctx);
    }

    @Override
    public void miningSpeed(ItemStack tool, int level, PlayerEvent.BreakSpeed event) {
        if (nativeHook("miningSpeed")) { base.miningSpeed(tool, level, event); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.event = event;
        ctx.entity = event.getEntity();
        run("miningSpeed", ctx);
    }

    @Override
    public void beforeBlockBreak(ItemStack tool, int level, BlockEvent.BreakEvent event) {
        if (nativeHook("beforeBlockBreak")) { base.beforeBlockBreak(tool, level, event); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.event = event;
        ctx.entity = event.getPlayer();
        ctx.pos = event.getPos();
        ctx.state = event.getState();
        run("beforeBlockBreak", ctx);
    }

    @Override
    public void afterBlockBreak(ItemStack tool, int level, Level world, BlockState state, BlockPos pos, LivingEntity entity, boolean effective) {
        if (nativeHook("afterBlockBreak")) { base.afterBlockBreak(tool, level, world, state, pos, entity, effective); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.world = world;
        ctx.state = state;
        ctx.pos = pos;
        ctx.entity = entity;
        ctx.effective = effective;
        run("afterBlockBreak", ctx);
    }

    @Override
    public void onBlockDrops(ItemStack tool, int level, BlockDropsEvent event) {
        if (nativeHook("onBlockDrops")) { base.onBlockDrops(tool, level, event); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.event = event;
        ctx.world = event.getLevel();
        ctx.pos = event.getPos();
        ctx.state = event.getState();
        run("onBlockDrops", ctx);
    }

    @Override
    public void onDamageTaken(ItemStack tool, int level, LivingIncomingDamageEvent event) {
        if (nativeHook("onDamageTaken")) { base.onDamageTaken(tool, level, event); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.event = event;
        ctx.entity = event.getEntity();
        run("onDamageTaken", ctx);
    }

    @Override
    public void inventoryTick(ItemStack tool, Level world, Entity entity, int slotId, boolean selected, CompoundTag data) {
        if (nativeHook("inventoryTick")) { base.inventoryTick(tool, world, entity, slotId, selected, data); }
        TraitContext ctx = new TraitContext(tool, data.getInt("Level"));
        ctx.world = world;
        ctx.entity = entity;
        ctx.slot = slotId;
        ctx.selected = selected;
        ctx.data = data;
        run("inventoryTick", ctx);
        if (ctx.data != data) {
            for (String key : new ArrayList<>(data.getAllKeys())) { data.remove(key); }
            data.merge(ctx.data);
        }
    }

    @Override
    public void afterHit(ItemStack tool, int level, LivingEntity target, LivingEntity attacker) {
        if (nativeHook("afterHit")) { base.afterHit(tool, level, target, attacker); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.target = target;
        ctx.attacker = attacker;
        run("afterHit", ctx);
    }

    @Override
    public void afterDamage(ItemStack tool, int level, LivingEntity attacker, LivingDamageEvent.Post event) {
        if (nativeHook("afterDamage")) { base.afterDamage(tool, level, attacker, event); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.attacker = attacker;
        ctx.target = event.getEntity();
        ctx.event = event;
        ctx.damage = event.getNewDamage();
        run("afterDamage", ctx);
    }

    @Override
    public void onProjectileLaunch(ItemStack tool, int level, AbstractArrow projectile) {
        if (nativeHook("onProjectileLaunch")) { base.onProjectileLaunch(tool, level, projectile); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.projectile = projectile;
        ctx.world = projectile.level();
        run("onProjectileLaunch", ctx);
    }

    @Override
    public void onProjectileTick(ItemStack tool, int level, AbstractArrow projectile) {
        if (nativeHook("onProjectileTick")) { base.onProjectileTick(tool, level, projectile); }
        TraitContext ctx = new TraitContext(tool, level);
        ctx.projectile = projectile;
        ctx.world = projectile.level();
        run("onProjectileTick", ctx);
    }

}
