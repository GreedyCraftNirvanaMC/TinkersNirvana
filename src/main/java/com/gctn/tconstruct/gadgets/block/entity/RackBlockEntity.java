/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's TileItemRack and TileDryingRack. */
package com.gctn.tconstruct.gadgets.block.entity;

import com.gctn.tconstruct.gadgets.RemainingGadgets;
import com.gctn.tconstruct.gadgets.block.RackBlock;
import com.gctn.tconstruct.gadgets.recipe.DryingRecipe;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tables.TableMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class RackBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    public final TableMaterial material = new TableMaterial(Blocks.OAK_PLANKS.defaultBlockState());
    private int time, duration;
    private String recipeId = "";
    private final RecipeManager.CachedCheck<SingleRecipeInput, DryingRecipe> recipes = RecipeManager.createCheck(RemainingGadgets.DRYING_TYPE.get());
    private final IItemHandler handler = new InvWrapper(this) {
        @Override public int getSlotLimit(int slot) { return slot == 0 ? 1 : 64; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return slot == 0 && isEmpty() ? super.insertItem(slot, stack, simulate) : stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return !drying() || slot == 1 ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY; }
    };
    public RackBlockEntity(BlockPos pos, BlockState state) { super(RemainingGadgets.RACK_ENTITY.get(), pos, state); }
    public boolean drying() { return ((RackBlock) getBlockState().getBlock()).drying; }
    public IItemHandler itemHandler() { return handler; }
    public float progress() { return duration > 0 && !getItem(0).isEmpty() ? Math.min(1, time / (float) duration) : 0; }
    public ItemStack displayed() { return getItem(1).isEmpty() ? getItem(0) : getItem(1); }
    @Override public int getContainerSize() { return 2; }
    @Override public NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 0 && isEmpty(); }
    @Override public boolean canTakeItem(net.minecraft.world.Container target, int slot, ItemStack stack) { return !drying() || slot == 1; }
    private void reset() { time = duration = 0; recipeId = ""; }
    @Override public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            reset(); stack = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
            if (drying() && !stack.isEmpty() && level != null && !level.isClientSide && recipes.getRecipeFor(new SingleRecipeInput(stack), level).isEmpty()) { slot = 1; }
        }
        super.setItem(slot, stack);
    }
    @Override public ItemStack removeItem(int slot, int amount) { if (slot == 0) { reset(); } return super.removeItem(slot, amount); }
    @Override public ItemStack removeItemNoUpdate(int slot) { if (slot == 0) { reset(); } return super.removeItemNoUpdate(slot); }
    public void interact(Player player, InteractionHand hand) {
        if (!canOpen(player)) { return; }
        if (isEmpty()) { setItem(0, player.getItemInHand(hand).split(1)); }
        else { int slot = getItem(1).isEmpty() ? 0 : 1; ItemStack stack = removeItem(slot, getItem(slot).getCount()); player.getInventory().placeItemBackInInventory(stack); }
        setChanged();
    }
    public static void tick(Level level, BlockPos pos, BlockState state, RackBlockEntity rack) {
        if (level.isClientSide) { if (rack.duration > 0 && rack.time < rack.duration) { rack.time++; } return; }
        if (rack.getItem(0).isEmpty() || !rack.getItem(1).isEmpty()) { return; }
        var recipe = rack.recipes.getRecipeFor(new SingleRecipeInput(rack.getItem(0)), level);
        if (recipe.isEmpty()) { rack.items.set(1, rack.items.get(0)); rack.items.set(0, ItemStack.EMPTY); rack.reset(); rack.setChanged(); return; }
        int duration = Math.max(1, (int) Math.min(Integer.MAX_VALUE, recipe.get().value().time() * com.gctn.tconstruct.Config.DRYING_TIME_MULTIPLIER.get()));
        if (!rack.recipeId.equals(recipe.get().id().toString()) || rack.duration != duration) {
            rack.reset(); rack.recipeId = recipe.get().id().toString(); rack.duration = duration; rack.setChanged();
        }
        if (++rack.time >= rack.duration) {
            rack.items.set(1, recipe.get().value().assemble(new SingleRecipeInput(rack.getItem(0)), level.registryAccess()));
            rack.items.set(0, ItemStack.EMPTY); rack.reset(); rack.setChanged();
        } else if (rack.time % 20 == 0) { rack.setChangedWithoutSync(); }
    }
    private void setChangedWithoutSync() { super.setChanged(); }
    @Override public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide && !isRemoved()) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock()); }
    }
    @Override protected Component getDefaultName() { return Component.translatable(drying() ? "block.tconstruct.drying_rack" : "block.tconstruct.item_rack"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return null; }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); items = NonNullList.withSize(2, ItemStack.EMPTY); ContainerHelper.loadAllItems(tag, items, registries);
        time = Math.max(0, tag.getInt("Time")); duration = Math.max(0, tag.getInt("MaxTime")); recipeId = tag.getString("Recipe"); material.load(tag, registries); requestModelDataUpdate();
        if (level != null && level.isClientSide) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); ContainerHelper.saveAllItems(tag, items, registries); tag.putInt("Time", time); tag.putInt("MaxTime", duration); tag.putString("Recipe", recipeId); material.save(tag);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public net.neoforged.neoforge.client.model.data.ModelData getModelData() { return material.modelData(); }
    @Override protected void applyImplicitComponents(DataComponentInput input) { super.applyImplicitComponents(input); material.set(input.getOrDefault(TinkerDataComponents.TABLE_MATERIAL.get(), material.state())); }
    @Override protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder output) { super.collectImplicitComponents(output); material.collect(output); }
}
