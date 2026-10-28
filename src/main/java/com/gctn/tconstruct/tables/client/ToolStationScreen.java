package com.gctn.tconstruct.tables.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.tables.data.Mode; // 引入 com.gctn.tconstruct.tables.data.Mode，供本文件使用。
import com.gctn.tconstruct.tables.menu.ToolStationMenu; // 引入 com.gctn.tconstruct.tables.menu.ToolStationMenu，供本文件使用。
import com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition; // 引入 com.gctn.tconstruct.tables.data.ToolStationSlotPositions.SlotPosition，供本文件使用。
import net.minecraft.client.gui.GuiGraphics; // 引入 net.minecraft.client.gui.GuiGraphics，供本文件使用。
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen; // 引入 net.minecraft.client.gui.screens.inventory.AbstractContainerScreen，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.resources.ResourceLocation; // 引入 net.minecraft.resources.ResourceLocation，供本文件使用。
import net.minecraft.world.entity.player.Inventory; // 引入 net.minecraft.world.entity.player.Inventory，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.Items; // 引入 net.minecraft.world.item.Items，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.api.distmarker.OnlyIn; // 引入 net.neoforged.api.distmarker.OnlyIn，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 绘制工具台布局，并通过原版菜单按钮数据包发送模式切换请求。
 * 槽位是否可见取决于同步后的菜单状态，界面本身不创建任何预览物品。
 */
