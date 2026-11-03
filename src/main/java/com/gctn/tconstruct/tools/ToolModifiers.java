/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.stats.HeadMaterialStats;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.AntiqueTraits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/** 强化的注册、纯预览及消耗共用同一计划，扩展不依赖 KubeJS 或其他可选模组。 */
public final class ToolModifiers {
    private static final Map<String, Definition> MODIFIERS = new LinkedHashMap<>();
    static {
        add("haste", 5, 50, Items.REDSTONE, (stats, units) -> {
            for (int i = 0; i < units; i++) {
                stats.miningSpeed += stats.miningSpeed <= 15 ? .15F - .05F * stats.miningSpeed / 15
                        : stats.miningSpeed <= 25 ? .1F - .05F * (stats.miningSpeed - 15) / 10 : .05F;
            }
            stats.miningSpeed += units / 50 * .5F;
            stats.attackSpeed *= 1 + units * .004F;
            stats.drawSpeed *= 1 + units * .002F;
        });
        add("sharpness", 5, 72, Items.QUARTZ, (stats, units) -> {
            for (int i = 0; i < units; i++) {
                stats.attack += stats.attack <= 10 ? .05F - .025F * stats.attack / 10 : stats.attack <= 20 ? .025F - .01F * stats.attack / 20 : .015F;
            }
            stats.attack += units / 72 * .25F;
        });
        add("diamond", 1, 1, Items.DIAMOND, (stats, units) -> { stats.durability += 500; stats.attack += 1; stats.miningSpeed += .5F; if (stats.harvestLevel < 3) { stats.harvestLevel++; } });
        add("emerald", 1, 1, Items.EMERALD, (stats, units) -> { stats.durability += stats.baseDurability / 2; if (stats.harvestLevel < 2) { stats.harvestLevel++; } });
        add("luck", 3, 60, Items.LAPIS_LAZULI, (stats, units) -> {});
        add("fiery", 5, 25, Items.BLAZE_POWDER, (stats, units) -> {});
        add("knockback", 10, 1, Items.PISTON, (stats, units) -> {});
        add("bane_of_arthropods", 5, 24, Items.FERMENTED_SPIDER_EYE, (stats, units) -> {});
        register(new Definition("smite", 5, 24, stack -> stack.is(com.gctn.tconstruct.common.soil.SoilModule.BLOCKS.get("consecrated_soil").get().asItem()), (stats, units) -> {}));
        add("shulking", 1, 1, Items.POPPED_CHORUS_FRUIT, (stats, units) -> {});
        add("webbed", 3, 1, Items.COBWEB, (stats, units) -> {});
        add("soulbound", 1, 1, Items.NETHER_STAR, (stats, units) -> {});
        add("endearment", 1, 1, Items.DRAGON_HEAD, (stats, units) -> {});
        add("incognito", 1, 1, Items.WET_SPONGE, (stats, units) -> {});
        add("fins", 1, 1, Items.COD, (stats, units) -> {});
        supply("reinforced", "reinforcement", 5);
        supply("necrotic", "necrotic_bone", 5);
        supply("silktouch", "silky_jewel", 1);
        supply("autosmelt", "lava_crystal", 1);
        supply("mending_moss", "mending_moss", 3);
        supply("harvestwidth", "expander_horizontal", 1);
        supply("harvestheight", "expander_vertical", 1);
        supply("creative", "creative_modifier", 100);
        register(new Definition("beheading", 10, 1, stack -> stack.is(Items.ENDER_PEARL), (stats, units) -> {}));
        register(new Definition("blasting", 3, 3, stack -> stack.is(Items.TNT), (stats, units) -> {}));
        register(new Definition("glowing", 1, 1, stack -> stack.is(Items.ENDER_EYE), (stats, units) -> {}));
    }

