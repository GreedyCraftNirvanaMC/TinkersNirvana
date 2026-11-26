/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.events;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/** Integration hooks corresponding to Antique's melting and casting events. Fired on the server. */
@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class SmelteryEvents {
    public static final ResourceKey<DamageType> ENTITY_MELTING_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, SmelteryFluids.id("smeltery"));

    private SmelteryEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    private static void checkMeltingDamage(EntityInvulnerabilityCheckEvent event) {
        var source = event.getSource();
        if (event.getOriginalInvulnerability() && source.is(ENTITY_MELTING_DAMAGE) && source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.IS_FALL) && event.getEntity() instanceof LivingEntity entity
                && entity.fireImmune() && !entity.isInvulnerable() && !entity.isRemoved()) {
            // 怀古的自定义火焰伤害可熔化烈焰人，抗火药水仍由 LivingEntity 的伤害流程处理。
            event.setInvulnerable(false);
        }
    }

    public static final class Melting extends Event implements ICancellableEvent {
        public final SmelteryBlockEntity controller;
        public final ItemStack input;
        public FluidStack result;

        public Melting(SmelteryBlockEntity controller, ItemStack input, FluidStack result) {
            this.controller = controller;
            this.input = input.copy();
            this.result = result.copy();
        }
    }

    public static final class Casting extends Event implements ICancellableEvent {
        public final SmelteryComponentBlockEntity table;
        public final SmelteryRecipes.Casting recipe;

        public Casting(SmelteryComponentBlockEntity table, SmelteryRecipes.Casting recipe) {
            this.table = table;
            this.recipe = recipe;
        }
    }

    public static final class Casted extends Event {
        public final SmelteryComponentBlockEntity table;
        public ItemStack output;
        public boolean consumeCast;
        public boolean switchOutputs;

        public Casted(SmelteryComponentBlockEntity table, SmelteryRecipes.Casting recipe) {
            this.table = table;
            output = recipe.output().copy();
            consumeCast = recipe.consumeCast();
            switchOutputs = recipe.switchOutputs();
        }
    }
}
