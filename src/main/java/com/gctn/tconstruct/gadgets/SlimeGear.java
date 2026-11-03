/* SPDX-License-Identifier: LGPL-3.0-only; Antique's sling, boots, and piggyback registrations. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.item.PiggybackItem;
import com.gctn.tconstruct.gadgets.item.SlimeBootsItem;
import com.gctn.tconstruct.gadgets.item.SlimeSlingItem;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SlimeGear {
    public static final Map<String, Supplier<SlimeSlingItem>> SLINGS = new LinkedHashMap<>();
    public static final Map<String, Supplier<SlimeBootsItem>> BOOTS = new LinkedHashMap<>();
    public static final Map<String, Integer> COLORS = Map.of("green", 0x69bc5e, "blue", 0x74c5c8, "purple", 0xcc68ff, "blood", 0xb80000, "magma", 0xffab49, "pink", 0xbc9eb4);
    private static final DeferredRegister<ArmorMaterial> ARMOR = DeferredRegister.create(Registries.ARMOR_MATERIAL, TinkersNirvana.MODID);
    public static final Holder<ArmorMaterial> SLIME_ARMOR = ARMOR.register("slime", () -> new ArmorMaterial(Map.of(), 0, Holder.direct(SoundEvents.SLIME_BLOCK_PLACE), () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(id("slime"), "", true)), 0, 0));
    public static final Holder<ArmorMaterial> PIGGYBACK_ARMOR = ARMOR.register("piggyback", () -> new ArmorMaterial(Map.of(), 0, Holder.direct(SoundEvents.SLIME_BLOCK_PLACE), () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(id("piggyback"))), 0, 0));
    public static final Supplier<PiggybackItem> PIGGYBACK = ItemRegister.ITEMS.register("piggybackpack", PiggybackItem::new);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TinkersNirvana.MODID);
    public static final Supplier<AttachmentType<GearPhysics.BounceState>> BOUNCE = ATTACHMENTS.register("bounce_motion", () -> AttachmentType.builder(GearPhysics.BounceState::new).build());
    public static final Supplier<AttachmentType<GearPhysics.CarryState>> CARRY_STATE = ATTACHMENTS.register("carried_passengers", () -> AttachmentType.builder(GearPhysics.CarryState::new).build());
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, TinkersNirvana.MODID);
    public static final Holder<MobEffect> CARRY = EFFECTS.register("carry", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xab7955) {}
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, id("carrying"), -.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID);
    public static final Supplier<net.minecraft.world.item.crafting.RecipeSerializer<com.gctn.tconstruct.gadgets.recipe.SlimeGearMixingRecipe>> MIX_SLING = RECIPES.register("slimesling_mixing", () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(category -> new com.gctn.tconstruct.gadgets.recipe.SlimeGearMixingRecipe(category, false)));
    public static final Supplier<net.minecraft.world.item.crafting.RecipeSerializer<com.gctn.tconstruct.gadgets.recipe.SlimeGearMixingRecipe>> MIX_BOOTS = RECIPES.register("slime_boots_mixing", () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(category -> new com.gctn.tconstruct.gadgets.recipe.SlimeGearMixingRecipe(category, true)));
    static {
        for (String color : new String[]{"green", "blue", "purple", "blood", "magma", "pink"}) {
            SLINGS.put(color, ItemRegister.ITEMS.register("slimesling_" + color, () -> new SlimeSlingItem(color)));
            BOOTS.put(color, ItemRegister.ITEMS.register("slime_boots_" + color, () -> new SlimeBootsItem(color)));
        }
    }
    private SlimeGear() {}
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, path); }
    public static void register(IEventBus bus) { ARMOR.register(bus); ATTACHMENTS.register(bus); EFFECTS.register(bus); RECIPES.register(bus); }
}
