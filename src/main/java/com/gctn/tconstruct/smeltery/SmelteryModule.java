/*
 * Smeltery behavior and assets adapted from TinkersAntique (Slime Knights and contributors).
 * SPDX-License-Identifier: LGPL-3.0-only
 * See third_party/NOTICE.md for source attribution.
 */
package com.gctn.tconstruct.smeltery;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.ChannelBlock;
import com.gctn.tconstruct.smeltery.block.SearedGlassBlock;
import com.gctn.tconstruct.smeltery.block.SearedLadderBlock;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.TinkerTankBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SearedFurnaceBlockEntity;
import com.gctn.tconstruct.smeltery.block.SearedSlabBlock;
import com.gctn.tconstruct.smeltery.block.SearedStairBlock;
import com.gctn.tconstruct.smeltery.block.ClearStainedGlassBlock;
import com.gctn.tconstruct.smeltery.loot.ClearGlassWithoutSilk;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.smeltery.menu.TinkerTankMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.EnumMap;
import java.util.function.Supplier;

public final class SmelteryModule {
    public static final Map<String, DeferredBlock<SmelteryBlock>> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, Supplier<Item>> ITEMS = new LinkedHashMap<>();
    private static final Collection<DeferredBlock<?>> MATERIAL_BLOCKS = new ArrayList<>();
    public static final Map<String, DeferredBlock<?>> SEARED_SHAPES = new LinkedHashMap<>();
    public static final Map<DyeColor, DeferredBlock<ClearStainedGlassBlock>> STAINED_GLASS = new EnumMap<>(DyeColor.class);
    public static final DeferredBlock<SmelteryBlock> CONTROLLER = block("smeltery_controller", SmelteryBlock.Kind.CONTROLLER);
    public static final DeferredBlock<SmelteryBlock> TINKER_TANK = block("tinker_tank_controller", SmelteryBlock.Kind.TINKER_TANK);
    public static final DeferredBlock<SmelteryBlock> FURNACE = block("seared_furnace_controller", SmelteryBlock.Kind.FURNACE);
    public static final DeferredBlock<SmelteryBlock> DRAIN = block("smeltery_drain", SmelteryBlock.Kind.DRAIN);
    public static final DeferredBlock<SmelteryBlock> TANK = block("seared_tank", SmelteryBlock.Kind.TANK);
    public static final DeferredBlock<SmelteryBlock> GAUGE = block("seared_gauge", SmelteryBlock.Kind.TANK);
    public static final DeferredBlock<SmelteryBlock> WINDOW = block("seared_window", SmelteryBlock.Kind.TANK);
    public static final DeferredBlock<SmelteryBlock> GLASS = block("seared_glass", SmelteryBlock.Kind.GLASS);
    public static final DeferredBlock<SmelteryBlock> LADDER = block("seared_ladder", SmelteryBlock.Kind.LADDER);
    public static final DeferredBlock<SmelteryBlock> FAUCET = block("faucet", SmelteryBlock.Kind.FAUCET);
    public static final DeferredBlock<SmelteryBlock> CHANNEL = block("channel", SmelteryBlock.Kind.CHANNEL);
    public static final DeferredBlock<SmelteryBlock> TABLE = block("casting_table", SmelteryBlock.Kind.TABLE);
    public static final DeferredBlock<SmelteryBlock> BASIN = block("casting_basin", SmelteryBlock.Kind.BASIN);
    public static final DeferredBlock<Block> GROUT = BlockRegister.registerBlockWithItem("grout", () -> new Block(BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<Block> MUD_BRICKS = BlockRegister.registerBlockWithItem("mud_bricks", () -> new Block(BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<Block> LAVAWOOD = BlockRegister.registerBlockWithItem("lavawood", () ->
            new Block(BlockBehaviour.Properties.of().strength(2.0F, 4.2F).sound(SoundType.WOOD).lightLevel(state -> 7)));
    public static final DeferredBlock<com.gctn.tconstruct.smeltery.block.ClearGlassBlock> CLEAR_GLASS = BlockRegister.registerBlockWithItem("clear_glass",
            () -> new com.gctn.tconstruct.smeltery.block.ClearGlassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));

    static {
        for (String type : new String[]{"stone", "cobble", "paver", "brick", "brick_cracked", "brick_fancy",
                "brick_square", "brick_triangle", "brick_small", "road", "tile", "creeper"}) {
            String name = type.equals("brick") ? "seared_bricks" : "seared_" + type;
            var base = block(name, SmelteryBlock.Kind.BRICK);
            SEARED_SHAPES.put(name + "_slab", BlockRegister.registerBlockWithItem(name + "_slab", () ->
                    new SearedSlabBlock(BlockBehaviour.Properties.ofFullCopy(base.get()))));
            SEARED_SHAPES.put(name + "_stairs", BlockRegister.registerBlockWithItem(name + "_stairs", () ->
                    new SearedStairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(base.get()))));
        }
        ITEMS.put("grout", () -> GROUT.get().asItem());
        MATERIAL_BLOCKS.addAll(java.util.List.of(MUD_BRICKS, LAVAWOOD, CLEAR_GLASS));
        for (DyeColor color : DyeColor.values()) {
            var glass = BlockRegister.registerBlockWithItem("clear_stained_glass_" + color.getName(), () ->
                    new ClearStainedGlassBlock(color, BlockBehaviour.Properties.ofFullCopy(CLEAR_GLASS.get()).mapColor(color.getMapColor())));
            STAINED_GLASS.put(color, glass);
            MATERIAL_BLOCKS.add(glass);
        }
        for (String name : new String[]{"seared_brick", "mud_brick", "cast_blank", "cast_ingot", "cast_nugget",
                "cast_gem", "cast_plate", "cast_gear", "cast_pickaxe_head", "cast_tool_rod", "cast_binding",
                "clay_cast_pickaxe_head", "clay_cast_tool_rod", "clay_cast_binding"}) {
            ITEMS.put(name, ItemRegister.ITEMS.register(name, () -> new Item(new Item.Properties())));
        }
        for (String metal : new String[]{"cobalt", "ardite", "manyullyn", "pigiron", "knightslime", "alubrass", "alumite",
                "bronze", "tin", "aluminum", "steel"}) {
            for (String form : new String[]{"ingot", "nugget"}) {
                String name = metal + "_" + form;
                ITEMS.put(name, ItemRegister.ITEMS.register(name, () -> new Item(new Item.Properties())));
            }
            // 原版金属块要求镐但没有等级门槛，信标基座资格由数据标签提供。
            MATERIAL_BLOCKS.add(BlockRegister.registerBlockWithItem(metal + "_block", () ->
                    new Block(BlockBehaviour.Properties.of().strength(5).sound(SoundType.METAL).requiresCorrectToolForDrops())));
        }
    }

    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID);
    private static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, TinkersNirvana.MODID);
    public static final Supplier<LootItemConditionType> CLEAR_GLASS_WITHOUT_SILK = LOOT_CONDITIONS.register("clear_glass_without_silk", () ->
            new LootItemConditionType(ClearGlassWithoutSilk.CODEC));
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TinkersNirvana.MODID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TinkersNirvana.MODID);
    public static final Supplier<DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent>> TANK_FLUID = COMPONENTS.registerComponentType("tank_fluid",
            builder -> builder.persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC)
                    .networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC));
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersNirvana.MODID);
    public static final Supplier<CreativeModeTab> TAB = TABS.register("smeltery", () -> CreativeModeTab.builder()
            .title(Component.translatable("tab.tconstruct.smeltery")).icon(() -> TANK.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                BLOCKS.values().forEach(block -> output.accept(block.get()));
                SEARED_SHAPES.values().forEach(block -> output.accept(block.get()));
                MATERIAL_BLOCKS.forEach(block -> output.accept(block.get()));
                ITEMS.values().forEach(item -> output.accept(item.get()));
                SmelteryFluids.ENTRIES.values().forEach(fluid -> output.accept(fluid.bucket.get()));
            }).build());
    public static final Supplier<BlockEntityType<SmelteryBlockEntity>> CONTROLLER_ENTITY = ENTITIES.register("smeltery",
            () -> BlockEntityType.Builder.of(SmelteryBlockEntity::new, CONTROLLER.get()).build(null));
    public static final Supplier<BlockEntityType<TinkerTankBlockEntity>> TINKER_TANK_ENTITY = ENTITIES.register("tinker_tank",
            () -> BlockEntityType.Builder.of(TinkerTankBlockEntity::new, TINKER_TANK.get()).build(null));
    public static final Supplier<BlockEntityType<SearedFurnaceBlockEntity>> FURNACE_ENTITY = ENTITIES.register("seared_furnace",
            () -> BlockEntityType.Builder.of(SearedFurnaceBlockEntity::new, FURNACE.get()).build(null));
    public static final Supplier<BlockEntityType<SmelteryComponentBlockEntity>> COMPONENT_ENTITY = ENTITIES.register("smeltery_component",
            () -> BlockEntityType.Builder.of(SmelteryComponentBlockEntity::new, java.util.stream.Stream.concat(
                    BLOCKS.values().stream().filter(block -> block != CONTROLLER && block != TINKER_TANK && block != FURNACE),
                    SEARED_SHAPES.values().stream()).map(Supplier::get).toArray(Block[]::new)).build(null));
    public static final Supplier<MenuType<SmelteryMenu>> MENU = MENUS.register("smeltery", () -> IMenuTypeExtension.create(SmelteryMenu::new));
    public static final Supplier<MenuType<SmelteryMenu>> FURNACE_MENU = MENUS.register("seared_furnace", () -> IMenuTypeExtension.create(SmelteryMenu::furnace));
    public static final Supplier<MenuType<TinkerTankMenu>> TINKER_TANK_MENU = MENUS.register("tinker_tank", () -> IMenuTypeExtension.create(TinkerTankMenu::new));
    public static final Supplier<SoundEvent> LOOP = sound("smeltery_loop");
    public static final Supplier<SoundEvent> POUR = sound("faucet_pour");
    public static final Supplier<SoundEvent> TRIGGER = sound("faucet_trigger");
    public static final Supplier<SoundEvent> SIZZLE = sound("sizzle");

    private SmelteryModule() {
    }

    private static Supplier<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(SmelteryFluids.id(name)));
    }

    private static DeferredBlock<SmelteryBlock> block(String name, SmelteryBlock.Kind kind) {
        DeferredBlock<SmelteryBlock> block = BlockRegister.registerBlockWithItem(name,
                // 旧版 setResistance(20) 先乘三再除五，现代属性直接保存最终抗爆值 12。
                () -> createBlock(kind, BlockBehaviour.Properties.of().strength(3.0F, 12.0F).requiresCorrectToolForDrops()
                        .sound(kind == SmelteryBlock.Kind.TABLE || kind == SmelteryBlock.Kind.BASIN ? SoundType.STONE : SoundType.METAL)
                        // 流道等专用方块没有激活属性，先判断控制器种类再读取状态。
                        .lightLevel(state -> (kind == SmelteryBlock.Kind.FURNACE || kind == SmelteryBlock.Kind.CONTROLLER)
                                && state.getValue(SmelteryBlock.ACTIVE) ? kind == SmelteryBlock.Kind.FURNACE ? 15 : 13 : 0)));
        BLOCKS.put(name, block);
        return block;
    }

    private static SmelteryBlock createBlock(SmelteryBlock.Kind kind, BlockBehaviour.Properties properties) {
        // 完整焦黑墙壁应遮光；透明玻璃、储罐和非完整模型保留各自的物理行为。
        switch (kind) {
            case GLASS, TANK -> properties.noOcclusion().isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false);
            case TABLE, BASIN, FAUCET, CHANNEL, LADDER -> properties.noOcclusion().isValidSpawn((state, level, pos, type) -> false);
            default -> { }
        }
        return switch (kind) {
            case CHANNEL -> new ChannelBlock(properties);
            case GLASS -> new SearedGlassBlock(properties);
            case LADDER -> new SearedLadderBlock(properties);
            default -> new SmelteryBlock(kind, properties);
        };
    }

    public static Collection<DeferredBlock<?>> entries() {
        Collection<DeferredBlock<?>> entries = new ArrayList<>(BLOCKS.values());
        entries.addAll(SEARED_SHAPES.values());
        return entries;
    }

    public static Item item(String name) {
        return ITEMS.get(name).get();
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        MENUS.register(bus);
        LOOT_CONDITIONS.register(bus);
        SOUNDS.register(bus);
        COMPONENTS.register(bus);
        TABS.register(bus);
        SmelteryFluids.register(bus);
        bus.addListener(SmelteryModule::capabilities);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes::initialize));
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        // Only drains expose the shared liquids. Component wrappers resolve their master on every call,
        // so automation cannot keep operating on an invalid or replaced controller through a cached handle.
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COMPONENT_ENTITY.get(), (entity, side) -> entity.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CONTROLLER_ENTITY.get(), (entity, side) -> entity.itemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FURNACE_ENTITY.get(), (entity, side) -> entity.itemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, COMPONENT_ENTITY.get(), (entity, side) -> entity.itemHandler(side));
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) ->
                new net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack(TANK_FLUID, stack, 4000),
                TANK.get().asItem(), GAUGE.get().asItem(), WINDOW.get().asItem());
    }
}
