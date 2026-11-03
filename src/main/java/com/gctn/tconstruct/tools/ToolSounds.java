/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ToolSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TinkersNirvana.MODID);
    public static final Supplier<SoundEvent> FRYPAN = sound("frypan_hit");
    public static final Supplier<SoundEvent> SQUEAK = sound("toy_squeak");
    public static final Supplier<SoundEvent> CHARGED = sound("charged");
    public static final Supplier<SoundEvent> DISCHARGE = sound("discharge");
    public static final Supplier<SoundEvent> CROSSBOW_RELOAD = sound("crossbow_reload");
    private ToolSounds() {}
    private static Supplier<SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, id)));
    }
    public static void register(IEventBus bus) { SOUNDS.register(bus); }
}
