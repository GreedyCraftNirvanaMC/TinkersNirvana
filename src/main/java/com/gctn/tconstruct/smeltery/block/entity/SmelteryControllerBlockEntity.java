package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.*;
import com.gctn.tconstruct.smeltery.block.SmelteryControllerBlock;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.library.materials.MaterialValue;
import com.gctn.tconstruct.smeltery.recipe.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.*;

/**
 * 管理多方块结构归属、可变大小物品库存、加热、燃料和分层流体。
 * 结构变化时保留流体，只弹出不再容纳的物品；所需区块未加载时暂停访问。
 * 服务端菜单为每位玩家同步 27 个槽位，方块更新只发送流体和结构的可视状态。
 */
public class SmelteryControllerBlockEntity extends BaseContainerBlockEntity {
    public static final int INGOT = MaterialValue.VALUE_Ingot;
    public static final int CAPACITY_PER_BLOCK = INGOT * 8;
    private NonNullList<ItemStack> items = NonNullList.create();
    private int[] heat = new int[0];
    private SmelteryRecipe[] recipes = new SmelteryRecipe[0];
    private List<net.minecraft.world.item.crafting.RecipeHolder<SmelteryRecipe>> recipeSnapshot;
    private final MultiFluidTank tank = new MultiFluidTank(() -> { fluidsDirty = true; setChanged(); });
    private SmelteryStructure structure;
    private final Set<BlockPos> members = new HashSet<>();
    // 所需区块缺失时暂停访问，直到所有构件都可检查前不调整库存或弹出物品。
    // 这种情况需要与结构实际损坏区分处理。
    private boolean suspended, needsCheck = true, fluidsDirty, clientFormed;
    private int ticks, fuel;
    private BlockPos min = BlockPos.ZERO, max = BlockPos.ZERO;

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override public int getSlots() { return isFormed() ? items.size() : 0; }
        @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < getSlots() ? getItem(slot).copy() : ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot >= 0 && slot < getSlots() && canPlaceItem(slot, stack); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack) || !getItem(slot).isEmpty()) return stack;
            if (!simulate) setItem(slot, stack.copyWithCount(1));
            return stack.copyWithCount(stack.getCount() - 1);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= getSlots() || amount <= 0) return ItemStack.EMPTY;
            return simulate ? getItem(slot).copy() : removeItem(slot, amount);
        }
    };

    public SmelteryControllerBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.CONTROLLER_ENTITY.get(), pos, state); }
    public MultiFluidTank tank() { return tank; }
    public IItemHandler itemHandler() { return itemHandler; }
    public boolean isFormed() { return level != null && level.isClientSide ? clientFormed : structure != null && !suspended && structure.loaded(level); }
    public boolean contains(BlockPos pos) { return members.contains(pos); }
    public BlockPos min() { return min; }
    public BlockPos max() { return max; }
    public int fuel() { return fuel; }
    public int progress(int slot) {
        if (slot < 0 || slot >= heat.length || recipes[slot] == null) return 0;
        return (int) Math.clamp((long) heat[slot] * 1000 / recipes[slot].time(), 0L, 1000L);
    }
    public void requestCheck() { needsCheck = true; }

    public static void tick(Level level, BlockPos pos, BlockState state, SmelteryControllerBlockEntity controller) {
        controller.serverTick();
    }
    private void serverTick() {
        ticks++;
        if (needsCheck || ticks % 20 == 0) checkStructure();
        if (!isFormed()) return;
        // RecipeManager 在重载时替换不可变的配方映射，此前一直复用
        // 已有索引列表；旧配方进度不能直接用于完成新配方。
        var currentRecipes = SmelteryRecipes.all(level);
        if (recipeSnapshot != currentRecipes) {
            if (recipeSnapshot != null) Arrays.fill(heat, 0);
            Arrays.fill(recipes, null);
            recipeSnapshot = currentRecipes;
        }
        // 消耗燃料或推进进度前，先模拟是否能完整接收输出，
        // 避免满罐时仍消耗熔炼输入；每个槽位只容纳一个物品。
        boolean heating = false;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack input = items.get(slot);
            if (input.isEmpty()) continue;
            var recipe = recipes[slot];
            if (recipe == null) recipes[slot] = recipe = SmelteryRecipes.melting(level, input);
            if (recipe == null || recipe.temperature() > 1000 || tank.fill(recipe.fluidResult(), FluidAction.SIMULATE) != recipe.fluidResult().getAmount()) continue;
            if (!ensureFuel()) break;
            heating = true;
            if (++heat[slot] >= recipe.time()) {
                // 本轮已在消耗输入前完成容量检查。
                tank.fill(recipe.fluidResult(), FluidAction.EXECUTE);
                items.set(slot, ItemStack.EMPTY);
                heat[slot] = 0;
                recipes[slot] = null;
                setChanged();
            }
        }
        if (ticks % 5 == 0) heating |= alloy();
        if (ticks % 20 == 0) heating |= interactWithEntities();
        if (heating && fuel > 0) { fuel--; setChanged(); }
        if (ticks % 5 == 0 && fluidsDirty) { fluidsDirty = false; sync(); }
        if (ticks % 20 == 0) setChanged();
    }

    private boolean ensureFuel() {
        // 未用完的工作刻数保留到后续操作，机器空闲时不消耗燃料。
        // 结构中的任意燃料储罐都可以供应下一小批熔岩。
        if (fuel > 0) return true;
        for (BlockPos pos : structure.tanks()) {
            if (level.getBlockEntity(pos) instanceof SearedTankBlockEntity fuelTank && fuelTank.tank().getFluidAmount() >= 50) {
                fuelTank.tank().drain(50, FluidAction.EXECUTE);
                fuel = 100;
                setChanged();
                return true;
            }
        }
        return false;
    }

    private boolean alloy() {
        boolean worked = false;
        if (tank.amount() > tank.capacity()) return false;
        for (var holder : SmelteryRecipes.all(level)) {
            var recipe = holder.value();
            if (recipe.operation() != SmelteryRecipe.Operation.ALLOY || recipe.temperature() > 1000) continue;
            // 先在副本上模拟整个事务，同时正确处理多次要求同一种流体的配方。
            MultiFluidTank trial = new MultiFluidTank(() -> {});
            trial.setCapacity(tank.capacity());
            tank.contents().forEach(f -> trial.fill(f, FluidAction.EXECUTE));
            boolean matches = true;
            for (var input : recipe.fluids()) if (trial.drain(input, FluidAction.EXECUTE).getAmount() != input.getAmount()) { matches = false; break; }
            if (!matches || trial.fill(recipe.fluidResult(), FluidAction.SIMULATE) != recipe.fluidResult().getAmount() || !ensureFuel()) continue;
            for (var input : recipe.fluids()) tank.drain(input, FluidAction.EXECUTE);
            tank.fill(recipe.fluidResult(), FluidAction.EXECUTE);
            worked = true;
        }
        return worked;
    }

    private boolean interactWithEntities() {
        AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        for (var entity : level.getEntitiesOfClass(ItemEntity.class, bounds, e -> e.isAlive() && !e.hasPickUpDelay())) {
            ItemStack input = entity.getItem();
            if (SmelteryRecipes.melting(level, input) == null) continue;
            for (int slot = 0; slot < items.size() && !input.isEmpty(); slot++) {
                if (items.get(slot).isEmpty()) { setItem(slot, input.split(1)); }
            }
            if (input.isEmpty()) entity.discard();
            else entity.setItem(input);
        }
        boolean worked = false;
        for (var entity : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            FluidStack blood = new FluidStack(SmelteryFluids.BLOOD.source().get(), 20);
            if (tank.fill(blood, FluidAction.SIMULATE) == 20 && ensureFuel()
                    && entity.hurt(level.damageSources().inFire(), 2)) { tank.fill(blood, FluidAction.EXECUTE); worked = true; }
        }
        return worked;
    }

    public void checkStructure() {
        if (level == null || level.isClientSide) return;
        needsCheck = false;
        if (structure != null && !structure.loaded(level)) { suspended = true; updateActive(); return; }
        var detection = SmelteryStructure.detect(level, worldPosition);
        if (detection.unloaded()) { suspended = true; updateActive(); return; }
        suspended = false;
        SmelteryStructure next = detection.structure();
        if (Objects.equals(structure, next) && (next != null || members.isEmpty() && tank.capacity() == 0)) { updateActive(); return; }
        // 认领新结构范围前先解除旧归属，已加载的构件
        // 会先检查是否仍属于该控制器，再暴露其能力。
        releaseMembers();
        structure = next;
        if (next != null) {
            members.addAll(next.members());
            min = next.min(); max = next.max();
            for (BlockPos pos : members) if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.setController(worldPosition);
            resize(next.volume());
            tank.setCapacity(next.volume() * CAPACITY_PER_BLOCK);
        } else { resize(0); tank.setCapacity(0); }
        updateActive();
        sync();
    }

    private void updateActive() {
        BlockState state = getBlockState();
        if (state.getValue(SmelteryControllerBlock.ACTIVE) != isFormed()) {
            level.setBlock(worldPosition, state.setValue(SmelteryControllerBlock.ACTIVE, isFormed()), 3);
            sync();
        }
    }
    private void releaseMembers() {
        if (level != null) for (BlockPos pos : members) {
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part) part.clearController(worldPosition);
        }
        members.clear();
    }
    public void releaseStructure() { releaseMembers(); structure = null; tank.setCapacity(0); }
    private void resize(int size) {
        if (size == items.size()) return;
        // 保留仍可容纳输入的槽位顺序和进度，只弹出超出的
        // 物品；流体容量缩小时不截断已有流体。
        var resized = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            if (i < size) resized.set(i, items.get(i));
            else if (!items.get(i).isEmpty()) Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, items.get(i));
        }
        items = resized;
        heat = Arrays.copyOf(heat, size);
        recipes = Arrays.copyOf(recipes, size);
        setChanged();
    }
    private void sync() { setChanged(); if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); }

    @Override public int getContainerSize() { return items.size(); }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; heat = new int[items.size()]; recipes = new SmelteryRecipe[items.size()]; }
    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return isFormed() && level != null && SmelteryRecipes.melting(level, stack) != null; }
    @Override public void setItem(int slot, ItemStack stack) { super.setItem(slot, stack); heat[slot] = 0; recipes[slot] = null; }
    @Override public ItemStack removeItem(int slot, int count) { ItemStack result = super.removeItem(slot, count); if (!result.isEmpty()) { heat[slot] = 0; recipes[slot] = null; } return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack result = super.removeItemNoUpdate(slot); if (slot >= 0 && slot < heat.length) { heat[slot] = 0; recipes[slot] = null; } return result; }
    @Override protected Component getDefaultName() { return Component.translatable("container.tconstruct.smeltery"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new SmelteryMenu(id, inventory, this); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("InventorySize", items.size());
        // 原版 ContainerHelper 使用字节保存 Slot，而大型冶炼炉可能超过 256 个槽位。
        ListTag inventoryTag = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", slot);
                inventoryTag.add(items.get(slot).save(registries, entry));
            }
        }
        tag.put("Items", inventoryTag);
        tag.putIntArray("Heat", heat);
        tag.putLongArray("Members", members.stream().mapToLong(BlockPos::asLong).toArray());
        saveVisuals(tag, registries);
    }
    private void saveVisuals(CompoundTag tag, HolderLookup.Provider registries) {
        // 附近客户端只需要这些可视数据，无需接收整个可变大小库存。
        // 物品内容仅通过分页菜单同步。
        tank.save(tag, registries);
        tag.putInt("Capacity", tank.capacity());
        tag.putInt("Fuel", fuel);
        tag.putBoolean("Formed", isFormed());
        tag.putLong("Min", min.asLong()); tag.putLong("Max", max.asLong());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 限制读取存档 NBT 时的内存分配：有效结构每层最多有 9×9 个物品
        // 槽位；配方引用在加载后重新构建。
        int limit = level == null ? 81 * 4096 : 81 * level.getHeight();
        items = NonNullList.withSize(Math.clamp(tag.getInt("InventorySize"), 0, limit), ItemStack.EMPTY);
        ListTag inventoryTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < inventoryTag.size(); i++) {
            CompoundTag entry = inventoryTag.getCompound(i);
            int slot = entry.contains("Slot", Tag.TAG_BYTE) ? entry.getByte("Slot") & 255 : entry.getInt("Slot");
            if (slot >= 0 && slot < items.size()) items.set(slot, ItemStack.parse(registries, entry).orElse(ItemStack.EMPTY));
        }
        heat = Arrays.copyOf(tag.getIntArray("Heat"), items.size());
        for (int i = 0; i < heat.length; i++) heat[i] = Math.clamp(heat[i], 0, 72000);
        recipes = new SmelteryRecipe[items.size()];
        recipeSnapshot = null;
        members.clear();
        for (long pos : tag.getLongArray("Members")) members.add(BlockPos.of(pos));
        loadVisuals(tag, registries);
        structure = null;
        needsCheck = true;
    }
    private void loadVisuals(CompoundTag tag, HolderLookup.Provider registries) {
        tank.load(tag, registries);
        tank.setCapacity(Math.max(0, tag.getInt("Capacity")));
        fuel = Math.clamp(tag.getInt("Fuel"), 0, 100);
        min = BlockPos.of(tag.getLong("Min")); max = BlockPos.of(tag.getLong("Max"));
        clientFormed = tag.getBoolean("Formed");
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); saveVisuals(tag, registries); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) { loadVisuals(tag, registries); }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) { loadVisuals(packet.getTag(), registries); }
}
