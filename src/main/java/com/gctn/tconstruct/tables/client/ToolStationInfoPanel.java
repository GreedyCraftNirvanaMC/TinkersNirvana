/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.Config;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import com.gctn.tconstruct.tables.recipe.ToolStationAssemblyRecipes;
import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import com.gctn.tconstruct.tools.ToolCombat;
import com.gctn.tconstruct.tools.ToolDefinition.Action;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

final class ToolStationInfoPanel extends AbstractScrollWidget {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/panel.png");
    static final int PREFERRED_WIDTH = 126;
    private static final int TEXTURE_HEIGHT = 83;
    private static final int BORDER = 4;
    private static final int PADDING = 5;
    private static final int CAPTION_HEIGHT = 12;
    private static final int TEXT_COLOR = 0xffe0e0e0;

    private final Font font;
    private final ToolStationMenu menu;
    private final boolean details;
    private Mode displayedMode;
    private int displayedParts;
    private ItemStack displayedStack = ItemStack.EMPTY;
    private List<FormattedCharSequence> lines = List.of();
    private List<HoverEntry> hoverEntries = List.of();

    ToolStationInfoPanel(Font font, ToolStationMenu menu, int x, int y, int width, int height, boolean details) {
        super(x, y, width, height, Component.translatable("gui.tconsnirvana.tool_station.stats"));
        this.font = font;
        this.menu = menu;
        this.details = details;
    }

    void update() {
        Mode mode = menu.getMode();
        ItemStack stack = menu.displayedTool();
        int presentParts = 0;
        if (details && mode != Mode.DEFAULT && mode != Mode.DISASSEMBLE && stack.isEmpty()) {
            for (int slot = 0; slot < ToolStationMenu.INPUT_SLOT_COUNT; slot++) {
                var required = menu.getRequiredInputItem(mode, slot);
                if (required != null && menu.getSlot(slot + 1).getItem().is(required)) { presentParts |= 1 << slot; }
            }
        }
        if (mode == displayedMode && presentParts == displayedParts && ItemStack.matches(stack, displayedStack)) {
            return;
        }

        boolean resetScroll = mode != displayedMode || !stack.is(displayedStack.getItem());
        displayedMode = mode;
        displayedParts = presentParts;
        // 比较副本以识别同步时原地更新的耐久、强化和名称。
        displayedStack = stack.copy();
        MutableComponent content = Component.empty();
        hoverEntries = List.of();
        if (mode == Mode.DISASSEMBLE) {
            setMessage(details ? Component.empty() : stationText("warning"));
            if (!details) {
                addLine(content, Component.translatable("gui.tconsnirvana.tool_station.disassembly").withStyle(ChatFormatting.RED));
            }
        } else if (stack.getItem() instanceof TinkerTools tool) {
            Component toolName = tool instanceof AntiqueTool antique
                    ? Component.translatable("item.tconsnirvana." + antique.definition().id() + ".name")
                    : stack.getHoverName();
            setMessage(details ? stationText("traits") : toolName);
            if (details) {
                showTraits(stack, resetScroll);
                return;
            }
            appendToolStats(content, stack, tool);
        } else if (mode == Mode.DEFAULT) {
            setMessage(details ? Config.DECONSTRUCT_TOOLS.get() ? stationText("deconstruct") : Component.empty() : stationText("repair_title"));
            if (!details || Config.DECONSTRUCT_TOOLS.get()) {
                addLine(content, stationText(details ? "deconstruct_info" : "repair"));
            }
        } else {
            String id = mode == Mode.AXE ? "hatchet" : mode.name().toLowerCase(Locale.ROOT);
            setMessage(details ? stationText("parts")
                    : Component.translatable("container.tconsnirvana.toolstation.mode." + mode.name().toLowerCase(Locale.ROOT)));
            if (details) {
                appendRequiredParts(content, mode);
            } else {
                addLine(content, Component.translatable("gui.tconsnirvana.tool_station.description." + id));
            }
        }
        lines = font.split(content, getWidth() - 2 * PADDING);
        setScrollAmount(resetScroll ? 0 : scrollAmount());
    }

    private static Component stationText(String name) {
        return Component.translatable("gui.tconsnirvana.tool_station." + name);
    }

    private static void appendToolStats(MutableComponent content, ItemStack stack, TinkerTools tool) {
        addLine(content, TinkerTools.getDurabilityTip(stack));
        if (tool instanceof AntiqueTool antique) {
            var stats = ToolBuilder.savedStats(stack);
            if (!antique.definition().effectiveBlocks().isEmpty()) {
                if (antique.definition().action() == Action.MATTOCK) {
                    addLine(content, Component.translatable("stat.mattock.axelevel.name").append(" ")
                            .append(HarvestLevels.getHarvestLevelName(ToolBuilder.headHarvestLevel(stack, "Head"))));
                    addLine(content, Component.translatable("stat.mattock.shovellevel.name").append(" ")
                            .append(HarvestLevels.getHarvestLevelName(ToolBuilder.headHarvestLevel(stack, "Shovel"))));
                } else {
                    addLine(content, Component.translatable("tooltip.tconsnirvana.tool.mininglevel",
                            HarvestLevels.getHarvestLevelName(stats.getInt("HarvestLevel"))));
                }
                addLine(content, stat("tooltip.tconsnirvana.tool.miningspeed", stats.getFloat("MiningSpeed"), ChatFormatting.BLUE));
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
                default -> {
                    var player = net.minecraft.client.Minecraft.getInstance().player;
                    float baseAttack = player == null ? 1F : (float) player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                    addLine(content, stat("tooltip.tconsnirvana.tool.attack", ToolCombat.applyDamageCutoff(
                            baseAttack + stats.getFloat("Attack"), antique.definition().action()), ChatFormatting.RED));
                }
            }
        }
        int remaining = tool.getAvailableModifierSlots(stack);
        if (remaining > 0) { addLine(content, stationText("remaining_modifiers").copy().append(" " + remaining)); }
    }

