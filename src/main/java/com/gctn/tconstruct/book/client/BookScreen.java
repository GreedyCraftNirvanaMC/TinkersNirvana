/* SPDX-License-Identifier: LGPL-3.0-only
 * Mantle's MIT-licensed GuiBook, GuiArrow and ElementStructure adapted to 1.21.1.
 * Source attribution and MIT notice: third_party/Mantle-LICENSE.txt.
 */
package com.gctn.tconstruct.book.client;

import com.gctn.tconstruct.book.BookNetwork;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** Mantle's 206×200 paper leaves and cover, with native widgets and component-aware tooltips. */
public final class BookScreen extends Screen {
    private static final ResourceLocation BOOK = ResourceLocation.fromNamespaceAndPath("tconstruct", "textures/gui/book/mantle_book.png");
    private static final ResourceLocation FRONT = ResourceLocation.fromNamespaceAndPath("tconstruct", "textures/gui/book/mantle_bookfront.png");
    private static final ResourceLocation MODIFY = ResourceLocation.fromNamespaceAndPath("tconstruct", "textures/gui/book/modify.png");
    private static final int BOOK_WIDTH = 412, BOOK_HEIGHT = 200;
    private final BookDocument document;
    private final InteractionHand hand;
    private final int slot;
    private final Deque<Integer> history = new ArrayDeque<>();
    private final Map<String, ModelView> models = new HashMap<>();
    private final List<Hit> hits = new ArrayList<>();
    private final List<StructureControl> structureControls = new ArrayList<>();
    private final Map<String, String> searchIndex = new HashMap<>();
    private List<BookContents.Page> results = List.of();
    private BookButton previous, next, back, index, searchButton, coverButton;
    private EditBox search;
    private int spread = -1, resultPage, ticks;
    private float scale = 1, left, top;
    private boolean searching, closed;
    private ModelView dragging;
    private record Hit(float x, float y, float width, float height, String target, ItemStack item, Style style, Component label, ModelView model) {
        boolean contains(double mouseX, double mouseY) { return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height; }
    }
    private static final class ModelView { float yaw = -45, pitch = 25, zoom = 1; int layer = -1; boolean animate; }
    private record StructureControl(int side, Kind kind, BookButton button) {}

