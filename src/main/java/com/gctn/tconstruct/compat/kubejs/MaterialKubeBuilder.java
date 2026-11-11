package com.gctn.tconstruct.compat.kubejs;

import com.gctn.tconstruct.api.MaterialDefinition;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

@ReturnsSelf
public final class MaterialKubeBuilder extends BuilderBase<MaterialDefinition> {
    private final MaterialDefinition definition;
    public MaterialKubeBuilder(ResourceLocation id) {
        super(id);
        definition = new MaterialDefinition(id.toString());
    }

    @Override public void generateLang(dev.latvian.mods.kubejs.client.LangKubeEvent event) {}

    @Override public MaterialDefinition createObject() { return definition; }
    public MaterialKubeBuilder color(int value) { definition.color(value); return this; }
    public MaterialKubeBuilder displayName(Component value) { definition.displayName(value); return this; }
    public MaterialKubeBuilder translationKey(String value) { definition.translationKey(value); return this; }
    public MaterialKubeBuilder craftable(boolean value) { definition.craftable(value); return this; }
    public MaterialKubeBuilder castable(boolean value) { definition.castable(value); return this; }
    public MaterialKubeBuilder head(int durability, float speed, float attack, int tier) { definition.head(durability, speed, attack, tier); return this; }
    public MaterialKubeBuilder handle(float multiplier, int durability) { definition.handle(multiplier, durability); return this; }
    public MaterialKubeBuilder extra(int durability) { definition.extra(durability); return this; }
    public MaterialKubeBuilder bow(float drawSpeed, float range, float damage) { definition.bow(drawSpeed, range, damage); return this; }
    public MaterialKubeBuilder bowString(float multiplier) { definition.bowString(multiplier); return this; }
    public MaterialKubeBuilder shaft(float multiplier, int bonusAmmo) { definition.shaft(multiplier, bonusAmmo); return this; }
    public MaterialKubeBuilder fletching(float accuracy, float multiplier) { definition.fletching(accuracy, multiplier); return this; }
    public MaterialKubeBuilder item(String id, float value) { definition.item(id, value); return this; }
    public MaterialKubeBuilder itemTag(String id, float value) { definition.itemTag(id, value); return this; }
    public MaterialKubeBuilder representativeItem(String id) { definition.representativeItem(id); return this; }
    public MaterialKubeBuilder castingFluid(String id) { definition.castingFluid(id); return this; }
    public MaterialKubeBuilder requireItems(String... tags) { definition.requireItems(tags); return this; }
    public MaterialKubeBuilder trait(String id, int level) { definition.trait(id, level); return this; }
    public MaterialKubeBuilder partTrait(String stat, String id, int level) { definition.partTrait(stat, id, level); return this; }
    public MaterialKubeBuilder headTrait(String id, int level) { definition.headTrait(id, level); return this; }
    public MaterialKubeBuilder handleTrait(String id, int level) { definition.handleTrait(id, level); return this; }
    public MaterialKubeBuilder extraTrait(String id, int level) { definition.extraTrait(id, level); return this; }
    public MaterialKubeBuilder bowTrait(String id, int level) { definition.bowTrait(id, level); return this; }
}
