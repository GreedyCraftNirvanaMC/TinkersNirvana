/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.tables.menu.NearbyInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Shared adjacent inventory panel, kept outside the station's existing physical slots. */
public final class NearbyInventoryPanel {
    private final NearbyInventory inventory;
    private Button previous, next;
    public NearbyInventoryPanel(NearbyInventory inventory) { this.inventory = inventory; }
    public void init(int left, int top, int menuId, Consumer<Button> add) {
        if (inventory == null || !inventory.present()) { return; }
        previous = Button.builder(Component.literal("<"), button -> send(menuId, NearbyInventory.PREVIOUS)).bounds(left + inventory.x(), top - 2, 16, 16).build();
        next = Button.builder(Component.literal(">"), button -> send(menuId, NearbyInventory.NEXT)).bounds(left + inventory.x() + 74, top - 2, 16, 16).build();
        add.accept(previous); add.accept(next);
    }
    private void send(int menuId, int button) { var gameMode = Minecraft.getInstance().gameMode; if (gameMode != null) { gameMode.handleInventoryButtonClick(menuId, button); } }
    public void update() {
        if (previous == null) { return; }
        previous.visible = next.visible = inventory.visible();
        previous.active = inventory.page() > 0; next.active = inventory.page() + 1 < inventory.pages();
    }
    public void render(GuiGraphics graphics, int left, int top) {
        if (inventory == null || !inventory.visible()) { return; }
        int x = left + inventory.x();
        graphics.fill(x - 4, top - 5, x + 94, top + 180, 0xffc6c6c6);
        graphics.fill(x - 4, top - 5, x + 94, top - 3, 0xffffffff);
        graphics.fill(x + 92, top - 5, x + 94, top + 180, 0xff555555);
        graphics.drawCenteredString(Minecraft.getInstance().font, (inventory.page() + 1) + "/" + inventory.pages(), x + 45, top + 2, 0xff404040);
        for (int i = 0; i < NearbyInventory.PAGE_SIZE; i++) {
            int sx = x + i % 5 * 18, sy = top + 17 + i / 5 * 18;
            graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xff373737);
            graphics.fill(sx, sy, sx + 17, sy + 17, 0xff8b8b8b);
            graphics.fill(sx, sy + 16, sx + 17, sy + 17, 0xffffffff);
            graphics.fill(sx + 16, sy, sx + 17, sy + 17, 0xffffffff);
        }
    }
    public boolean contains(double mouseX, double mouseY, int left, int top) {
        return inventory != null && inventory.visible() && mouseX >= left + inventory.x() - 4 && mouseX < left + inventory.x() + 94 && mouseY >= top - 5 && mouseY < top + 180;
    }
}