@OnlyIn(Dist.CLIENT) // 通过 @OnlyIn(Dist.CLIENT) 声明框架需要读取的类型或生命周期信息。
public class ToolStationScreen extends AbstractContainerScreen<ToolStationMenu> { // 定义 ToolStationScreen 的实现类型，具体职责见上方说明。
    private static final ResourceLocation BACKGROUND_TEXTURE = // 设置 BACKGROUND_TEXTURE 为 。
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/toolstation.png"); // 调用 fromNamespaceAndPath，执行当前流程所需的处理。
    private static final ResourceLocation ICON_TEXTURE = // 设置 ICON_TEXTURE 为 。
            ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "textures/gui/icons.png"); // 调用 fromNamespaceAndPath，执行当前流程所需的处理。

    private static final int MODE_BUTTON_X = -108; // 设置 MODE_BUTTON_X 为 -108。
    private static final int MODE_BUTTON_Y = 10; // 设置 MODE_BUTTON_Y 为 10。
    private static final int MODE_BUTTON_SPACING = 22; // 设置 MODE_BUTTON_SPACING 为 22。
    private static final float INPUT_SLOT_ICON_ALPHA = 0.35F; // 设置 INPUT_SLOT_ICON_ALPHA 为 0.35F。
    private static final DefaultSlotIcon[] DEFAULT_SLOT_ICONS = { // 设置 DEFAULT_SLOT_ICONS 为 {。
            new DefaultSlotIcon(32, 44, 0), // 创建 new DefaultSlotIcon(32, 44, 0),，作为当前对象或调用的组成部分。
            new DefaultSlotIcon(14, 64, 18), // 创建 new DefaultSlotIcon(14, 64, 18),，作为当前对象或调用的组成部分。
            new DefaultSlotIcon(10, 39, 36), // 创建 new DefaultSlotIcon(10, 39, 36),，作为当前对象或调用的组成部分。
            new DefaultSlotIcon(33, 21, 55), // 创建 new DefaultSlotIcon(33, 21, 55),，作为当前对象或调用的组成部分。
            new DefaultSlotIcon(54, 39, 72), // 创建 new DefaultSlotIcon(54, 39, 72),，作为当前对象或调用的组成部分。
            new DefaultSlotIcon(50, 64, 90) // 创建 new DefaultSlotIcon(50, 64, 90)，作为当前对象或调用的组成部分。
    }; // 结束当前作用域并完成本次表达式或声明。

    private final List<ModeButton> modeButtons = new ArrayList<>(); // 设置 modeButtons 为 new ArrayList<>()。

    public ToolStationScreen(ToolStationMenu menu, Inventory playerInventory, Component title) { // 定义 ToolStationScreen 的调用入口，参数和具体处理见下方实现。
        super(menu, playerInventory, title); // 向当前声明或调用传入 super(menu, playerInventory, title)。
        this.imageWidth = 176; // 设置 this.imageWidth 为 176。
        this.imageHeight = 174; // 设置 this.imageHeight 为 174。
        this.titleLabelX = 8; // 设置 this.titleLabelX 为 8。
        this.titleLabelY = 6; // 设置 this.titleLabelY 为 6。
        this.inventoryLabelX = 8; // 设置 this.inventoryLabelX 为 8。
        this.inventoryLabelY = this.imageHeight - 94; // 设置 this.inventoryLabelY 为 this.imageHeight - 94。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void init() { // 定义 init 的调用入口，参数和具体处理见下方实现。
        super.init(); // 调用 init，执行当前流程所需的处理。
        this.modeButtons.clear(); // 清空当前集合中的内容。
        this.addModeButton(Mode.DEFAULT, ICON_TEXTURE, 55, 1, 16, 16, 0); // 调用 addModeButton，执行当前流程所需的处理。
        this.addModeButton(Mode.DISASSEMBLE, new ItemStack(Items.SHEARS), 1); // 调用 addModeButton，执行当前流程所需的处理。
        this.addModeButton(Mode.PICKAXE, new ItemStack(Items.WOODEN_PICKAXE), 2); // 调用 addModeButton，执行当前流程所需的处理。
        this.addModeButton(Mode.SHOVEL, new ItemStack(Items.WOODEN_SHOVEL), 3); // 调用 addModeButton，执行当前流程所需的处理。
        this.addModeButton(Mode.AXE, new ItemStack(Items.WOODEN_AXE), 4); // 调用 addModeButton，执行当前流程所需的处理。
        this.addModeButton(Mode.DEBUG, new ItemStack(Items.DEBUG_STICK), 5); // 调用 addModeButton，执行当前流程所需的处理。
        this.updateModeButtons(); // 调用 updateModeButtons，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) { // 定义 render 的调用入口，参数和具体处理见下方实现。
        this.updateModeButtons(); // 调用 updateModeButtons，执行当前流程所需的处理。
        super.render(guiGraphics, mouseX, mouseY, partialTick); // 调用 render，执行当前流程所需的处理。
        this.renderTooltip(guiGraphics, mouseX, mouseY); // 绘制当前悬停内容的提示。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) { // 定义 renderBg 的调用入口，参数和具体处理见下方实现。
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight); // 绘制纹理中的指定区域。
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos - 110, this.topPos, 0, 180, 108, 7); // 绘制纹理中的指定区域。
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos - 2, this.topPos, 131, 180, 2, 7); // 绘制纹理中的指定区域。
        guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + this.imageWidth, this.topPos, 0, 180, 133, 7); // 绘制纹理中的指定区域。
        for (int inputSlot = 0; inputSlot < ToolStationMenu.INPUT_SLOT_COUNT; inputSlot++) { // 遍历 int inputSlot = 0; inputSlot < ToolStationMenu.INPUT_SLOT_COUNT; inputSlot++，逐项执行当前处理。
            if (this.menu.isInputSlotVisible(inputSlot)) { // 检查 this.menu.isInputSlotVisible(inputSlot)；按条件决定是否执行本分支。
                SlotPosition position = this.menu.getInputSlotPosition(inputSlot); // 设置 position 为 this.menu.getInputSlotPosition(inputSlot)。
                renderItemSlot(guiGraphics, position.x(), position.y()); // 调用 renderItemSlot，执行当前流程所需的处理。
                renderInputSlotIcon(guiGraphics, inputSlot, position.x(), position.y()); // 调用 renderInputSlotIcon，执行当前流程所需的处理。
            } // 结束当前作用域。
        } // 结束当前作用域。
        if (!this.menu.isMode(Mode.DISASSEMBLE)) { // 检查 !this.menu.isMode(Mode.DISASSEMBLE)；按条件决定是否执行本分支。
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + 104, this.topPos + 38, 0, 241, 8, 15); // 绘制纹理中的指定区域。
        } // 结束当前作用域。
        else { // 处理前述条件均不满足时的替代分支。
            guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos + 82, this.topPos + 38, 8, 241, 8, 15); // 绘制纹理中的指定区域。
        } // 结束当前作用域。
    } // 结束当前作用域。

    private void renderItemSlot(GuiGraphics guiGraphics, int x, int y) { // 定义 renderItemSlot 的调用入口，参数和具体处理见下方实现。
        guiGraphics.blit(ICON_TEXTURE, this.leftPos + x, this.topPos + y, 144, 216, 18, 18); // 绘制纹理中的指定区域。
    } // 结束当前作用域。

    private void renderInputSlotIcon(GuiGraphics guiGraphics, int inputSlot, int x, int y) { // 定义 renderInputSlotIcon 的调用入口，参数和具体处理见下方实现。
        if (this.menu.isMode(Mode.DEFAULT)) { // 检查 this.menu.isMode(Mode.DEFAULT)；按条件决定是否执行本分支。
            this.renderDefaultSlotIcon(guiGraphics, inputSlot); // 调用 renderDefaultSlotIcon，执行当前流程所需的处理。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        ItemStack icon = this.menu.getInputSlotIcon(inputSlot); // 设置 icon 为 this.menu.getInputSlotIcon(inputSlot)。
        if (icon.isEmpty()) { // 检查 icon.isEmpty()；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, INPUT_SLOT_ICON_ALPHA); // 设置本次绘制使用的颜色与透明度。
        guiGraphics.renderItem(icon, this.leftPos + x + 1, this.topPos + y + 1); // 绘制物品图标。
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // 设置本次绘制使用的颜色与透明度。
    } // 结束当前作用域。

    private void renderDefaultSlotIcon(GuiGraphics guiGraphics, int inputSlot) { // 定义 renderDefaultSlotIcon 的调用入口，参数和具体处理见下方实现。
        if (!this.menu.areSlotsEmpty(inputSlot)) { // 检查 !this.menu.areSlotsEmpty(inputSlot)；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        DefaultSlotIcon icon = DEFAULT_SLOT_ICONS[inputSlot]; // 设置 icon 为 DEFAULT_SLOT_ICONS[inputSlot]。
        guiGraphics.blit(ICON_TEXTURE, this.leftPos + icon.x(), this.topPos + icon.y(), icon.u(), 237, 16, 13); // 绘制纹理中的指定区域。
    } // 结束当前作用域。

    private void addModeButton(Mode mode, ItemStack icon, int index) { // 定义 addModeButton 的调用入口，参数和具体处理见下方实现。
        int x = this.leftPos + MODE_BUTTON_X + index % 5 * MODE_BUTTON_SPACING; // 设置 x 为 this.leftPos + MODE_BUTTON_X + index % 5 * MODE_BUTTON_SPACING。
        int y = this.topPos + MODE_BUTTON_Y + (index / 5) * MODE_BUTTON_SPACING; // 设置 y 为 this.topPos + MODE_BUTTON_Y + (index / 5) * MODE_BUTTON_SPACING。
        ModeButton button = new ModeButton(x, y, mode, icon, BACKGROUND_TEXTURE, ICON_TEXTURE, // 设置 button 为 new ModeButton(x, y, mode, icon, BACKGROUND_TEXTURE, ICON_TEXTURE,。
                this.menu::isMode, selectedMode -> this.setMode(selectedMode.getButtonId())); // 定义回调或映射规则，在对应条目被处理时执行。
        this.modeButtons.add(this.addRenderableWidget(button)); // 向集合或构建器追加条目。
    } // 结束当前作用域。

    private void addModeButton(Mode mode, ResourceLocation resourceLocation, int uOffset, int vOffset, int uWidth, int vHeight, int index) { // 定义 addModeButton 的调用入口，参数和具体处理见下方实现。
        int x = this.leftPos + MODE_BUTTON_X + index % 5 * MODE_BUTTON_SPACING; // 设置 x 为 this.leftPos + MODE_BUTTON_X + index % 5 * MODE_BUTTON_SPACING。
        int y = this.topPos + MODE_BUTTON_Y + (index / 5) * MODE_BUTTON_SPACING; // 设置 y 为 this.topPos + MODE_BUTTON_Y + (index / 5) * MODE_BUTTON_SPACING。
        ModeButton button = new ModeButton(x, y, mode, resourceLocation, uOffset, vOffset, uWidth, vHeight, // 设置 button 为 new ModeButton(x, y, mode, resourceLocation, uOffset, vOffset, uWidth, vHeight,。
                BACKGROUND_TEXTURE, ICON_TEXTURE, this.menu::isMode, // 向当前声明或调用传入 BACKGROUND_TEXTURE, ICON_TEXTURE, this.menu::isMode。
                selectedMode -> this.setMode(selectedMode.getButtonId())); // 定义回调或映射规则，在对应条目被处理时执行。
        this.modeButtons.add(this.addRenderableWidget(button)); // 向集合或构建器追加条目。
    } // 结束当前作用域。

    private void setMode(int buttonId) { // 定义 setMode 的调用入口，参数和具体处理见下方实现。
        if (this.minecraft == null || this.minecraft.gameMode == null || this.minecraft.player == null) { // 检查 this.minecraft == null || this.minecraft.gameMode == null || this.minecraft.player == null；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        this.menu.clickMenuButton(this.minecraft.player, buttonId); // 调用 clickMenuButton，执行当前流程所需的处理。
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId); // 调用 handleInventoryButtonClick，执行当前流程所需的处理。
        this.updateModeButtons(); // 调用 updateModeButtons，执行当前流程所需的处理。
    } // 结束当前作用域。

    private void updateModeButtons() { // 定义 updateModeButtons 的调用入口，参数和具体处理见下方实现。
        for (ModeButton button : this.modeButtons) { // 遍历 ModeButton button : this.modeButtons，逐项执行当前处理。
            button.visible = this.menu.isModeButtonVisible(button.getMode()); // 设置 button.visible 为 this.menu.isModeButtonVisible(button.getMode())。
            button.active = button.visible && !this.menu.isMode(button.getMode()); // 设置 button.active 为 button.visible && !this.menu.isMode(button.getMode())。
        } // 结束当前作用域。
    } // 结束当前作用域。

    private record DefaultSlotIcon(int x, int y, int u) { // 定义 DefaultSlotIcon 的数据记录，具体职责见上方说明。
    } // 结束当前作用域。
} // 结束当前作用域。
