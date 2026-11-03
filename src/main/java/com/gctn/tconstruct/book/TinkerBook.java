/* SPDX-License-Identifier: LGPL-3.0-only; Antique's guide item and first-login delivery. */
package com.gctn.tconstruct.book;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.function.Supplier;

@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class TinkerBook {
    public static final Supplier<Item> ITEM = ItemRegister.ITEMS.register("book", () -> new Item(new Item.Properties().stacksTo(1)) {
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide) { com.gctn.tconstruct.book.client.BookScreen.open(hand); }
            else { BookNetwork.begin(player, hand); }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
        }
    });
    private TinkerBook() {}
    public static void init(net.neoforged.bus.api.IEventBus bus) { BookNetwork.register(bus); }
    @SubscribeEvent private static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!com.gctn.tconstruct.Config.SPAWN_WITH_BOOK.get()) { return; }
        Player player = event.getEntity(); var data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!data.getBoolean("tconstruct:spawned_book")) {
            net.neoforged.neoforge.items.ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ITEM.get()));
            data.putBoolean("tconstruct:spawned_book", true); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        }
    }
}
