/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery.network;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.smeltery.menu.TinkerTankMenu;
import com.gctn.tconstruct.smeltery.block.entity.FluidSmelteryMaster;
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
        registrar.playToServer(Scroll.TYPE, Scroll.CODEC, (packet, context) -> {
            // 只能翻动当前有效菜单，不能通过位置包操作远处结构。
            if (context.player().containerMenu instanceof SmelteryMenu menu && menu.containerId == packet.menuId
                    && menu.stillValid(context.player())) {
                menu.changePage(packet.row);
            }
        });
        registrar.playToClient(Page.TYPE, Page.CODEC, (packet, context) -> {
            if (context.player().containerMenu instanceof SmelteryMenu menu && menu.containerId == packet.menuId) {
                menu.confirmPage(packet.row);
            }
        });
        registrar.playToServer(SelectFluid.TYPE, SelectFluid.CODEC, (packet, context) -> {
            // A packet carries the menu ID, not a world position that could target an arbitrary tank.
            if (context.player().containerMenu instanceof SmelteryMenu menu && menu.containerId == packet.menuId
                    && menu.stillValid(context.player()) && menu.controller instanceof FluidSmelteryMaster controller) {
                controller.liquids().moveToBottom(packet.fluidIndex);
            } else if (context.player().containerMenu instanceof TinkerTankMenu menu && menu.containerId == packet.menuId
                    && menu.stillValid(context.player())) {
                // 储罐沿用同一选液协议，仍须核对当前菜单及玩家距离。
                menu.controller.liquids.moveToBottom(packet.fluidIndex);
            }
        });
    }

    public record Scroll(int menuId, int row) implements CustomPacketPayload {
        public static final Type<Scroll> TYPE = new Type<>(SmelteryFluids.id("scroll_smeltery"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Scroll> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.menuId); buffer.writeVarInt(packet.row); },
                buffer -> new Scroll(buffer.readVarInt(), buffer.readVarInt()));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Page(int menuId, int row) implements CustomPacketPayload {
        public static final Type<Page> TYPE = new Type<>(SmelteryFluids.id("smeltery_page"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Page> CODEC = StreamCodec.of(
                (buffer, packet) -> { buffer.writeVarInt(packet.menuId); buffer.writeVarInt(packet.row); },
                buffer -> new Page(buffer.readVarInt(), buffer.readVarInt()));
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
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
