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
    private final Map<String, ModelView> models = new HashMap<>();
    private final List<Hit> hits = new ArrayList<>();
    private final List<StructureControl> structureControls = new ArrayList<>();
    private BookButton previous, next, back, index, coverButton;

    private int spread = -1, returnSpread = -2, frames;
    private float scale = 1, left, top;
    private boolean closed;
    private ModelView dragging;
    private record Hit(float x, float y, float width, float height, String target, ItemStack item, Style style, Component label, ModelView model) {
        boolean contains(double mouseX, double mouseY) { return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height; }
    }
    private static final class ModelView { float yaw = -45, pitch = 25; int limit = Integer.MAX_VALUE, frame, fullSteps = 5; boolean animate; }
    private record StructureControl(int side, Kind kind, BookButton button) {}

    private BookScreen(InteractionHand hand) {
        this(hand, hand == InteractionHand.MAIN_HAND ? Minecraft.getInstance().player.getInventory().selected : 40,
                new BookDocument(Minecraft.getInstance().font, BookContents.pages()), BookNetwork.savedPage(Minecraft.getInstance().player.getItemInHand(hand)));
    }
    BookScreen(InteractionHand hand, int slot, BookDocument document, String saved) {
        super(Component.translatable("item.tconstruct.book")); this.hand = hand; this.slot = slot; this.document = document;
        if (!saved.isEmpty()) { spread = spreadFor(document.find(saved)); }
    }
    public static void open(InteractionHand hand) { Minecraft.getInstance().setScreen(new BookScreen(hand)); }
    private static int spreadFor(int page) { return page == 0 ? 0 : (page + 1) / 2; }
    private int lastSpread() { return document.leaves.size() / 2; }
    private BookDocument.Leaf leaf(int side) {
        int page = spread == 0 ? (side == 1 ? 0 : -1) : spread * 2 - 1 + side;
        return spread < 0 || page < 0 || page >= document.leaves.size() ? null : document.leaves.get(page);
    }
    private int paperX(int side) { return side == 0 ? 16 : 214; }
    @Override protected void init() {
        scale = Math.min(1, Math.min((width - 12F) / BOOK_WIDTH, (height - 28F) / BOOK_HEIGHT));
        left = (float) Math.floor(width / 2F) - BOOK_WIDTH * scale / 2; top = (float) Math.floor(height / 2F) - BOOK_HEIGHT * scale / 2;
        previous = button(Kind.PREV, 22, 175, "previous", () -> turn(-1));
        next = button(Kind.NEXT, 371, 175, "next", () -> turn(1));
        back = button(Kind.LEFT, 197, 193, "back", this::back);
        index = button(Kind.INDEX, -9, 0, "contents", () -> { returnSpread = -2; navigate("go-to-page:index"); });
        coverButton = button(Kind.COVER, 103, 0, "open", () -> { spread = 0; updateControls(); });
        structureControls.clear();
        for (int side = 0; side < 2; side++) {
            final int pageSide = side;
            var button = button(Kind.REFRESH, paperX(side) + BookDocument.WIDTH - 18, 12, "structure.animate", () -> modelControl(pageSide));
            structureControls.add(new StructureControl(side, Kind.REFRESH, button));
        }
        updateControls();
    }
    private int px(float x) { return Math.round(left + x * scale); }
    private int py(float y) { return Math.round(top + y * scale); }
    private BookButton button(Kind kind, int x, int y, String message, Runnable action) { return addRenderableWidget(new BookButton(kind, x, y, Component.translatable("book.tconstruct." + message), action)); }
    private void updateControls() {
        frames = 0;
        coverButton.visible = spread < 0; previous.visible = spread >= 0; next.visible = spread < lastSpread();
        next.setPosition(px(spread < 0 ? 286 : 371), py(175));
        index.visible = spread > 0; back.visible = returnSpread >= -1;
        for (var control : structureControls) { var leaf = leaf(control.side); control.button.visible = leaf != null && structure(leaf) != null && structure(leaf).value().height() > 1; }
    }
    private void turn(int direction) {
        spread = Mth.clamp(spread + direction, -1, lastSpread());
        returnSpread = -2; dragging = null; updateControls();
    }
    private void navigate(String target) {
        boolean returner = !target.startsWith("go-to-page:");
        for (String prefix : List.of("go-to-page-rtn:", "go-to-page:")) { if (target.startsWith(prefix)) { target = target.substring(prefix.length()); break; } }
        if (returner) { returnSpread = spread; }
        spread = spreadFor(document.find(target)); dragging = null; updateControls();
    }
    private void back() { if (returnSpread >= -1) { spread = returnSpread; } returnSpread = -2; updateControls(); }
    private static BookDocument.Structure structure(BookDocument.Leaf leaf) { return leaf.elements().stream().filter(BookDocument.Structure.class::isInstance).map(BookDocument.Structure.class::cast).findFirst().orElse(null); }
    private ModelView view(BookDocument.Leaf leaf) { return models.computeIfAbsent(leaf.id(), ignored -> new ModelView()); }
    private void modelControl(int side) {
        var leaf = leaf(side); if (leaf == null) { return; } var structure = structure(leaf); if (structure == null) { return; }
        var model = view(leaf);
        model.animate = !model.animate;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        frames++;
        renderBackground(graphics, mouseX, mouseY, partialTick); hits.clear();
        float mx = (mouseX - left) / scale, my = (mouseY - top) / scale;
        var pose = graphics.pose(); pose.pushPose(); pose.translate(left, top, 0); pose.scale(scale, scale, 1);
        if (spread < 0) { cover(graphics); }
        else {
            tint(0xffffce85); graphics.blit(BOOK, 0, 0, 0, 0, BOOK_WIDTH, BOOK_HEIGHT, 512, 512); tint(0xffffffff);
            for (int side = 0; side < 2; side++) {
                var leaf = leaf(side); if (leaf == null) { continue; }
                int x = side * 206; graphics.blit(BOOK, x, 0, x, 200, 206, 200, 512, 512);
                renderPage(graphics, leaf, side, mx, my);
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
        var title = BookContents.paper(Component.translatable("book.tconstruct.cover_title")); float size = font.width(title) <= 67 ? 2.5F : 2;
        var pose = graphics.pose(); pose.pushPose(); pose.translate(206 + 3 * size, 95.5F - 4 * size, 1); pose.scale(size, size, 1);
        graphics.drawString(font, title, -font.width(title) / 2, 0, 0xffae8000, true); pose.popPose();
        var subtitle = BookContents.paper(Component.translatable("book.tconstruct.cover_subtitle"));
        pose.pushPose(); pose.translate(216.5F, 182, 1); pose.scale(1.5F, 1.5F, 1);
        graphics.drawString(font, subtitle, -font.width(subtitle) / 2, 0, 0xffae8000, true); pose.popPose();
    }
    private void renderPage(GuiGraphics graphics, BookDocument.Leaf leaf, int side, float mouseX, float mouseY) {
        int x = paperX(side), y = 12;
        var source = document.sourceById.get(leaf.source());
        boolean tinker = source == null || source.data().has("modifier") || source.id().startsWith("modifier:");
        drawTitle(graphics, leaf.title(), x, y, BookDocument.WIDTH, tinker, tinker && source != null);
        // The centered book may start on a half GUI pixel. Round the clip outwards to retain edge pixels.
        graphics.enableScissor((int) Math.floor(left + x * scale), (int) Math.floor(top + y * scale),
                (int) Math.ceil(left + (x + BookDocument.WIDTH) * scale), (int) Math.ceil(top + (y + BookDocument.HEIGHT) * scale));
        for (var element : leaf.elements(side == 1)) {
            if (element instanceof BookDocument.Text text) {
                int tx = x + text.x(), ty = y + text.y();
                graphics.pose().pushPose(); graphics.pose().translate(tx, ty, 0); graphics.pose().scale(text.scale(), text.scale(), 1);
                graphics.drawString(font, text.line(), 0, 0, 0xff000000, text.shadow()); graphics.pose().popPose();
                if (mouseY >= ty && mouseY < ty + 9 * text.scale() && mouseX >= tx && mouseX < tx + font.width(text.line()) * text.scale()) {
                    Style style = font.getSplitter().componentStyleAtWidth(text.line(), (int) ((mouseX - tx) / text.scale()));
                    if (style != null && (style.getHoverEvent() != null || style.getClickEvent() != null)) { hits.add(new Hit(tx, ty, Math.max(1, font.width(text.line()) * text.scale()), 9 * text.scale(), style.getClickEvent() == null ? null : style.getClickEvent().getValue(), ItemStack.EMPTY, style, null, null)); }
                }
            } else if (element instanceof BookDocument.Icon icon) { item(graphics, current(icon.items()), x + icon.x(), y + icon.y(), icon.scale(), icon.tooltip()); }
            else if (element instanceof BookDocument.Image image) { graphics.blit(image.texture(), x + image.x(), y + image.y(), image.width(), image.height(), 0, 0, image.textureWidth(), image.textureHeight(), image.textureWidth(), image.textureHeight()); }
            else if (element instanceof BookDocument.Link link) { link(graphics, x + link.x(), y + link.y(), link.width(), link.label(), link.target(), link.icon(), mouseX, mouseY); }
            else if (element instanceof BookDocument.Tile tile) {
                int tx = x + tile.x(), ty = y + tile.y();
                if (mouseX >= tx && mouseX < tx + tile.size() && mouseY >= ty && mouseY < ty + tile.size()) { graphics.fill(tx, ty, tx + tile.size(), ty + tile.size(), 0x77ee541c); }
                int offset = (int) (tile.scale() * 2); item(graphics, tile.icon(), tx + offset, ty + offset, tile.scale(), tile.label());
                hits.add(new Hit(tx, ty, tile.size(), tile.size(), tile.target(), ItemStack.EMPTY, null, tile.label(), null));
            }
            else if (element instanceof BookDocument.Section section) { section(graphics, x + section.x(), y + section.y(), section, mouseX, mouseY); }
            else if (element instanceof BookDocument.Diagram diagram) { diagram(graphics, x + diagram.x(), y + diagram.y(), diagram); }
            else if (element instanceof BookDocument.Structure model) { model(graphics, x + model.x(), y + model.y(), model, view(leaf)); }
        }
        graphics.flush(); graphics.disableScissor();
        int page = spread == 0 ? 1 : spread * 2 + side;
        var number = BookContents.paper(Component.literal(Integer.toString(page)));
        graphics.drawString(font, number, x + (BookDocument.WIDTH - font.width(number)) / 2, y + BookDocument.HEIGHT - 10, 0xffaaaaaa, false);
    }
    private void drawTitle(GuiGraphics graphics, Component title, int x, int y, int width, boolean centered, boolean shadow) {
        if (title.getString().isEmpty()) { return; }
        float size = centered ? 1.2F : 1;
        var line = font.split(BookContents.paper(title.copy().withStyle(net.minecraft.ChatFormatting.UNDERLINE)), (int) (width / size)).getFirst();
        var pose = graphics.pose(); pose.pushPose(); pose.translate(x + (centered ? (width - font.width(line) * size) / 2 : 0), y, 0); pose.scale(size, size, 1);
        graphics.drawString(font, line, 0, 0, 0xff000000, shadow); pose.popPose();
    }
    private void drawLine(GuiGraphics graphics, FormattedCharSequence line, int x, int y) { graphics.drawString(font, line, x, y, 0xff000000, false); }
    private ItemStack current(List<ItemStack> items) { return items.isEmpty() ? ItemStack.EMPTY : items.get(Math.floorMod(frames / 91, items.size())); }
    private void item(GuiGraphics graphics, ItemStack item, int x, int y, float scale) {
        item(graphics, item, x, y, scale, null);
    }
    private void item(GuiGraphics graphics, ItemStack item, int x, int y, float scale, Component tooltip) {
        if (item.isEmpty()) { return; }
        var pose = graphics.pose(); pose.pushPose(); pose.translate(x, y, 0); pose.scale(scale, scale, 1); graphics.renderItem(item, 0, 0); pose.popPose();
        hits.add(new Hit(x, y, 16 * scale, 16 * scale, null, tooltip == null ? item : ItemStack.EMPTY, null, tooltip, null));
    }
    private void link(GuiGraphics graphics, int x, int y, int width, Component label, String target, ItemStack icon, float mx, float my) {
        boolean hovered = mx >= x && mx < x + width && my >= y && my < y + 9;
        var text = BookContents.paper(Component.literal(hovered ? " > " : "   ").append(label).withColor(hovered ? 0x880000 : 0));
        var lines = font.split(text, width); if (!lines.isEmpty()) { drawLine(graphics, lines.getFirst(), x, y); }
        hits.add(new Hit(x, y, width, 9, target, ItemStack.EMPTY, null, label, null));
    }
    private void section(GuiGraphics graphics, int x, int y, BookDocument.Section section, float mx, float my) {
        if (mx >= x && mx < x + 42 && my >= y && my < y + 42) { graphics.fill(x + 5, y + 3, x + 37, y + 35, 0x77ee541c); }
        item(graphics, section.icon(), x + 5, y + 5, 2);
        var label = BookContents.paper(section.label());
        graphics.drawString(font, label, x + 21 - font.width(label) / 2, y + 38, mx >= x && mx < x + 42 && my >= y && my < y + 42 ? 0xff000000 : 0x7f000000, false);
        hits.add(new Hit(x, y, 42, 46, "go-to-page:" + section.target(), ItemStack.EMPTY, null, section.label(), null));
    }
    private void diagram(GuiGraphics graphics, int x, int y, BookDocument.Diagram diagram) {
        if (diagram.modifier()) {
            int count = diagram.inputs().size(), width = count < 4 ? 4 + 18 * Math.max(1, count) : 58, height = count < 4 ? 22 : 41;
            int sx = x + 29 - width / 2, sy = y + 20 - height / 2, cx = sx + (width - 16) / 2, cy = sy - 24;
            graphics.blit(MODIFY, sx + (width - 42) / 2, cy, 214, 0, 42, 46, 256, 256);
            tint(0xffff844c); graphics.blit(MODIFY, sx, sy, 0, count <= 1 ? 75 : count == 2 ? 97 : count == 3 ? 119 : 141, width, height, 256, 256); tint(0xffffffff);
            graphics.blit(MODIFY, cx - 3, cy - 3, 0, 75, 22, 22, 256, 256); graphics.renderItem(current(diagram.tools()), cx, cy);
            int[] dx = {3, 21, 39, 12, 30}, dy = {3, 3, 3, 22, 22};
            for (int i = 0; i < Math.min(5, count); i++) { item(graphics, current(diagram.inputs().get(i)), sx + dx[i], sy + dy[i], 1); }
        } else {
            int cx = x + 28, cy = y + 28;
            graphics.blit(MODIFY, x + 15, y + 28, 214, 0, 42, 46, 256, 256);
            tint(0xffff844c); graphics.blit(MODIFY, x, y, 0, 0, 72, 72, 256, 256); tint(0xffffffff);
            graphics.blit(MODIFY, cx - 3, cy - 3, 0, 75, 22, 22, 256, 256); graphics.renderItem(current(diagram.tools()), cx, cy);
            int[] dx = {-21, -25, 0, 25, 21}, dy = {22, -4, -25, -4, 22};
            for (int i = 0; i < Math.min(5, diagram.inputs().size()); i++) { graphics.renderItem(current(diagram.inputs().get(i)), cx + dx[i], cy + dy[i]); }
        }
    }
    private void model(GuiGraphics graphics, int x, int y, BookDocument.Structure element, ModelView view) {
        var model = element.value(); graphics.flush(); Lighting.setupFor3DItems(); var pose = graphics.pose(); pose.pushPose();
        float scale = 100F / Math.max(model.width(), Math.max(model.height(), model.depth())) * Math.min(element.width() / (float) BookDocument.WIDTH, element.height() / (float) BookDocument.HEIGHT);
        pose.translate(x + element.width() / 2, y + element.height() / 2, 100); pose.scale(scale, -scale, 1);
        pose.mulPose(Axis.XP.rotationDegrees(view.pitch)); pose.mulPose(Axis.YP.rotationDegrees(view.yaw)); pose.translate(-model.width() / 2F, -model.height() / 2F, -model.depth() / 2F);
        var blocks = model.blocks();
        if (!view.animate) { view.limit = blocks.size() - 1; }
        else if (!blocks.isEmpty() && ++view.frame % 20 == 0 && (view.limit < blocks.size() - 1 || ++view.fullSteps >= 5)) { view.limit = (view.limit + 1) % blocks.size(); view.fullSteps = 0; }
        for (int i = 0; i < blocks.size() && i <= view.limit; i++) { var block = blocks.get(i); pose.pushPose(); pose.translate(block.x(), block.y(), block.z()); minecraft.getBlockRenderer().renderSingleBlock(block.state(), pose, graphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY); pose.popPose(); }
        graphics.flush(); pose.popPose(); Lighting.setupForFlatItems();
        hits.add(new Hit(x, y, element.width(), element.height(), null, ItemStack.EMPTY, null, null, view));
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (super.mouseClicked(x, y, button)) { return true; }
        double mx = (x - left) / scale, my = (y - top) / scale;
        for (int i = hits.size() - 1; i >= 0; i--) {
            var hit = hits.get(i); if (!hit.contains(mx, my)) { continue; }
            if (button == 0 && hit.target != null) { navigate(hit.target); return true; }
            if (hit.model != null && (button == 0 || button == 1)) { dragging = hit.model; return true; }
        }
        return false;
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dx, double dy) { if (dragging != null && (button == 0 || button == 1)) { dragging.yaw += (float) dx / scale / 10; dragging.pitch += (float) dy / scale / 10; return true; } return super.mouseDragged(x, y, button, dx, dy); }
    @Override public boolean mouseReleased(double x, double y, int button) { dragging = null; return super.mouseReleased(x, y, button); }
    @Override public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (dy != 0) { turn(dy < 0 ? 1 : -1); } return true;
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == 262 || key == 68) { turn(1); return true; }
        if (key == 263 || key == 65) { turn(-1); return true; }
        return super.keyPressed(key, scan, modifiers);
    }
    @Override public void removed() {
        if (!closed) { closed = true; var leaf = leaf(spread == 0 ? 1 : 0); if (minecraft.getConnection() != null) { PacketDistributor.sendToServer(new BookNetwork.SavePage(hand, slot, leaf == null ? "" : leaf.id())); } } super.removed();
    }
    private enum Kind {
        NEXT(412, 0, 18, 10), PREV(412, 10, 18, 10), LEFT(412, 30, 18, 10), INDEX(412, 40, 18, 18), REFRESH(412, 76, 18, 18), COVER(0, 0, 206, 200);
        final int u, v, width, height; Kind(int u, int v, int width, int height) { this.u = u; this.v = v; this.width = width; this.height = height; }
    }
    private final class BookButton extends AbstractButton {
        final Kind kind; final Runnable action;
        BookButton(Kind kind, int x, int y, Component message, Runnable action) { super(px(x), py(y), Math.max(1, Math.round(kind.width * scale)), Math.max(1, Math.round(kind.height * scale)), message); this.kind = kind; this.action = action; setTooltip(Tooltip.create(message)); }
        @Override public void onPress() { action.run(); }
        @Override protected void updateWidgetNarration(NarrationElementOutput output) { defaultButtonNarrationText(output); }
        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (kind == Kind.COVER) { return; }
            int color = isHoveredOrFocused() ? 0xffff541c : 0xffffffd3;
            if (kind == Kind.REFRESH) { var control = structureControls.stream().filter(candidate -> candidate.button == this).findFirst().orElse(null); var leaf = control == null ? null : leaf(control.side); color = isHoveredOrFocused() ? 0xff76d1e8 : leaf != null && view(leaf).animate ? 0xff67c768 : 0xffe3e3bc; }
            tint(color); graphics.blit(BOOK, getX(), getY(), getWidth(), getHeight(), kind.u, kind.v, kind.width, kind.height, 512, 512); tint(0xffffffff);
        }
    }
}
