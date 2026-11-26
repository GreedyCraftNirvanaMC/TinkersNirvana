package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tables.data.Mode;
import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ToolStationScreen extends AbstractContainerScreen<ToolStationMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/toolstation.png");
    private static final ResourceLocation ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/icons.png");

    private static final int MAX_MODE_BUTTON_COLUMNS = 5;
    private static final int MODE_BUTTON_Y = 10;
    private static final int MODE_BUTTON_SPACING = 22;
    private static final int SIDE_PANEL_GAP = 4;
    private static final int INFO_PANEL_Y = 11;
    private static final int WORKSHOP_PANEL_OFFSET = 2;
    private static final int SIDE_BUTTON_HEIGHT = 18;
    private static final float INPUT_SLOT_ICON_ALPHA = 0.35F;
    private static final List<Mode> TOOL_ORDER = List.of(
            Mode.PICKAXE, Mode.SHOVEL, Mode.AXE, Mode.MATTOCK, Mode.KAMA,
            Mode.HAMMER, Mode.EXCAVATOR, Mode.LUMBERAXE, Mode.SCYTHE,
            Mode.BROADSWORD, Mode.LONGSWORD, Mode.RAPIER, Mode.FRYPAN, Mode.BATTLESIGN, Mode.CLEAVER,
            Mode.SHORTBOW, Mode.LONGBOW, Mode.ARROW, Mode.CROSSBOW, Mode.BOLT, Mode.SHURIKEN);
    private static final List<String> PREVIEW_MATERIALS = List.of("wood", "iron", "cobalt", "manyullyn");
    private static final DefaultSlotIcon[] DEFAULT_SLOT_ICONS = {
            new DefaultSlotIcon(32, 44, 0),
            new DefaultSlotIcon(14, 64, 18),
            new DefaultSlotIcon(10, 39, 36),
            new DefaultSlotIcon(33, 21, 55),
            new DefaultSlotIcon(54, 39, 72),
            new DefaultSlotIcon(50, 64, 90)
    };

    private final List<ModeButton> modeButtons = new ArrayList<>();
    private final EnumMap<Mode, ItemStack> toolPreviews = new EnumMap<>(Mode.class);
    private final NearbyInventoryPanel nearbyPanel;
    private ToolStationInfoPanel infoPanel;
    private ToolStationInfoPanel detailsPanel;
    private Button sidePanelButton;
    private boolean showingNearby;
    private net.minecraft.client.gui.components.EditBox nameField;
    private String observedName;
    private boolean applyingServerName;
    private int modeButtonColumns;

    public ToolStationScreen(ToolStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 174;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
        nearbyPanel = new NearbyInventoryPanel(menu.nearby());
    }

    @Override
    protected void init() {
        super.init();
        var tabs = WorkshopNavigation.tabs(menu.position());
        topPos = WorkshopNavigation.panelTop(topPos, tabs);
        this.modeButtons.clear();
        toolPreviews.clear();
        initSidePanels();
        this.modeButtonColumns = Math.clamp(leftPos / MODE_BUTTON_SPACING, 1, MAX_MODE_BUTTON_COLUMNS);
        nearbyPanel.init(leftPos, topPos, menu.containerId, this::addRenderableWidget);
        nameField = new net.minecraft.client.gui.components.EditBox(font, leftPos + 70, topPos + 8, 98, 10, Component.translatable("gui.tconsnirvana.tool_name"));
        nameField.setMaxLength(50);
        nameField.setBordered(false);
        nameField.setTextColor(0x202020);
        observedName = menu.pendingName();
        nameField.setValue(observedName);
        nameField.setResponder(name -> {
            if (!applyingServerName) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.gctn.tconstruct.tables.WorkshopNetwork.Rename(menu.containerId, name));
            }
        });
        addRenderableWidget(nameField);
        WorkshopNavigation.add(tabs, menu.position(), menu.containerId, leftPos, topPos, this::addRenderableWidget);
        this.addModeButton(Mode.DEFAULT, ICON_TEXTURE, 55, 1, 16, 16);
        for (Mode mode : TOOL_ORDER) {
            String id = mode == Mode.AXE ? "hatchet" : mode.name().toLowerCase(java.util.Locale.ROOT);
            ItemStack preview = preview(AntiqueTools.get(id));
            toolPreviews.put(mode, preview);
            this.addModeButton(mode, preview);
        }
        this.updateModeButtons();
    }

    private void initSidePanels() {
        boolean hasNearby = menu.nearby() != null && menu.nearby().present();
        int statsOffset = imageWidth + WORKSHOP_PANEL_OFFSET;
        int statsOuterWidth = ToolStationInfoPanel.PREFERRED_WIDTH;
        int nearbyRight = hasNearby ? nearbyPanel.rightEdge() : 0;
        int combinedRight = nearbyRight + SIDE_PANEL_GAP + statsOuterWidth;
        // 空间充足时并排显示，两栏放不下时共用右侧位置。
        boolean sideBySide = hasNearby && width - combinedRight >= 4 * MODE_BUTTON_SPACING;
        if (sideBySide) {
            leftPos = Math.min(leftPos, width - combinedRight);
            statsOffset = nearbyRight + SIDE_PANEL_GAP;
        } else if (hasNearby) {
            leftPos = Math.min(leftPos, Math.max(MODE_BUTTON_SPACING, width - nearbyRight));
            topPos = Math.max(topPos, SIDE_BUTTON_HEIGHT + 2);
        } else {
            int minModeSpace = Math.min(leftPos, 3 * MODE_BUTTON_SPACING);
            leftPos = Math.min(leftPos, Math.max(minModeSpace, width - statsOffset - statsOuterWidth));
        }

        int panelX = leftPos + statsOffset;
        int panelWidth = Math.clamp(width - panelX, 32, ToolStationInfoPanel.PREFERRED_WIDTH);
        int panelHeight = Math.clamp((height - topPos - INFO_PANEL_Y - SIDE_PANEL_GAP) / 2, 24, 83);
        int panelY = topPos + INFO_PANEL_Y;
        infoPanel = addRenderableWidget(new ToolStationInfoPanel(font, menu, panelX, panelY, panelWidth, panelHeight, false));
        detailsPanel = addRenderableWidget(new ToolStationInfoPanel(font, menu, panelX, panelY + panelHeight + SIDE_PANEL_GAP,
                panelWidth, panelHeight, true));
        sidePanelButton = null;
        if (hasNearby && !sideBySide) {
            sidePanelButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
                showingNearby = !showingNearby;
                updateSidePanels();
            }).bounds(panelX, topPos - SIDE_BUTTON_HEIGHT - 2, Math.min(panelWidth + 8, nearbyRight - statsOffset),
                    SIDE_BUTTON_HEIGHT).build());
        }
        updateSidePanels();
    }

    private void updateSidePanels() {
        boolean usingSharedSpace = sidePanelButton != null;
        infoPanel.visible = detailsPanel.visible = !usingSharedSpace || !showingNearby;
        if (menu.nearby() != null) {
            menu.nearby().visible(!usingSharedSpace || showingNearby);
        }
        if (usingSharedSpace) {
            sidePanelButton.setMessage(Component.translatable(showingNearby
                    ? "gui.tconsnirvana.tool_station.stats" : "gui.tconsnirvana.tool_station.nearby"));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        nameField.visible = !menu.isMode(Mode.DISASSEMBLE);
        if (!nameField.visible && getFocused() == nameField) {
            setFocused(null);
        }
        if (!menu.pendingName().equals(observedName) && !nameField.isFocused()) {
            observedName = menu.pendingName();
            if (!nameField.getValue().equals(observedName)) {
                applyingServerName = true;
                nameField.setValue(observedName);
                applyingServerName = false;
            }
        }
        this.updateModeButtons();
        infoPanel.update();
        detailsPanel.update();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.detailsPanel.renderTraitTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        nearbyPanel.update(); nearbyPanel.render(guiGraphics, leftPos, topPos);
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        int beamWidth = modeButtonColumns * MODE_BUTTON_SPACING;
        renderBeam(guiGraphics, leftPos - beamWidth, beamWidth);
        if (infoPanel.visible) {
            renderBeam(guiGraphics, infoPanel.getX() - 2, infoPanel.getWidth() + 2);
            renderPanelHangers(guiGraphics, infoPanel);
            renderPanelHangers(guiGraphics, detailsPanel);
        }
        if (!nameField.visible) {
            guiGraphics.fill(leftPos + 68, topPos + 6, leftPos + 170, topPos + 18, 0xffc6c6c6);
        } else if (nameField.isFocused()) {
            guiGraphics.blit(BACKGROUND_TEXTURE, leftPos + 68, topPos + 6, 0, 210, 102, 12);
        }
        renderSelectionPreview(guiGraphics);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 30);
        for (int inputSlot = 0; inputSlot < ToolStationMenu.INPUT_SLOT_COUNT; inputSlot++) {
            if (this.menu.isInputSlotVisible(inputSlot)) {
                SlotPosition position = this.menu.getInputSlotPosition(inputSlot);
                renderItemSlot(guiGraphics, position.x(), position.y());
                renderInputSlotIcon(guiGraphics, inputSlot, position.x(), position.y());
            }
        }
        guiGraphics.pose().popPose();
        if (!this.menu.isMode(Mode.DISASSEMBLE)) {
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + 104, this.topPos + 38, 0, 241, 8, 15);
        }
        if (this.menu.isMode(Mode.DISASSEMBLE) || this.menu.isReadyForDisassembly()) {
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + 82, this.topPos + 38, 8, 241, 8, 15);
        }
    }

    private void renderSelectionPreview(GuiGraphics graphics) {
        if (menu.isMode(Mode.DISASSEMBLE)) { return; }
        if (menu.isMode(Mode.DEFAULT)) {
            RenderSystem.enableBlend();
            graphics.setColor(1, 1, 1, .18F);
            graphics.blit(ICON_TEXTURE, leftPos + 10, topPos + 22, 67, 60, 54, 0, 18, 18, 256, 256);
            graphics.setColor(1, 1, 1, 1);
            RenderSystem.disableBlend();
            return;
        }
        ItemStack preview = toolPreviews.get(menu.getMode());
        if (preview == null) { return; }
        // 物品模型上方覆盖浅灰色，与怀古的装配轮廓一致，槽框绘制在其上。
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 10, topPos + 22, -140);
        graphics.pose().scale(3.7F, 3.7F, 1);
        graphics.renderFakeItem(preview, 0, 0);
        graphics.pose().popPose();
        graphics.fill(leftPos + 7, topPos + 18, leftPos + 87, topPos + 82, 25, 0xd1c6c6c6);
    }

    private void renderBeam(GuiGraphics graphics, int x, int beamWidth) {
        int textureY = menu.isForge() ? 187 : 180;
        graphics.blit(BACKGROUND_TEXTURE, x, topPos, 0, textureY, beamWidth - 2, 7);
        graphics.blit(BACKGROUND_TEXTURE, x + beamWidth - 2, topPos, 131, textureY, 2, 7);
    }

    private void renderPanelHangers(GuiGraphics graphics, ToolStationInfoPanel panel) {
        int offset = menu.isForge() ? 36 : 18;
        int y = panel.getY() - 4;
        graphics.blit(BACKGROUND_TEXTURE, panel.getX() + 5, y, offset, 174, 5, 4);
        graphics.blit(BACKGROUND_TEXTURE, panel.getX() + panel.getWidth() - 14, y, offset + 9, 174, 9, 4);
    }

    private static ItemStack preview(AntiqueTools.Entry entry) {
        ItemStack stack = new ItemStack(entry.item().get());
        CompoundTag materials = new CompoundTag();
        var parts = entry.definition().parts();
        for (int i = 0; i < parts.size(); i++) {
            var part = parts.get(i);
            String material = switch (part.stat()) {
                case "Shaft" -> "wood";
                case "Fletching" -> "feather";
                case "BowString" -> "string";
                default -> PREVIEW_MATERIALS.get(i);
            };
            materials.putString(part.key(), material);
        }
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(materials));
        return stack;
    }

    private void renderItemSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(ICON_TEXTURE, this.leftPos + x, this.topPos + y, 144, 216, 18, 18);
    }

    private void renderInputSlotIcon(GuiGraphics guiGraphics, int inputSlot, int x, int y) {
        if (!this.menu.areSlotsEmpty(inputSlot)) {
            return;
        }
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
        DefaultSlotIcon icon = DEFAULT_SLOT_ICONS[inputSlot];
        guiGraphics.blit(ICON_TEXTURE, this.leftPos + icon.x(), this.topPos + icon.y(), icon.u(), 237, 16, 13);
    }

    private void addModeButton(Mode mode, ItemStack icon) {
        ModeButton button = new ModeButton(0, 0, mode, icon, BACKGROUND_TEXTURE, ICON_TEXTURE,
                menu::isForge, this::isSelectedMode, selectedMode -> this.setMode(selectedMode.getButtonId()));
        this.modeButtons.add(this.addRenderableWidget(button));
    }

    private void addModeButton(Mode mode, ResourceLocation resourceLocation, int uOffset, int vOffset, int uWidth, int vHeight) {
        ModeButton button = new ModeButton(0, 0, mode, resourceLocation, uOffset, vOffset, uWidth, vHeight,
                BACKGROUND_TEXTURE, ICON_TEXTURE, menu::isForge, this::isSelectedMode,
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
        Mode currentMode = this.menu.getMode();
        this.menu.updateInputSlotPositions();
        int visibleCount = (int) modeButtons.stream().filter(button -> menu.isModeButtonVisible(button.getMode())).count();
        int lastRow = Math.max(1, (visibleCount - 1) / modeButtonColumns);
        int rowSpacing = Math.clamp((height - topPos - MODE_BUTTON_Y - 18) / lastRow, 18, MODE_BUTTON_SPACING);
        int index = 0;
        for (ModeButton button : this.modeButtons) {
            button.visible = this.menu.isModeButtonVisible(button.getMode());
            button.active = button.visible && currentMode != Mode.DISASSEMBLE && !isSelectedMode(button.getMode());
            if (button.visible) {
                int column = index % modeButtonColumns;
                int row = index / modeButtonColumns;
                button.setX(leftPos - (modeButtonColumns - column) * MODE_BUTTON_SPACING + 2);
                button.setY(topPos + MODE_BUTTON_Y + row * rowSpacing);
                index++;
            }
        }
    }

    private boolean isSelectedMode(Mode mode) {
        return menu.isMode(mode) || mode == Mode.DEFAULT && menu.isMode(Mode.DISASSEMBLE);
    }
    private record DefaultSlotIcon(int x, int y, int u) {
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        if (nameField.isFocused() && key != 256) { return nameField.keyPressed(key, scan, modifiers) || nameField.canConsumeInput(); }
        return super.keyPressed(key, scan, modifiers);
    }
    public List<Rect2i> occupiedAreas() {
        List<Rect2i> areas = new ArrayList<>();
        nearbyPanel.addOccupiedArea(areas, leftPos, topPos);
        if (infoPanel != null && infoPanel.visible) {
            areas.add(infoPanel.bounds());
            areas.add(detailsPanel.bounds());
        }
        if (sidePanelButton != null) {
            areas.add(new Rect2i(sidePanelButton.getX(), sidePanelButton.getY(), sidePanelButton.getWidth(), sidePanelButton.getHeight()));
        }
        for (var button : modeButtons) {
            if (button.visible) { areas.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight())); }
        }
        return areas;
    }

    @Override protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        for (ModeButton modeButton : modeButtons) {
            if (modeButton.visible && mouseX >= modeButton.getX() && mouseX < modeButton.getX() + modeButton.getWidth()
                    && mouseY >= modeButton.getY() && mouseY < modeButton.getY() + modeButton.getHeight()) {
                return false;
            }
        }
        return !infoPanel.isMouseOver(mouseX, mouseY) && !detailsPanel.isMouseOver(mouseX, mouseY)
                && (sidePanelButton == null || !sidePanelButton.isMouseOver(mouseX, mouseY))
                && !nearbyPanel.contains(mouseX, mouseY, left, top)
                && super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }
}
