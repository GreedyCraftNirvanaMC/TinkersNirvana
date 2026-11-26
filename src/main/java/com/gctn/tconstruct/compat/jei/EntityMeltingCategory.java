/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.common.network.EntityRecipeSync;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;

final class EntityMeltingCategory implements IRecipeCategory<EntityMeltingCategory.Display> {
    static final RecipeType<Display> TYPE = RecipeType.create(TinkersNirvana.MODID, "entity_melting", Display.class);
    private static final ResourceLocation ARROW = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private final IDrawable icon;
    private final IDrawable slot;

    EntityMeltingCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(new ItemStack(SmelteryModule.CONTROLLER.get()));
        slot = gui.getSlotDrawable();
    }

    static List<Display> displays(List<EntityRecipeSync.Melting> entries) {
        return entries.stream().flatMap(entry -> BuiltInRegistries.ENTITY_TYPE.getOptional(entry.entity()).stream()
                        .map(type -> new Display(type, entry)))
                .toList();
    }

    @Override public RecipeType<Display> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.tconsnirvana.entity_melting"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 180; }
    @Override public int getHeight() { return 63; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        var egg = SpawnEggItem.byId(recipe.entityType);
        if (egg != null) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(egg));
        }
        var output = recipe.entry.output();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 19)
                .setBackground(slot, -1, -1)
                .setFluidRenderer(output.getAmount(), false, 16, 16)
                .addFluidStack(output.getFluid(), output.getAmount(), output.getComponentsPatch());
        if (!SmelteryRecipes.FUELS.isEmpty()) {
            var fuelSlot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 70, 40)
                    .setBackground(slot, -1, -1).setFluidRenderer(1000, false, 16, 16);
            for (var fuel : SmelteryRecipes.FUELS) { fuelSlot.addFluidStack(fuel.fluid(), 1000); }
        }
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var client = Minecraft.getInstance();
        recipe.preview.draw(graphics, 34, 36);
        var font = client.font;
        var name = recipe.entityType.getDescription();
        graphics.drawString(font, font.plainSubstrByWidth(name.getString(), getWidth() - 8), 4, 2, 0x404040, false);
        graphics.blitSprite(ARROW, 76, 19, 24, 16);
        graphics.drawString(font, Component.translatable("jei.tconsnirvana.entity_melting_amount",
                recipe.entry.output().getAmount()), 94, 45, 0x404040, false);
    }

    static final class Display {
        private final EntityType<?> entityType;
        private final EntityRecipeSync.Melting entry;
        private final JeiEntityPreview preview;

        private Display(EntityType<?> entityType, EntityRecipeSync.Melting entry) {
            this.entityType = entityType;
            this.entry = entry;
            this.preview = new JeiEntityPreview(entityType);
        }
    }
}
