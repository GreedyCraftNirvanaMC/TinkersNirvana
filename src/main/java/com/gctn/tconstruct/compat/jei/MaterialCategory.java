/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.book.TinkerBook;
import com.gctn.tconstruct.book.client.BookScreen;
import com.gctn.tconstruct.client.MaterialStatText;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.materials.MaterialInputs;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MaterialCategory implements IRecipeCategory<MaterialCategory.Display> {
    enum Kind {
        HARVEST("harvest_stats", "pickaxe_head", List.of("Head", "Extra", "Handle")),
        RANGED("ranged_stats", "bow_limb", List.of("Bow", "BowString")),
        PROJECTILE("projectile_stats", "arrow_shaft", List.of("Projectile", "Shaft", "Fletching"));

        final String id;
        final String icon;
        final List<String> roles;

        Kind(String id, String icon, List<String> roles) { this.id = id; this.icon = icon; this.roles = roles; }

        RecipeType<Display> type() { return RecipeType.create(TinkersNirvana.MODID, id, Display.class); }
    }

    record Line(Component text, Component tooltip) {}
    record Display(Component name, ItemStack representative, FluidStack fluid, List<ItemStack> inputs,
                   List<ItemStack> parts, List<ItemStack> allParts, List<Line> lines, String guidePage) {}

    private static final int WIDTH = 164, HEIGHT = 139, TEXT_TOP = 26, LINE_HEIGHT = 10;
    private static final ScreenRectangle BOOK_BUTTON = new ScreenRectangle(WIDTH - 18, 1, 16, 16);
    private final Kind kind;
    private final IDrawable icon;
    private final IDrawable slot;

    MaterialCategory(Kind kind, IGuiHelper gui) {
        this.kind = kind;
        icon = gui.createDrawableItemStack(AntiqueParts.get(kind.icon).create(TinkerMaterials.wood));
        slot = gui.getSlotDrawable();
    }

    static Map<Kind, List<Display>> collect(RecipeCatalog catalog) {
        Map<Kind, List<Display>> result = new EnumMap<>(Kind.class);
        for (Kind kind : Kind.values()) { result.put(kind, new ArrayList<>()); }
        Map<Material, List<ItemStack>> raw = new HashMap<>();
        for (var item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            var match = MaterialInputs.match(stack);
            if (match != null) { raw.computeIfAbsent(match.material(), ignored -> new ArrayList<>()).add(stack); }
        }
        Map<Fluid, List<ItemStack>> meltingInputs = new HashMap<>();
        for (var recipe : catalog.get(RecipeCatalog.Kind.MELTING)) {
            meltingInputs.computeIfAbsent(recipe.outputs().getFirst().fluid().getFluid(), ignored -> new ArrayList<>())
                    .addAll(recipe.inputs().getFirst().items());
        }
        for (Material material : TinkerMaterials.materials) {
            if (material.isHidden()) { continue; }
            var rawInputs = raw.getOrDefault(material, List.of());
            ItemStack representative = material.getRepresentativeItem();
            if (representative.isEmpty() && !rawInputs.isEmpty()) { representative = rawInputs.getFirst(); }
            if (representative.isEmpty()) { continue; }
            FluidStack fluid = materialFluid(material);
            List<ItemStack> inputs = new ArrayList<>(rawInputs);
            if (!fluid.isEmpty()) { inputs.addAll(meltingInputs.getOrDefault(fluid.getFluid(), List.of())); }
            var allParts = parts(material, List.of("Head", "Extra", "Handle", "Bow", "BowString", "Projectile", "Shaft", "Fletching"));
            for (Kind kind : Kind.values()) {
                var roles = kind.roles.stream().filter(role -> material.hasStats(statRole(role))).toList();
                if (roles.isEmpty()) { continue; }
                var parts = parts(material, roles);
                if (parts.isEmpty()) { continue; }
                var lines = new ArrayList<Line>();
                for (String role : roles) { addLines(lines, material, role); }
                String page = "material:" + material.identifier;
                if (kind != Kind.HARVEST && roles.size() == 1 && !roles.getFirst().equals("Projectile")) {
                    page = "ranged:" + roles.getFirst();
                }
                result.get(kind).add(new Display(material.getDisplayName(), representative, fluid, List.copyOf(inputs),
                        parts, allParts, List.copyOf(lines), page));
            }
        }
        return result;
    }

    private static FluidStack materialFluid(Material material) {
        if (!material.isCastable()) { return FluidStack.EMPTY; }
        var head = AntiqueParts.get("pickaxe_head").create(material);
        var recipe = SmelteryRecipes.findMelting(head, Map.of());
        return recipe == null ? FluidStack.EMPTY : recipe.output().copyWithAmount(1000);
    }

    private static String statRole(String role) { return role.equals("Projectile") ? "Head" : role; }

    private static List<ItemStack> parts(Material material, List<String> roles) {
        var parts = new ArrayList<ItemStack>();
        var required = AntiqueTools.entries().values().stream().flatMap(tool -> tool.definition().parts().stream()).toList();
        for (var part : AntiqueParts.entries().values()) {
            if (part.id().equals("shard") || part.id().equals("bolt_core")) { continue; }
            boolean match = roles.contains(part.stat()) || required.stream().anyMatch(component ->
                    component.part().equals(part.id()) && roles.stream().anyMatch(role -> component.stat().equals(statRole(role))
                            && (!role.equals("Projectile") || component.part().equals("arrow_head") || component.part().equals("knife_blade"))));
            if (match && part.acceptsMaterial(material)) {
                var stack = part.create(material);
                if (!stack.isEmpty()) { parts.add(stack); }
            }
        }
        if (roles.contains("Projectile") || roles.contains("Shaft")) {
            for (var recipe : SmelteryRecipes.CASTING) {
                ItemStack output = recipe.output();
                if (!output.is(AntiqueParts.get("bolt_core").item().get())) { continue; }
                var data = TinkerDataComponents.getMaterialTag(output);
                if (roles.contains("Shaft") && data.getString(Tags.PART_MATERIAL).equals(material.identifier)
                        || roles.contains("Projectile") && data.getString("Coating").equals(material.identifier)) {
                    parts.add(output.copy());
                }
            }
        }
        return List.copyOf(parts);
    }

    private static void addLines(List<Line> lines, Material material, String role) {
        var traits = switch (role) {
            case "Projectile" -> material.getTraitsForStats(List.of("Head", "Projectile"));
            case "Bow" -> material.getTraitsForStats(List.of("Bow", "Head"));
            default -> material.getTraits(role);
        };
        var traitNames = traits.stream().map(entry -> entry.trait().getDisplayName(entry.level())).distinct().toList();
        var heading = Component.translatable("stat." + MaterialStatText.type(role) + ".name").withStyle(ChatFormatting.UNDERLINE);
        if (!traitNames.isEmpty()) {
            heading.append("  ");
            for (int i = 0; i < traitNames.size(); i++) {
                if (i > 0) { heading.append(", "); }
                heading.append(traitNames.get(i));
            }
        }
        var description = Component.empty();
        for (var trait : traits.stream().distinct().toList()) {
            if (!description.getSiblings().isEmpty()) { description.append("\n"); }
            description.append(trait.trait().getDescription());
        }
        lines.add(new Line(heading, description));
        for (Component value : MaterialStatText.stats(material.stats.get(statRole(role)))) {
            var hover = value.getStyle().getHoverEvent();
            lines.add(new Line(value, hover == null ? null : hover.getValue(HoverEvent.Action.SHOW_TEXT)));
        }
    }

    @Override public RecipeType<Display> getRecipeType() { return kind.type(); }
    @Override public Component getTitle() { return Component.translatable("jei.tconsnirvana." + kind.id); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Display recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 2).setBackground(slot, -1, -1).addItemStack(recipe.representative());
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(recipe.inputs());
        if (!recipe.fluid().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 21, 2).setBackground(slot, -1, -1)
                    .setFluidRenderer(1000, false, 16, 16)
                    .addFluidStack(recipe.fluid().getFluid(), recipe.fluid().getAmount(), recipe.fluid().getComponentsPatch());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 38, 2).setBackground(slot, -1, -1).addItemStacks(recipe.parts());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(recipe.allParts());
    }

    @Override
    public void draw(Display recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, font.plainSubstrByWidth(recipe.name().getString(), WIDTH - 84), WIDTH / 2, 6, 0xffffffff);
        for (int i = 0; i < recipe.lines().size(); i++) {
            var line = recipe.lines().get(i).text();
            graphics.drawString(font, net.minecraft.locale.Language.getInstance().getVisualOrder(font.substrByWidth(line, WIDTH - 6)),
                    3, TEXT_TOP + i * LINE_HEIGHT, 0xff404040, false);
        }
        if (Config.JEI_GUIDEBOOK_BUTTON.get()) {
            slot.draw(graphics, WIDTH - 19, 1);
            graphics.renderFakeItem(new ItemStack(TinkerBook.ITEM.get()), WIDTH - 18, 2);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, Display recipe, IRecipeSlotsView slots, double x, double y) {
        if (Config.JEI_GUIDEBOOK_BUTTON.get() && x >= WIDTH - 18 && x < WIDTH - 2 && y >= 1 && y < 17) {
            tooltip.add(Component.translatable("jei.tconsnirvana.material_guide"));
        } else if (y >= TEXT_TOP && x >= 3 && x < WIDTH - 3) {
            int index = (int) (y - TEXT_TOP) / LINE_HEIGHT;
            if (index >= recipe.lines().size()) { return; }
            var line = recipe.lines().get(index);
            if (line.tooltip() != null && !line.tooltip().getString().isBlank()) {
                for (String text : line.tooltip().getString().split("\n")) { tooltip.add(Component.literal(text)); }
            }
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Display recipe, IFocusGroup focuses) {
        if (!Config.JEI_GUIDEBOOK_BUTTON.get()) { return; }
        builder.addInputHandler(new IJeiInputHandler() {
            @Override public ScreenRectangle getArea() { return BOOK_BUTTON; }
            @Override public boolean handleInput(double x, double y, IJeiUserInput input) {
                if (!input.getKey().equals(InputConstants.Type.MOUSE.getOrCreate(0))) { return false; }
                if (!input.isSimulate()) { BookScreen.openMaterialPage(recipe.guidePage()); }
                return true;
            }
        });
    }
}
