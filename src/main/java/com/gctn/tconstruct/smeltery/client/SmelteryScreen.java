package com.gctn.tconstruct.smeltery.client; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.menu.SmelteryMenu; // 引入 com.gctn.tconstruct.smeltery.menu.SmelteryMenu，供本文件使用。
import net.minecraft.client.gui.GuiGraphics; // 引入 net.minecraft.client.gui.GuiGraphics，供本文件使用。
import net.minecraft.client.gui.components.Button; // 引入 net.minecraft.client.gui.components.Button，供本文件使用。
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen; // 引入 net.minecraft.client.gui.screens.inventory.AbstractContainerScreen，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.world.entity.player.Inventory; // 引入 net.minecraft.world.entity.player.Inventory，供本文件使用。
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions; // 引入 net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions，供本文件使用。

/**
 * 客户端冶炼炉界面，展示库存分页、加热进度、燃料和可选择的流体层。
 * 点击检测与绘制使用相同的分层尺寸，最终流体选择由服务端菜单确认。
 */
public class SmelteryScreen extends AbstractContainerScreen<SmelteryMenu> { // 定义 SmelteryScreen 的实现类型，具体职责见上方说明。
    public SmelteryScreen(SmelteryMenu menu, Inventory inventory, Component title) { // 定义 SmelteryScreen 的调用入口，参数和具体处理见下方实现。
        super(menu, inventory, title); // 向当前声明或调用传入 super(menu, inventory, title)。
        imageWidth = 236; imageHeight = 222; inventoryLabelY = 128; // 设置 imageWidth 为 236; imageHeight = 222; inventoryLabelY = 128。
    } // 结束当前作用域。
    @Override protected void init() { // 实现父类或接口约定；定义 init 的调用入口，参数和具体处理见下方实现。
        super.init(); // 调用 init，执行当前流程所需的处理。
        addRenderableWidget(Button.builder(Component.literal("<"), b -> send(SmelteryMenu.PREVIOUS)).bounds(leftPos + 8, topPos + 103, 22, 20).build()); // 定义回调或映射规则，在对应条目被处理时执行。
        addRenderableWidget(Button.builder(Component.literal(">"), b -> send(SmelteryMenu.NEXT)).bounds(leftPos + 148, topPos + 103, 22, 20).build()); // 定义回调或映射规则，在对应条目被处理时执行。
    } // 结束当前作用域。
    private void send(int button) { if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button); } // 定义 send 的调用入口；检查 minecraft != null && minecraft.gameMode != null；按条件决定是否执行本分支。
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { // 实现父类或接口约定；定义 render 的调用入口，参数和具体处理见下方实现。
        super.render(graphics, mouseX, mouseY, partialTick); // 调用 render，执行当前流程所需的处理。
        renderTooltip(graphics, mouseX, mouseY); // 绘制当前悬停内容的提示。
        int index = fluidAt(mouseX, mouseY); // 设置 index 为 fluidAt(mouseX, mouseY)。
        if (index >= 0 && menu.controller() != null) { // 检查 index >= 0 && menu.controller() != null；按条件决定是否执行本分支。
            var fluid = menu.controller().tank().contents().get(index); // 设置 fluid 为 menu.controller().tank().contents().get(index)。
            graphics.renderTooltip(font, Component.translatable("gui.tconstruct.smeltery.fluid", fluid.getHoverName(), fluid.getAmount()), mouseX, mouseY); // 绘制当前悬停内容的提示。
        } else if (isHovering(8, 88, 162, 10, mouseX, mouseY)) { // 前一条件不成立时继续检查本分支条件。
            graphics.renderTooltip(font, Component.translatable("gui.tconstruct.smeltery.fuel"), mouseX, mouseY); // 绘制当前悬停内容的提示。
        } // 结束当前作用域。
    } // 结束当前作用域。
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) { // 实现父类或接口约定；定义 renderBg 的调用入口，参数和具体处理见下方实现。
        int x = leftPos, y = topPos; // 设置 x 为 leftPos, y = topPos。
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF373331); // 按给定动作模拟或执行流体注入。
        graphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC6C6C6); // 按给定动作模拟或执行流体注入。
        for (var slot : menu.slots) if (slot.isActive()) { // 遍历 var slot : menu.slots) if (slot.isActive()，逐项执行当前处理。
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xFF373737); // 按给定动作模拟或执行流体注入。
            graphics.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xFF8B8B8B); // 按给定动作模拟或执行流体注入。
        } // 结束当前作用域。
        graphics.fill(x + 8, y + 88, x + 170, y + 98, 0xFF373737); // 按给定动作模拟或执行流体注入。
        graphics.fill(x + 9, y + 89, x + 9 + menu.fuel() * 160 / 100, y + 97, 0xFFDC6A20); // 按给定动作模拟或执行流体注入。
        for (int i = 0; i < SmelteryMenu.VISIBLE_SLOTS; i++) { // 遍历 int i = 0; i < SmelteryMenu.VISIBLE_SLOTS; i++，逐项执行当前处理。
            int progress = menu.progress(i); // 设置 progress 为 menu.progress(i)。
            if (progress > 0) graphics.fill(x + 8 + i % 9 * 18, y + 42 + i / 9 * 18, x + 8 + i % 9 * 18 + progress * 16 / 1000, y + 44 + i / 9 * 18, 0xFFFF8A20); // 检查 progress > 0；按条件决定是否执行本分支。
        } // 结束当前作用域。
        graphics.fill(x + 182, y + 27, x + 228, y + 124, 0xFF24201E); // 按给定动作模拟或执行流体注入。
        if (menu.controller() != null) { // 检查 menu.controller() != null；按条件决定是否执行本分支。
            var tank = menu.controller().tank(); // 设置 tank 为 menu.controller().tank()。
            int bottom = 123; // 设置 bottom 为 123。
            for (var fluid : tank.contents()) { // 遍历 var fluid : tank.contents()，逐项执行当前处理。
                int height = Math.max(1, (int) (95L * fluid.getAmount() / Math.max(1, Math.max(tank.capacity(), tank.amount())))); // 设置 height 为 Math.max(1, (int) (95L * fluid.getAmount() / Math.max(1, Math.max(tank.capacity(), tank.amount()))))。
                height = Math.min(height, bottom - 28); // 设置 height 为 Math.min(height, bottom - 28)。
                if (height <= 0) break; // 检查 height <= 0；按条件决定是否执行本分支。
                int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid) | 0xFF000000; // 设置 color 为 IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid) | 0xFF000000。
                graphics.fill(x + 183, y + bottom - height, x + 227, y + bottom, color); // 按给定动作模拟或执行流体注入。
                bottom -= height; // 将当前计算结果累积到 bottom，保持本轮状态一致。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) { // 实现父类或接口约定；定义 renderLabels 的调用入口，参数和具体处理见下方实现。
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false); // 绘制界面文字。
        graphics.drawString(font, Component.translatable("gui.tconstruct.smeltery.inventory"), inventoryLabelX, inventoryLabelY, 0x404040, false); // 绘制界面文字。
        graphics.drawCenteredString(font, Component.translatable("gui.tconstruct.smeltery.page", menu.currentPage() + 1, menu.pages()), 89, 109, 0xFFFFFF); // 以居中方式绘制文字。
        if (menu.controller() != null) graphics.drawString(font, Component.translatable("gui.tconstruct.smeltery.capacity", menu.controller().tank().amount(), menu.controller().tank().capacity()), 8, 17, 0x404040, false); // 检查 menu.controller() != null；按条件决定是否执行本分支。
    } // 结束当前作用域。
    private int fluidAt(double mouseX, double mouseY) { // 定义 fluidAt 的调用入口，参数和具体处理见下方实现。
        if (!isHovering(183, 28, 44, 95, mouseX, mouseY) || menu.controller() == null) return -1; // 检查 !isHovering(183, 28, 44, 95, mouseX, mouseY) || menu.controller() == null；按条件决定是否执行本分支。
        var tank = menu.controller().tank(); // 设置 tank 为 menu.controller().tank()。
        int bottom = 123, index = 0; // 设置 bottom 为 123, index = 0。
        for (var fluid : tank.contents()) { // 遍历 var fluid : tank.contents()，逐项执行当前处理。
            int height = Math.min(bottom - 28, Math.max(1, (int) (95L * fluid.getAmount() / Math.max(1, Math.max(tank.capacity(), tank.amount()))))); // 设置 height 为 Math.min(bottom - 28, Math.max(1, (int) (95L * fluid.getAmount() / Math.max(1, Math.max(tank.capacity(), ta…。
            if (height <= 0) break; // 检查 height <= 0；按条件决定是否执行本分支。
            if (mouseY - topPos >= bottom - height && mouseY - topPos < bottom) return index; // 检查 mouseY - topPos >= bottom - height && mouseY - topPos < bottom；按条件决定是否执行本分支。
            bottom -= height; index++; // 将当前计算结果累积到 bottom，保持本轮状态一致。
        } // 结束当前作用域。
        return -1; // 返回 -1，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { // 实现父类或接口约定；定义 mouseClicked 的调用入口，参数和具体处理见下方实现。
        int index = fluidAt(mouseX, mouseY); // 设置 index 为 fluidAt(mouseX, mouseY)。
        if (button == 0 && index >= 0) { send(index); return true; } // 检查 button == 0 && index >= 0；按条件决定是否执行本分支。
        return super.mouseClicked(mouseX, mouseY, button); // 返回 super.mouseClicked(mouseX, mouseY, button)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
