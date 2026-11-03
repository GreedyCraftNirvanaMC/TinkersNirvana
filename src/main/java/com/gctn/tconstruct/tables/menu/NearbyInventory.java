/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's adjacent table inventories. */
package com.gctn.tconstruct.tables.menu;

import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.WorkshopModule.Kind;
import com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.function.Consumer;

/** Fixed network slots backed by a fresh, permission-checked capability on every access. */
public final class NearbyInventory {
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS = 90, NEXT = 91;
    public record Source(BlockPos pos, Direction side, int size) {
        public void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeBlockPos(pos); buffer.writeByte(side == null ? -1 : side.ordinal()); buffer.writeVarInt(size);
        }
        public static Source read(RegistryFriendlyByteBuf buffer) {
            BlockPos pos = buffer.readBlockPos(); int side = buffer.readByte(); int size = buffer.readVarInt();
            if (size < 1 || size > 4096 || side < -1 || side >= Direction.values().length) { throw new IllegalArgumentException("Invalid workshop inventory"); }
            return new Source(pos, side < 0 ? null : Direction.values()[side], size);
        }
    }
    public static void write(RegistryFriendlyByteBuf buffer, Source source) {
        buffer.writeBoolean(source != null); if (source != null) { source.write(buffer); }
    }
    public static Source read(RegistryFriendlyByteBuf buffer) { return buffer.readBoolean() ? Source.read(buffer) : null; }
    public static Source find(Player player, BlockPos origin, Kind kind) {
        if (kind == Kind.CRAFTING) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos pos = origin.relative(direction);
                if (WorkshopModule.isTable(player.level(), pos)) { continue; }
                if (blacklisted(player, pos)) { continue; }
                Source source = inspect(player, pos, direction.getOpposite());
                if (source != null) { return source; }
            }
        } else if (kind == null || kind == Kind.STENCIL || kind == Kind.PART_BUILDER) {
            Kind wanted = kind == null ? Kind.PART_CHEST : Kind.PATTERN_CHEST;
            for (BlockPos pos : WorkshopModule.connected(player.level(), origin, player)) {
                if (player.level().getBlockEntity(pos) instanceof WorkshopBlockEntity entity && entity.kind() == wanted) {
                    Source source = inspect(player, pos, null);
                    if (source != null) { return source; }
                }
            }
        }
        return null;
    }
    private static Source inspect(Player player, BlockPos pos, Direction side) {
        if (!allowed(player, pos)) { return null; }
        IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        return handler instanceof IItemHandlerModifiable && handler.getSlots() > 0 && handler.getSlots() <= 4096 ? new Source(pos, side, handler.getSlots()) : null;
    }
    private static boolean allowed(Player player, BlockPos pos) {
        if (!player.level().hasChunkAt(pos) || !player.mayInteract(player.level(), pos)) { return false; }
        return !(player.level().getBlockEntity(pos) instanceof BaseContainerBlockEntity container) || container.canOpen(player);
    }
    private static boolean blacklisted(Player player, BlockPos pos) {
        var blocked = com.gctn.tconstruct.Config.CRAFTING_STATION_BLACKLIST.get();
        if (blocked.isEmpty()) { return false; }
        var entity = player.level().getBlockEntity(pos);
        return blocked.contains(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock()).toString())
                || entity != null && (blocked.contains(entity.getClass().getName()) || blocked.contains(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()).toString()));
    }
    private final Player player;
    private final Source source;
    private final BlockEntity original;
    private final IItemHandlerModifiable client;
    private final SimpleContainerData data = new SimpleContainerData(1);
    private final int x;
    private boolean visible = true;
    private final IItemHandlerModifiable pageHandler = new IItemHandlerModifiable() {
        private int index(int slot) { return player.level().isClientSide ? slot : page() * PAGE_SIZE + slot; }
        private IItemHandlerModifiable access(int slot) { return index(slot) < size() ? handler() : null; }
        @Override public int getSlots() { return PAGE_SIZE; }
        @Override public ItemStack getStackInSlot(int slot) { var handler = access(slot); return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(index(slot)); }
        @Override public void setStackInSlot(int slot, ItemStack stack) { var handler = access(slot); if (handler != null) { handler.setStackInSlot(index(slot), stack); } }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { var handler = access(slot); return handler == null ? stack : handler.insertItem(index(slot), stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { var handler = access(slot); return handler == null ? ItemStack.EMPTY : handler.extractItem(index(slot), amount, simulate); }
        @Override public int getSlotLimit(int slot) { var handler = access(slot); return handler == null ? 0 : handler.getSlotLimit(index(slot)); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { var handler = access(slot); return handler != null && handler.isItemValid(index(slot), stack); }
    };
    public NearbyInventory(Player player, Source source, int x) {
        this.player = player; this.source = source; this.x = x;
        original = source == null ? null : player.level().getBlockEntity(source.pos);
        client = player.level().isClientSide && source != null ? new ItemStackHandler(PAGE_SIZE) : null;
    }
    public void addSlots(Consumer<Slot> add) {
        if (source == null) { return; }
        for (int i = 0; i < PAGE_SIZE; i++) {
            final int index = i;
            add.accept(new SlotItemHandler(pageHandler, i, x + i % 5 * 18, 17 + i / 5 * 18) {
                @Override public boolean isActive() { return visible && page() * PAGE_SIZE + index < size(); }
            });
        }
    }
    private IItemHandlerModifiable handler() {
        if (client != null) { return client; }
        if (source == null || !allowed(player, source.pos) || player.level().getBlockEntity(source.pos) != original) { return null; }
        if (!(original instanceof WorkshopBlockEntity) && blacklisted(player, source.pos)) { return null; }
        IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, source.pos, source.side);
        return handler instanceof IItemHandlerModifiable modifiable && handler.getSlots() == source.size ? modifiable : null;
    }
    public boolean valid() { return source == null || handler() != null; }
    /** Swap a stored pattern with the builder template without using the cursor or trusting client slot data. */
    public boolean swapPattern(int visibleSlot, net.minecraft.world.Container builder) {
        if (player.level().isClientSide || source == null || visibleSlot < 0 || visibleSlot >= PAGE_SIZE || !visible || handler() == null
                || !(original instanceof WorkshopBlockEntity chest) || chest.kind() != Kind.PATTERN_CHEST) { return false; }
        int slot = page() * PAGE_SIZE + visibleSlot;
        if (slot >= source.size) { return false; }
        ItemStack wanted = chest.getItem(slot), previous = builder.getItem(0);
        if (!(wanted.getItem() instanceof com.gctn.tconstruct.tools.PatternItem) || wanted.getCount() != 1
                || !previous.isEmpty() && (!(previous.getItem() instanceof com.gctn.tconstruct.tools.PatternItem) || previous.getCount() != 1
                || !WorkshopBlockEntity.validInput(Kind.PATTERN_CHEST, chest, slot, previous))) { return false; }
        chest.setItem(slot, previous); builder.setItem(0, wanted); chest.setChanged(); builder.setChanged(); return true;
    }
    public boolean present() { return source != null; }
    public SimpleContainerData data() { return data; }
    public int page() { return data.get(0); }
    public int pages() { return (size() + PAGE_SIZE - 1) / PAGE_SIZE; }
    private int size() { return source == null ? 0 : source.size; }
    public int x() { return x; }
    public void visible(boolean visible) { this.visible = visible; }
    public boolean visible() { return present() && visible; }
    public boolean click(int id) {
        int page = page() + (id == PREVIOUS ? -1 : id == NEXT ? 1 : 0);
        if ((id != PREVIOUS && id != NEXT) || !valid() || page < 0 || page >= pages()) { return false; }
        data.set(0, page); return true;
    }
}
