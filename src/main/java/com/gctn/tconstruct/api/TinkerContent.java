/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.api;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolModifiers;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import net.minecraft.resources.ResourceLocation;

/** Java 与脚本共用的公开入口；启动期间调用，无需依赖 KubeJS。 */
public final class TinkerContent {
    private static final java.util.Map<Material, ResourceLocation> CASTING_FLUIDS = new java.util.LinkedHashMap<>();
    private static final java.util.Set<Material> INSTALLED_CASTING = new java.util.HashSet<>();
    private TinkerContent() {}

    public static Material registerMaterial(String id, int color, boolean craftable, boolean castable) {
        if (ResourceLocation.tryParse(id) == null || TinkerMaterials.getMaterial(id) != null) {
            throw new IllegalArgumentException("Invalid or duplicate material: " + id);
        }
        Material material = new Material(id, color | 0xff000000).setCraftable(craftable).setCastable(castable);
        TinkerMaterials.materials.add(material);
        return material;
    }

    public static void representativeItem(Material material, String itemId) {
        ResourceLocation id = ResourceLocation.parse(itemId);
        material.setRepresentativeItem(() -> new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id)));
    }

    public static void head(Material material, int durability, float speed, float attack, int tier) {
        if (durability < 1 || speed <= 0 || !Float.isFinite(speed) || attack < 0 || !Float.isFinite(attack) || tier < 0) {
            throw new IllegalArgumentException("Invalid head stats");
        }
        material.addMaterialStats(new HeadMaterialStats(durability, tier, attack, speed));
    }
    public static void handle(Material material, float multiplier, int durability) {
        if (multiplier <= 0 || !Float.isFinite(multiplier)) { throw new IllegalArgumentException("Invalid handle multiplier"); }
        material.addMaterialStats(new HandleMaterialStats(multiplier, durability));
    }
    public static void extra(Material material, int durability) { material.addMaterialStats(new ExtraMaterialStats(durability)); }
    public static void bow(Material material, float drawSpeed, float range, float damage) {
        if (drawSpeed <= 0 || range <= 0 || !Float.isFinite(drawSpeed) || !Float.isFinite(range) || !Float.isFinite(damage)) { throw new IllegalArgumentException("Invalid bow stats"); }
        material.addMaterialStats(new BowMaterialStats(drawSpeed, range, damage));
    }
    public static void shaft(Material material, float multiplier, int bonusAmmo) {
        if (multiplier <= 0 || !Float.isFinite(multiplier) || bonusAmmo < 0) { throw new IllegalArgumentException("Invalid shaft stats"); }
        material.addMaterialStats(new ArrowShaftMaterialStats(multiplier, bonusAmmo));
    }
    public static void fletching(Material material, float accuracy, float multiplier) {
        if (accuracy < 0 || accuracy > 1 || multiplier <= 0 || !Float.isFinite(accuracy) || !Float.isFinite(multiplier)) { throw new IllegalArgumentException("Invalid fletching stats"); }
        material.addMaterialStats(new FletchingMaterialStats(accuracy, multiplier));
    }
    public static void bowString(Material material, float multiplier) {
        if (multiplier <= 0 || !Float.isFinite(multiplier)) { throw new IllegalArgumentException("Invalid bowstring stats"); }
        material.addMaterialStats(new BowStringMaterialStats(multiplier));
    }

    /** stat 为 null 时作为默认词条；指定部位的列表会替换默认列表。 */
    public static void trait(Material material, String stat, String id, int level) {
        AbstractTraits definition = TinkerTraits.getTrait(id);
        if (definition == null) { throw new IllegalArgumentException("Unknown trait: " + id); }
        material.addMaterialTraits(AbstractTraits.TraitPosPair.of(stat, new AbstractTraits.TraitEntry(definition, level)));
    }

    public static void item(Material material, String item, float value) { MaterialInputs.item(material, item, value); }
    public static void tag(Material material, String tag, float value) { MaterialInputs.tag(material, tag, value); }
    public static <T extends AbstractTraits> T registerTrait(T trait) { return TinkerTraits.registerTrait(trait); }
    public static void registerModifier(ToolModifiers.Definition definition) { ToolModifiers.register(definition); }
    public static void attribute(com.gctn.tconstruct.tools.ToolStats stats, String modifierId, String attributeId, double amount, String operation, String slot) {
        var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE, ResourceLocation.parse(attributeId));
        stats.attribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(key), modifierId, amount,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.valueOf(operation.toUpperCase(java.util.Locale.ROOT)),
                net.minecraft.world.entity.EquipmentSlotGroup.valueOf(slot.toUpperCase(java.util.Locale.ROOT)));
    }
    public static void modifierItem(String modifier, String item, int value) {
        ResourceLocation id = ResourceLocation.parse(item);
        ToolModifiers.ingredient(modifier, stack -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id), value);
    }
    public static void modifierTag(String modifier, String tag, int value) {
        var id = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse(tag));
        ToolModifiers.ingredient(modifier, stack -> stack.is(id), value);
    }
    public static void headDrop(java.util.function.Predicate<net.minecraft.world.entity.LivingEntity> entity,
                                java.util.function.Function<net.minecraft.world.entity.LivingEntity, net.minecraft.world.item.ItemStack> output) {
        com.gctn.tconstruct.tools.HeadDrops.register(entity, output);
    }

    /** 将扩展材料关联到已经由本模组或其他模组注册的流体；在启动脚本中调用。 */
    public static void castingMaterial(Material material, String fluidId) {
        ResourceLocation id = ResourceLocation.tryParse(fluidId);
        if (id == null || CASTING_FLUIDS.putIfAbsent(material, id) != null) { throw new IllegalArgumentException("Invalid or duplicate casting material: " + fluidId); }
        material.setCastable(true);
    }

    public static void installCastingRecipes() {
        for (var entry : CASTING_FLUIDS.entrySet()) {
            Material material = entry.getKey();
            if (INSTALLED_CASTING.contains(material)) { continue; }
            var fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(entry.getValue());
            if (fluid == net.minecraft.world.level.material.Fluids.EMPTY) { throw new IllegalArgumentException("Unknown casting fluid: " + entry.getValue()); }
            for (var part : com.gctn.tconstruct.library.toolparts.AntiqueParts.entries().values()) {
                if (!java.util.Set.of("Head", "Handle", "Extra", "Bow").contains(part.stat()) || !material.hasStats(part.stat())) { continue; }
                var output = part.create(material);
                if (output.isEmpty()) { continue; }
                int amount = Math.round(part.cost() * 144);
                com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes.MELTING.add(com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes.melting(
                        stack -> stack.is(output.getItem()) && com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(stack)
                                .getString(com.gctn.tconstruct.library.utils.Tags.PART_MATERIAL).equals(material.identifier), fluid, amount));
                com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes.CASTING.add(com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes.casting(false,
                        stack -> stack.is(com.gctn.tconstruct.smeltery.SmelteryModule.item("cast_" + part.id())), fluid, amount, output, false, false));
                com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes.CASTING.add(com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes.casting(false,
                        stack -> com.gctn.tconstruct.Config.CLAY_CASTS.get() && stack.is(com.gctn.tconstruct.smeltery.SmelteryModule.item("clay_cast_" + part.id())),
                        fluid, amount, output, true, false));
            }
            INSTALLED_CASTING.add(material);
        }
    }
}
