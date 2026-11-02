/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.events;

import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/** Integration hooks corresponding to Antique's melting and casting events. Fired on the server. */
public final class SmelteryEvents {
    private SmelteryEvents() {
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