    private void showTraits(ItemStack stack, boolean resetScroll) {
        List<FormattedCharSequence> traitLines = new ArrayList<>();
        List<HoverEntry> tips = new ArrayList<>();
        for (var entry : ToolTraits.getTraits(stack)) {
            List<FormattedCharSequence> row = font.split(entry.trait().getDisplayName(entry.level()), getWidth() - 2 * PADDING);
            tips.add(new HoverEntry(traitLines.size(), traitLines.size() + row.size(), entry.trait().getDescription()));
            traitLines.addAll(row);
        }
        for (String id : ToolModifiers.entries().keySet()) {
            int units = ToolModifiers.units(stack, id);
            if (units > 0) {
                traitLines.addAll(font.split(Component.translatable("tooltip.tconsnirvana.modifier",
                        Component.translatable("modifier.tconsnirvana." + id), ToolModifiers.level(stack, id), units),
                        getWidth() - 2 * PADDING));
            }
        }
        lines = traitLines.isEmpty() ? font.split(stationText("no_traits"), getWidth() - 2 * PADDING) : traitLines;
        hoverEntries = tips;
        setScrollAmount(resetScroll ? 0 : scrollAmount());
    }

    private void appendRequiredParts(MutableComponent content, Mode mode) {
        var recipe = ToolStationAssemblyRecipes.get(mode);
        if (recipe == null) {
            return;
        }
        for (int i = 0; i < ToolStationMenu.INPUT_SLOT_COUNT; i++) {
            var part = recipe.getRequiredItem(i);
            if (part != null) {
                Component name = Component.literal(" * ").append(part.getDescription());
                if (!menu.getSlot(i + 1).getItem().is(part)) { name = name.copy().withStyle(ChatFormatting.RED); }
                addLine(content, name);
            }
        }
    }

    private static Component stat(String key, float value) {
        return stat(key, value, ChatFormatting.WHITE);
    }

    private static Component stat(String key, float value, ChatFormatting color) {
        return Component.translatable(key, Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value)).withStyle(color));
    }

    private static void addLine(MutableComponent content, Component line) {
        if (!content.getSiblings().isEmpty()) { content.append("\n"); }
        content.append(line);
    }

    Rect2i bounds() {
        return new Rect2i(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        return visible && x >= getX() && x < getX() + getWidth()
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
        return bodyOffset() + lines.size() * font.lineHeight;
    }

    @Override
    protected boolean scrollbarVisible() {
        return getMaxScrollAmount() > 0;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (isMouseOver(x, y) && scrollbarVisible() && x >= getX() + getWidth() - 7 && y >= getY() + bodyOffset()) {
            return super.mouseClicked(getX() + getWidth(), y, button);
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    protected void renderDecorations(GuiGraphics graphics) {
        if (!scrollbarVisible()) { return; }
        int u = menu.isForge() ? 12 : 6;
        int x = getX() + getWidth() - 6;
        int y = getY() + bodyOffset();
        int track = Math.max(5, getHeight() - bodyOffset() - PADDING);
        graphics.blit(TEXTURE, x, y, 3, track, u, 88, 3, 8, 256, 256);
        int handleY = y + (int) ((track - 5) * scrollAmount() / getMaxScrollAmount());
        graphics.blit(TEXTURE, x, handleY, u, 83, 3, 5);
    }

    @Override
    protected double scrollRate() {
        return font.lineHeight;
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int y = getY() + bodyOffset();
        graphics.enableScissor(getX() + BORDER, y, getX() + getWidth() - BORDER, getY() + getHeight() - BORDER);
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, getX() + PADDING, y, TEXT_COLOR, false);
            y += font.lineHeight;
        }
        graphics.disableScissor();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (bodyOffset() != PADDING) {
            graphics.enableScissor(getX() + BORDER, getY() + BORDER, getX() + getWidth() - BORDER, getY() + bodyOffset());
            Component caption = getMessage().copy().withStyle(ChatFormatting.UNDERLINE);
            graphics.drawString(font, caption, getX() + (getWidth() - font.width(caption)) / 2,
                    getY() + PADDING, TEXT_COLOR, false);
            if (!hoverEntries.isEmpty()) {
                graphics.drawString(font, "?", getX() + getWidth() - BORDER - font.width("?"), getY() + PADDING,
                        0xff8a8a8a, false);
            }
            graphics.disableScissor();
        }
    }

    void renderTraitTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) {
            return;
        }
        if (!hoverEntries.isEmpty() && mouseX >= getX() + getWidth() - BORDER - font.width("?")
                && mouseY >= getY() + PADDING && mouseY < getY() + bodyOffset()) {
            graphics.renderTooltip(font, font.split(stationText("hover_traits"), 180), mouseX, mouseY);
            return;
        }
        if (mouseY < getY() + bodyOffset() || mouseY >= getY() + getHeight() - BORDER) { return; }
        int row = (mouseY - getY() - bodyOffset() + (int) scrollAmount()) / font.lineHeight;
        for (HoverEntry entry : hoverEntries) {
            if (row >= entry.start() && row < entry.end() && !entry.text().getString().isBlank()) {
                graphics.renderTooltip(font, font.split(entry.text(), 180), mouseX, mouseY);
                return;
            }
        }
    }

    private int bodyOffset() {
        return getMessage().getString().isEmpty() ? PADDING : PADDING + CAPTION_HEIGHT;
    }

    private record HoverEntry(int start, int end, Component text) {}
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
