package com.gctn.tconstruct.compat.kubejs;

import com.gctn.tconstruct.api.TraitDefinition;
import com.gctn.tconstruct.api.TraitContext;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.ReturnsSelf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

@ReturnsSelf
public final class TraitKubeBuilder extends BuilderBase<TraitDefinition> {
    private final TraitDefinition definition;
    public TraitKubeBuilder(ResourceLocation id) {
        super(id);
        definition = new TraitDefinition(id.toString());
    }

    @Override public void generateLang(dev.latvian.mods.kubejs.client.LangKubeEvent event) {}

    @Override public TraitDefinition createObject() { return definition; }
    public TraitKubeBuilder displayName(Component value) { definition.displayName(value); return this; }
    public TraitKubeBuilder description(Component value) { definition.description(value); return this; }
    public TraitKubeBuilder translationKey(String key) { definition.translationKey(key); return this; }
    public TraitKubeBuilder descriptionKey(String key) { definition.descriptionKey(key); return this; }
    public TraitKubeBuilder color(int value) { definition.color(value); return this; }
    public TraitKubeBuilder maxLevel(int value) { definition.maxLevel(value); return this; }
    public TraitKubeBuilder priority(int value) { definition.priority(value); return this; }
    public TraitKubeBuilder stacking(String mode) { definition.stacking(mode); return this; }
    public TraitKubeBuilder modifyModifierSlots(Consumer<TraitContext> handler) { definition.appendHandler("modifyModifierSlots", handler); return this; }
    public TraitKubeBuilder onExperienceDrop(Consumer<TraitContext> handler) { definition.appendHandler("onExperienceDrop", handler); return this; }
    public TraitKubeBuilder onProjectileHitBlock(Consumer<TraitContext> handler) { definition.appendHandler("onProjectileHitBlock", handler); return this; }
    public TraitKubeBuilder modifyStats(Consumer<TraitContext> handler) { definition.appendHandler("modifyStats", handler); return this; }
    public TraitKubeBuilder modifyAttack(Consumer<TraitContext> handler) { definition.appendHandler("modifyAttack", handler); return this; }
    public TraitKubeBuilder modifyDurability(Consumer<TraitContext> handler) { definition.appendHandler("modifyDurability", handler); return this; }
    public TraitKubeBuilder onToolDamage(Consumer<TraitContext> handler) { definition.appendHandler("onToolDamage", handler); return this; }
    public TraitKubeBuilder onToolHeal(Consumer<TraitContext> handler) { definition.appendHandler("onToolHeal", handler); return this; }
    public TraitKubeBuilder onRepair(Consumer<TraitContext> handler) { definition.appendHandler("onRepair", handler); return this; }
    public TraitKubeBuilder miningSpeed(Consumer<TraitContext> handler) { definition.appendHandler("miningSpeed", handler); return this; }
    public TraitKubeBuilder beforeBlockBreak(Consumer<TraitContext> handler) { definition.appendHandler("beforeBlockBreak", handler); return this; }
    public TraitKubeBuilder afterBlockBreak(Consumer<TraitContext> handler) { definition.appendHandler("afterBlockBreak", handler); return this; }
    public TraitKubeBuilder onBlockDrops(Consumer<TraitContext> handler) { definition.appendHandler("onBlockDrops", handler); return this; }
    public TraitKubeBuilder onDamageTaken(Consumer<TraitContext> handler) { definition.appendHandler("onDamageTaken", handler); return this; }
    public TraitKubeBuilder inventoryTick(Consumer<TraitContext> handler) { definition.appendHandler("inventoryTick", handler); return this; }
    public TraitKubeBuilder afterHit(Consumer<TraitContext> handler) { definition.appendHandler("afterHit", handler); return this; }
    public TraitKubeBuilder afterDamage(Consumer<TraitContext> handler) { definition.appendHandler("afterDamage", handler); return this; }
    public TraitKubeBuilder onProjectileLaunch(Consumer<TraitContext> handler) { definition.appendHandler("onProjectileLaunch", handler); return this; }
    public TraitKubeBuilder onProjectileTick(Consumer<TraitContext> handler) { definition.appendHandler("onProjectileTick", handler); return this; }
}
