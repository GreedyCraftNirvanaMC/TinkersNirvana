/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class GearNetwork {
    private GearNetwork() {}
    @SubscribeEvent private static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Velocity.TYPE, Velocity.CODEC, (packet, context) -> {
            var player = context.player(); player.setDeltaMovement(packet.motion);
            if (packet.bounce) { var bounce = player.getData(SlimeGear.BOUNCE); bounce.active = true; bounce.wasAir = false; bounce.groundTicks = 0; }
        });
    }
    public record Velocity(Vec3 motion, boolean bounce) implements CustomPacketPayload {
        public Velocity(Vec3 motion) { this(motion, true); }
        public static final Type<Velocity> TYPE = new Type<>(SlimeGear.id("sling_velocity"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Velocity> CODEC = StreamCodec.of((buffer, packet) -> {
            buffer.writeDouble(packet.motion.x); buffer.writeDouble(packet.motion.y); buffer.writeDouble(packet.motion.z);
            buffer.writeBoolean(packet.bounce);
        }, buffer -> new Velocity(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