    private BookScreen(InteractionHand hand) {
        super(Component.translatable("item.tconstruct.book")); this.hand = hand;
        var player = Minecraft.getInstance().player; slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
        document = new BookDocument(Minecraft.getInstance().font, BookContents.pages());
        String saved = BookNetwork.savedPage(player.getItemInHand(hand)); if (!saved.isEmpty()) { spread = spreadFor(document.find(saved)); }
        for (var page : document.sources) { StringBuilder text = new StringBuilder(page.title().getString()); for (var line : page.text()) { text.append(' ').append(line.getString()); } searchIndex.put(page.id(), text.toString().toLowerCase(Locale.ROOT)); }
    }
    public static void open(InteractionHand hand) { Minecraft.getInstance().setScreen(new BookScreen(hand)); }
    private static int spreadFor(int page) { return page == 0 ? 0 : (page + 1) / 2; }
    private int lastSpread() { return document.leaves.size() / 2; }
    private BookDocument.Leaf leaf(int side) {
        int page = spread == 0 ? (side == 1 ? 0 : -1) : spread * 2 - 1 + side;
        return spread < 0 || page < 0 || page >= document.leaves.size() ? null : document.leaves.get(page);
    }
    private int paperX(int side) { return side == 0 ? 24 : 214; }
    @Override protected void init() {
        scale = Math.min(1, Math.min((width - 12F) / BOOK_WIDTH, (height - 28F) / BOOK_HEIGHT));
        left = (width - BOOK_WIDTH * scale) / 2; top = (height - BOOK_HEIGHT * scale) / 2;
        previous = button(Kind.PREV, 22, 175, "previous", () -> turn(-1));
        next = button(Kind.NEXT, 371, 175, "next", () -> turn(1));
        back = button(Kind.LEFT, 197, 202, "back", this::back);
        index = button(Kind.INDEX, 4, 3, "contents", () -> navigate("index"));
        searchButton = button(Kind.SEARCH, 390, 3, "search_button", this::toggleSearch);
        coverButton = button(Kind.COVER, 103, 0, "open", () -> { spread = 0; updateControls(); });
        String query = search == null ? "" : search.getValue();
        search = addRenderableWidget(new EditBox(font, px(24), py(30), Math.max(30, Math.round(174 * scale)), Math.max(14, Math.round(16 * scale)), Component.translatable("book.tconstruct.search")));
        search.setMaxLength(100); search.setHint(Component.translatable("book.tconstruct.search")); search.setValue(query); search.setResponder(value -> updateSearch());
        structureControls.clear();
        for (int side = 0; side < 2; side++) {
            final int pageSide = side;
            for (Kind kind : List.of(Kind.UP, Kind.DOWN, Kind.REFRESH)) {
                int x = paperX(side) + (kind == Kind.UP ? 154 : kind == Kind.DOWN ? 166 : 161), y = kind == Kind.REFRESH ? 1 : 23;
                var button = button(kind, x, y, kind == Kind.REFRESH ? "structure.animate" : "layer.all", () -> modelControl(pageSide, kind));
                structureControls.add(new StructureControl(side, kind, button));
            }
        }
        updateSearch(); updateControls();
    }
    private int px(float x) { return Math.round(left + x * scale); }
    private int py(float y) { return Math.round(top + y * scale); }
    private BookButton button(Kind kind, int x, int y, String message, Runnable action) { return addRenderableWidget(new BookButton(kind, x, y, Component.translatable("book.tconstruct." + message), action)); }
    private void updateSearch() {
        String query = search.getValue().strip().toLowerCase(Locale.ROOT); resultPage = 0;
        results = query.isEmpty() ? document.sources : document.sources.stream().filter(page -> searchIndex.get(page.id()).contains(query)).toList(); updateControls();
    }
    private void updateControls() {
        if (search == null) { return; }
        search.visible = searching; searchButton.visible = spread >= 0 || searching; coverButton.visible = spread < 0 && !searching;
        previous.visible = searching ? resultPage > 0 : spread >= 0;
        next.visible = searching ? (resultPage + 1) * 24 < results.size() : spread < lastSpread();
        next.setPosition(px(spread < 0 && !searching ? 286 : 371), py(175));
        index.visible = spread >= 0 || searching; back.visible = searching || !history.isEmpty();
        for (var control : structureControls) { var leaf = leaf(control.side); control.button.visible = !searching && leaf != null && structure(leaf) != null; }
    }
    private void turn(int direction) {
        if (searching) { resultPage = Mth.clamp(resultPage + direction, 0, Math.max(0, (results.size() - 1) / 24)); }
        else { spread = Mth.clamp(spread + direction, -1, lastSpread()); }
        dragging = null; updateControls();
    }
    private void navigate(String target) {
        if (history.size() == 32) { history.removeFirst(); } history.addLast(spread);
        searching = false; search.setFocused(false); spread = spreadFor(document.find(target)); dragging = null; updateControls();
    }
    private void back() { if (searching) { searching = false; search.setFocused(false); } else if (!history.isEmpty()) { spread = history.removeLast(); } updateControls(); }
    private void toggleSearch() { searching = !searching; updateControls(); if (searching) { setFocused(search); } search.setFocused(searching); }
    private static BookDocument.Structure structure(BookDocument.Leaf leaf) { return leaf.elements().stream().filter(BookDocument.Structure.class::isInstance).map(BookDocument.Structure.class::cast).findFirst().orElse(null); }
    private ModelView view(BookDocument.Leaf leaf) { return models.computeIfAbsent(leaf.id(), ignored -> new ModelView()); }
    private void modelControl(int side, Kind kind) {
        var leaf = leaf(side); if (leaf == null) { return; } var structure = structure(leaf); if (structure == null) { return; }
        var model = view(leaf);
        if (kind == Kind.REFRESH) { model.animate = !model.animate; }
        else { model.animate = false; model.layer += kind == Kind.UP ? 1 : -1; if (model.layer >= structure.value().height()) { model.layer = -1; } if (model.layer < -1) { model.layer = structure.value().height() - 1; } }
    }
    @Override public void tick() {
        ticks++;
        if (ticks % 30 == 0) { for (int side = 0; side < 2; side++) { var leaf = leaf(side); if (leaf != null && structure(leaf) != null && view(leaf).animate) { var model = view(leaf); if (++model.layer >= structure(leaf).value().height()) { model.layer = -1; } } } }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick); hits.clear();
        float mx = (mouseX - left) / scale, my = (mouseY - top) / scale;
        var pose = graphics.pose(); pose.pushPose(); pose.translate(left, top, 0); pose.scale(scale, scale, 1);
        if (spread < 0 && !searching) { cover(graphics); }
        else {
            tint(0xffffce85); graphics.blit(BOOK, 0, 0, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, 512, 512); tint(0xffffffff);
            for (int side = 0; side < 2; side++) {
                var leaf = leaf(side); if (!searching && leaf == null) { continue; }
                int x = side * 206; graphics.blit(BOOK, x, 0, x, 200, 206, 200, 512, 512);
                if (searching) { renderSearch(graphics, side, mx, my); } else { renderPage(graphics, leaf, side, mx, my); }
            }
        }
        graphics.flush(); pose.popPose();
        for (var widget : renderables) { widget.render(graphics, mouseX, mouseY, partialTick); }
        for (int i = hits.size() - 1; i >= 0; i--) {
            var hit = hits.get(i);
            if (!hit.contains(mx, my)) { continue; }
            if (hit.style != null && hit.style.getHoverEvent() != null) { graphics.renderComponentHoverEffect(font, hit.style, mouseX, mouseY); }
            else if (!hit.item.isEmpty()) { graphics.renderTooltip(font, hit.item, mouseX, mouseY); }
            else if (hit.label != null) { graphics.renderTooltip(font, hit.label, mouseX, mouseY); }
            break;
        }
    }
    private static void tint(int color) { RenderSystem.setShaderColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, (color >>> 24) / 255F); }
    private void cover(GuiGraphics graphics) {
        tint(0xffffce85); graphics.blit(FRONT, 103, 0, 0, 0, 206, 200, 512, 512); tint(0xffffffff);
        graphics.blit(FRONT, 103, 0, 0, 200, 206, 200, 512, 512);
        var pose = graphics.pose(); pose.pushPose(); pose.translate(211, 85, 1); pose.scale(2, 2, 1);
        graphics.drawCenteredString(font, Component.translatable("book.tconstruct.cover_title"), 0, 0, 0xffae8000); pose.popPose();
        graphics.drawCenteredString(font, Component.translatable("book.tconstruct.cover_subtitle"), 211, 170, 0xffae8000);
    }
    private void renderPage(GuiGraphics graphics, BookDocument.Leaf leaf, int side, float mouseX, float mouseY) {
        int x = paperX(side), y = 12; boolean structure = structure(leaf) != null;
        boolean outerStrip = side == 1 && leaf.id().startsWith("material:") && leaf.id().equals(leaf.source());
        drawTitle(graphics, leaf.title(), x, y, structure ? 148 : BookDocument.WIDTH);
        graphics.enableScissor(px(x), py(y), px(x + BookDocument.WIDTH), py(y + BookDocument.HEIGHT - 13));
        for (var element : leaf.elements()) {
            if (element instanceof BookDocument.Text text) {
                int tx = x + text.x() - (outerStrip ? 24 : 0), ty = y + text.y(); drawLine(graphics, text.line(), tx, ty);
                if (mouseY >= ty && mouseY < ty + 10 && mouseX >= tx && mouseX < x + BookDocument.WIDTH) {
                    Style style = font.getSplitter().componentStyleAtWidth(text.line(), (int) (mouseX - tx));
                    if (style != null && (style.getHoverEvent() != null || style.getClickEvent() != null)) { hits.add(new Hit(tx, ty, Math.max(1, font.width(text.line())), 10, style.getClickEvent() == null ? null : style.getClickEvent().getValue(), ItemStack.EMPTY, style, null, null)); }
                }
            } else if (element instanceof BookDocument.Icon icon) { item(graphics, current(icon.items()), x + (outerStrip ? BookDocument.WIDTH - 16 : icon.x()), y + icon.y(), 1); }
            else if (element instanceof BookDocument.Image image) { graphics.blit(image.texture(), x + image.x(), y + image.y(), image.width(), image.height(), 0, 0, image.textureWidth(), image.textureHeight(), image.textureWidth(), image.textureHeight()); }
            else if (element instanceof BookDocument.Link link) { link(graphics, x + link.x() - (outerStrip ? 24 : 0), y + link.y(), link.width(), link.label(), link.target(), link.icon(), mouseX, mouseY); }
            else if (element instanceof BookDocument.Section section) { section(graphics, x + section.x(), y + section.y(), section, mouseX, mouseY); }
            else if (element instanceof BookDocument.Diagram diagram) { diagram(graphics, x + diagram.x(), y + diagram.y(), diagram); }
            else if (element instanceof BookDocument.Structure model) { model(graphics, x + model.x(), y + model.y(), model, view(leaf)); }
        }
        graphics.flush(); graphics.disableScissor();
        int page = spread == 0 ? 1 : spread * 2 + side;
        graphics.drawCenteredString(font, Component.literal(Integer.toString(page)), x + BookDocument.WIDTH / 2, y + BookDocument.HEIGHT - 10, 0xff77736c);
    }
    private void drawTitle(GuiGraphics graphics, Component title, int x, int y, int width) {
        if (title.getString().isEmpty()) { return; }
        var line = font.split(title.copy().withStyle(net.minecraft.ChatFormatting.UNDERLINE), width).getFirst(); drawLine(graphics, line, x + (width - font.width(line)) / 2, y);
        hits.add(new Hit(x, y, width, 10, null, ItemStack.EMPTY, null, title, null));
    }
    private static Style paperStyle(Style style) {
        var color = style.getColor(); if (color == null) { return style; } int rgb = color.getValue(), r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        return r * .2126 + g * .7152 + b * .0722 > 150 ? style.withColor((r * 3 / 5 << 16) | (g * 3 / 5 << 8) | b * 3 / 5) : style;
    }
    private void drawLine(GuiGraphics graphics, FormattedCharSequence line, int x, int y) { graphics.drawString(font, sink -> line.accept((index, style, codePoint) -> sink.accept(index, paperStyle(style), codePoint)), x, y, 0xff201b16, false); }
    private ItemStack current(List<ItemStack> items) { return items.isEmpty() ? ItemStack.EMPTY : items.get(Math.floorMod(ticks / 30, items.size())); }
    private void item(GuiGraphics graphics, ItemStack item, int x, int y, float scale) {
        if (item.isEmpty()) { return; }
        var pose = graphics.pose(); pose.pushPose(); pose.translate(x, y, 0); pose.scale(scale, scale, 1); graphics.renderItem(item, 0, 0); pose.popPose();
        hits.add(new Hit(x, y, 16 * scale, 16 * scale, null, item, null, null, null));
    }
    private void link(GuiGraphics graphics, int x, int y, int width, Component label, String target, ItemStack icon, float mx, float my) {
        boolean hovered = mx >= x && mx < x + width && my >= y && my < y + 11;
        if (hovered) { graphics.fill(x, y, x + width, y + 11, 0x55ee541c); }
        int padding = icon.isEmpty() ? 0 : 11;
        if (!icon.isEmpty()) { item(graphics, icon, x, y, .6F); }
        graphics.drawString(font, font.plainSubstrByWidth(label.getString(), width - padding - 2), x + padding, y + 1, hovered ? 0xff000000 : 0xff453220, false);
        hits.add(new Hit(x, y, width, 11, target, ItemStack.EMPTY, null, label, null));
    }
    private void section(GuiGraphics graphics, int x, int y, BookDocument.Section section, float mx, float my) {
        if (mx >= x && mx < x + 42 && my >= y && my < y + 42) { graphics.fill(x + 5, y + 3, x + 37, y + 35, 0x77ee541c); }
        item(graphics, section.icon(), x + 5, y + 3, 2);
        var lines = font.split(section.label(), 45); int lineY = y + 36;
        for (var line : lines) { drawLine(graphics, line, x + 21 - font.width(line) / 2, lineY); lineY += 10; }
        hits.add(new Hit(x, y, 42, Math.max(42, lineY - y), section.target(), ItemStack.EMPTY, null, section.label(), null));
    }
    private void diagram(GuiGraphics graphics, int x, int y, BookDocument.Diagram diagram) {
        if (diagram.modifier()) {
            int count = diagram.inputs().size(), width = count < 4 ? 4 + 18 * Math.max(1, count) : 58, height = count < 4 ? 22 : 41;
            int cx = x + 29, cy = y + 12, sx = x + 37 - width / 2, sy = y + 41;
            graphics.blit(MODIFY, cx - 13, cy, 214, 0, 42, 46, 256, 256);
            tint(0xffb3864d); graphics.blit(MODIFY, sx, sy, 0, count <= 1 ? 75 : count == 2 ? 97 : count == 3 ? 119 : 141, width, height, 256, 256); tint(0xffffffff);
            graphics.blit(MODIFY, cx - 3, cy - 3, 0, 75, 22, 22, 256, 256); item(graphics, diagram.tool(), cx, cy, 1);
            int[] dx = {3, 21, 39, 12, 30}, dy = {3, 3, 3, 22, 22};
            for (int i = 0; i < Math.min(5, count); i++) { item(graphics, current(diagram.inputs().get(i)), sx + dx[i], sy + dy[i], 1); }
        } else {
            int cx = x + 28, cy = y + 28;
            graphics.blit(MODIFY, x + 15, y + 28, 214, 0, 42, 46, 256, 256);
            tint(0xffb3864d); graphics.blit(MODIFY, x, y, 0, 0, 72, 72, 256, 256); tint(0xffffffff);
            graphics.blit(MODIFY, cx - 3, cy - 3, 0, 75, 22, 22, 256, 256); item(graphics, diagram.tool(), cx, cy, 1);
            int[] dx = {-21, -25, 0, 25, 21}, dy = {22, -4, -25, -4, 22};
            for (int i = 0; i < Math.min(5, diagram.inputs().size()); i++) { item(graphics, current(diagram.inputs().get(i)), cx + dx[i], cy + dy[i], 1); }
        }
    }
    private void model(GuiGraphics graphics, int x, int y, BookDocument.Structure element, ModelView view) {
        var model = element.value(); graphics.flush(); Lighting.setupFor3DItems(); var pose = graphics.pose(); pose.pushPose();
        float radius = (float) Math.sqrt(model.width() * model.width() + model.depth() * model.depth());
        float scale = Math.min((element.width() - 20) / radius, (element.height() - 24F) / (model.height() + radius * .5F)) * view.zoom;
        pose.translate(x + element.width() / 2F, y + element.height() / 2F, 100); pose.scale(scale, -scale, scale);
        pose.mulPose(Axis.XP.rotationDegrees(view.pitch)); pose.mulPose(Axis.YP.rotationDegrees(view.yaw)); pose.translate(-model.width() / 2F, -model.height() / 2F, -model.depth() / 2F);
        for (var block : model.blocks()) { if (view.layer >= 0 && block.y() > view.layer) { continue; } pose.pushPose(); pose.translate(block.x(), block.y(), block.z()); minecraft.getBlockRenderer().renderSingleBlock(block.state(), pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY); pose.popPose(); }
        graphics.flush(); pose.popPose(); Lighting.setupForFlatItems();
        graphics.drawCenteredString(font, Component.translatable(view.layer < 0 ? "book.tconstruct.layer.all" : "book.tconstruct.layer", view.layer + 1), x + element.width() / 2, y + element.height() - 8, 0xff776342);
        hits.add(new Hit(x, y, element.width(), element.height() - 10, null, ItemStack.EMPTY, null, Component.translatable("book.tconstruct.structure.controls"), view));
    }
    private void renderSearch(GuiGraphics graphics, int side, float mx, float my) {
        int x = paperX(side); drawTitle(graphics, Component.translatable("book.tconstruct.search_results"), x, 12, BookDocument.WIDTH);
        int first = resultPage * 24 + side * 12;
        if (results.isEmpty() && side == 0) { graphics.drawWordWrap(font, Component.translatable("book.tconstruct.empty_search"), x, 56, BookDocument.WIDTH, 0xff453220); }
        for (int i = first; i < Math.min(results.size(), first + 12); i++) { var page = results.get(i); link(graphics, x, 49 + (i - first) * 10, BookDocument.WIDTH, page.title(), page.id(), page.icons().isEmpty() ? ItemStack.EMPTY : page.icons().getFirst(), mx, my); }
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) { return true; }
        double mx = (x - left) / scale, my = (y - top) / scale;
        for (int i = hits.size() - 1; i >= 0; i--) {
            var hit = hits.get(i); if (!hit.contains(mx, my)) { continue; }
            if (button == 0 && hit.target != null) { navigate(hit.target); return true; }
            if (hit.model != null) { if (button == 0) { dragging = hit.model; } else if (button == 1) { hit.model.pitch = 25; hit.model.yaw = -45; hit.model.zoom = 1; } return true; }
        }
        return false;
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) { if (dragging != null && button == 0) { dragging.yaw += (float) dx / scale; dragging.pitch = Mth.clamp(dragging.pitch + (float) dy / scale, -80, 80); return true; } return super.mouseDragged(x, y, button, dx, dy); }
    @Override public boolean mouseReleased(double x, double y, int button) { dragging = null; return super.mouseReleased(x, y, button); }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        double mx = (x - left) / scale, my = (y - top) / scale;
        for (var hit : hits) { if (hit.model != null && hit.contains(mx, my)) { hit.model.zoom = Mth.clamp(hit.model.zoom + (float) dy * .1F, .5F, 2); return true; } }
        if (dy != 0) { turn(dy < 0 ? 1 : -1); } return true;
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == 256 && searching) { back(); return true; }
        if (key == 70 && hasControlDown()) { toggleSearch(); return true; }
        if (!search.isFocused()) {
            if (key == 262 || key == 267 || key == 68) { turn(1); return true; }
            if (key == 263 || key == 266 || key == 65) { turn(-1); return true; }
            if (key == 268) { navigate("index"); return true; }
        }
        return super.keyPressed(key, scan, modifiers);
    }
    @Override public void removed() {
        if (!closed) { closed = true; var leaf = leaf(spread == 0 ? 1 : 0); PacketDistributor.sendToServer(new BookNetwork.SavePage(hand, slot, leaf == null ? "" : leaf.id())); } super.removed();
    }
    private enum Kind {
        NEXT(412, 0, 18, 10), PREV(412, 10, 18, 10), LEFT(412, 30, 18, 10), INDEX(412, 40, 18, 18), UP(412, 58, 10, 18), DOWN(422, 58, 10, 18), REFRESH(412, 76, 18, 18), SEARCH(0, 0, 14, 14), COVER(0, 0, 206, 200);
        final int u, v, width, height; Kind(int u, int v, int width, int height) { this.u = u; this.v = v; this.width = width; this.height = height; }
    }
    private final class BookButton extends AbstractButton {
        final Kind kind; final Runnable action;
        BookButton(Kind kind, int x, int y, Component message, Runnable action) { super(px(x), py(y), Math.max(1, Math.round(kind.width * scale)), Math.max(1, Math.round(kind.height * scale)), message); this.kind = kind; this.action = action; setTooltip(Tooltip.create(message)); }
        @Override public void onPress() { action.run(); }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (kind == Kind.COVER) { return; }
            if (kind == Kind.SEARCH) {
                int color = isHoveredOrFocused() ? 0xffff541c : 0xffd6aa63;
                graphics.fill(getX() + 2, getY() + 1, getX() + 9, getY() + 8, color); graphics.fill(getX() + 3, getY() + 2, getX() + 8, getY() + 7, 0xff514030); graphics.fill(getX() + 8, getY() + 8, getX() + 12, getY() + 10, color); return;
            }
            tint(isHoveredOrFocused() ? 0xffff541c : 0xffffffd3); graphics.blit(BOOK, getX(), getY(), getWidth(), getHeight(), kind.u, kind.v, kind.width, kind.height, 512, 512); tint(0xffffffff);
        }
    }
}
