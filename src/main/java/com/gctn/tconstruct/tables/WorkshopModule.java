/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's tool tables. */
package com.gctn.tconstruct.tables;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.tables.block.ToolStationBlock;
import com.gctn.tconstruct.tables.block.WorkshopBlock;
import com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity;
import com.gctn.tconstruct.tables.menu.WorkshopMenu;
import com.gctn.tconstruct.tables.menu.PersistentCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Shared registration and bounded discovery for connected workshop tables. */
public final class WorkshopModule {
    public enum Kind {
        CRAFTING("crafting_station", 9), STENCIL("stencil_table", 1), PART_BUILDER("part_builder", 7),
        PATTERN_CHEST("pattern_chest", 256), PART_CHEST("part_chest", 256);
        public final String id;
        public final int size;
        Kind(String id, int size) { this.id = id; this.size = size; }
        public boolean storage() { return this == PATTERN_CHEST || this == PART_CHEST; }
    }
    public static final Map<Kind, DeferredBlock<WorkshopBlock>> BLOCKS = new LinkedHashMap<>();
    static {
        for (Kind kind : Kind.values()) {
            var block = BlockRegister.BLOCKS.register(kind.id, () -> new WorkshopBlock(kind,
                    BlockBehaviour.Properties.of().strength(1, 5).sound(SoundType.WOOD).noOcclusion()));
            BLOCKS.put(kind, block);
            ItemRegister.ITEMS.register(kind.id, () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID);
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID);
    private static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>> CONDITIONS = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.CONDITION_CODECS, TinkersNirvana.MODID);
    public static final Supplier<com.mojang.serialization.MapCodec<? extends net.neoforged.neoforge.common.conditions.ICondition>> FLINT_CONDITION = CONDITIONS.register("flint_recipe_enabled", () -> com.gctn.tconstruct.common.FlintRecipeCondition.CODEC);
    public static final Supplier<net.minecraft.world.item.crafting.RecipeSerializer<com.gctn.tconstruct.tables.recipe.KitRepairRecipe>> KIT_REPAIR = RECIPES.register("repair", () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(com.gctn.tconstruct.tables.recipe.KitRepairRecipe::new));
    public static final Supplier<net.minecraft.world.item.crafting.RecipeSerializer<com.gctn.tconstruct.tables.recipe.TableMaterialRecipe>> TABLE_RECIPE = RECIPES.register("table", com.gctn.tconstruct.tables.recipe.TableMaterialRecipe.Serializer::new);
    public static final Supplier<BlockEntityType<WorkshopBlockEntity>> ENTITY = ENTITIES.register("workshop", () ->
            BlockEntityType.Builder.of(WorkshopBlockEntity::new, BLOCKS.values().stream().map(Supplier::get).toArray(WorkshopBlock[]::new)).build(null));
    public static final Supplier<MenuType<WorkshopMenu>> MENU = MENUS.register("workshop", () -> IMenuTypeExtension.create(WorkshopMenu::new));
    public static final Supplier<MenuType<PersistentCraftingMenu>> CRAFTING_MENU = MENUS.register("crafting_station", () -> IMenuTypeExtension.create(PersistentCraftingMenu::new));

    private WorkshopModule() {}
    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        MENUS.register(bus);
        RECIPES.register(bus);
        CONDITIONS.register(bus);
        bus.addListener(WorkshopModule::capabilities);
    }
    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ENTITY.get(), (entity, side) -> entity.itemHandler());
    }
    public static boolean isTable(Level level, BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        return block instanceof WorkshopBlock || block instanceof ToolStationBlock;
    }
    public static List<BlockPos> connected(Level level, BlockPos origin, Player player) {
        var found = connectedByDistance(level, origin, player);
        // 从任意相邻工作台打开时，按方块 ID 排列，重复类型使用固定坐标作为入口。
        var byType = new java.util.TreeMap<net.minecraft.resources.ResourceLocation, BlockPos>();
        found.sort(Comparator.naturalOrder());
        for (BlockPos pos : found) {
            byType.putIfAbsent(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()), pos);
        }
        return List.copyOf(byType.values());
    }
    public static List<BlockPos> connectedByDistance(Level level, BlockPos origin, Player player) {
        List<BlockPos> found = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        HashSet<BlockPos> visited = new HashSet<>();
        queue.add(origin); visited.add(origin);
        while (!queue.isEmpty() && visited.size() <= 64) {
            BlockPos pos = queue.removeFirst();
            if (!level.hasChunkAt(pos) || !isTable(level, pos)) { continue; }
            if (player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64) { found.add(pos); }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(direction);
                if (visited.size() < 64 && level.hasChunkAt(next) && isTable(level, next) && visited.add(next)) { queue.addLast(next); }
            }
        }
        return found;
    }
    public static boolean navigate(Player player, BlockPos origin, int button) {
        if (button < 100 || button >= 164) { return false; }
        var tables = connected(player.level(), origin, player);
        int index = button - 100;
        if (index >= tables.size()) { return false; }
        BlockPos target = tables.get(index);
        if (player.level().getBlockState(target).getBlock() == player.level().getBlockState(origin).getBlock()) { return false; }
        open(player, target, true);
        return true;
    }
    public static void open(Player player, BlockPos pos) {
        open(player, pos, false);
    }
    private static void open(Player player, BlockPos pos, boolean switching) {
        if (!player.level().isClientSide && player.level().getBlockEntity(pos) instanceof MenuProvider provider) {
            MenuProvider menu = switching ? new NavigationMenuProvider(provider) : provider;
            if (provider instanceof WorkshopBlockEntity entity) {
                var source = com.gctn.tconstruct.tables.menu.NearbyInventory.find(player, pos, entity.kind());
                player.openMenu(menu, buffer -> {
                    buffer.writeBlockPos(pos);
                    if (entity.kind() != Kind.CRAFTING) { buffer.writeEnum(entity.kind()); }
                    com.gctn.tconstruct.tables.menu.NearbyInventory.write(buffer, source);
                });
            } else {
                var source = com.gctn.tconstruct.tables.menu.NearbyInventory.find(player, pos, null);
                player.openMenu(menu, buffer -> { buffer.writeBlockPos(pos); com.gctn.tconstruct.tables.menu.NearbyInventory.write(buffer, source); });
            }
        }
    }

    private record NavigationMenuProvider(MenuProvider target) implements MenuProvider {
        @Override public Component getDisplayName() { return target.getDisplayName(); }
        @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return target.createMenu(id, inventory, player);
        }
        // 让服务端正常关闭旧容器，同时跳过客户端的关闭包，避免恢复游戏视角并重置鼠标。
        @Override public boolean shouldTriggerClientSideContainerClosingOnOpen() { return false; }
    }
}
