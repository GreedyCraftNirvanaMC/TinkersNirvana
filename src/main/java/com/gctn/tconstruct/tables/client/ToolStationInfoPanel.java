/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes;
import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolModifiers;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.Locale;

final class ToolStationInfoPanel extends AbstractScrollWidget {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/panel.png");
    static final int PREFERRED_WIDTH = 126;
    private static final int TEXTURE_HEIGHT = 83;
    private static final int BORDER = 4;
    private static final int PADDING = 8;
    private static final int TEXT_COLOR = 0xffe0e0e0;

    private final Font font;
    private final ToolStationMenu menu;
    private Mode displayedMode;
    private ItemStack displayedStack = ItemStack.EMPTY;
    private List<FormattedCharSequence> lines = List.of();

    ToolStationInfoPanel(Font font, ToolStationMenu menu, int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("gui.tconsnirvana.tool_station.stats"));
        this.font = font;
        this.menu = menu;
    }

    void update() {
        Mode mode = menu.getMode();
        ItemStack stack = menu.displayedTool();
        if (mode == displayedMode && ItemStack.matches(stack, displayedStack)) {
            return;
        }

        boolean resetScroll = mode != displayedMode || !stack.is(displayedStack.getItem());
        displayedMode = mode;
        // 比较副本以识别同步时原地更新的耐久、强化和名称。
        displayedStack = stack.copy();
        MutableComponent content = Component.empty().append(heading(getMessage()));
        if (mode == Mode.DISASSEMBLE) {
            addLine(content, Component.translatable("gui.tconsnirvana.tool_station.disassembly").withStyle(ChatFormatting.RED));
        }
        if (stack.getItem() instanceof TinkerTools tool) {
            appendToolStats(content, stack, tool);
        } else if (mode == Mode.DEFAULT) {
            addLine(content, Component.translatable("gui.tconsnirvana.tool_station.repair"));
        } else if (mode != Mode.DISASSEMBLE) {
            appendRequiredParts(content, mode);
        }
        lines = font.split(content, getWidth() - 2 * PADDING);
        setScrollAmount(resetScroll ? 0 : scrollAmount());
    }

    private static void appendToolStats(MutableComponent content, ItemStack stack, TinkerTools tool) {
        addLine(content, stack.getHoverName());
        addLine(content, TinkerTools.getDurabilityTip(stack));
        if (tool instanceof AntiqueTool antique) {
            var stats = ToolBuilder.savedStats(stack);
            if (!antique.definition().effectiveBlocks().isEmpty()) {
                addLine(content, Component.translatable("tooltip.tconsnirvana.tool.mininglevel",
                        HarvestLevels.getHarvestLevelName(stats.getInt("HarvestLevel"))));
                addLine(content, stat("tooltip.tconsnirvana.tool.miningspeed", stats.getFloat("MiningSpeed")));
            }
            switch (antique.definition().action()) {
                case SHORTBOW, LONGBOW, CROSSBOW -> {
                    addLine(content, stat("book.tconsnirvana.stat.draw", stats.getFloat("DrawSpeed")));
                    addLine(content, stat("book.tconsnirvana.stat.range", stats.getFloat("Range")));
                    addLine(content, stat("book.tconsnirvana.stat.bonus", stats.getFloat("ProjectileDamage")));
                }
                case ARROW, BOLT -> {
                    addLine(content, stat("tooltip.tconsnirvana.tool.attack", stats.getFloat("ProjectileDamage")));
                    addLine(content, stat("book.tconsnirvana.stat.accuracy", stats.getFloat("Accuracy")));
                }
                default -> addLine(content, stat("tooltip.tconsnirvana.tool.attack", stats.getFloat("Attack")));
            }
        }
        addLine(content, Component.translatable("tooltip.tconsnirvana.tool.modifiers", tool.getAvailableModifierSlots(stack)));

        var traits = ToolTraits.getTraits(stack);
        if (!traits.isEmpty()) {
            addLine(content, heading(Component.translatable("gui.tconsnirvana.tool_station.traits")));
            for (var entry : traits) {
                addLine(content, entry.trait().getDisplayName(entry.level()));
            }
        }
        for (String id : ToolModifiers.entries().keySet()) {
            int units = ToolModifiers.units(stack, id);
            if (units > 0) {
                addLine(content, Component.translatable("tooltip.tconsnirvana.modifier",
                        Component.translatable("modifier.tconsnirvana." + id), ToolModifiers.level(stack, id), units));
            }
        }
    }

    private static void appendRequiredParts(MutableComponent content, Mode mode) {
        var recipe = ToolStationAssemblyRecipes.get(mode);
        if (recipe == null) {
            return;
        }
        addLine(content, Component.translatable("container.tconsnirvana.toolstation.mode." + mode.name().toLowerCase(Locale.ROOT)));
        addLine(content, heading(Component.translatable("gui.tconsnirvana.tool_station.parts")));
        for (int i = 0; i < ToolStationMenu.INPUT_SLOT_COUNT; i++) {
            var part = recipe.getRequiredItem(i);
            if (part != null) {
                addLine(content, part.getDescription());
            }
        }
    }

    private static Component stat(String key, float value) {
        return Component.translatable(key, ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value));
    }

    private static MutableComponent heading(Component title) {
        return title.copy().withStyle(ChatFormatting.BOLD);
    }

    private static void addLine(MutableComponent content, Component line) {
        // 文字作为同级组件追加，标题的粗体不会传给后续行。
        content.append("\n").append(line);
    }

    Rect2i bounds() {
        return new Rect2i(getX(), getY(), getWidth() + scrollbarWidth(), getHeight());
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return visible && x >= getX() && x < getX() + getWidth() + scrollbarWidth()
                && y >= getY() && y < getY() + getHeight();
    }

    @Override
    protected void renderBackground(GuiGraphics graphics) {
        // 保留怀古木框和金属框的四像素边缘，只拉伸中间的面板区域。
        int textureY = menu.isForge() ? TEXTURE_HEIGHT : 0;
        int[] widths = {BORDER, getWidth() - 2 * BORDER, BORDER};
        int[] heights = {BORDER, getHeight() - 2 * BORDER, BORDER};
        int[] sourceWidths = {BORDER, PREFERRED_WIDTH - 2 * BORDER, BORDER};
        int[] sourceHeights = {BORDER, TEXTURE_HEIGHT - 2 * BORDER, BORDER};
        int y = getY();
        int v = textureY;
        for (int row = 0; row < heights.length; row++) {
            int x = getX();
            int u = PREFERRED_WIDTH;
            for (int column = 0; column < widths.length; column++) {
                graphics.blit(TEXTURE, x, y, widths[column], heights[row], u, v,
                        sourceWidths[column], sourceHeights[row], 256, 256);
                x += widths[column];
                u += sourceWidths[column];
            }
            y += heights[row];
            v += sourceHeights[row];
        }
    }

    @Override
    protected int getInnerHeight() {
        return lines.size() * font.lineHeight + PADDING;
    }

    @Override
    protected boolean scrollbarVisible() {
        return getMaxScrollAmount() > 0;
    }

    @Override
    protected double scrollRate() {
        return font.lineHeight;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int y = getY() + PADDING;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, getX() + PADDING, y, TEXT_COLOR, false);
            y += font.lineHeight;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
