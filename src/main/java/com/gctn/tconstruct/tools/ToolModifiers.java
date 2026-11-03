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
    private record IngredientValue(String modifier, Predicate<ItemStack> ingredient, int value) {}
    private static final java.util.List<IngredientValue> INGREDIENTS = new java.util.ArrayList<>();
    static {
        add("haste", 5, 50, Items.REDSTONE, (stats, units) -> {
            for (int i = 0; i < units; i++) {
                stats.miningSpeed += stats.miningSpeed <= 15 ? .15F - .05F * stats.miningSpeed / 15
                        : stats.miningSpeed <= 25 ? .1F - .05F * (stats.miningSpeed - 15) / 10 : .05F;
            }
            stats.miningSpeed += units / 50 * .5F;
            stats.attackSpeed += stats.baseAttackSpeed * units * .004F;
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
        add("knockback", 99, 10, Items.PISTON, (stats, units) -> {});
        add("bane_of_arthropods", 5, 24, Items.FERMENTED_SPIDER_EYE, (stats, units) -> {});
        register(new Definition("smite", 5, 24, stack -> stack.is(com.gctn.tconstruct.common.soil.SoilModule.BLOCKS.get("consecrated_soil").get().asItem()), (stats, units) -> {}));
        add("shulking", 1, 50, Items.POPPED_CHORUS_FRUIT, (stats, units) -> {});
        add("webbed", 3, 1, Items.COBWEB, (stats, units) -> {});
        add("soulbound", 1, 1, Items.NETHER_STAR, (stats, units) -> {});
        add("endearment", 1, 1, Items.DRAGON_HEAD, (stats, units) -> {});
        add("incognito", 1, 1, Items.WET_SPONGE, (stats, units) -> {});
        add("fins", 1, 2, Items.COD, (stats, units) -> {});
        supply("reinforced", "reinforcement", 5);
        supply("necrotic", "necrotic_bone", 5);
        supply("silktouch", "silky_jewel", 1);
        supply("autosmelt", "lava_crystal", 1);
        supply("mending_moss", "mending_moss", 3);
        supply("harvestwidth", "expander_horizontal", 1);
        supply("harvestheight", "expander_vertical", 1);
        supply("creative", "creative_modifier", Integer.MAX_VALUE);
        register(new Definition("beheading", 10, 1, stack -> stack.is(Items.ENDER_PEARL), (stats, units) -> {}));
        register(new Definition("blasting", 3, 3, stack -> stack.is(Items.TNT), (stats, units) -> {}));
        register(new Definition("glowing", 1, 1, stack -> stack.is(Items.ENDER_EYE), (stats, units) -> {}));
        ingredient("haste", stack -> stack.is(Items.REDSTONE_BLOCK), 9);
        ingredient("luck", stack -> stack.is(Items.LAPIS_BLOCK), 9);
        ingredient("sharpness", stack -> stack.is(Items.QUARTZ_BLOCK), 4);
        ingredient("knockback", stack -> stack.is(Items.STICKY_PISTON), 1);
        ingredient("fins", stack -> stack.is(net.minecraft.tags.TagKey.create(Registries.ITEM, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "foods/raw_fish"))), 1);
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
            if (id == null || net.minecraft.resources.ResourceLocation.tryParse(id) == null || maxLevel < 1 || unitsPerLevel < 1 || (long) maxLevel * unitsPerLevel > Integer.MAX_VALUE || ingredient == null || modifyStats == null) { throw new IllegalArgumentException("Invalid modifier: " + id); }
        }
    }
    public static void register(Definition definition) {
        if (MODIFIERS.putIfAbsent(definition.id(), definition) != null) { throw new IllegalArgumentException("Duplicate modifier: " + definition.id()); }
    }
    public static Map<String, Definition> entries() { return Collections.unmodifiableMap(MODIFIERS); }
    /** Adds tag/predicate-based equivalents such as one storage block worth nine modifier ingredients. */
    public static void ingredient(String modifier, Predicate<ItemStack> ingredient, int value) {
        if (!MODIFIERS.containsKey(modifier) || ingredient == null || value < 1 || value > 1000000) { throw new IllegalArgumentException("Invalid modifier ingredient: " + modifier); }
        INGREDIENTS.add(new IngredientValue(modifier, ingredient, value));
    }
    public static int ingredientValue(Definition definition, ItemStack stack) {
        for (int i = INGREDIENTS.size() - 1; i >= 0; i--) { var entry = INGREDIENTS.get(i); if (entry.modifier.equals(definition.id()) && entry.ingredient.test(stack)) { return entry.value; } }
        return definition.ingredient().test(stack) ? 1 : 0;
    }
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
        return (int) Math.min(Integer.MAX_VALUE, MODIFIERS.keySet().stream().filter(id -> !java.util.Set.of("creative", "endearment", "incognito", "beheading").contains(id))
                .mapToLong(id -> id.equals("luck") || id.equals("blasting") ? (units(tool, id) > 0 ? 1 : 0) : level(tool, id)).sum());
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
                if (item.definition().effectiveBlocks().isEmpty() || mat.identifier.equals(material.getString("FortifiedMaterial"))) { return null; }
                material.putInt("FortifiedTier", tier);
                material.putString("FortifiedMaterial", mat.identifier);
                counts[slot] = 1; counts[flint] = 1; changed = true;
                for (int other = 1; other < 6; other++) { if (!container.getItem(other).isEmpty() && counts[other] == 0) { return null; } }
                break;
            }
        }
        if (!changed) {
            for (int slot = 1; slot < 6; slot++) {
                ItemStack input = container.getItem(slot);
                if (input.isEmpty()) { continue; }
                Definition match = MODIFIERS.values().stream().filter(definition -> ingredientValue(definition, input) > 0).findFirst().orElse(null);
                if (match == null) { return null; }
                int current = modifiers.getInt(match.id());
                if (match.id().equals("beheading") || match.id().equals("glowing")) { return null; }
                int capacity = match.id().equals("luck") ? 360 : match.maxLevel() * match.unitsPerLevel();
                var staged = source.copyWithCount(1); var stagedData = material.copy(); stagedData.put("Modifiers", modifiers.copy()); staged.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(stagedData));
                int free = item.getTotalModifierSlots(staged) - usedSlots(staged);
                if (!java.util.Set.of("creative", "endearment", "incognito", "beheading").contains(match.id())) {
                    if (match.id().equals("luck") || match.id().equals("blasting")) { if (current == 0 && free <= 0) { return null; } }
                    else { capacity = (int) Math.min(capacity, Math.max(0L, (long) level(staged, match.id()) + free) * match.unitsPerLevel()); }
                }
                int value = ingredientValue(match, input);
                int consumed = (int) Math.min(input.getCount(), ((long) Math.max(0, capacity - current) + value - 1) / value);
                int add = (int) Math.min(Math.max(0, capacity - current), (long) consumed * value);
                if (match.id().equals("blasting")) { add -= add % 3; }
                if (match.id().equals("fins")) { add -= add % 2; }
                if (add <= 0) { return null; }
                counts[slot] = (int) (((long) add + value - 1) / value);
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
            var embossed = new CompoundTag();
            com.gctn.tconstruct.tools.traits.ToolTraits.addTraits(embossed, mat.getTraitsForStats(matchingPart.traitStats(item.definition().action())));
            material.put("EmbossedTraits", embossed);
            material.putString("EmbossedPart", part.id());
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
        var material = TinkerDataComponents.getMaterialTag(tool);
        if (material.contains("FortifiedTier")) { stats.harvestLevel = material.getInt("FortifiedTier"); }
    }

    public static boolean isCompatible(ItemStack tool) {
        if (!(tool.getItem() instanceof AntiqueTool item)) { return false; }
        var modifiers = data(tool);
        if (modifiers.getInt("silktouch") > 0 && modifiers.getInt("luck") > 0) { return false; }
        if (AntiqueTraits.has(tool, "squeaky") && (modifiers.getInt("luck") > 0 || modifiers.getInt("silktouch") > 0 || modifiers.getInt("blasting") > 0)) { return false; }
        if (modifiers.getInt("blasting") > 0 && (item.definition().effectiveBlocks().isEmpty() || modifiers.getInt("luck") > 0 || modifiers.getInt("silktouch") > 0)) { return false; }
        if (modifiers.getInt("fins") > 0 && !item.isAmmo()) { return false; }
        return item.getTotalModifierSlots(tool) >= usedSlots(tool);
    }

    /** 原版附魔参与掉落表和其他模组事件，避免自行仿造时运与精准采集。 */
    public static void applyEnchantments(ItemStack tool, net.minecraft.core.HolderLookup.Provider registries) {
        var lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        var builder = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                tool.getOrDefault(DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY));
        builder.removeIf(enchantment -> enchantment.is(Enchantments.FORTUNE) || enchantment.is(Enchantments.LOOTING) || enchantment.is(Enchantments.SILK_TOUCH));
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
        return AntiqueTraits.has(tool, "squeaky") ? 0 : damage;
    }

    public static void afterHit(ItemStack tool, LivingEntity target, LivingEntity attacker) {
        int fire = units(tool, "fiery");
        if (fire > 0) { target.igniteForSeconds(1 + fire / 8); com.gctn.tconstruct.tools.traits.TraitDamage.hurt(target, attacker, "fiery", fire / 15F); }
        if (units(tool, "knockback") > 0) { target.knockback(units(tool, "knockback") * .1, attacker.getX() - target.getX(), attacker.getZ() - target.getZ()); }
        if (units(tool, "shulking") > 0) { target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, units(tool, "shulking") / 2 + 10)); }
        if (level(tool, "webbed") > 0) { target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, level(tool, "webbed") * 60, 1)); }
    }

    public static void afterDamage(ItemStack tool, LivingEntity attacker, float actualDamage) {
        int necrotic = level(tool, "necrotic");
        if (necrotic > 0 && actualDamage > 0) { attacker.heal(actualDamage * necrotic * .05F); }
    }

    public static void smeltDrops(BlockDropsEvent event) {
        if (!event.getTool().isCorrectToolForDrops(event.getState())) { return; }
        var extra = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        for (var entity : event.getDrops()) {
            ItemStack input = entity.getItem();
            var recipe = event.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), event.getLevel());
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().assemble(new SingleRecipeInput(input), event.getLevel().registryAccess());
                if (!result.isEmpty()) {
                    int count = Math.multiplyExact(input.getCount(), result.getCount());
                    int fortune = ToolModifiers.level(event.getTool(), "luck");
                    if (com.gctn.tconstruct.Config.AUTOSMELT_FORTUNE.get() && fortune > 0) { count = count * event.getLevel().random.nextInt(fortune + 1) + 1; }
                    entity.setItem(result.copyWithCount(Math.min(result.getMaxStackSize(), count)));
                    for (int remaining = count - entity.getItem().getCount(); remaining > 0;) {
                        int batch = Math.min(result.getMaxStackSize(), remaining); remaining -= batch;
                        var drop = new net.minecraft.world.entity.item.ItemEntity(event.getLevel(), entity.getX(), entity.getY(), entity.getZ(), result.copyWithCount(batch));
                        drop.setDefaultPickUpDelay(); extra.add(drop);
                    }
                    float experience = recipe.get().value().getExperience() * input.getCount();
                    int whole = net.minecraft.util.Mth.floor(experience);
                    if (event.getLevel().random.nextFloat() < experience - whole) { whole++; }
                    event.setDroppedExperience(event.getDroppedExperience() + whole);
                }
            }
        }
        event.getDrops().addAll(extra);
    }

    public record Plan(ItemStack result, int[] counts) {
        public void consume(Container container) {
            container.removeItem(0, 1);
            for (int slot = 1; slot < 6; slot++) { container.removeItem(slot, counts[slot]); }
        }
    }
}
