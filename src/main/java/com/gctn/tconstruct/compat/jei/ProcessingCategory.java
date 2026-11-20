package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.TinkersNirvana;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class ProcessingCategory implements IRecipeCategory<RecipeCatalog.Display> {
    private static final ResourceLocation ARROW = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");
    private static final int ARROW_WIDTH = 24;
    private static final int WIDTH = 142;
    private static final int HEIGHT = 38;
    private static final int FIRST_SLOT = 4;
    private static final int SLOT_Y = 5;
    private static final int SLOT_SPACING = 19;
    private static final int ARROW_GAP = 7;
    private final RecipeType<RecipeCatalog.Display> type;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable background;
    private final int inputColumns;

    ProcessingCategory(RecipeCatalog.Kind kind, IGuiHelper helper) {
        type = type(kind);
        title = Component.translatable("jei.tconsnirvana." + kind.id());
        icon = helper.createDrawableItemStack(new ItemStack(kind.station.get()));
        slot = helper.getSlotDrawable();
        background = helper.createBlankDrawable(WIDTH, HEIGHT);
        inputColumns = switch (kind) {
            case ALLOYING -> 4;
            case CASTING_TABLE, CASTING_BASIN, PART_BUILDING -> 2;
            default -> 1;
        };
    }

    static RecipeType<RecipeCatalog.Display> type(RecipeCatalog.Kind kind) {
        return RecipeType.create(TinkersNirvana.MODID, kind.id(), RecipeCatalog.Display.class);
    }

    @Override public RecipeType<RecipeCatalog.Display> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public IDrawable getBackground() { return background; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, RecipeCatalog.Display recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.inputs().size(); i++) {
            addSlot(builder, RecipeIngredientRole.INPUT, FIRST_SLOT + i * SLOT_SPACING, recipe.inputs().get(i));
        }
        for (int i = 0; i < recipe.outputs().size(); i++) {
            addSlot(builder, RecipeIngredientRole.OUTPUT, outputX() + i * SLOT_SPACING, recipe.outputs().get(i));
        }
    }

    private void addSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, RecipeCatalog.Ingredient ingredient) {
        var recipeSlot = builder.addSlot(role, x, SLOT_Y).setBackground(slot, -1, -1);
        var fluid = ingredient.fluid();
        if (fluid.isEmpty()) {
            recipeSlot.addItemStacks(ingredient.items());
        } else {
            recipeSlot.setFluidRenderer(fluid.getAmount(), false, 16, 16);
            recipeSlot.addFluidStack(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch());
        }
    }

    private int arrowX() { return FIRST_SLOT + inputColumns * SLOT_SPACING + ARROW_GAP; }
    private int outputX() { return arrowX() + ARROW_WIDTH + ARROW_GAP; }

    @Override public void draw(RecipeCatalog.Display recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        if (!recipe.outputs().isEmpty()) {
            graphics.blitSprite(ARROW, arrowX(), SLOT_Y, ARROW_WIDTH, 16);
        }
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, recipe.detail(), FIRST_SLOT, 27, 0x404040, false);
    }
}
