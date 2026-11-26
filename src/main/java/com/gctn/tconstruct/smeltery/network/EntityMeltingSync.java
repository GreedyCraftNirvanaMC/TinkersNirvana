/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.smeltery.network;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class EntityMeltingSync {
    private static final int MAX_ENTRIES = 16384;
    private static final Map<Connection, List<Entry>> RECIPES_BY_CONNECTION = new WeakHashMap<>();

    private EntityMeltingSync() {}

    public record Entry(ResourceLocation entity, FluidStack output) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Entry> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::entity, FluidStack.STREAM_CODEC, Entry::output, Entry::new);
    }

    private record Recipes(List<Entry> entries) implements CustomPacketPayload {
        private static final Type<Recipes> TYPE = new Type<>(SmelteryFluids.id("entity_melting_recipes"));
        private static final StreamCodec<RegistryFriendlyByteBuf, Recipes> CODEC =
                Entry.CODEC.apply(ByteBufCodecs.list(MAX_ENTRIES)).map(Recipes::new, Recipes::entries);

        @Override public Type<Recipes> type() { return TYPE; }
    }

    @SubscribeEvent
    private static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(Recipes.TYPE, Recipes.CODEC, (packet, context) ->
                RECIPES_BY_CONNECTION.put(context.connection(), List.copyOf(packet.entries())));
    }

    @SubscribeEvent
    private static void sync(OnDatapackSyncEvent event) {
        var players = event.getRelevantPlayers()
                .filter(player -> NetworkRegistry.hasChannel(player.connection, Recipes.TYPE.id())).toList();
        if (players.isEmpty()) { return; }
        // 配置可因服务器而异。与原版配方同步一起发送生效数据，切换服务器时按连接隔离。
        var recipes = new Recipes(snapshot(event.getPlayerList().getServer().overworld(), players.getFirst()));
        for (ServerPlayer player : players) { PacketDistributor.sendToPlayer(player, recipes); }
    }

    public static List<Entry> recipes(Connection connection) {
        return RECIPES_BY_CONNECTION.getOrDefault(connection, List.of());
    }

    public static List<Entry> snapshot(ServerLevel level, ServerPlayer player) {
        if (!Config.ENTITY_MELTING_BLOOD.get()) { return List.of(); }
        List<Entry> entries = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (entries.size() >= MAX_ENTRIES) {
                TinkersNirvana.LOGGER.warn("Entity melting display exceeds {} entries", MAX_ENTRIES);
                break;
            }
            if (type == EntityType.PLAYER) {
                add(entries, player, type);
            } else if (type.create(level) instanceof LivingEntity prototype) {
                add(entries, prototype, type);
            }
        }
        return List.copyOf(entries);
    }

    private static void add(List<Entry> entries, LivingEntity entity, EntityType<?> type) {
        FluidStack output = SmelteryRecipes.explicitEntityMelting(entity);
        if (!output.isEmpty()) { entries.add(new Entry(BuiltInRegistries.ENTITY_TYPE.getKey(type), output)); }
    }
}
