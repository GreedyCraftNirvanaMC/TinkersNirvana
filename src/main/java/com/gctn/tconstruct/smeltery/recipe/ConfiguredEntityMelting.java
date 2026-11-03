/* SPDX-License-Identifier: LGPL-3.0-only; Antique's entityMelting configuration. */
package com.gctn.tconstruct.smeltery.recipe;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.ConfiguredEntities;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ConfiguredEntityMelting {
    private record Rule(Predicate<LivingEntity> entity, FluidStack fluid) {}
    private static List<Rule> rules = List.of();
    private static List<? extends String> snapshot;
    private ConfiguredEntityMelting() {}
    public static FluidStack find(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) { return FluidStack.EMPTY; }
        var values = Config.ENTITY_MELTING.get();
        if (!values.equals(snapshot)) {
            List<Rule> parsed = new ArrayList<>();
            for (String value : values) {
                try {
                    String[] fields = value.split(";", -1);
                    if (fields.length != 4) { throw new IllegalArgumentException("Expected entity;subtypes;fluid;amount"); }
                    String name = fields[2]; ResourceLocation id = name.contains(":") ? ResourceLocation.parse(name) : name.equals("water") || name.equals("lava") ? ResourceLocation.withDefaultNamespace(name) : SmelteryFluids.id(name);
                    int amount = Integer.parseInt(fields[3]); if (amount < 1 || amount > 1000000) { throw new IllegalArgumentException("Amount must be 1..1000000 mB"); }
                    if (!BuiltInRegistries.FLUID.containsKey(id)) { continue; }
                    FluidStack fluid = new FluidStack(BuiltInRegistries.FLUID.get(id), amount); if (fluid.isEmpty()) { continue; }
                    parsed.add(new Rule(ConfiguredEntities.matcher(fields[0], fields[1], level), fluid));
                } catch (RuntimeException error) { TinkersNirvana.LOGGER.error("Invalid entityMelting entry {}: {}", value, error.getMessage()); }
            }
            rules = List.copyOf(parsed); snapshot = List.copyOf(values);
        }
        for (int i = rules.size() - 1; i >= 0; i--) { Rule rule = rules.get(i); if (rule.entity.test(entity)) { return rule.fluid.copy(); } }
        return FluidStack.EMPTY;
    }
}
