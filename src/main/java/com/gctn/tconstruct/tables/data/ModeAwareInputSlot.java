package com.gctn.tconstruct.tables.data; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.menu.ToolStationMenu; // 引入 com.gctn.tconstruct.tables.menu.ToolStationMenu，供本文件使用。
import net.minecraft.world.Container; // 引入 net.minecraft.world.Container，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.inventory.Slot; // 引入 net.minecraft.world.inventory.Slot，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

/**
 * 物理输入槽在特定工具台模式下的一个视图。
 * 未激活的视图拒绝放入和取出；取走拆解预览中的部件时确认拆解。
 */
public class ModeAwareInputSlot extends Slot { // 定义 ModeAwareInputSlot 的实现类型，具体职责见上方说明。
    private final ToolStationMenu menu; // 声明 menu，保存后续流程使用的状态或依赖。
    private final Mode slotMode; // 声明 slotMode，保存后续流程使用的状态或依赖。
    private final int slotIndex; // 声明 slotIndex，保存后续流程使用的状态或依赖。

    public ModeAwareInputSlot(ToolStationMenu menu, Container container, int slot, int x, int y, Mode slotMode, int slotIndex) { // 定义 ModeAwareInputSlot 的调用入口，参数和具体处理见下方实现。
        super(container, slot, x, y); // 向当前声明或调用传入 super(container, slot, x, y)。
        this.menu = menu; // 设置 this.menu 为 menu。
        this.slotMode = slotMode; // 设置 this.slotMode 为 slotMode。
        this.slotIndex = slotIndex; // 设置 this.slotIndex 为 slotIndex。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean isActive() { // 定义 isActive 的调用入口，参数和具体处理见下方实现。
        return this.menu.isInputSlotAvailable(this.slotMode, this.slotIndex); // 返回 this.menu.isInputSlotAvailable(this.slotMode, this.slotIndex)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean mayPlace(ItemStack stack) { // 定义 mayPlace 的调用入口，参数和具体处理见下方实现。
        return this.isActive() // 返回 this.isActive()，将当前分支的结果交给调用者。
                && super.mayPlace(stack) // 调用 mayPlace，执行当前流程所需的处理。
                && this.menu.canPlaceInput(this.slotMode, this.slotIndex, stack); // 调用 canPlaceInput，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean mayPickup(Player player) { // 定义 mayPickup 的调用入口，参数和具体处理见下方实现。
        return this.isActive() && super.mayPickup(player); // 返回 this.isActive() && super.mayPickup(player)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void onTake(Player player, ItemStack stack) { // 定义 onTake 的调用入口，参数和具体处理见下方实现。
        super.onTake(player, stack); // 调用 onTake，执行当前流程所需的处理。
        this.menu.disassemblyPartTaken(); // 调用 disassemblyPartTaken，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void setChanged() { // 定义 setChanged 的调用入口，参数和具体处理见下方实现。
        super.setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
        this.menu.inputsChanged(); // 通知输入变化并刷新相关状态。
    } // 结束当前作用域。
} // 结束当前作用域。