    public static void tick(ItemStack tool, net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (level.isClientSide || TinkerTools.isBroken(tool) || !(entity instanceof LivingEntity living)) { return; }
        int moss = level(tool, "mending_moss");
        if (moss > 0 && (slot < 9 || living.getOffhandItem() == tool) && tool.isDamaged()) {
            CompoundTag data = TinkerDataComponents.getMaterialTag(tool);
            if (data.getInt("MossXP") > 0 && level.getGameTime() - data.getLong("MossLastHeal") > 150) {
                data.putInt("MossXP", data.getInt("MossXP") - 1);
                data.putLong("MossLastHeal", level.getGameTime());
                tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
                TinkerTools.healTool(tool, 2 + moss, living);
            }
        }
        if (selected && level(tool, "glowing") > 0 && entity.tickCount % 10 == 0
                && level.getMaxLocalRawBrightness(entity.blockPosition()) < 8 && tool.getItem() instanceof AntiqueTool item) {
            for (var pos : new net.minecraft.core.BlockPos[]{entity.blockPosition(), entity.blockPosition().above(), entity.blockPosition().north(),
                    entity.blockPosition().east(), entity.blockPosition().south(), entity.blockPosition().west(), entity.blockPosition().below()}) {
                if (living instanceof Player player && !player.mayInteract(level, pos)) { continue; }
                var snapshot = net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, pos);
                if (!com.gctn.tconstruct.gadgets.GadgetModule.GLOW.get().addGlow(level, pos, net.minecraft.core.Direction.DOWN)) { continue; }
                var placed = new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(snapshot, snapshot.getState(), living);
                if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(placed).isCanceled()) { snapshot.restore(); }
                else { item.damage(tool, 1, living, net.minecraft.world.entity.EquipmentSlot.MAINHAND); }
                break;
            }
        }
    }
    private ToolModifiers() {}
    public record Definition(String id, int maxLevel, int unitsPerLevel, Predicate<ItemStack> ingredient,
                             BiConsumer<ToolStats, Integer> modifyStats) {
        public Definition {
            if (maxLevel < 1 || unitsPerLevel < 1 || ingredient == null || modifyStats == null) { throw new IllegalArgumentException("Invalid modifier: " + id); }
        }
    }
    public static void register(Definition definition) {
        if (MODIFIERS.putIfAbsent(definition.id(), definition) != null) { throw new IllegalArgumentException("Duplicate modifier: " + definition.id()); }
    }
    public static Map<String, Definition> entries() { return Collections.unmodifiableMap(MODIFIERS); }
    private static void add(String id, int max, int units, Item ingredient, BiConsumer<ToolStats, Integer> effect) {
        register(new Definition(id, max, units, stack -> stack.is(ingredient), effect));
    }
    private static void supply(String id, String item, int max) {
        register(new Definition(id, max, 1, stack -> stack.is(ToolSupplies.item(item)), (stats, units) -> {}));
    }
    public static CompoundTag data(ItemStack tool) { return TinkerDataComponents.getMaterialTag(tool).getCompound("Modifiers"); }
    public static int units(ItemStack tool, String id) { return Math.max(0, data(tool).getInt(id)); }
    public static int level(ItemStack tool, String id) {
        Definition definition = MODIFIERS.get(id);
        int units = units(tool, id);
        if (id.equals("luck")) { return units >= 360 ? 3 : units >= 180 ? 2 : units >= 60 ? 1 : 0; }
        return definition == null || units == 0 ? 0 : Math.min(definition.maxLevel(), (units - 1) / definition.unitsPerLevel() + 1);
    }
    public static int usedSlots(ItemStack tool) {
        return MODIFIERS.keySet().stream().filter(id -> !java.util.Set.of("creative", "endearment", "incognito", "beheading").contains(id))
                .mapToInt(id -> id.equals("luck") || id.equals("blasting") ? (units(tool, id) > 0 ? 1 : 0) : level(tool, id)).sum();
    }

    public static Plan plan(Container container) {
        ItemStack source = container.getItem(0);
        if (!(source.getItem() instanceof AntiqueTool item)) { return null; }
        Plan special = specialPlan(container, source, item);
        if (special != null) { return special; }
        ItemStack result = source.copyWithCount(1);
        CompoundTag material = TinkerDataComponents.getMaterialTag(result);
        CompoundTag modifiers = material.getCompound("Modifiers").copy();
        int[] counts = new int[6];
        boolean changed = false;
        // 磨制套件加燧石用于提升挖掘等级，不改变原来的材料或词条。
        var kit = AntiqueParts.get("sharpening_kit");
        for (int slot = 1; slot < 6; slot++) {
            if (container.getItem(slot).is(kit.item().get())) {
                var mat = TinkerMaterials.getMaterial(TinkerDataComponents.getMaterialTag(container.getItem(slot)).getString(Tags.PART_MATERIAL));
                int flint = -1;
                for (int other = 1; other < 6; other++) { if (container.getItem(other).is(Items.FLINT)) { flint = other; break; } }
                if (mat == null || flint < 0 || mat.getStats("Head", HeadMaterialStats.class) == null) { return null; }
                int tier = mat.getStats("Head", HeadMaterialStats.class).harvestLevel;
                if (tier <= ToolBuilder.savedStats(source).getInt("HarvestLevel")) { return null; }
                material.putInt("FortifiedTier", tier);
                counts[slot] = 1; counts[flint] = 1; changed = true;
                for (int other = 1; other < 6; other++) { if (!container.getItem(other).isEmpty() && counts[other] == 0) { return null; } }
                break;
            }
        }
        if (!changed) {
            for (int slot = 1; slot < 6; slot++) {
                ItemStack input = container.getItem(slot);
                if (input.isEmpty()) { continue; }
                Definition match = MODIFIERS.values().stream().filter(definition -> definition.ingredient().test(input)).findFirst().orElse(null);
                if (match == null) { return null; }
                int current = modifiers.getInt(match.id());
                if (match.id().equals("beheading") || match.id().equals("glowing")) { return null; }
                int capacity = match.id().equals("luck") ? 360 : match.maxLevel() * match.unitsPerLevel();
                int add = Math.min(input.getCount(), capacity - current);
                if (match.id().equals("blasting")) { add -= add % 3; }
                if (add <= 0) { return null; }
                counts[slot] = add;
                modifiers.putInt(match.id(), current + add);
                changed = true;
            }
        }
        if (!changed || (modifiers.getInt("silktouch") > 0 && modifiers.getInt("luck") > 0)
                || (AntiqueTraits.has(source, "squeaky") && (modifiers.getInt("luck") > 0 || modifiers.getInt("silktouch") > 0))) { return null; }
        if (modifiers.getInt("blasting") > 0 && (item.definition().effectiveBlocks().isEmpty() || modifiers.getInt("luck") > 0
                || modifiers.getInt("silktouch") > 0 || AntiqueTraits.has(source, "squeaky"))) { return null; }
        if (modifiers.getInt("fins") > 0 && !item.isAmmo()) { return null; }
        if ((modifiers.getInt("harvestwidth") > 0 || modifiers.getInt("harvestheight") > 0)
                && switch (item.definition().action()) { case HAMMER, EXCAVATOR, LUMBERAXE, SCYTHE -> false; default -> true; }) { return null; }
        material.put("Modifiers", modifiers);
        result.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(material));
        if (((TinkerTools) result.getItem()).getTotalModifierSlots(result) < usedSlots(result)) { return null; }
        ToolBuilder.rebuild(result, item.definition());
        return new Plan(result, counts);
    }

    private static Plan specialPlan(Container container, ItemStack source, AntiqueTool item) {
        int[] counts = new int[6];
        ItemStack result = source.copyWithCount(1);
        CompoundTag material = TinkerDataComponents.getMaterialTag(result);
        CompoundTag modifiers = material.getCompound("Modifiers").copy();
        String id = null;
        int pearl = find(container, stack -> stack.is(Items.ENDER_PEARL));
        int obsidian = find(container, stack -> stack.is(Items.OBSIDIAN));
        int eye = find(container, stack -> stack.is(Items.ENDER_EYE));
        int dust = find(container, stack -> stack.is(Items.GLOWSTONE_DUST) && stack.getCount() >= 2);
        if (pearl >= 0 && obsidian >= 0 && level(source, "beheading") < 10) {
            counts[pearl] = 1; counts[obsidian] = 1; id = "beheading";
        } else if (eye >= 0 && dust >= 0 && units(source, "glowing") == 0) {
            counts[eye] = 1; counts[dust] = 2; id = "glowing";
        } else {
            int partSlot = find(container, stack -> AntiqueParts.get(stack) != null);
            int green = find(container, stack -> stack.is(com.gctn.tconstruct.common.soil.SoilModule.CRYSTALS.get("green").get()));
            int blue = find(container, stack -> stack.is(com.gctn.tconstruct.common.soil.SoilModule.CRYSTALS.get("blue").get()));
            int magma = find(container, stack -> stack.is(com.gctn.tconstruct.common.soil.SoilModule.CRYSTALS.get("magma").get()));
            int gold = find(container, stack -> stack.is(Items.GOLD_BLOCK));
            if (partSlot < 0 || green < 0 || blue < 0 || magma < 0 || gold < 0 || material.contains("Embossed")) { return null; }
            var part = AntiqueParts.get(container.getItem(partSlot));
            var matchingPart = item.definition().parts().stream().filter(required -> required.part().equals(part.id())).findFirst().orElse(null);
            if (matchingPart == null) { return null; }
            var mat = TinkerMaterials.getMaterial(TinkerDataComponents.getMaterialTag(container.getItem(partSlot)).getString(Tags.PART_MATERIAL));
            if (mat == null || mat.getTraitsForStats(matchingPart.traitStats(item.definition().action())).isEmpty()) { return null; }
            var traits = TinkerDataComponents.getTraitsTag(result);
            com.gctn.tconstruct.tools.traits.ToolTraits.addTraits(traits, mat.getTraitsForStats(matchingPart.traitStats(item.definition().action())));
            result.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(traits));
            material.putString("Embossed", mat.identifier);
            for (int slot : new int[]{partSlot, green, blue, magma, gold}) { counts[slot] = 1; }
        }
        for (int slot = 1; slot < 6; slot++) { if (!container.getItem(slot).isEmpty() && counts[slot] == 0) { return null; } }
        if (id != null) { modifiers.putInt(id, modifiers.getInt(id) + 1); }
        if (AntiqueTraits.has(result, "squeaky") && (modifiers.getInt("luck") > 0 || modifiers.getInt("silktouch") > 0)) { return null; }
        material.put("Modifiers", modifiers);
        result.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(material));
        if (item.getTotalModifierSlots(result) < usedSlots(result)) { return null; }
        ToolBuilder.rebuild(result, item.definition());
        return new Plan(result, counts);
    }

    private static int find(Container container, Predicate<ItemStack> predicate) {
        for (int slot = 1; slot < 6; slot++) { if (predicate.test(container.getItem(slot))) { return slot; } }
        return -1;
    }

    public static void modifyStats(ItemStack tool, ToolStats stats) {
        for (Definition definition : MODIFIERS.values()) {
            int units = units(tool, definition.id());
            if (units > 0) { definition.modifyStats().accept(stats, Math.min(units, definition.maxLevel() * definition.unitsPerLevel())); }
        }
        stats.harvestLevel = Math.max(stats.harvestLevel, TinkerDataComponents.getMaterialTag(tool).getInt("FortifiedTier"));
    }

    /** 原版附魔参与掉落表和其他模组事件，避免自行仿造时运与精准采集。 */
    public static void applyEnchantments(ItemStack tool, net.minecraft.core.HolderLookup.Provider registries) {
        var lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                tool.getOrDefault(DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY));
        int luck = level(tool, "luck");
        if (luck > 0) {
            builder.upgrade(lookup.getOrThrow(Enchantments.FORTUNE), luck);
            builder.upgrade(lookup.getOrThrow(Enchantments.LOOTING), luck);
        }
        if (level(tool, "silktouch") > 0 || AntiqueTraits.has(tool, "squeaky")) { builder.upgrade(lookup.getOrThrow(Enchantments.SILK_TOUCH), 1); }
        tool.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
    }

    public static float modifyAttack(ItemStack tool, LivingEntity attacker, LivingEntity target, float damage) {
        if (target.getType().is(EntityTypeTags.UNDEAD)) { damage += units(tool, "smite") / 24F * 2.5F; }
        if (target.getType().is(EntityTypeTags.ARTHROPOD)) { damage += units(tool, "bane_of_arthropods") / 24F * 2.5F; }
        if (!target.fireImmune()) { damage += units(tool, "fiery") / 15F; }
        return AntiqueTraits.has(tool, "squeaky") ? 0 : damage;
    }

    public static void afterHit(ItemStack tool, LivingEntity target, LivingEntity attacker) {
        int fire = units(tool, "fiery");
        if (fire > 0) { target.igniteForSeconds(1 + fire / 8); }
        if (level(tool, "knockback") > 0) { target.knockback(level(tool, "knockback") * .5, attacker.getX() - target.getX(), attacker.getZ() - target.getZ()); }
        if (level(tool, "necrotic") > 0) { attacker.heal(ToolBuilder.savedStats(tool).getFloat("Attack") * level(tool, "necrotic") * .05F); }
        if (level(tool, "shulking") > 0) { target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60)); }
        if (level(tool, "webbed") > 0) { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, level(tool, "webbed") - 1)); }
    }

    public static void smeltDrops(BlockDropsEvent event) {
        for (var entity : event.getDrops()) {
            ItemStack input = entity.getItem();
            var recipe = event.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), event.getLevel());
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(input), event.getLevel().registryAccess());
                if (!result.isEmpty()) {
                    result.setCount(Math.min(result.getMaxStackSize(), input.getCount() * result.getCount()));
                    entity.setItem(result);
                }
            }
        }
    }

    public record Plan(ItemStack result, int[] counts) {
        public void consume(Container container) {
            container.removeItem(0, 1);
            for (int slot = 1; slot < 6; slot++) { container.removeItem(slot, counts[slot]); }
        }
    }
}
