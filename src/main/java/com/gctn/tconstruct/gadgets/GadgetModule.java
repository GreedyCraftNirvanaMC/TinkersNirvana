/*
 * 根据 TinkersAntique 的 TinkerGadgets 和石制、褐砂石方块移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.block.BrownstoneBlock;
import com.gctn.tconstruct.gadgets.block.BrownstoneSlabBlock;
import com.gctn.tconstruct.gadgets.block.BrownstoneStairBlock;
import com.gctn.tconstruct.gadgets.block.WoodenHopperBlock;
import com.gctn.tconstruct.gadgets.block.PunjiBlock;
import com.gctn.tconstruct.gadgets.block.WoodenRailBlock;
import com.gctn.tconstruct.gadgets.block.TrapdoorRailBlock;
import com.gctn.tconstruct.gadgets.block.entity.WoodenHopperBlockEntity;
import com.gctn.tconstruct.gadgets.menu.WoodenHopperMenu;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** 移植的装饰、物流与陷阱方块，以及相应物品和菜单。 */
public final class GadgetModule {
    public static final Map<String, DeferredBlock<?>> BLOCKS = new LinkedHashMap<>();
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID);
    public static final DeferredBlock<WoodenHopperBlock> WOODEN_HOPPER = BlockRegister.registerBlockWithItem("wooden_hopper", () ->
            new WoodenHopperBlock(BlockBehaviour.Properties.of().strength(3, 4.8F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<Block> FIREWOOD = BlockRegister.registerBlockWithItem("firewood", () ->
            new Block(BlockBehaviour.Properties.of().strength(2, 4.2F).sound(SoundType.WOOD).lightLevel(state -> 7)));
    public static final DeferredBlock<PunjiBlock> PUNJI = BlockRegister.registerBlockWithItem("punji", () ->
            new PunjiBlock(BlockBehaviour.Properties.of().strength(3).sound(SoundType.GRASS).noOcclusion()));
    public static final DeferredBlock<WoodenRailBlock> WOODEN_RAIL = BlockRegister.registerBlockWithItem("wood_rail", () ->
            new WoodenRailBlock(railProperties()));
    public static final DeferredBlock<TrapdoorRailBlock> TRAPDOOR_RAIL = BlockRegister.registerBlockWithItem("wood_rail_trapdoor", () ->
            new TrapdoorRailBlock(railProperties()));
    public static final Supplier<BlockEntityType<WoodenHopperBlockEntity>> WOODEN_HOPPER_ENTITY = ENTITIES.register("wooden_hopper", () ->
            BlockEntityType.Builder.of(WoodenHopperBlockEntity::new, WOODEN_HOPPER.get()).build(null));
    public static final Supplier<MenuType<WoodenHopperMenu>> WOODEN_HOPPER_MENU = MENUS.register("wooden_hopper", () ->
            new MenuType<>(WoodenHopperMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<Item> STONE_ROD = ItemRegister.ITEMS.register("stone_stick", () -> new Item(new Item.Properties()));
    public static final DeferredBlock<TorchBlock> STONE_TORCH = BlockRegister.BLOCKS.register("stone_torch", () ->
            new TorchBlock(ParticleTypes.FLAME, torchProperties()));
    public static final DeferredBlock<WallTorchBlock> STONE_WALL_TORCH = BlockRegister.BLOCKS.register("stone_wall_torch", () ->
            new WallTorchBlock(ParticleTypes.FLAME, torchProperties().dropsLike(STONE_TORCH.get())));
    public static final Supplier<Item> STONE_TORCH_ITEM = ItemRegister.ITEMS.register("stone_torch", () ->
            new StandingAndWallBlockItem(STONE_TORCH.get(), STONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final DeferredBlock<LadderBlock> STONE_LADDER = BlockRegister.registerBlockWithItem("stone_ladder", () ->
            new LadderBlock(BlockBehaviour.Properties.of().strength(0.1F).sound(SoundType.STONE)
                    .noCollission().noOcclusion().pushReaction(PushReaction.DESTROY)));

    static {
        BLOCKS.put("wooden_hopper", WOODEN_HOPPER);
        BLOCKS.put("punji", PUNJI);
        BLOCKS.put("wood_rail", WOODEN_RAIL);
        BLOCKS.put("wood_rail_trapdoor", TRAPDOOR_RAIL);
        BLOCKS.put("firewood", FIREWOOD);
        addBuildingShapes("lavawood", SmelteryModule.LAVAWOOD);
        addBuildingShapes("firewood", FIREWOOD);
        addBuildingShapes("mud_bricks", SmelteryModule.MUD_BRICKS);
        BLOCKS.put("stone_torch", STONE_TORCH);
        BLOCKS.put("stone_wall_torch", STONE_WALL_TORCH);
        BLOCKS.put("stone_ladder", STONE_LADDER);
        for (String type : new String[]{"smooth", "rough", "paver", "brick", "brick_cracked", "brick_fancy",
                "brick_square", "brick_triangle", "brick_small", "road", "tile", "creeper"}) {
            String name = "brownstone_" + type;
            var base = BlockRegister.registerBlockWithItem(name, () ->
                    new BrownstoneBlock(BlockBehaviour.Properties.of().strength(3, 12).sound(SoundType.STONE).requiresCorrectToolForDrops()));
            BLOCKS.put(name, base);
            BLOCKS.put(name + "_slab", BlockRegister.registerBlockWithItem(name + "_slab", () ->
                    new BrownstoneSlabBlock(BlockBehaviour.Properties.ofFullCopy(base.get()))));
            BLOCKS.put(name + "_stairs", BlockRegister.registerBlockWithItem(name + "_stairs", () ->
                    new BrownstoneStairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(base.get()))));
        }
    }

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersNirvana.MODID);
    public static final Supplier<CreativeModeTab> TAB = TABS.register("gadgets", () -> CreativeModeTab.builder()
            .title(Component.translatable("tab.tconstruct.gadgets"))
            .icon(() -> STONE_TORCH_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                BLOCKS.forEach((name, block) -> {
                    if (!name.equals("stone_wall_torch")) {
                        output.accept(block.get());
                    }
                });
                output.accept(STONE_ROD.get());
            }).build());

    private GadgetModule() {
    }

    private static BlockBehaviour.Properties torchProperties() {
        // 石柄火把与原生火把共用放置、掉落、火焰粒子和亮度规则。
        return BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(state -> 14)
                .sound(SoundType.STONE).pushReaction(PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties railProperties() {
        return BlockBehaviour.Properties.of().noCollission().noOcclusion().strength(0.2F).sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }

    private static void addBuildingShapes(String name, DeferredBlock<Block> base) {
        BLOCKS.put(name, base);
        BLOCKS.put(name + "_slab", BlockRegister.registerBlockWithItem(name + "_slab", () ->
                new SlabBlock(BlockBehaviour.Properties.ofFullCopy(base.get()))));
        // 原版楼梯继承硬度和声音，但不继承基础方块的亮度；台阶仍保留七级光照。
        BLOCKS.put(name + "_stairs", BlockRegister.registerBlockWithItem(name + "_stairs", () ->
                new StairBlock(base.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(base.get()).lightLevel(state -> 0))));
    }

    public static Collection<DeferredBlock<?>> entries() {
        return new ArrayList<>(BLOCKS.values());
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
        ENTITIES.register(bus);
        MENUS.register(bus);
        bus.addListener(GadgetModule::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        // 木漏斗在所有面提供同一单槽物品能力，原生漏斗与模组管道均可连接。
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, WOODEN_HOPPER_ENTITY.get(), (entity, side) -> entity.itemHandler());
    }
}
