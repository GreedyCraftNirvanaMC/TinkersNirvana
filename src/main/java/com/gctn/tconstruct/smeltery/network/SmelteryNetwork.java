/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.network;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class SmelteryNetwork {
    private SmelteryNetwork() {
    }

    @SubscribeEvent
    private static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(Heats.TYPE, Heats.CODEC, (packet, context) -> {
            if (context.player().containerMenu instanceof SmelteryMenu menu && menu.containerId == packet.menuId) {
                menu.applyHeats(packet.values);
            }
        });
        registrar.playToServer(SelectFluid.TYPE, SelectFluid.CODEC, (packet, context) -> {
            // A packet carries the menu ID, not a world position that could target an arbitrary tank.
            if (context.player().containerMenu instanceof SmelteryMenu menu && menu.containerId == packet.menuId
                    && menu.stillValid(context.player())) {
                menu.controller.liquids.moveToBottom(packet.fluidIndex);
            }
        });
    }

    public record Heats(int menuId, int[] values) implements CustomPacketPayload {
        public static final Type<Heats> TYPE = new Type<>(SmelteryFluids.id("smeltery_heats"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Heats> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.menuId); buffer.writeVarIntArray(packet.values); },
                buffer -> new Heats(buffer.readVarInt(), buffer.readVarIntArray(3072)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SelectFluid(int menuId, int fluidIndex) implements CustomPacketPayload {
        public static final Type<SelectFluid> TYPE = new Type<>(SmelteryFluids.id("select_smeltery_fluid"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SelectFluid> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.menuId); buffer.writeVarInt(packet.fluidIndex); },
                buffer -> new SelectFluid(buffer.readVarInt(), buffer.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
