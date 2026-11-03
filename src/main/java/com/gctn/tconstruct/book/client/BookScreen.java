/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.book.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native widgets, searchable contents, live material pages, and layer-by-layer multiblock previews. */
public class BookScreen extends Screen {
    private static String lastPage = "";
    private final List<BookContents.Page> pages = BookContents.pages();
    private final java.util.Map<String, String> searchIndex = new java.util.HashMap<>();
    private List<BookContents.Page> visible = pages;
    private List<FormattedCharSequence> lines = List.of();
    private EditBox search;
    private Button previous, next, layerButton;
    private int left, top, panelWidth, panelHeight, bodyX, bodyY, bodyWidth, bodyHeight, selected, scroll, listScroll, contentHeight, layer = -1;
    private String section = "";
    private float angle = -45;
    private boolean draggingModel;
    private final List<Icon> icons = new ArrayList<>();
    private record Icon(ItemStack stack, int x, int y) {}
    public BookScreen() {
        super(Component.translatable("item.tconstruct.book"));
        for (int i = 0; i < pages.size(); i++) { var page = pages.get(i); StringBuilder text = new StringBuilder(page.title().getString()); for (var line : page.text()) { text.append(' ').append(line.getString()); } searchIndex.put(page.id(), text.toString().toLowerCase(Locale.ROOT)); if (page.id().equals(lastPage)) { selected = i; } }
    }
    public static void open() { Minecraft.getInstance().setScreen(new BookScreen()); }
    @Override protected void init() {
        panelWidth = Math.min(width - 12, 620); panelHeight = Math.min(height - 12, 420); left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        int sidebar = Math.min(128, panelWidth / 3); bodyX = left + sidebar + 12; bodyY = top + 49; bodyWidth = panelWidth - sidebar - 26; bodyHeight = panelHeight - 85;
        String query = search == null ? "" : search.getValue();
        search = addRenderableWidget(new EditBox(font, left + 9, top + 26, panelWidth - 18, 18, Component.translatable("book.tconstruct.search")));
        search.setMaxLength(100); search.setHint(Component.translatable("book.tconstruct.search")); search.setValue(query); search.setResponder(value -> filter());
        int buttonWidth = (panelWidth - 24) / 3;
        addRenderableWidget(Button.builder(Component.translatable("book.tconstruct.contents"), button -> { section = ""; search.setValue(""); filter(); }).bounds(left + 8, top + panelHeight - 26, buttonWidth, 20).build());
        previous = addRenderableWidget(Button.builder(Component.literal("◀"), button -> select(selected - 1)).bounds(left + 12 + buttonWidth, top + panelHeight - 26, buttonWidth, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal("▶"), button -> select(selected + 1)).bounds(left + 16 + buttonWidth * 2, top + panelHeight - 26, buttonWidth, 20).build());
        layerButton = addRenderableWidget(Button.builder(Component.empty(), button -> { var page = page(); if (page != null && page.structure() != null) { layer++; if (layer >= page.structure().height()) { layer = -1; } updateLayer(); } }).bounds(bodyX + bodyWidth - 90, bodyY + 15, 86, 18).build());
        filter();
    }
    private BookContents.Page page() { return visible.isEmpty() ? null : visible.get(Mth.clamp(selected, 0, visible.size() - 1)); }
    private void filter() {
        String current = page() == null ? lastPage : page().id(); String query = search.getValue().strip().toLowerCase(Locale.ROOT);
        visible = pages.stream().filter(page -> section.isEmpty() || section.equals(page.section())).filter(page -> query.isEmpty() || searchIndex.get(page.id()).contains(query)).toList();
        selected = 0; for (int i = 0; i < visible.size(); i++) { if (visible.get(i).id().equals(current)) { selected = i; break; } }
        scroll = listScroll = 0; layout();
    }
    private void select(int index) { if (index >= 0 && index < visible.size()) { selected = index; scroll = 0; layer = -1; layout(); } }
    private void layout() {
        var page = page(); lines = new ArrayList<>(); icons.clear();
        if (page != null) { for (Component paragraph : page.text()) { lines.addAll(font.split(paragraph, bodyWidth - 12)); if (paragraph.getString().isEmpty()) { lines.add(FormattedCharSequence.EMPTY); } } lastPage = page.id(); }
        int media = page == null ? 0 : page.structure() != null ? 150 : page.image() != null ? imageHeight(page) + 10 : 0;
        int iconRows = page == null || page.icons().isEmpty() ? 0 : (page.icons().size() + Math.max(1, (bodyWidth - 10) / 20) - 1) / Math.max(1, (bodyWidth - 10) / 20);
        contentHeight = media + iconRows * 20 + lines.size() * 11 + 20;
        previous.active = selected > 0; next.active = selected + 1 < visible.size(); updateLayer();
    }
    private void updateLayer() { var page = page(); layerButton.visible = page != null && page.structure() != null; layerButton.setMessage(Component.translatable(layer < 0 ? "book.tconstruct.layer.all" : "book.tconstruct.layer", layer + 1)); }
    private int imageHeight(BookContents.Page page) {
        var image = page.image(); float width = image.has("width") ? image.get("width").getAsFloat() : 150, height = image.has("height") ? image.get("height").getAsFloat() : 90;
        return Math.max(1, Math.round(Math.min(bodyWidth - 12, 260) * height / width));
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xff514332);
        graphics.fill(left + 3, top + 3, left + panelWidth - 3, top + panelHeight - 3, 0xffeee2c7);
        graphics.fill(bodyX - 7, bodyY - 2, bodyX - 6, bodyY + bodyHeight, 0xffc1ae8b);
        graphics.drawString(font, title, left + 10, top + 10, 0xff3d3024, false);
        var page = page();
        if (page != null) { graphics.drawString(font, (selected + 1) + " / " + visible.size(), left + panelWidth - 55, top + 10, 0xff66533e, false); }
        renderContents(graphics, mouseX, mouseY);
        graphics.enableScissor(bodyX, bodyY, bodyX + bodyWidth, bodyY + bodyHeight);
        int y = bodyY - scroll;
        if (page == null) { graphics.drawWordWrap(font, Component.translatable("book.tconstruct.empty_search"), bodyX, y, bodyWidth, 0xff5b4b39); }
        else {
            for (var line : font.split(page.title(), bodyWidth - 8)) { graphics.drawString(font, line, bodyX, y, 0xff3c2b1c, false); y += 11; }
            y += 7;
            if (page.structure() != null) { renderStructure(graphics, page.structure(), y); y += 150; }
            else if (page.image() != null) {
                var image = page.image(); ResourceLocation texture = ResourceLocation.tryParse(image.get("file").getAsString());
                int w = Math.min(bodyWidth - 12, 260), h = imageHeight(page), tw = image.has("texture_width") ? image.get("texture_width").getAsInt() : w, th = image.has("texture_height") ? image.get("texture_height").getAsInt() : h;
                if (texture != null) { graphics.blit(texture, bodyX, y, w, h, 0, 0, tw, th, tw, th); } y += h + 10;
            }
            icons.clear(); int column = 0, columns = Math.max(1, (bodyWidth - 10) / 20), iconY = y;
            for (ItemStack stack : page.icons()) { int x = bodyX + column * 20; graphics.renderItem(stack, x, iconY); icons.add(new Icon(stack, x, iconY)); if (++column >= columns) { column = 0; iconY += 20; } }
            if (!page.icons().isEmpty()) { y = iconY + (column > 0 ? 20 : 0) + 3; }
            for (var line : lines) { graphics.drawString(font, line, bodyX, y, 0xff473626, false); y += 11; }
            contentHeight = y - bodyY + scroll + 6;
        }
        graphics.disableScissor();
        if (contentHeight > bodyHeight) { int thumb = Math.max(12, bodyHeight * bodyHeight / contentHeight), offset = (bodyHeight - thumb) * scroll / Math.max(1, contentHeight - bodyHeight); graphics.fill(bodyX + bodyWidth - 3, bodyY + offset, bodyX + bodyWidth - 1, bodyY + offset + thumb, 0xff9b8057); }
        for (var widget : renderables) { widget.render(graphics, mouseX, mouseY, partialTick); }
        if (mouseY >= bodyY && mouseY < bodyY + bodyHeight) { for (Icon icon : icons) { if (mouseX >= icon.x && mouseX < icon.x + 16 && mouseY >= icon.y && mouseY < icon.y + 16) { graphics.renderTooltip(font, icon.stack, mouseX, mouseY); } } }
    }
    private void renderContents(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = left + 8, width = bodyX - x - 14, y = bodyY - listScroll;
        graphics.enableScissor(x, bodyY, x + width, bodyY + bodyHeight);
        if (section.isEmpty() && search.getValue().isBlank()) {
            var sections = pages.stream().map(BookContents.Page::section).distinct().toList();
            for (String name : sections) { if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 24) { graphics.fill(x, y, x + width, y + 24, 0x22906c35); } graphics.drawWordWrap(font, Component.translatable("book.tconstruct.section." + name), x + 2, y + 5, width - 4, 0xff57402a); y += 24; }
        } else {
            for (int i = 0; i < visible.size(); i++) {
                if (i == selected) { graphics.fill(x, y, x + width, y + 24, 0x33906c35); }
                var title = font.split(visible.get(i).title(), width - 4); for (int line = 0; line < Math.min(2, title.size()); line++) { graphics.drawString(font, title.get(line), x + 2, y + 2 + line * 10, 0xff57402a, false); } y += 24;
            }
        }
        graphics.disableScissor();
    }
    private void renderStructure(GuiGraphics graphics, BookContents.Structure structure, int y) {
        graphics.flush(); Lighting.setupFor3DItems();
        var pose = graphics.pose(); pose.pushPose();
        pose.translate(bodyX + bodyWidth / 2F, y + 82, 200);
        float scale = Math.min(18, (bodyWidth - 12F) / (structure.width() + structure.depth()));
        pose.scale(scale, -scale, scale); pose.mulPose(Axis.XP.rotationDegrees(28)); pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-structure.width() / 2F, -structure.height() / 2F, -structure.depth() / 2F);
        for (var block : structure.blocks()) {
            if (layer >= 0 && block.y() > layer) { continue; }
            pose.pushPose(); pose.translate(block.x(), block.y(), block.z());
            minecraft.getBlockRenderer().renderSingleBlock(block.state(), pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        graphics.flush(); pose.popPose(); Lighting.setupForFlatItems();
        graphics.drawWordWrap(font, Component.translatable("book.tconstruct.structure_hint"), bodyX, y + 120, bodyWidth - 8, 0xff75634b);
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) { return true; }
        if (button == 0 && x >= left + 8 && x < bodyX - 12 && y >= bodyY && y < bodyY + bodyHeight) {
            int row = (int) (y - bodyY + listScroll) / 24;
            if (section.isEmpty() && search.getValue().isBlank()) { var sections = pages.stream().map(BookContents.Page::section).distinct().toList(); if (row < sections.size()) { section = sections.get(row); filter(); } }
            else { select(row); }
            return true;
        }
        if (button == 0 && page() != null && page().structure() != null && x >= bodyX && x < bodyX + bodyWidth && y >= bodyY && y < bodyY + Math.min(bodyHeight, 140 - scroll)) { draggingModel = true; return true; }
        return false;
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) { if (draggingModel && button == 0) { angle += (float) dx * 2; return true; } return super.mouseDragged(x, y, button, dx, dy); }
    @Override public boolean mouseReleased(double x, double y, int button) { draggingModel = false; return super.mouseReleased(x, y, button); }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (x < bodyX) { int rows = section.isEmpty() && search.getValue().isBlank() ? (int) pages.stream().map(BookContents.Page::section).distinct().count() : visible.size(); listScroll = Mth.clamp(listScroll - (int) (dy * 24), 0, Math.max(0, rows * 24 - bodyHeight)); }
        else { scroll = Mth.clamp(scroll - (int) (dy * 24), 0, Math.max(0, contentHeight - bodyHeight)); }
        return true;
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (!search.isFocused()) { if (key == 262 || key == 267) { select(selected + 1); return true; } if (key == 263 || key == 266) { select(selected - 1); return true; } }
        return super.keyPressed(key, scan, modifiers);
    }
}
