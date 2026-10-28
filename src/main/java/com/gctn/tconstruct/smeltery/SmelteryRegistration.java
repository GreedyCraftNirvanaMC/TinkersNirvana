package com.gctn.tconstruct.smeltery;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.smeltery.block.*;
import com.gctn.tconstruct.smeltery.block.entity.*;
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * Registers smeltery blocks, items, entities, recipes, menus and automation capabilities.
 * Block/item entries share the common registers; machine types have their own deferred registries.
 * Capability adapters resolve current ownership rather than holding stale multiblock references.
 */
public final class SmelteryRegistration {
    public static final TagKey<Block> FLOOR = TagKey.create(Registries.BLOCK, id("smeltery_floor"));
    public static final TagKey<Block> WALL = TagKey.create(Registries.BLOCK, id("smeltery_wall"));
    public static final DeferredBlock<Block> GROUT = BlockRegister.registerBlockWithItem("grout", () -> new Block(BlockBehaviour.Properties.of().strength(1).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_BRICKS = component("seared_bricks", SmelteryComponentBlock.Kind.BRICK);
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_STONE = component("seared_stone", SmelteryComponentBlock.Kind.BRICK);
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_GLASS = component("seared_glass", SmelteryComponentBlock.Kind.GLASS);
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_TANK = component("seared_tank", SmelteryComponentBlock.Kind.TANK);
    public static final DeferredBlock<SmelteryComponentBlock> SMELTERY_DRAIN = component("smeltery_drain", SmelteryComponentBlock.Kind.DRAIN);
    public static final DeferredBlock<SmelteryControllerBlock> CONTROLLER = BlockRegister.registerBlockWithItem("smeltery_controller", () -> new SmelteryControllerBlock(properties().lightLevel(s -> s.getValue(SmelteryControllerBlock.ACTIVE) ? 13 : 0)));
    public static final DeferredBlock<FaucetBlock> FAUCET = BlockRegister.registerBlockWithItem("faucet", () -> new FaucetBlock(properties().noOcclusion()));
    public static final DeferredBlock<CastingBlock> CASTING_TABLE = BlockRegister.registerBlockWithItem("casting_table", () -> new CastingBlock(properties().noOcclusion(), false));
    public static final DeferredBlock<CastingBlock> CASTING_BASIN = BlockRegister.registerBlockWithItem("casting_basin", () -> new CastingBlock(properties().noOcclusion(), true));
    public static final DeferredItem<Item> SEARED_BRICK = ItemRegister.ITEMS.registerSimpleItem("seared_brick");
    public static final DeferredItem<Item> INGOT_CAST = ItemRegister.ITEMS.registerSimpleItem("ingot_cast");
    public static final DeferredItem<Item> COBALT_INGOT = ItemRegister.ITEMS.registerSimpleItem("cobalt_ingot");
    public static final DeferredItem<Item> COBALT_NUGGET = ItemRegister.ITEMS.registerSimpleItem("cobalt_nugget");
    public static final DeferredBlock<Block> COBALT_BLOCK = BlockRegister.registerBlockWithItem("cobalt_block", () -> new Block(properties()));

    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID);
    private static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_TYPE, TinkersNirvana.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID);
    public static final Supplier<BlockEntityType<SmelteryControllerBlockEntity>> CONTROLLER_ENTITY = ENTITIES.register("smeltery", () -> BlockEntityType.Builder.of(SmelteryControllerBlockEntity::new, CONTROLLER.get()).build(null));
    public static final Supplier<BlockEntityType<SmelteryComponentBlockEntity>> COMPONENT_ENTITY = ENTITIES.register("smeltery_component", () -> BlockEntityType.Builder.of(SmelteryComponentBlockEntity::new, SEARED_BRICKS.get(), SEARED_STONE.get(), SEARED_GLASS.get(), SMELTERY_DRAIN.get()).build(null));
    public static final Supplier<BlockEntityType<SearedTankBlockEntity>> TANK_ENTITY = ENTITIES.register("seared_tank", () -> BlockEntityType.Builder.of(SearedTankBlockEntity::new, SEARED_TANK.get()).build(null));
    public static final Supplier<BlockEntityType<CastingBlockEntity>> CASTING_ENTITY = ENTITIES.register("casting", () -> BlockEntityType.Builder.of(CastingBlockEntity::new, CASTING_TABLE.get(), CASTING_BASIN.get()).build(null));
    public static final Supplier<BlockEntityType<FaucetBlockEntity>> FAUCET_ENTITY = ENTITIES.register("faucet", () -> BlockEntityType.Builder.of(FaucetBlockEntity::new, FAUCET.get()).build(null));
    public static final Supplier<MenuType<SmelteryMenu>> MENU = MENUS.register("smeltery", () -> IMenuTypeExtension.create(SmelteryMenu::new));
    public static final Supplier<RecipeType<SmelteryRecipe>> RECIPE_TYPE = RECIPES.register("smeltery", () -> new RecipeType<>() { @Override public String toString() { return "tconstruct:smeltery"; } });
    public static final Supplier<RecipeSerializer<SmelteryRecipe>> RECIPE_SERIALIZER = SERIALIZERS.register("smeltery", SmelteryRecipe.Serializer::new);

    private static BlockBehaviour.Properties properties() { return BlockBehaviour.Properties.of().strength(3, 20).sound(SoundType.METAL).requiresCorrectToolForDrops(); }
    private static DeferredBlock<SmelteryComponentBlock> component(String name, SmelteryComponentBlock.Kind kind) {
        return BlockRegister.registerBlockWithItem(name, () -> new SmelteryComponentBlock(
                kind == SmelteryComponentBlock.Kind.GLASS || kind == SmelteryComponentBlock.Kind.TANK ? properties().noOcclusion() : properties(), kind));
    }
    public static List<DeferredBlock<?>> blocks() { return List.of(GROUT, SEARED_BRICKS, SEARED_STONE, SEARED_GLASS, SEARED_TANK, SMELTERY_DRAIN, CONTROLLER, FAUCET, CASTING_TABLE, CASTING_BASIN, COBALT_BLOCK); }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, path); }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        MENUS.register(bus);
        RECIPES.register(bus);
        SERIALIZERS.register(bus);
        SmelteryFluids.register(bus);
        bus.addListener(SmelteryRegistration::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TANK_ENTITY.get(), (tank, side) -> tank.tank());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COMPONENT_ENTITY.get(), (part, side) -> part.getBlockState().is(SMELTERY_DRAIN.get()) ? part.drainHandler() : null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CONTROLLER_ENTITY.get(), (controller, side) -> controller.itemHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CASTING_ENTITY.get(), (casting, side) -> casting.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CASTING_ENTITY.get(), (casting, side) -> casting.automation());
    }
}
