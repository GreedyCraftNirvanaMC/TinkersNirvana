/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.common.network;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryFluids;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import com.gctn.tconstruct.tools.HeadDrops;
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
import net.minecraft.world.item.ItemStack;
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
public final class EntityRecipeSync {
    private static final int MAX_ENTRIES = 16384;
    private static final Map<Connection, MeltingRecipes> MELTING_BY_CONNECTION = new WeakHashMap<>();
    private static final Map<Connection, BeheadingRecipes> BEHEADING_BY_CONNECTION = new WeakHashMap<>();

    private EntityRecipeSync() {}

    public record Melting(ResourceLocation entity, FluidStack output) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Melting> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Melting::entity, FluidStack.STREAM_CODEC, Melting::output, Melting::new);
    }

    public record Beheading(ResourceLocation entity, ItemStack output) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Beheading> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Beheading::entity, ItemStack.STREAM_CODEC, Beheading::output, Beheading::new);
    }

    public record Snapshot(List<Melting> melting, List<Beheading> beheading) {}

    private record MeltingRecipes(List<Melting> entries) implements CustomPacketPayload {
        private static final Type<MeltingRecipes> TYPE = new Type<>(SmelteryFluids.id("entity_melting_recipes"));
        private static final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipes> CODEC =
                Melting.CODEC.apply(ByteBufCodecs.list(MAX_ENTRIES)).map(MeltingRecipes::new, MeltingRecipes::entries);

        @Override public Type<MeltingRecipes> type() { return TYPE; }
    }

    private record BeheadingRecipes(List<Beheading> entries) implements CustomPacketPayload {
        private static final Type<BeheadingRecipes> TYPE = new Type<>(SmelteryFluids.id("entity_beheading_recipes"));
        private static final StreamCodec<RegistryFriendlyByteBuf, BeheadingRecipes> CODEC =
                Beheading.CODEC.apply(ByteBufCodecs.list(MAX_ENTRIES)).map(BeheadingRecipes::new, BeheadingRecipes::entries);

        @Override public Type<BeheadingRecipes> type() { return TYPE; }
    }

    @SubscribeEvent
    private static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToClient(MeltingRecipes.TYPE, MeltingRecipes.CODEC, (packet, context) ->
                MELTING_BY_CONNECTION.put(context.connection(), packet));
        registrar.playToClient(BeheadingRecipes.TYPE, BeheadingRecipes.CODEC, (packet, context) ->
                BEHEADING_BY_CONNECTION.put(context.connection(), packet));
    }

    @SubscribeEvent
    private static void sync(OnDatapackSyncEvent event) {
        var meltingPlayers = event.getRelevantPlayers()
                .filter(player -> NetworkRegistry.hasChannel(player.connection, MeltingRecipes.TYPE.id())).toList();
        var beheadingPlayers = event.getRelevantPlayers()
                .filter(player -> NetworkRegistry.hasChannel(player.connection, BeheadingRecipes.TYPE.id())).toList();
        if (meltingPlayers.isEmpty() && beheadingPlayers.isEmpty()) { return; }
        // 配置可因服务器而异。与原版配方同步一起发送生效数据，切换服务器时按连接隔离。
        var samplePlayer = meltingPlayers.isEmpty() ? beheadingPlayers.getFirst() : meltingPlayers.getFirst();
        var snapshot = snapshot(event.getPlayerList().getServer().overworld(), samplePlayer,
                !meltingPlayers.isEmpty(), !beheadingPlayers.isEmpty());
        var melting = new MeltingRecipes(snapshot.melting());
        var beheading = new BeheadingRecipes(snapshot.beheading());
        for (ServerPlayer player : meltingPlayers) { PacketDistributor.sendToPlayer(player, melting); }
        for (ServerPlayer player : beheadingPlayers) { PacketDistributor.sendToPlayer(player, beheading); }
    }

    public static Snapshot recipes(Connection connection) {
        var melting = MELTING_BY_CONNECTION.get(connection);
        var beheading = BEHEADING_BY_CONNECTION.get(connection);
        return new Snapshot(melting == null ? List.of() : List.copyOf(melting.entries()),
                beheading == null ? List.of() : List.copyOf(beheading.entries()));
    }

    public static Snapshot snapshot(ServerLevel level, ServerPlayer player, boolean includeMelting, boolean includeBeheading) {
        boolean showMelting = includeMelting && Config.ENTITY_MELTING_BLOOD.get();
        List<Melting> melting = new ArrayList<>();
        List<Beheading> beheading = new ArrayList<>();
        if (!showMelting && !includeBeheading) { return new Snapshot(List.of(), List.of()); }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (!((type == EntityType.PLAYER ? player : type.create(level)) instanceof LivingEntity entity)) { continue; }
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (showMelting) {
                FluidStack output = SmelteryRecipes.explicitEntityMelting(entity);
                if (!output.isEmpty()) { melting.add(new Melting(id, output)); }
            }
            // 旧版只将静态斩首配方展示给 JEI，玩家头颅的身份则在实际掉落时绑定。
            if (includeBeheading && type != EntityType.PLAYER) {
                for (ItemStack output : HeadDrops.configuredDrops(entity)) {
                    beheading.add(new Beheading(id, output));
                }
            }
        }
        return new Snapshot(bounded(melting), bounded(beheading));
    }

    private static <T> List<T> bounded(List<T> entries) {
        if (entries.size() > MAX_ENTRIES) {
            TinkersNirvana.LOGGER.warn("Entity recipe display exceeds {} entries", MAX_ENTRIES);
            return List.copyOf(entries.subList(0, MAX_ENTRIES));
        }
        return List.copyOf(entries);
    }
}
