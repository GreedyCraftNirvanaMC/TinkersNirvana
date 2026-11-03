/* SPDX-License-Identifier: LGPL-3.0-only; Antique's seven decorative frames. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.entity.FancyItemFrame;
import com.gctn.tconstruct.gadgets.item.FancyFrameItem;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class FancyFrames {
    public enum Kind { JEWEL, ALUBRASS, COBALT, ARDITE, MANYULLYN, GOLD, CLEAR;
        public String id() { return name().toLowerCase(Locale.ROOT); }
    }
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TinkersNirvana.MODID);
    public static final Supplier<EntityType<FancyItemFrame>> ENTITY = ENTITIES.register("fancy_frame", () -> EntityType.Builder.<FancyItemFrame>of(FancyItemFrame::new, MobCategory.MISC)
            .sized(.5f, .5f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("tconstruct:fancy_frame"));
    public static final Map<Kind, Supplier<FancyFrameItem>> ITEMS = new LinkedHashMap<>();
    static { for (Kind kind : Kind.values()) { ITEMS.put(kind, ItemRegister.ITEMS.register("fancy_frame_" + kind.id(), () -> new FancyFrameItem(kind))); } }
    private FancyFrames() {}
    public static void register(IEventBus bus) { ENTITIES.register(bus); }
}
