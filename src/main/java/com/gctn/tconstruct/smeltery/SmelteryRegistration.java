package com.gctn.tconstruct.smeltery; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.init.BlockRegister; // 引入 com.gctn.tconstruct.init.BlockRegister，供本文件使用。
import com.gctn.tconstruct.init.ItemRegister; // 引入 com.gctn.tconstruct.init.ItemRegister，供本文件使用。
import com.gctn.tconstruct.smeltery.block.*; // 引入 com.gctn.tconstruct.smeltery.block.*，供本文件使用。
import com.gctn.tconstruct.smeltery.block.entity.*; // 引入 com.gctn.tconstruct.smeltery.block.entity.*，供本文件使用。
import com.gctn.tconstruct.smeltery.menu.SmelteryMenu; // 引入 com.gctn.tconstruct.smeltery.menu.SmelteryMenu，供本文件使用。
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipe; // 引入 com.gctn.tconstruct.smeltery.recipe.SmelteryRecipe，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.resources.ResourceLocation; // 引入 net.minecraft.resources.ResourceLocation，供本文件使用。
import net.minecraft.tags.TagKey; // 引入 net.minecraft.tags.TagKey，供本文件使用。
import net.minecraft.world.flag.FeatureFlags; // 引入 net.minecraft.world.flag.FeatureFlags，供本文件使用。
import net.minecraft.world.inventory.MenuType; // 引入 net.minecraft.world.inventory.MenuType，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.item.crafting.RecipeSerializer; // 引入 net.minecraft.world.item.crafting.RecipeSerializer，供本文件使用。
import net.minecraft.world.item.crafting.RecipeType; // 引入 net.minecraft.world.item.crafting.RecipeType，供本文件使用。
import net.minecraft.world.level.block.Block; // 引入 net.minecraft.world.level.block.Block，供本文件使用。
import net.minecraft.world.level.block.SoundType; // 引入 net.minecraft.world.level.block.SoundType，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntityType; // 引入 net.minecraft.world.level.block.entity.BlockEntityType，供本文件使用。
import net.minecraft.world.level.block.state.BlockBehaviour; // 引入 net.minecraft.world.level.block.state.BlockBehaviour，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.capabilities.Capabilities; // 引入 net.neoforged.neoforge.capabilities.Capabilities，供本文件使用。
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent; // 引入 net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent，供本文件使用。
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension; // 引入 net.neoforged.neoforge.common.extensions.IMenuTypeExtension，供本文件使用。
import net.neoforged.neoforge.registries.DeferredBlock; // 引入 net.neoforged.neoforge.registries.DeferredBlock，供本文件使用。
import net.neoforged.neoforge.registries.DeferredItem; // 引入 net.neoforged.neoforge.registries.DeferredItem，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。
import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

/**
 * 注册冶炼炉的方块、物品、方块实体、配方、菜单和自动化能力。
 * 方块与物品共用通用注册器，机器类型使用各自的延迟注册器。
 * 能力适配器每次解析当前归属，避免持有已经失效的多方块结构引用。
 */
