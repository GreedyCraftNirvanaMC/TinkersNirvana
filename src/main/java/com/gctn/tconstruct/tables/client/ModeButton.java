package com.gctn.tconstruct.tables.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.data.Mode; // 引入 com.gctn.tconstruct.tables.data.Mode，供本文件使用。
import net.minecraft.client.gui.GuiGraphics; // 引入 net.minecraft.client.gui.GuiGraphics，供本文件使用。
import net.minecraft.client.gui.components.AbstractButton; // 引入 net.minecraft.client.gui.components.AbstractButton，供本文件使用。
import net.minecraft.client.gui.narration.NarrationElementOutput; // 引入 net.minecraft.client.gui.narration.NarrationElementOutput，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.resources.ResourceLocation; // 引入 net.minecraft.resources.ResourceLocation，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.api.distmarker.OnlyIn; // 引入 net.neoforged.api.distmarker.OnlyIn，供本文件使用。

import java.util.Locale; // 引入 java.util.Locale，供本文件使用。
import java.util.function.Consumer; // 引入 java.util.function.Consumer，供本文件使用。
import java.util.function.Predicate; // 引入 java.util.function.Predicate，供本文件使用。

/**
 * 客户端模式按钮，显示图标、选中状态和可翻译的朗读文本。
 * 按钮回调只请求服务端切换模式，不直接修改库存状态。
 */
@OnlyIn(Dist.CLIENT) // 通过 @OnlyIn(Dist.CLIENT) 声明框架需要读取的类型或生命周期信息。
class ModeButton extends AbstractButton { // 定义 ModeButton 的实现类型，具体职责见上方说明。
    private static final int SIZE = 18; // 设置 SIZE 为 18。
    private static final int BUTTON_TEXTURE_X = 180; // 设置 BUTTON_TEXTURE_X 为 180。
    private static final int ACTIVE_BUTTON_TEXTURE_X = 144; // 设置 ACTIVE_BUTTON_TEXTURE_X 为 144。
    private static final int BUTTON_TEXTURE_Y = 180; // 设置 BUTTON_TEXTURE_Y 为 180。

    private final Mode mode; // 声明 mode，保存后续流程使用的状态或依赖。
    private final ItemStack itemIcon; // 声明 itemIcon，保存后续流程使用的状态或依赖。
    private final ResourceLocation iconTexture; // 声明 iconTexture，保存后续流程使用的状态或依赖。
    private final int iconU; // 声明 iconU，保存后续流程使用的状态或依赖。
    private final int iconV; // 声明 iconV，保存后续流程使用的状态或依赖。
    private final int iconWidth; // 声明 iconWidth，保存后续流程使用的状态或依赖。
    private final int iconHeight; // 声明 iconHeight，保存后续流程使用的状态或依赖。
    private final ResourceLocation backgroundTexture; // 声明 backgroundTexture，保存后续流程使用的状态或依赖。
    private final ResourceLocation buttonTexture; // 声明 buttonTexture，保存后续流程使用的状态或依赖。
    private final Predicate<Mode> isSelected; // 声明 isSelected，保存后续流程使用的状态或依赖。
    private final Consumer<Mode> onPressed; // 声明 onPressed，保存后续流程使用的状态或依赖。

    ModeButton(int x, int y, Mode mode, ItemStack itemIcon, ResourceLocation backgroundTexture, // 调用 ModeButton，执行当前流程所需的处理。
               ResourceLocation buttonTexture, Predicate<Mode> isSelected, Consumer<Mode> onPressed) { // 在 ModeButton 中补充当前声明或表达式的 ResourceLocation buttonTexture, Predicate<Mode> isSelected, Consumer<Mode> onPressed) { 部分。
        this(x, y, mode, itemIcon, null, 0, 0, 0, 0, backgroundTexture, buttonTexture, isSelected, onPressed); // 调用 this，执行当前流程所需的处理。
    } // 结束当前作用域。

    ModeButton(int x, int y, Mode mode, ResourceLocation iconTexture, int iconU, int iconV, // 调用 ModeButton，执行当前流程所需的处理。
               int iconWidth, int iconHeight, ResourceLocation backgroundTexture, ResourceLocation buttonTexture, // 向当前声明或调用传入 int iconWidth, int iconHeight, ResourceLocation backgroundTexture, ResourceLocation buttonTexture。
               Predicate<Mode> isSelected, Consumer<Mode> onPressed) { // 在 ModeButton 中补充当前声明或表达式的 Predicate<Mode> isSelected, Consumer<Mode> onPressed) { 部分。
        this(x, y, mode, ItemStack.EMPTY, iconTexture, iconU, iconV, iconWidth, iconHeight, // 调用 this，执行当前流程所需的处理。
                backgroundTexture, buttonTexture, isSelected, onPressed); // 向当前声明或调用传入 backgroundTexture, buttonTexture, isSelected, onPressed)。
    } // 结束当前作用域。

