package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.gadgets.RemainingGadgets;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.client.SmelteryScreen;
import com.gctn.tconstruct.smeltery.client.TinkerTankScreen;
import com.gctn.tconstruct.smeltery.network.EntityMeltingSync;
import com.gctn.tconstruct.tables.WorkshopModule;
import com.gctn.tconstruct.tables.block.TableBlocks;
import com.gctn.tconstruct.tables.client.PersistentCraftingScreen;
import com.gctn.tconstruct.tables.client.ToolStationScreen;
import com.gctn.tconstruct.tables.client.WorkshopScreen;
import com.gctn.tconstruct.tables.menu.PersistentCraftingMenu;
import com.gctn.tconstruct.tables.recipe.TableMaterialRecipe;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.PatternItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
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
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@JeiPlugin
public final class TinkerJeiPlugin implements IModPlugin {
    @Override public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "jei");
    }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        var helpers = registration.getJeiHelpers();
        var itemRenderer = helpers.getIngredientManager().getIngredientRenderer(VanillaTypes.ITEM_STACK);
        for (var kind : RecipeCatalog.Kind.values()) {
            registration.addRecipeCategories(new ProcessingCategory(kind, helpers.getGuiHelper(), itemRenderer));
        }
        registration.addRecipeCategories(new EntityMeltingCategory(helpers.getGuiHelper()));
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) { return; }
        var catalog = RecipeCatalog.create(level.getRecipeManager(), level.registryAccess());
        for (var kind : RecipeCatalog.Kind.values()) {
            registration.addRecipes(ProcessingCategory.type(kind), catalog.get(kind));
        }
        var connection = minecraft.getConnection();
        if (connection != null) {
            registration.addRecipes(EntityMeltingCategory.TYPE,
                    EntityMeltingCategory.displays(EntityMeltingSync.recipes(connection.getConnection())));
        }
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var kind : RecipeCatalog.Kind.values()) {
            registration.addRecipeCatalyst(new ItemStack(kind.station.get()), ProcessingCategory.type(kind));
        }
        registration.addRecipeCatalyst(new ItemStack(SmelteryModule.CONTROLLER.get()), ProcessingCategory.type(RecipeCatalog.Kind.FUEL));
        registration.addRecipeCatalyst(new ItemStack(SmelteryModule.CONTROLLER.get()), EntityMeltingCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(SmelteryModule.FURNACE.get()),
                RecipeTypes.SMELTING, ProcessingCategory.type(RecipeCatalog.Kind.FUEL));
        registration.addRecipeCatalyst(new ItemStack(WorkshopModule.BLOCKS.get(WorkshopModule.Kind.CRAFTING).get()), RecipeTypes.CRAFTING);
    }

    @Override public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new IRecipeTransferInfo<PersistentCraftingMenu, RecipeHolder<CraftingRecipe>>() {
            @Override public Class<PersistentCraftingMenu> getContainerClass() { return PersistentCraftingMenu.class; }
            @Override public Optional<MenuType<PersistentCraftingMenu>> getMenuType() {
                return Optional.of(WorkshopModule.CRAFTING_MENU.get());
            }
            @Override public mezz.jei.api.recipe.RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
                return RecipeTypes.CRAFTING;
            }
            @Override public boolean canHandle(PersistentCraftingMenu menu, RecipeHolder<CraftingRecipe> recipe) { return true; }
            @Override public List<Slot> getRecipeSlots(PersistentCraftingMenu menu, RecipeHolder<CraftingRecipe> recipe) {
                return menu.slots.subList(1, 10);
            }
            @Override public List<Slot> getInventorySlots(PersistentCraftingMenu menu, RecipeHolder<CraftingRecipe> recipe) {
                // 合成站将附近库存接在玩家库存后面，转移时只用当前页的有效槽。
                return menu.slots.subList(10, menu.slots.size()).stream().filter(Slot::isActive).toList();
            }
        });
    }

    @Override public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addExtension(TableMaterialRecipe.class, new TableCraftingExtension());
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
        var ingredients = registration.getJeiHelpers().getIngredientManager();
        registration.addGuiContainerHandler(SmelteryScreen.class, new IGuiContainerHandler<SmelteryScreen>() {
            @Override public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(SmelteryScreen screen, double x, double y) {
                return screen.fluidLayerAt(x, y)
                        .flatMap(layer -> ingredients.createClickableIngredient(layer.fluid(), layer.area(), false));
            }
        });
        registration.addGuiContainerHandler(TinkerTankScreen.class, new IGuiContainerHandler<TinkerTankScreen>() {
            @Override public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(TinkerTankScreen screen, double x, double y) {
                return screen.fluidLayerAt(x, y)
                        .flatMap(layer -> ingredients.createClickableIngredient(layer.fluid(), layer.area(), false));
            }
        });
    }

    private static <T extends AbstractContainerScreen<?>> void protectSidePanels(
            IGuiHandlerRegistration registration, Class<T> type, Function<T, List<Rect2i>> areas) {
        registration.addGuiContainerHandler(type, new IGuiContainerHandler<>() {
            @Override public List<Rect2i> getGuiExtraAreas(T screen) { return areas.apply(screen); }
        });
    }

    @Override public void registerItemSubtypes(ISubtypeRegistration registration) {
        var tableSubtype = new Subtype(stack -> {
            var material = stack.get(TinkerDataComponents.TABLE_MATERIAL.get());
            return material == null ? "" : material.toString();
        });
        for (var table : WorkshopModule.BLOCKS.values()) {
            registration.registerSubtypeInterpreter(table.get().asItem(), tableSubtype);
        }
        registration.registerSubtypeInterpreter(TableBlocks.TOOL_FORGE.get().asItem(), tableSubtype);
        registration.registerSubtypeInterpreter(RemainingGadgets.ITEM_RACK.get().asItem(), tableSubtype);
        registration.registerSubtypeInterpreter(RemainingGadgets.DRYING_RACK.get().asItem(), tableSubtype);
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