public final class SmelteryRegistration { // 定义 SmelteryRegistration 的实现类型，具体职责见上方说明。
    public static final TagKey<Block> FLOOR = TagKey.create(Registries.BLOCK, id("smeltery_floor")); // 设置 FLOOR 为 TagKey.create(Registries.BLOCK, id("smeltery_floor"))。
    public static final TagKey<Block> WALL = TagKey.create(Registries.BLOCK, id("smeltery_wall")); // 设置 WALL 为 TagKey.create(Registries.BLOCK, id("smeltery_wall"))。
    public static final DeferredBlock<Block> GROUT = BlockRegister.registerBlockWithItem("grout", () -> new Block(BlockBehaviour.Properties.of().strength(1).sound(SoundType.GRAVEL))); // 设置 GROUT 为 BlockRegister.registerBlockWithItem("grout", () -> new Block(BlockBehaviour.Properties.of().strength(1).sou…。
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_BRICKS = component("seared_bricks", SmelteryComponentBlock.Kind.BRICK); // 设置 SEARED_BRICKS 为 component("seared_bricks", SmelteryComponentBlock.Kind.BRICK)。
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_STONE = component("seared_stone", SmelteryComponentBlock.Kind.BRICK); // 设置 SEARED_STONE 为 component("seared_stone", SmelteryComponentBlock.Kind.BRICK)。
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_GLASS = component("seared_glass", SmelteryComponentBlock.Kind.GLASS); // 设置 SEARED_GLASS 为 component("seared_glass", SmelteryComponentBlock.Kind.GLASS)。
    public static final DeferredBlock<SmelteryComponentBlock> SEARED_TANK = component("seared_tank", SmelteryComponentBlock.Kind.TANK); // 设置 SEARED_TANK 为 component("seared_tank", SmelteryComponentBlock.Kind.TANK)。
    public static final DeferredBlock<SmelteryComponentBlock> SMELTERY_DRAIN = component("smeltery_drain", SmelteryComponentBlock.Kind.DRAIN); // 设置 SMELTERY_DRAIN 为 component("smeltery_drain", SmelteryComponentBlock.Kind.DRAIN)。
    public static final DeferredBlock<SmelteryControllerBlock> CONTROLLER = BlockRegister.registerBlockWithItem("smeltery_controller", () -> new SmelteryControllerBlock(properties().lightLevel(s -> s.getValue(SmelteryControllerBlock.ACTIVE) ? 13 : 0))); // 设置 CONTROLLER 为 BlockRegister.registerBlockWithItem("smeltery_controller", () -> new SmelteryControllerBlock(properties().l…。
    public static final DeferredBlock<FaucetBlock> FAUCET = BlockRegister.registerBlockWithItem("faucet", () -> new FaucetBlock(properties().noOcclusion())); // 设置 FAUCET 为 BlockRegister.registerBlockWithItem("faucet", () -> new FaucetBlock(properties().noOcclusion()))。
    public static final DeferredBlock<CastingBlock> CASTING_TABLE = BlockRegister.registerBlockWithItem("casting_table", () -> new CastingBlock(properties().noOcclusion(), false)); // 设置 CASTING_TABLE 为 BlockRegister.registerBlockWithItem("casting_table", () -> new CastingBlock(properties().noOcclusion(), fal…。
    public static final DeferredBlock<CastingBlock> CASTING_BASIN = BlockRegister.registerBlockWithItem("casting_basin", () -> new CastingBlock(properties().noOcclusion(), true)); // 设置 CASTING_BASIN 为 BlockRegister.registerBlockWithItem("casting_basin", () -> new CastingBlock(properties().noOcclusion(), true))。
    public static final DeferredItem<Item> SEARED_BRICK = ItemRegister.ITEMS.registerSimpleItem("seared_brick"); // 设置 SEARED_BRICK 为 ItemRegister.ITEMS.registerSimpleItem("seared_brick")。
    public static final DeferredItem<Item> INGOT_CAST = ItemRegister.ITEMS.registerSimpleItem("ingot_cast"); // 设置 INGOT_CAST 为 ItemRegister.ITEMS.registerSimpleItem("ingot_cast")。
    public static final DeferredItem<Item> COBALT_INGOT = ItemRegister.ITEMS.registerSimpleItem("cobalt_ingot"); // 设置 COBALT_INGOT 为 ItemRegister.ITEMS.registerSimpleItem("cobalt_ingot")。
    public static final DeferredItem<Item> COBALT_NUGGET = ItemRegister.ITEMS.registerSimpleItem("cobalt_nugget"); // 设置 COBALT_NUGGET 为 ItemRegister.ITEMS.registerSimpleItem("cobalt_nugget")。
    public static final DeferredBlock<Block> COBALT_BLOCK = BlockRegister.registerBlockWithItem("cobalt_block", () -> new Block(properties())); // 设置 COBALT_BLOCK 为 BlockRegister.registerBlockWithItem("cobalt_block", () -> new Block(properties()))。

    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID); // 设置 ENTITIES 为 DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID)。
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID); // 设置 MENUS 为 DeferredRegister.create(Registries.MENU, TinkersNirvana.MODID)。
    private static final DeferredRegister<RecipeType<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_TYPE, TinkersNirvana.MODID); // 设置 RECIPES 为 DeferredRegister.create(Registries.RECIPE_TYPE, TinkersNirvana.MODID)。
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID); // 设置 SERIALIZERS 为 DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID)。
    public static final Supplier<BlockEntityType<SmelteryControllerBlockEntity>> CONTROLLER_ENTITY = ENTITIES.register("smeltery", () -> BlockEntityType.Builder.of(SmelteryControllerBlockEntity::new, CONTROLLER.get()).build(null)); // 设置 CONTROLLER_ENTITY 为 ENTITIES.register("smeltery", () -> BlockEntityType.Builder.of(SmelteryControllerBlockEntity::new, CONTROLL…。
    public static final Supplier<BlockEntityType<SmelteryComponentBlockEntity>> COMPONENT_ENTITY = ENTITIES.register("smeltery_component", () -> BlockEntityType.Builder.of(SmelteryComponentBlockEntity::new, SEARED_BRICKS.get(), SEARED_STONE.get(), SEARED_GLASS.get(), SMELTERY_DRAIN.get()).build(null)); // 设置 COMPONENT_ENTITY 为 ENTITIES.register("smeltery_component", () -> BlockEntityType.Builder.of(SmelteryComponentBlockEntity::new,…。
    public static final Supplier<BlockEntityType<SearedTankBlockEntity>> TANK_ENTITY = ENTITIES.register("seared_tank", () -> BlockEntityType.Builder.of(SearedTankBlockEntity::new, SEARED_TANK.get()).build(null)); // 设置 TANK_ENTITY 为 ENTITIES.register("seared_tank", () -> BlockEntityType.Builder.of(SearedTankBlockEntity::new, SEARED_TANK.g…。
    public static final Supplier<BlockEntityType<CastingBlockEntity>> CASTING_ENTITY = ENTITIES.register("casting", () -> BlockEntityType.Builder.of(CastingBlockEntity::new, CASTING_TABLE.get(), CASTING_BASIN.get()).build(null)); // 设置 CASTING_ENTITY 为 ENTITIES.register("casting", () -> BlockEntityType.Builder.of(CastingBlockEntity::new, CASTING_TABLE.get(),…。
    public static final Supplier<BlockEntityType<FaucetBlockEntity>> FAUCET_ENTITY = ENTITIES.register("faucet", () -> BlockEntityType.Builder.of(FaucetBlockEntity::new, FAUCET.get()).build(null)); // 设置 FAUCET_ENTITY 为 ENTITIES.register("faucet", () -> BlockEntityType.Builder.of(FaucetBlockEntity::new, FAUCET.get()).build(nu…。
    public static final Supplier<MenuType<SmelteryMenu>> MENU = MENUS.register("smeltery", () -> IMenuTypeExtension.create(SmelteryMenu::new)); // 设置 MENU 为 MENUS.register("smeltery", () -> IMenuTypeExtension.create(SmelteryMenu::new))。
    public static final Supplier<RecipeType<SmelteryRecipe>> RECIPE_TYPE = RECIPES.register("smeltery", () -> new RecipeType<>() { @Override public String toString() { return "tconstruct:smeltery"; } }); // 设置 RECIPE_TYPE 为 RECIPES.register("smeltery", () -> new RecipeType<>() { @Override public String toString() { return "tconst…。
    public static final Supplier<RecipeSerializer<SmelteryRecipe>> RECIPE_SERIALIZER = SERIALIZERS.register("smeltery", SmelteryRecipe.Serializer::new); // 设置 RECIPE_SERIALIZER 为 SERIALIZERS.register("smeltery", SmelteryRecipe.Serializer::new)。

    private static BlockBehaviour.Properties properties() { return BlockBehaviour.Properties.of().strength(3, 20).sound(SoundType.METAL).requiresCorrectToolForDrops(); } // 定义 properties 的调用入口；返回 BlockBehaviour.Properties.of().strength(3, 20).sound(SoundType.METAL).requiresCorrectToolForDrops()，将当前分支的结果交给调用者。
    private static DeferredBlock<SmelteryComponentBlock> component(String name, SmelteryComponentBlock.Kind kind) { // 定义 component 的调用入口，参数和具体处理见下方实现。
        return BlockRegister.registerBlockWithItem(name, () -> new SmelteryComponentBlock( // 返回 BlockRegister.registerBlockWithItem(name, () -> new SmelteryComponentBlock(，将当前分支的结果交给调用者。
                kind == SmelteryComponentBlock.Kind.GLASS || kind == SmelteryComponentBlock.Kind.TANK ? properties().noOcclusion() : properties(), kind)); // 设置 kind 为 = SmelteryComponentBlock.Kind.GLASS || kind == SmelteryComponentBlock.Kind.TANK ? properties().noOcclusion(…。
    } // 结束当前作用域。
    public static List<DeferredBlock<?>> blocks() { return List.of(GROUT, SEARED_BRICKS, SEARED_STONE, SEARED_GLASS, SEARED_TANK, SMELTERY_DRAIN, CONTROLLER, FAUCET, CASTING_TABLE, CASTING_BASIN, COBALT_BLOCK); } // 定义 blocks 的调用入口；返回 List.of(GROUT, SEARED_BRICKS, SEARED_STONE, SEARED_GLASS, SEARED_TANK, SMELTERY_DRAIN, CONTROLLER, FAUCET, …，将当前分支的结果交给调用者。
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, path); } // 定义 id 的调用入口；返回 ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, path)，将当前分支的结果交给调用者。

    public static void register(IEventBus bus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        ENTITIES.register(bus); // 登记对象工厂或事件处理器。
        MENUS.register(bus); // 登记对象工厂或事件处理器。
        RECIPES.register(bus); // 登记对象工厂或事件处理器。
        SERIALIZERS.register(bus); // 登记对象工厂或事件处理器。
        SmelteryFluids.register(bus); // 登记对象工厂或事件处理器。
        bus.addListener(SmelteryRegistration::capabilities); // 调用 addListener，执行当前流程所需的处理。
    } // 结束当前作用域。

    private static void capabilities(RegisterCapabilitiesEvent event) { // 定义 capabilities 的调用入口，参数和具体处理见下方实现。
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TANK_ENTITY.get(), (tank, side) -> tank.tank()); // 定义回调或映射规则，在对应条目被处理时执行。
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, COMPONENT_ENTITY.get(), (part, side) -> part.getBlockState().is(SMELTERY_DRAIN.get()) ? part.drainHandler() : null); // 定义回调或映射规则，在对应条目被处理时执行。
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CONTROLLER_ENTITY.get(), (controller, side) -> controller.itemHandler()); // 定义回调或映射规则，在对应条目被处理时执行。
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, CASTING_ENTITY.get(), (casting, side) -> casting.fluidHandler()); // 定义回调或映射规则，在对应条目被处理时执行。
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CASTING_ENTITY.get(), (casting, side) -> casting.automation()); // 定义回调或映射规则，在对应条目被处理时执行。
    } // 结束当前作用域。
} // 结束当前作用域。
