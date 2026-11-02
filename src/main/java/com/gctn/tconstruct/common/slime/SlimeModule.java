/*
 * 根据 TinkersAntique 的 TinkerCommons 与史莱姆方块、食物注册移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.slime;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** 普通、凝固史莱姆与对应史莱姆球的统一注册入口。 */
public final class SlimeModule {
    public static final String[] COLORS = {"green", "blue", "purple", "blood", "magma", "pink"};
    public static final Map<String, DeferredBlock<ColoredSlimeBlock>> FULL_BLOCKS = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<CongealedSlimeBlock>> CONGEALED = new LinkedHashMap<>();
    public static final Map<String, Supplier<Item>> BALLS = new LinkedHashMap<>();
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TinkersNirvana.MODID);
    public static final Supplier<RecipeSerializer<SlimeMixingRecipe>> MIXING = RECIPES.register("slime_mixing", () -> new SimpleCraftingRecipeSerializer<>(SlimeMixingRecipe::new));
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersNirvana.MODID);

    static {
        for (String color : COLORS) {
            if (!color.equals("green")) {
                FULL_BLOCKS.put(color, BlockRegister.registerBlockWithItem("slime_" + color, () ->
                        new ColoredSlimeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SLIME_BLOCK))));
                BALLS.put(color, ItemRegister.ITEMS.register("slimeball_" + color, () ->
                        new SlimeballItem(new Item.Properties().food(food(color)))));
            }
            CONGEALED.put(color, BlockRegister.registerBlockWithItem("slime_congealed_" + color, () ->
                    new CongealedSlimeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(0.5F)
                            .friction(0.5F).sound(SoundType.SLIME_BLOCK)
                            .isRedstoneConductor((state, level, pos) -> true)
                            .isSuffocating((state, level, pos) -> true)
                            .isViewBlocking((state, level, pos) -> true))));
        }
        TABS.register("slime", () -> CreativeModeTab.builder().title(Component.translatable("tab.tconstruct.slime"))
                .icon(() -> BALLS.get("blue").get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    // 粉色方块与原版一样隐藏，粉色史莱姆球仍可用于搜索与调试。
                    FULL_BLOCKS.forEach((color, block) -> { if (!color.equals("pink")) { output.accept(block.get()); } });
                    CONGEALED.forEach((color, block) -> { if (!color.equals("pink")) { output.accept(block.get()); } });
                    BALLS.values().forEach(item -> output.accept(item.get()));
                }).build());
    }

    private SlimeModule() {
    }

    public static void register(IEventBus bus) {
        RECIPES.register(bus);
        TABS.register(bus);
    }

    public static Item ball(String color) {
        return color.equals("green") ? Items.SLIME_BALL : BALLS.get(color).get();
    }

    public static Block fullBlock(String color) {
        return color.equals("green") ? Blocks.SLIME_BLOCK : FULL_BLOCKS.get(color).get();
    }

    public static TagKey<Item> ballTag(String color) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "slime_balls/" + color));
    }

    private static FoodProperties food(String color) {
        // Mantle 对含药水效果的食物默认允许满饥饿值食用，饱和度参数为倍率。
        var builder = new FoodProperties.Builder().nutrition(color.equals("magma") ? 2 : 1).alwaysEdible();
        switch (color) {
            case "blue" -> builder.saturationModifier(1)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 900, 2), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, 1200, 2), 1);
            case "purple" -> builder.saturationModifier(2)
                    .effect(() -> new MobEffectInstance(MobEffects.UNLUCK, 900), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.LUCK, 1200), 1);
            case "blood" -> builder.saturationModifier(1.5F)
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 900, 2), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200), 1);
            case "magma" -> builder.saturationModifier(1)
                    .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 900), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.WITHER, 300), 1)
                    .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200), 1);
            case "pink" -> builder.saturationModifier(1)
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 2), 1);
            default -> throw new IllegalArgumentException(color);
        }
        return builder.build();
    }
}
