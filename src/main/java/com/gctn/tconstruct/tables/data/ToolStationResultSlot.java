package com.gctn.tconstruct.tables.data; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tables.menu.ToolStationMenu; // 引入 com.gctn.tconstruct.tables.menu.ToolStationMenu，供本文件使用。
import net.minecraft.world.Container; // 引入 net.minecraft.world.Container，供本文件使用。
import net.minecraft.world.entity.player.Player; // 引入 net.minecraft.world.entity.player.Player，供本文件使用。
import net.minecraft.world.inventory.Slot; // 引入 net.minecraft.world.inventory.Slot，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

/**
 * 将结果槽的放入与取出操作交给服务端事务状态处理。
 * 取出前记录操作模式，因为清空拆解源工具会取消对应预览。
 * 只有拆解模式允许放入源工具，其他模式仅展示生成的结果。
 */
public class ToolStationResultSlot extends Slot { // 定义 ToolStationResultSlot 的实现类型，具体职责见上方说明。
    private final ToolStationMenu menu; // 声明 menu，保存后续流程使用的状态或依赖。
    private Mode takenMode; // 声明 takenMode，保存后续流程使用的状态或依赖。

    // 绑定工具台菜单，让结果槽把放入、取出事件交回菜单处理。
    public ToolStationResultSlot(ToolStationMenu menu, Container container, int slot, int x, int y) { // 定义 ToolStationResultSlot 的调用入口，参数和具体处理见下方实现。
        super(container, slot, x, y); // 向当前声明或调用传入 super(container, slot, x, y)。
        this.menu = menu; // 设置 this.menu 为 menu。
    } // 结束当前作用域。

    // 只有拆解模式下允许把完整工具放入结果槽作为拆解源。
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean mayPlace(ItemStack stack) { // 定义 mayPlace 的调用入口，参数和具体处理见下方实现。
        return this.menu.canPlaceResultSlot(stack); // 返回 this.menu.canPlaceResultSlot(stack)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 按菜单当前状态判断结果槽是否能被取走。
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean mayPickup(Player player) { // 定义 mayPickup 的调用入口，参数和具体处理见下方实现。
        boolean mayPickup = this.menu.canTakeResultSlot(); // 设置 mayPickup 为 this.menu.canTakeResultSlot()。
        if (mayPickup) { // 检查 mayPickup；按条件决定是否执行本分支。
            this.takenMode = this.menu.getMode(); // 设置 this.takenMode 为 this.menu.getMode()。
        } // 结束当前作用域。
        return mayPickup; // 返回 mayPickup，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 工具台结果槽只允许单个物品。
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int getMaxStackSize() { // 定义 getMaxStackSize 的调用入口，参数和具体处理见下方实现。
        return 1; // 返回 1，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    // 玩家放入或清空结果槽时，通知菜单生成或取消拆解预览。
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) { // 定义 setByPlayer 的调用入口，参数和具体处理见下方实现。
        super.setByPlayer(newStack, oldStack); // 调用 setByPlayer，执行当前流程所需的处理。
        this.menu.resultSlotChanged(newStack); // 调用 resultSlotChanged，执行当前流程所需的处理。
    } // 结束当前作用域。

    // 玩家取走结果槽物品时，通知菜单消费材料或处理拆解状态。
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void onTake(Player player, ItemStack stack) { // 定义 onTake 的调用入口，参数和具体处理见下方实现。
        super.onTake(player, stack); // 调用 onTake，执行当前流程所需的处理。
        if (this.takenMode != null) { // 检查 this.takenMode != null；按条件决定是否执行本分支。
            this.menu.resultSlotTaken(this.takenMode); // 调用 resultSlotTaken，执行当前流程所需的处理。
            this.takenMode = null; // 设置 this.takenMode 为 null。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
