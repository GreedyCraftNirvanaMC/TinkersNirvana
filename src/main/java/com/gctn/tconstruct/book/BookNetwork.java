/* SPDX-License-Identifier: LGPL-3.0-only; per-book reading position inspired by Mantle's BookHelper. */
package com.gctn.tconstruct.book;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class BookNetwork {
    private static final String DATA = "tconstruct:book";
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TinkersNirvana.MODID);
    private static final Supplier<AttachmentType<ReadingSession>> SESSION = ATTACHMENTS.register("book_reading", () -> AttachmentType.builder(ReadingSession::new).build());
    private static final class ReadingSession { ItemStack stack = ItemStack.EMPTY; InteractionHand hand; int slot; }
    private BookNetwork() {}
    static void register(IEventBus bus) { ATTACHMENTS.register(bus); }
    static void begin(Player player, InteractionHand hand) {
        var session = player.getData(SESSION); session.stack = player.getItemInHand(hand); session.hand = hand; session.slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
    }
    public static String savedPage(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(DATA).getString("Page"); }
    @SubscribeEvent private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SavePage.TYPE, SavePage.CODEC, (packet, context) -> {
            Player player = context.player();
            if (!player.isAlive() || !player.hasData(SESSION)) { return; }
            var session = player.getData(SESSION); var stack = player.getItemInHand(packet.hand);
            if (session.hand != packet.hand || session.slot != packet.slot || session.stack != stack || !stack.is(TinkerBook.ITEM.get())
                    || packet.hand == InteractionHand.MAIN_HAND && player.getInventory().selected != packet.slot) { return; }
            player.removeData(SESSION);
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); var book = data.getCompound(DATA);
            book.putString("Page", packet.page); data.put(DATA, book); stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data)); player.getInventory().setChanged();
        });
    }
    public record SavePage(InteractionHand hand, int slot, String page) implements CustomPacketPayload {
        public static final Type<SavePage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "book_page"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SavePage> CODEC = StreamCodec.of((buffer, packet) -> {
            buffer.writeEnum(packet.hand); buffer.writeVarInt(packet.slot); buffer.writeUtf(packet.page, 256);
        }, buffer -> new SavePage(buffer.readEnum(InteractionHand.class), buffer.readVarInt(), buffer.readUtf(256)));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
