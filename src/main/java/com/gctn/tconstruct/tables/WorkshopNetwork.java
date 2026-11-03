/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.tables;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class WorkshopNetwork {
    private WorkshopNetwork() {}
    @SubscribeEvent private static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(Rename.TYPE, Rename.CODEC, (packet, context) -> {
            if (context.player().containerMenu instanceof ToolStationMenu menu && menu.containerId == packet.menuId && menu.stillValid(context.player())) {
                menu.rename(packet.name);
            }
        });
        registrar.playToClient(Name.TYPE, Name.CODEC, (packet, context) -> {
            if (context.player().containerMenu instanceof ToolStationMenu menu && menu.containerId == packet.menuId) { menu.receiveName(packet.name); }
        });
    }
    public record Rename(int menuId, String name) implements CustomPacketPayload {
        public static final Type<Rename> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "rename_tool"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Rename> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.menuId); buffer.writeUtf(packet.name, 50); }, buffer -> new Rename(buffer.readVarInt(), buffer.readUtf(50)));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record Name(int menuId, String name) implements CustomPacketPayload {
        public static final Type<Name> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "tool_name"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Name> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.menuId); buffer.writeUtf(packet.name, 50); }, buffer -> new Name(buffer.readVarInt(), buffer.readUtf(50)));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