    private ModeButton(int x, int y, Mode mode, ItemStack itemIcon, ResourceLocation iconTexture, // 定义 ModeButton 的调用入口，参数和具体处理见下方实现。
                       int iconU, int iconV, int iconWidth, int iconHeight, ResourceLocation backgroundTexture, // 向当前声明或调用传入 int iconU, int iconV, int iconWidth, int iconHeight, ResourceLocation backgroundTexture。
                       ResourceLocation buttonTexture, Predicate<Mode> isSelected, Consumer<Mode> onPressed) { // 在 ModeButton 中补充当前声明或表达式的 ResourceLocation buttonTexture, Predicate<Mode> isSelected, Consumer<Mode> onPressed) { 部分。
        super(x, y, SIZE, SIZE, Component.translatable( // 调用 translatable，执行当前流程所需的处理。
                "container.tconstruct.toolstation.mode." + mode.name().toLowerCase(Locale.ROOT))); // 调用 name，执行当前流程所需的处理。
        this.mode = mode; // 设置 this.mode 为 mode。
        this.itemIcon = itemIcon; // 设置 this.itemIcon 为 itemIcon。
        this.iconTexture = iconTexture; // 设置 this.iconTexture 为 iconTexture。
        this.iconU = iconU; // 设置 this.iconU 为 iconU。
        this.iconV = iconV; // 设置 this.iconV 为 iconV。
        this.iconWidth = iconWidth; // 设置 this.iconWidth 为 iconWidth。
        this.iconHeight = iconHeight; // 设置 this.iconHeight 为 iconHeight。
        this.backgroundTexture = backgroundTexture; // 设置 this.backgroundTexture 为 backgroundTexture。
        this.buttonTexture = buttonTexture; // 设置 this.buttonTexture 为 buttonTexture。
        this.isSelected = isSelected; // 设置 this.isSelected 为 isSelected。
        this.onPressed = onPressed; // 设置 this.onPressed 为 onPressed。
    } // 结束当前作用域。

    Mode getMode() { // 调用 getMode，执行当前流程所需的处理。
        return this.mode; // 返回 this.mode，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void onPress() { // 定义 onPress 的调用入口，参数和具体处理见下方实现。
        this.onPressed.accept(this.mode); // 调用 accept，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) { // 定义 renderWidget 的调用入口，参数和具体处理见下方实现。
        int textureX = this.isSelected.test(this.mode) ? ACTIVE_BUTTON_TEXTURE_X : BUTTON_TEXTURE_X; // 设置 textureX 为 this.isSelected.test(this.mode) ? ACTIVE_BUTTON_TEXTURE_X : BUTTON_TEXTURE_X。
        guiGraphics.blit(this.buttonTexture, this.getX(), this.getY(), textureX, BUTTON_TEXTURE_Y, SIZE, SIZE); // 绘制纹理中的指定区域。
        guiGraphics.blit(this.backgroundTexture, this.getX() + 2, this.getY() - 4, 20, 174, 14, 4); // 绘制纹理中的指定区域。
        if (this.iconTexture != null) { // 检查 this.iconTexture != null；按条件决定是否执行本分支。
            int iconX = this.getX() + (SIZE - this.iconWidth) / 2; // 设置 iconX 为 this.getX() + (SIZE - this.iconWidth) / 2。
            int iconY = this.getY() + (SIZE - this.iconHeight) / 2; // 设置 iconY 为 this.getY() + (SIZE - this.iconHeight) / 2。
            guiGraphics.blit(this.iconTexture, iconX, iconY, this.iconU, this.iconV, this.iconWidth, this.iconHeight); // 绘制纹理中的指定区域。
        } else { // 处理前述条件均不满足时的替代分支。
            guiGraphics.renderItem(this.itemIcon, this.getX() + 1, this.getY() + 1); // 绘制物品图标。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { // 定义 updateWidgetNarration 的调用入口，参数和具体处理见下方实现。
        this.defaultButtonNarrationText(narrationElementOutput); // 调用 defaultButtonNarrationText，执行当前流程所需的处理。
    } // 结束当前作用域。
} // 结束当前作用域。
