/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.common.network.EntityRecipeSync;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.mojang.math.Axis;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Collections;
import java.util.List;

final class BeheadingCategory implements IRecipeCategory<BeheadingCategory.Display> {
    static final RecipeType<Display> TYPE = RecipeType.create(TinkersNirvana.MODID, "beheading", Display.class);
    private final ItemStack cleaver;
    private final IDrawable icon;
    private final IDrawable slot;

    BeheadingCategory(IGuiHelper gui) {
        cleaver = cleaver(TinkerMaterials.iron);
        icon = gui.createDrawableItemStack(cleaver);
        slot = gui.getSlotDrawable();
    }

    static ItemStack cleaver(Material material) {
        var tool = AntiqueTools.entries().get("cleaver");
        return ToolBuilder.assemble(tool, Collections.nCopies(tool.definition().parts().size(), material));
    }

    static List<Display> displays(List<EntityRecipeSync.Beheading> entries) {
        return entries.stream().flatMap(entry -> BuiltInRegistries.ENTITY_TYPE.getOptional(entry.entity()).stream()
                        .map(type -> new Display(type, entry.output(), new JeiEntityPreview(type))))
                .toList();
    }

    @Override public RecipeType<Display> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.tconsnirvana.beheading"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 142; }
    @Override public int getHeight() { return 63; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        var egg = SpawnEggItem.byId(recipe.type());
        if (egg != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(egg));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 107, 29).setBackground(slot, -1, -1)
                .addItemStack(recipe.output().copyWithCount(1));
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        recipe.preview().draw(graphics, 33, 38);
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, font.plainSubstrByWidth(recipe.preview().name().getString(), getWidth() - 8),
                4, 2, 0x404040, false);
        if (recipe.output().getCount() > 1) {
            graphics.drawCenteredString(font, Component.translatable("jei.tconsnirvana.beheading_amount",
                    recipe.output().getCount()), 115, 48, 0xff404040);
        }
        drawCleaver(graphics);
    }

    private void drawCleaver(GuiGraphics graphics) {
        double swing = Config.FANCY_JEI_BEHEADING.get() ? Math.PI * (Util.getMillis() % 2000) / 1000 : 0;
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(74 + Math.sin(swing) * 8, 37 + Math.cos(swing) * 4, 0);
        pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(swing) * 70));
        graphics.renderFakeItem(cleaver, -8, -8);
        pose.popPose();
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Display recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= 58 && mouseX < 91 && mouseY >= 22 && mouseY < 54) {
            tooltip.add(Component.translatable("jei.tconsnirvana.beheading_tool"));
        }
    }

    record Display(EntityType<?> type, ItemStack output, JeiEntityPreview preview) {}
}
