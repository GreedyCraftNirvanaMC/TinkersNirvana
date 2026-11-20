/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's processing and chest screens. */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.tables.WorkshopModule.Kind;
import com.gctn.tconstruct.tables.menu.WorkshopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WorkshopScreen extends AbstractContainerScreen<WorkshopMenu> {
    private final List<Button> selections = new ArrayList<>();
    private Button previous, next;
    private final NearbyInventoryPanel nearbyPanel;
    public WorkshopScreen(WorkshopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = menu.kind().storage() ? WorkshopMenu.STORAGE_HEIGHT : 184;
        inventoryLabelY = menu.kind().storage() ? 130 - WorkshopMenu.STORAGE_HEADER_REDUCTION : 82;
        titleLabelY = menu.kind().storage() ? 25 - WorkshopMenu.STORAGE_HEADER_REDUCTION : 6;
        nearbyPanel = new NearbyInventoryPanel(menu.nearby());
    }
    @Override protected void init() {
        super.init(); selections.clear();
        var tabs = WorkshopNavigation.tabs(menu.position());
        topPos = WorkshopNavigation.panelTop(topPos, tabs);
        nearbyPanel.init(leftPos, topPos, menu.containerId, this::addRenderableWidget);
        WorkshopNavigation.add(tabs, menu.position(), menu.containerId, leftPos, topPos, this::addRenderableWidget);
        if (menu.kind() == Kind.STENCIL) {
            var patterns = WorkshopMenu.patterns();
            for (int i = 0; i < patterns.size(); i++) {
                int index = i;
                var part = patterns.get(i);
                Button button = new WorkshopNavigation.IconButton(leftPos - 112 + i % 5 * 22, topPos + 8 + i / 5 * 22,
                        new ItemStack(part.item().get()), pressed -> send(index));
                button.setTooltip(Tooltip.create(part.displayName()));
                selections.add(addRenderableWidget(button));
            }
        } else if (menu.kind().storage()) {
            int pageButtonY = topPos + 23 - WorkshopMenu.STORAGE_HEADER_REDUCTION;
            previous = addRenderableWidget(Button.builder(Component.literal("<"), pressed -> send(menu.page() - 1)).bounds(leftPos + 120, pageButtonY, 16, 14).build());
            next = addRenderableWidget(Button.builder(Component.literal(">"), pressed -> send(menu.page() + 1)).bounds(leftPos + 150, pageButtonY, 16, 14).build());
        }
    }
    private void send(int id) { if (minecraft != null && minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id); } }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        nearbyPanel.update();
        for (int i = 0; i < selections.size(); i++) { selections.get(i).active = i != menu.selection(); }
        if (previous != null) {
            previous.visible = next.visible = menu.pages() > 1;
            previous.active = menu.page() > 0;
            next.active = menu.page() + 1 < menu.pages();
        }
        super.render(graphics, mouseX, mouseY, partialTick); renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        nearbyPanel.render(graphics, leftPos, topPos);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xffc6c6c6);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xffffffff);
        graphics.fill(leftPos + imageWidth - 2, topPos, leftPos + imageWidth, topPos + imageHeight, 0xff555555);
        for (int i = 0; i < menu.firstNearbySlot(); i++) {
            var slot = menu.slots.get(i);
            if (!slot.isActive()) { continue; }
            int x = leftPos + slot.x, y = topPos + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xff373737);
            graphics.fill(x, y, x + 17, y + 17, 0xff8b8b8b);
            graphics.fill(x, y + 16, x + 17, y + 17, 0xffffffff);
            graphics.fill(x + 16, y, x + 17, y + 17, 0xffffffff);
        }
        if (!menu.kind().storage()) { graphics.drawString(font, "→", leftPos + 93, topPos + 47, 0xff555555, false); }
        else if (menu.pages() > 1) { graphics.drawString(font, (menu.page() + 1) + "/" + menu.pages(), leftPos + 82, topPos + 27 - WorkshopMenu.STORAGE_HEADER_REDUCTION, 0xff404040, false); }
    }
    public List<Rect2i> occupiedAreas() {
        List<Rect2i> areas = new ArrayList<>();
        nearbyPanel.addOccupiedArea(areas, leftPos, topPos);
        for (var button : selections) {
            if (button.visible) { areas.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight())); }
        }
        return areas;
    }

    @Override protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return !nearbyPanel.contains(mouseX, mouseY, left, top) && super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }
}
