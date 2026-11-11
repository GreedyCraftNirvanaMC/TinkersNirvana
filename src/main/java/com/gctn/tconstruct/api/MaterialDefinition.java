package com.gctn.tconstruct.api;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Collects startup edits and resolves cross-registry references after registration. */
public final class MaterialDefinition {
    private final Material material;
    private final boolean existing;
    private final List<Runnable> pending = new ArrayList<>();

    public MaterialDefinition(String id) {
        ResourceLocation.parse(id);
        material = new Material(id);
        existing = false;
    }

    public MaterialDefinition(Material material) { this.material = material; existing = true; }
    public Material material() { return material; }
    public MaterialDefinition color(int value) { material.color = value | 0xff000000; return this; }
    public MaterialDefinition displayName(Component value) { material.displayName(value); return this; }
    public MaterialDefinition translationKey(String value) { material.translationKey(value); return this; }
    public MaterialDefinition craftable(boolean value) { material.setCraftable(value); return this; }
    public MaterialDefinition castable(boolean value) { material.setCastable(value); return this; }
    public MaterialDefinition head(int durability, float speed, float attack, int tier) { TinkerContent.head(material, durability, speed, attack, tier); return this; }
    public MaterialDefinition handle(float multiplier, int durability) { TinkerContent.handle(material, multiplier, durability); return this; }
    public MaterialDefinition extra(int durability) { TinkerContent.extra(material, durability); return this; }
    public MaterialDefinition bow(float drawSpeed, float range, float damage) { TinkerContent.bow(material, drawSpeed, range, damage); return this; }
    public MaterialDefinition bowString(float multiplier) { TinkerContent.bowString(material, multiplier); return this; }
    public MaterialDefinition shaft(float multiplier, int bonusAmmo) { TinkerContent.shaft(material, multiplier, bonusAmmo); return this; }
    public MaterialDefinition fletching(float accuracy, float multiplier) { TinkerContent.fletching(material, accuracy, multiplier); return this; }

    public MaterialDefinition item(String id, float value) {
        ResourceLocation key = ResourceLocation.parse(id);
        if (!Float.isFinite(value) || value <= 0) { throw new IllegalArgumentException("Invalid material value: " + value); }
        pending.add(() -> {
            if (BuiltInRegistries.ITEM.get(key) == Items.AIR) { throw new IllegalArgumentException("Unknown material input: " + id); }
            MaterialInputs.item(material, id, value);
        });
        material.fallbackItem(() -> new ItemStack(BuiltInRegistries.ITEM.get(key)));
        return this;
    }

    public MaterialDefinition itemTag(String id, float value) {
        ResourceLocation.parse(id);
        if (!Float.isFinite(value) || value <= 0) { throw new IllegalArgumentException("Invalid material value: " + value); }
        pending.add(() -> MaterialInputs.tag(material, id, value));
        return this;
    }
    public MaterialDefinition representativeItem(String id) {
        pending.add(() -> {
            if (BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)) == Items.AIR) { throw new IllegalArgumentException("Unknown representative item: " + id); }
            TinkerContent.representativeItem(material, id);
        });
        return this;
    }
    public MaterialDefinition requireItems(String... tags) { material.requireItems(tags); return this; }
    public MaterialDefinition castingFluid(String id) {
        ResourceLocation.parse(id);
        material.setCastable(true);
        pending.add(() -> {
            if (BuiltInRegistries.FLUID.get(ResourceLocation.parse(id)) == net.minecraft.world.level.material.Fluids.EMPTY) {
                throw new IllegalArgumentException("Unknown casting fluid: " + id);
            }
            TinkerContent.configureCastingMaterial(material, id);
        });
        return this;
    }
    public MaterialDefinition trait(String id, int level) { return addTrait(id, level); }
    public MaterialDefinition addTrait(String id, int level) { return partTrait(null, id, level); }
    public MaterialDefinition partTrait(String stat, String id, int level) {
        ContentNames.traitId(id);
        pending.add(() -> TinkerContent.trait(material, stat, id, level));
        return this;
    }
    public MaterialDefinition headTrait(String id, int level) { return partTrait("Head", id, level); }
    public MaterialDefinition handleTrait(String id, int level) { return partTrait("Handle", id, level); }
    public MaterialDefinition extraTrait(String id, int level) { return partTrait("Extra", id, level); }
    public MaterialDefinition bowTrait(String id, int level) { return partTrait("Bow", id, level); }
    public MaterialDefinition removeTrait(String id) { pending.add(() -> material.removeTrait(id)); return this; }
    public MaterialDefinition clearTraits() { pending.add(material::clearTraits); return this; }

    public Material install() {
        if (!existing && TinkerMaterials.getMaterial(material.identifier) != null) {
            throw new IllegalArgumentException("Duplicate material: " + material.identifier);
        }
        for (Runnable action : pending) { action.run(); }
        pending.clear();
        if (!existing) { TinkerMaterials.materials.add(material); }
        TinkerTraits.definitionsChanged();
        return material;
    }
}
