package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.client.SmelteryScreen;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.client.PersistentCraftingScreen;
import com.gctn.tconstruct.tables.client.ToolStationScreen;
import com.gctn.tconstruct.tables.client.WorkshopScreen;
import com.gctn.tconstruct.tables.menu.PersistentCraftingMenu;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.PatternItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JeiPlugin
public final class TinkerJeiPlugin implements IModPlugin {
    @Override public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "jei");
    }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        for (var kind : RecipeCatalog.Kind.values()) {
            registration.addRecipeCategories(new ProcessingCategory(kind, helper));
        }
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) { return; }
        var catalog = RecipeCatalog.create(level.getRecipeManager(), level.registryAccess());
        for (var kind : RecipeCatalog.Kind.values()) {
            registration.addRecipes(ProcessingCategory.type(kind), catalog.get(kind));
        }
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var kind : RecipeCatalog.Kind.values()) {
            registration.addRecipeCatalyst(new ItemStack(kind.station.get()), ProcessingCategory.type(kind));
        }
        registration.addRecipeCatalyst(new ItemStack(SmelteryModule.CONTROLLER.get()), ProcessingCategory.type(RecipeCatalog.Kind.FUEL));
        registration.addRecipeCatalyst(new ItemStack(SmelteryModule.FURNACE.get()),
                RecipeTypes.SMELTING, ProcessingCategory.type(RecipeCatalog.Kind.FUEL));
        registration.addRecipeCatalyst(new ItemStack(WorkshopModule.BLOCKS.get(WorkshopModule.Kind.CRAFTING).get()), RecipeTypes.CRAFTING);
    }

    @Override public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(PersistentCraftingMenu.class, WorkshopModule.CRAFTING_MENU.get(),
                RecipeTypes.CRAFTING, 1, 9, 10, 36);
    }

    @Override public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(PersistentCraftingScreen.class, new IGuiContainerHandler<>() {
            @Override public List<Rect2i> getGuiExtraAreas(PersistentCraftingScreen screen) { return screen.occupiedAreas(); }
            @Override public Collection<IGuiClickableArea> getGuiClickableAreas(PersistentCraftingScreen screen, double x, double y) {
                return List.of(IGuiClickableArea.createBasic(88, 32, 24, 17, RecipeTypes.CRAFTING));
            }
        });
        protectSidePanels(registration, WorkshopScreen.class, WorkshopScreen::occupiedAreas);
        protectSidePanels(registration, ToolStationScreen.class, ToolStationScreen::occupiedAreas);
        protectSidePanels(registration, SmelteryScreen.class, SmelteryScreen::occupiedAreas);
    }

    private static <T extends AbstractContainerScreen<?>> void protectSidePanels(
            IGuiHandlerRegistration registration, Class<T> type, Function<T, List<Rect2i>> areas) {
        registration.addGuiContainerHandler(type, new IGuiContainerHandler<>() {
            @Override public List<Rect2i> getGuiExtraAreas(T screen) { return areas.apply(screen); }
        });
    }

    @Override public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(PatternItem.PATTERN.get(), new Subtype(
                stack -> TinkerDataComponents.getMaterialTag(stack).getString("Pattern")));
        for (var part : AntiqueParts.entries().values()) {
            if (part.external()) { continue; }
            registration.registerSubtypeInterpreter(part.item().get(), new Subtype(stack -> {
                var material = TinkerDataComponents.getMaterialTag(stack);
                return encode(material.getString(Tags.PART_MATERIAL)) + encode(material.getString("Coating"));
            }));
        }
        for (var tool : AntiqueTools.entries().values()) {
            registration.registerSubtypeInterpreter(tool.item().get(), new Subtype(stack -> {
                var material = TinkerDataComponents.getMaterialTag(stack);
                return tool.definition().parts().stream().map(part -> encode(material.getString(part.key())))
                        .collect(Collectors.joining());
            }));
        }
    }

    private static String encode(String value) { return value.length() + ":" + value; }

    private record Subtype(Function<ItemStack, String> reference) implements ISubtypeInterpreter<ItemStack> {
        @Override public Object getSubtypeData(ItemStack stack, UidContext context) { return reference.apply(stack); }

        @Override public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) { return reference.apply(stack); }
    }
}
