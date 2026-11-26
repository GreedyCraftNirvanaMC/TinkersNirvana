/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.client.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.*;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.tools.*;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT)
public final class AntiqueToolClient {
    private AntiqueToolClient() {}
    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item event) {
        for (var entry : AntiqueTools.entries().values()) {
            event.register((stack, tint) -> {
                if (tint < 0 || tint >= entry.definition().parts().size()) { return -1; }
                String id = TinkerDataComponents.getMaterialTag(stack).getString(entry.definition().parts().get(tint).key());
                var material = TinkerMaterials.getMaterial(id);
                return material == null ? -1 : material.color;
            }, entry.item().get());
        }
        for (var part : AntiqueParts.entries().values()) {
            if (part.external()) { continue; }
            event.register((stack, tint) -> {
                var data = TinkerDataComponents.getMaterialTag(stack);
                var material = TinkerMaterials.getMaterial(part.id().equals("bolt_core") && tint == 1 ? data.getString("Coating") : data.getString(Tags.PART_MATERIAL));
                return material == null ? -1 : material.color;
            }, part.item().get());
        }
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (var entry : AntiqueTools.entries().values()) {
                var item = entry.item().get();
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "broken"),
                        (stack, level, entity, seed) -> TinkerTools.isBroken(stack) ? 1 : 0);
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
                        (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pull"),
                        (stack, level, entity, seed) -> entity == null || !(item instanceof AntiqueTool tool) ? 0
                                : ToolRanged.drawProgress(tool, stack, stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()));
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("charged"),
                        (stack, level, entity, seed) -> ToolRanged.isLoaded(stack) ? 1 : 0);
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("blocking"),
                        (stack, level, entity, seed) -> entity != null && entity.isBlocking() && entity.getUseItem() == stack ? 1 : 0);
            }
        });
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ToolRanged.PROJECTILE.get(), AntiqueProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof AntiqueTool item && !item.definition().id().equals("pickaxe")) {
            for (var entry : ToolTraits.getTraits(stack)) {
                event.getToolTip().add(entry.trait().getDisplayName(entry.level()));
                if (Screen.hasAltDown()) { if (!entry.trait().getDescription().getString().isBlank()) { event.getToolTip().add(entry.trait().getDescription()); } }
            }
            event.getToolTip().add(TinkerTools.getDurabilityTip(stack));
            if (Screen.hasShiftDown()) {
                event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.tool.modifiers", item.getAvailableModifierSlots(stack)));
                var data = TinkerDataComponents.getMaterialTag(stack);
                for (var part : item.definition().parts()) {
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.part_material",
                            Component.translatable("item.tconsnirvana." + part.part() + ".name"),
                            com.gctn.tconstruct.library.materials.Material.nameOf(data.getString(part.key()))));
                }
                event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.tool.miningspeed", ToolBuilder.savedStats(stack).getFloat("MiningSpeed")));
                event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.tool.mininglevel",
                        com.gctn.tconstruct.library.utils.HarvestLevels.getHarvestLevelName(ToolBuilder.savedStats(stack).getInt("HarvestLevel"))));
            } else if (Screen.hasControlDown()) {
                appendComponentDetails(stack, item.definition(), event.getToolTip()::add);
            } else {
                event.getToolTip().add(TinkerTools.shiftTip);
                event.getToolTip().add(TinkerTools.controlTip);
                event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.hold_for_traits", "Alt"));
            }
        }
        if (stack.getItem() instanceof AntiqueTool) {
            var materialData = TinkerDataComponents.getMaterialTag(stack);
            if (materialData.contains("FortifiedTier")) {
                Component tier = com.gctn.tconstruct.library.utils.HarvestLevels.getHarvestLevelName(materialData.getInt("FortifiedTier"));
                String material = materialData.getString("FortifiedMaterial");
                event.getToolTip().add(material.isEmpty() ? tier : Component.translatable("tooltip.tconsnirvana.fortified", com.gctn.tconstruct.library.materials.Material.nameOf(material), tier));
            }
            for (String id : ToolModifiers.entries().keySet()) {
                int units = ToolModifiers.units(stack, id);
                if (units > 0) { event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.modifier",
                        Component.translatable("modifier.tconsnirvana." + id), ToolModifiers.level(stack, id), units)); }
            }
        }
        var part = AntiqueParts.get(stack);
        if (part != null && part.external()) { return; }
        if (part != null && part.id().equals("bolt_core")) {
            String coating = TinkerDataComponents.getMaterialTag(stack).getString("Coating");
            if (!coating.isEmpty()) { event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.coating", com.gctn.tconstruct.library.materials.Material.nameOf(coating))); }
        }
        if (part != null && !java.util.Set.of("tool_rod", "binding").contains(part.id())) {
            var material = TinkerMaterials.getMaterial(TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL));
            if (material != null && Screen.hasShiftDown()) {
                var head = material.getStats("Head", com.gctn.tconstruct.library.stats.HeadMaterialStats.class);
                if (head != null && (part.stat().equals("Head") || part.stat().equals("Bow"))) {
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.part_durability", head.durability));
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.tool.miningspeed", head.miningspeed));
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.tool.attack", head.attack));
                }
                var bow = material.getStats("Bow", com.gctn.tconstruct.library.stats.BowMaterialStats.class);
                if (bow != null && part.stat().equals("Bow")) {
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.bow_stats", bow.drawSpeed, bow.range, bow.bonusDamage));
                }
                var shaft = material.getStats("Shaft", com.gctn.tconstruct.library.stats.ArrowShaftMaterialStats.class);
                if (shaft != null && part.stat().equals("Shaft")) {
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.shaft_stats", shaft.modifier, shaft.bonusAmmo));
                }
                var fletching = material.getStats("Fletching", com.gctn.tconstruct.library.stats.FletchingMaterialStats.class);
                if (fletching != null && part.stat().equals("Fletching")) {
                    event.getToolTip().add(Component.translatable("tooltip.tconsnirvana.fletching_stats", fletching.accuracy, fletching.modifier));
                }
            }
        }
        if (part != null && !java.util.Set.of("tool_rod", "pickaxe_head", "binding").contains(part.id())) {
            var material = TinkerMaterials.getMaterial(TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL));
            if (material != null) {
                for (var entry : material.getTraitsForStats(part.stat().equals("Bow") ? java.util.List.of("Bow", "Head") : java.util.List.of(part.stat()))) {
                    event.getToolTip().add(entry.trait().getDisplayName(entry.level()));
                    if (Screen.hasAltDown()) { if (!entry.trait().getDescription().getString().isBlank()) { event.getToolTip().add(entry.trait().getDescription()); } }
                }
            }
        }
    }

    public static void appendComponentDetails(ItemStack stack, ToolDefinition definition, Consumer<Component> tooltip) {
        var data = TinkerDataComponents.getMaterialTag(stack);
        for (var part : definition.parts()) {
            Material material = TinkerMaterials.getMaterial(data.getString(part.key()));
            if (material == null) { continue; }
            var component = AntiqueParts.get(part.part());
            if (component == null) { continue; }
            tooltip.accept(Component.translatable("tooltip.tconsnirvana.part_material",
                    component.item().get().getDescription(), material.getDisplayName())
                    .withColor(material.color).withStyle(net.minecraft.ChatFormatting.UNDERLINE));
            // 弓臂也提供头部属性，弩身则同时贡献手柄和附件属性。
            var stats = part.traitStats(definition.action());
            for (String type : stats) {
                appendComponentStats(material.getStats().get(type), tooltip);
            }
            for (var entry : material.getTraitsForStats(stats)) {
                tooltip.accept(entry.trait().getDisplayName(entry.level()));
            }
            tooltip.accept(Component.empty());
        }
    }

    private static void appendComponentStats(AbstractMaterialStats stats, Consumer<Component> tooltip) {
        if (stats == null) { return; }
        switch (stats) {
            case HeadMaterialStats head -> {
                tooltip.accept(Component.translatable("tooltip.tconsnirvana.part_durability", head.durability));
                tooltip.accept(Component.translatable("tooltip.tconsnirvana.tool.miningspeed", head.miningspeed));
                tooltip.accept(Component.translatable("tooltip.tconsnirvana.tool.mininglevel", HarvestLevels.getHarvestLevelName(head.harvestLevel)));
                tooltip.accept(Component.translatable("tooltip.tconsnirvana.tool.attack", head.attack));
            }
            case HandleMaterialStats handle -> {
                tooltip.accept(Component.translatable("tooltip.tconsnirvana.handle.modifier", handle.modifer));
                tooltip.accept(Component.translatable("tooltip.tconsnirvana.part_durability", handle.durability));
            }
            case ExtraMaterialStats extra -> tooltip.accept(Component.translatable("tooltip.tconsnirvana.part_durability", extra.durability));
            case BowMaterialStats bow -> tooltip.accept(Component.translatable("tooltip.tconsnirvana.bow_stats", bow.drawSpeed, bow.range, bow.bonusDamage));
            case BowStringMaterialStats string -> tooltip.accept(Component.translatable("tooltip.tconsnirvana.handle.modifier", string.modifier));
            case ArrowShaftMaterialStats shaft -> tooltip.accept(Component.translatable("tooltip.tconsnirvana.shaft_stats", shaft.modifier, shaft.bonusAmmo));
            case FletchingMaterialStats fletching -> tooltip.accept(Component.translatable("tooltip.tconsnirvana.fletching_stats", fletching.accuracy, fletching.modifier));
            default -> { }
        }
    }
}
