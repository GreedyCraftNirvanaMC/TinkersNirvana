/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.network.EntityMeltingSync;
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
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

    static List<Display> displays(List<EntityMeltingSync.Entry> entries) {
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
        var entity = recipe.preview(client.level, client.player);
        if (entity != null) {
            float scale = Math.min(32F / entity.getBbHeight(), 23F / entity.getBbWidth());
            // JEI 已把绘制原点移到配方内。原版跟随鼠标的入口使用屏幕绝对坐标裁剪，
            // 直接使用它会裁掉整个模型；这里沿用其底层实体渲染。
            InventoryScreen.renderEntityInInventory(graphics, 34, 36, Math.max(1, scale) / entity.getScale(),
                    new Vector3f(0F, entity.getBbHeight() / 2F, 0F),
                    new Quaternionf().rotateZ((float) Math.PI).rotateY(3.45F).rotateX(.08F), null, entity);
        }
        var font = client.font;
        var name = recipe.entityType.getDescription();
        graphics.drawString(font, font.plainSubstrByWidth(name.getString(), getWidth() - 8), 4, 2, 0x404040, false);
        graphics.blitSprite(ARROW, 76, 19, 24, 16);
        graphics.drawString(font, Component.translatable("jei.tconsnirvana.entity_melting_amount",
                recipe.entry.output().getAmount()), 94, 45, 0x404040, false);
    }

    static final class Display {
        private final EntityType<?> entityType;
        private final EntityMeltingSync.Entry entry;
        private ClientLevel previewLevel;
        private LivingEntity preview;

        private Display(EntityType<?> entityType, EntityMeltingSync.Entry entry) {
            this.entityType = entityType;
            this.entry = entry;
        }

        private LivingEntity preview(ClientLevel level, LivingEntity player) {
            if (entityType == EntityType.PLAYER) { return player; }
            if (level != null && previewLevel != level) {
                previewLevel = level;
                preview = entityType.create(level) instanceof LivingEntity living ? living : null;
            }
            return preview;
        }
    }
}
