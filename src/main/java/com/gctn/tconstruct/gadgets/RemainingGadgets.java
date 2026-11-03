/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's gadgets and edible registrations. */
package com.gctn.tconstruct.gadgets;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.block.RackBlock;
import com.gctn.tconstruct.gadgets.block.entity.RackBlockEntity;
import com.gctn.tconstruct.gadgets.recipe.DryingRecipe;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class RemainingGadgets {
    public static final Map<String, Supplier<Item>> ITEMS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<?>> BUILDING_BLOCKS = new LinkedHashMap<>();
    public static final DeferredBlock<RackBlock> ITEM_RACK = BlockRegister.registerBlockWithItem("item_rack", () -> new RackBlock(false, rack()));
    public static final DeferredBlock<RackBlock> DRYING_RACK = BlockRegister.registerBlockWithItem("drying_rack", () -> new RackBlock(true, rack()));
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID);
    public static final Supplier<BlockEntityType<RackBlockEntity>> RACK_ENTITY = ENTITIES.register("rack", () -> BlockEntityType.Builder.of(RackBlockEntity::new, ITEM_RACK.get(), DRYING_RACK.get()).build(null));
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TinkersNirvana.MODID);
    public static final Supplier<RecipeType<DryingRecipe>> DRYING_TYPE = TYPES.register("drying", () -> new RecipeType<>() { @Override public String toString() { return "tconstruct:drying"; } });
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID);
    public static final Supplier<RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER = SERIALIZERS.register("drying", DryingRecipe.Serializer::new);
    static {
        ITEMS.put("dried_brick", ItemRegister.ITEMS.register("dried_brick", () -> new Item(new Item.Properties())));
        for (String name : new String[]{"dried_clay", "dried_clay_bricks"}) {
            var block = BlockRegister.registerBlockWithItem(name, () -> new net.minecraft.world.level.block.Block(BlockBehaviour.Properties.of().strength(1.5f, 20).sound(SoundType.STONE).requiresCorrectToolForDrops()));
            BUILDING_BLOCKS.put(name, block);
            BUILDING_BLOCKS.put(name + "_slab", BlockRegister.registerBlockWithItem(name + "_slab", () -> new net.minecraft.world.level.block.SlabBlock(BlockBehaviour.Properties.ofFullCopy(block.get()).strength(3, 20))));
            BUILDING_BLOCKS.put(name + "_stairs", BlockRegister.registerBlockWithItem(name + "_stairs", () -> new net.minecraft.world.level.block.StairBlock(block.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(block.get()))));
        }
        String[] meat = {"monster", "beef", "chicken", "pork", "mutton", "rabbit", "fish", "salmon", "clownfish", "pufferfish"};
        int[] nutrition = {4, 8, 6, 8, 6, 5, 5, 6, 3, 3};
        float[] saturation = {.4f, 1, .8f, 1, 1, .8f, .8f, 1, .8f, .8f};
        for (int i = 0; i < meat.length; i++) { food("jerky_" + meat[i], new FoodProperties.Builder().nutrition(nutrition[i]).saturationModifier(saturation[i]).build()); }
        for (String color : new String[]{"green", "blue", "purple", "blood", "magma"}) {
            var effect = switch (color) { case "green" -> MobEffects.MOVEMENT_SPEED; case "blue" -> MobEffects.JUMP; case "purple" -> MobEffects.LUCK; case "blood" -> MobEffects.HEALTH_BOOST; default -> MobEffects.FIRE_RESISTANCE; };
            var properties = new FoodProperties.Builder().nutrition(color.equals("green") ? 1 : color.equals("magma") ? 6 : 3)
                    .saturationModifier(color.equals("purple") ? 2 : color.equals("blood") ? 1.5f : 1).alwaysEdible()
                    .effect(() -> new MobEffectInstance(effect, 1800, color.equals("green") || color.equals("blue") ? 2 : 0), 1).build();
            food("slimedrop_" + color, properties);
        }
    }
    private RemainingGadgets() {}
    private static BlockBehaviour.Properties rack() { return BlockBehaviour.Properties.of().strength(2).sound(SoundType.WOOD).noOcclusion(); }
    private static void food(String id, FoodProperties properties) { ITEMS.put(id, ItemRegister.ITEMS.register(id, () -> new Item(new Item.Properties().food(properties)))); }
    public static void register(IEventBus bus) { ENTITIES.register(bus); TYPES.register(bus); SERIALIZERS.register(bus); bus.addListener(RemainingGadgets::capabilities); }
    private static void capabilities(RegisterCapabilitiesEvent event) { event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RACK_ENTITY.get(), (entity, side) -> entity.itemHandler()); }
}
