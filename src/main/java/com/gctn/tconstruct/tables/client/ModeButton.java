package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.tables.data.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
class ModeButton extends AbstractButton {
    private static final int SIZE = 18;
    private static final int BUTTON_TEXTURE_X = 180;
    private static final int ACTIVE_BUTTON_TEXTURE_X = 144;

    private final Mode mode;
    private final ItemStack itemIcon;
    private final ResourceLocation iconTexture;
    private final int iconU;
    private final int iconV;
    private final int iconWidth;
    private final int iconHeight;
    private final ResourceLocation backgroundTexture;
    private final ResourceLocation buttonTexture;
    private final BooleanSupplier isForge;
    private final Predicate<Mode> isSelected;
    private final Consumer<Mode> onPressed;

    ModeButton(int x, int y, Mode mode, ItemStack itemIcon, ResourceLocation backgroundTexture,
               ResourceLocation buttonTexture, BooleanSupplier isForge, Predicate<Mode> isSelected, Consumer<Mode> onPressed) {
        this(x, y, mode, itemIcon, null, 0, 0, 0, 0, backgroundTexture, buttonTexture, isForge, isSelected, onPressed);
    }

    ModeButton(int x, int y, Mode mode, ResourceLocation iconTexture, int iconU, int iconV,
               int iconWidth, int iconHeight, ResourceLocation backgroundTexture, ResourceLocation buttonTexture,
               BooleanSupplier isForge, Predicate<Mode> isSelected, Consumer<Mode> onPressed) {
        this(x, y, mode, ItemStack.EMPTY, iconTexture, iconU, iconV, iconWidth, iconHeight,
                backgroundTexture, buttonTexture, isForge, isSelected, onPressed);
    }

    private ModeButton(int x, int y, Mode mode, ItemStack itemIcon, ResourceLocation iconTexture,
                       int iconU, int iconV, int iconWidth, int iconHeight, ResourceLocation backgroundTexture,
                       ResourceLocation buttonTexture, BooleanSupplier isForge, Predicate<Mode> isSelected, Consumer<Mode> onPressed) {
        super(x, y, SIZE, SIZE, Component.translatable(
                "container.tconsnirvana.toolstation.mode." + mode.name().toLowerCase(Locale.ROOT)));
        this.mode = mode;
        this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(this.getMessage()));
        this.itemIcon = itemIcon;
        this.iconTexture = iconTexture;
        this.iconU = iconU;
        this.iconV = iconV;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.backgroundTexture = backgroundTexture;
        this.buttonTexture = buttonTexture;
        this.isForge = isForge;
        this.isSelected = isSelected;
        this.onPressed = onPressed;
    }

    Mode getMode() {
        return this.mode;
    }

    @Override
    public void onPress() {
        this.onPressed.accept(this.mode);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int textureX = this.isSelected.test(this.mode) ? ACTIVE_BUTTON_TEXTURE_X
                : active && isHoveredOrFocused() ? 216 : BUTTON_TEXTURE_X;
        boolean forge = isForge.getAsBoolean();
        guiGraphics.blit(this.buttonTexture, this.getX(), this.getY(), textureX, forge ? 198 : 180, SIZE, SIZE);
        guiGraphics.blit(this.backgroundTexture, this.getX(), this.getY() - 2, forge ? 36 : 18, 176, SIZE, 2);
        if (this.iconTexture != null) {
            int iconX = this.getX() + (SIZE - this.iconWidth) / 2;
            int iconY = this.getY() + (SIZE - this.iconHeight) / 2;
            guiGraphics.blit(this.iconTexture, iconX, iconY, this.iconU, this.iconV, this.iconWidth, this.iconHeight);
        } else {
            guiGraphics.renderItem(this.itemIcon, this.getX() + 1, this.getY() + 1);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
