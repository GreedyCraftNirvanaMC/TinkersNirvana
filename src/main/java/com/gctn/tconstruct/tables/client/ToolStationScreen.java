package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the station layout and sends mode requests using native menu button packets.
 * Slot visibility comes from synchronized menu state; preview items are never created by the screen.
 */
@OnlyIn(Dist.CLIENT)
public class ToolStationScreen extends AbstractContainerScreen<ToolStationMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/toolstation.png");
    private static final ResourceLocation ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/icons.png");

    private static final int MODE_BUTTON_X = -108;
    private static final int MODE_BUTTON_Y = 10;
    private static final int MODE_BUTTON_SPACING = 22;
    private static final float INPUT_SLOT_ICON_ALPHA = 0.35F;
    private static final DefaultSlotIcon[] DEFAULT_SLOT_ICONS = {
            new DefaultSlotIcon(32, 44, 0),
            new DefaultSlotIcon(14, 64, 18),
            new DefaultSlotIcon(10, 39, 36),
            new DefaultSlotIcon(33, 21, 55),
            new DefaultSlotIcon(54, 39, 72),
            new DefaultSlotIcon(50, 64, 90)
    };

    private final List<ModeButton> modeButtons = new ArrayList<>();

    public ToolStationScreen(ToolStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 174;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButtons.clear();
        this.addModeButton(Mode.DEFAULT, ICON_TEXTURE, 55, 1, 16, 16, 0);
        this.addModeButton(Mode.DISASSEMBLE, new ItemStack(Items.SHEARS), 1);
        this.addModeButton(Mode.PICKAXE, new ItemStack(Items.WOODEN_PICKAXE), 2);
        this.addModeButton(Mode.SHOVEL, new ItemStack(Items.WOODEN_SHOVEL), 3);
        this.addModeButton(Mode.AXE, new ItemStack(Items.WOODEN_AXE), 4);
        this.addModeButton(Mode.DEBUG, new ItemStack(Items.DEBUG_STICK), 5);
        this.updateModeButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.updateModeButtons();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos - 110, this.topPos, 0, 180, 108, 7);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos - 2, this.topPos, 131, 180, 2, 7);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + this.imageWidth, this.topPos, 0, 180, 133, 7);
        for (int inputSlot = 0; inputSlot < ToolStationMenu.INPUT_SLOT_COUNT; inputSlot++) {
            if (this.menu.isInputSlotVisible(inputSlot)) {
                SlotPosition position = this.menu.getInputSlotPosition(inputSlot);
                renderItemSlot(guiGraphics, position.x(), position.y());
                renderInputSlotIcon(guiGraphics, inputSlot, position.x(), position.y());
            }
        }
        if (!this.menu.isMode(Mode.DISASSEMBLE)) {
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + 104, this.topPos + 38, 0, 241, 8, 15);
        }
        else {
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + 82, this.topPos + 38, 8, 241, 8, 15);
        }
    }

    private void renderItemSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ICON_TEXTURE, this.leftPos + x, this.topPos + y, 144, 216, 18, 18);
    }

    private void renderInputSlotIcon(GuiGraphics guiGraphics, int inputSlot, int x, int y) {
        if (this.menu.isMode(Mode.DEFAULT)) {
            this.renderDefaultSlotIcon(guiGraphics, inputSlot);
            return;
        }

        ItemStack icon = this.menu.getInputSlotIcon(inputSlot);
        if (icon.isEmpty()) {
            return;
        }

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, INPUT_SLOT_ICON_ALPHA);
        guiGraphics.renderItem(icon, this.leftPos + x + 1, this.topPos + y + 1);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderDefaultSlotIcon(GuiGraphics guiGraphics, int inputSlot) {
        if (!this.menu.areSlotsEmpty(inputSlot)) {
            return;
        }

        DefaultSlotIcon icon = DEFAULT_SLOT_ICONS[inputSlot];
        guiGraphics.blit(ICON_TEXTURE, this.leftPos + icon.x(), this.topPos + icon.y(), icon.u(), 237, 16, 13);
    }

    private void addModeButton(Mode mode, ItemStack icon, int index) {
        int x = this.leftPos + MODE_BUTTON_X + index % 5 * MODE_BUTTON_SPACING;
        int y = this.topPos + MODE_BUTTON_Y + (index / 5) * MODE_BUTTON_SPACING;
        ModeButton button = new ModeButton(x, y, mode, icon, BACKGROUND_TEXTURE, ICON_TEXTURE,
                this.menu::isMode, selectedMode -> this.setMode(selectedMode.getButtonId()));
        this.modeButtons.add(this.addRenderableWidget(button));
    }

    private void addModeButton(Mode mode, ResourceLocation resourceLocation, int uOffset, int vOffset, int uWidth, int vHeight, int index) {
        int x = this.leftPos + MODE_BUTTON_X + index % 5 * MODE_BUTTON_SPACING;
        int y = this.topPos + MODE_BUTTON_Y + (index / 5) * MODE_BUTTON_SPACING;
        ModeButton button = new ModeButton(x, y, mode, resourceLocation, uOffset, vOffset, uWidth, vHeight,
                BACKGROUND_TEXTURE, ICON_TEXTURE, this.menu::isMode,
                selectedMode -> this.setMode(selectedMode.getButtonId()));
        this.modeButtons.add(this.addRenderableWidget(button));
    }

    private void setMode(int buttonId) {
        if (this.minecraft == null || this.minecraft.gameMode == null || this.minecraft.player == null) {
            return;
        }
        this.menu.clickMenuButton(this.minecraft.player, buttonId);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        this.updateModeButtons();
    }

    private void updateModeButtons() {
        for (ModeButton button : this.modeButtons) {
            button.visible = this.menu.isModeButtonVisible(button.getMode());
            button.active = button.visible && !this.menu.isMode(button.getMode());
        }
    }

    private record DefaultSlotIcon(int x, int y, int u) {
    }
}
