package com.gctn.tconstruct.tables.data;

import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 将结果槽的放入与取出操作交给服务端事务状态处理。
 * 取出前记录操作模式，因为清空拆解源工具会取消对应预览。
 * 只有拆解模式允许放入源工具，其他模式仅展示生成的结果。
 */
public class ToolStationResultSlot extends Slot {
    private final ToolStationMenu menu;
    private Mode takenMode;

    // 绑定工具台菜单，让结果槽把放入、取出事件交回菜单处理。
    public ToolStationResultSlot(ToolStationMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    // 只有拆解模式下允许把完整工具放入结果槽作为拆解源。
    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.menu.canPlaceResultSlot(stack);
    }

    // 按菜单当前状态判断结果槽是否能被取走。
    @Override
    public boolean mayPickup(Player player) {
        boolean mayPickup = this.menu.canTakeResultSlot();
        if (mayPickup) {
            this.takenMode = this.menu.getMode();
        }
        return mayPickup;
    }

    // 工具台结果槽只允许单个物品。
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    // 玩家放入或清空结果槽时，通知菜单生成或取消拆解预览。
    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        super.setByPlayer(newStack, oldStack);
        this.menu.resultSlotChanged(newStack);
    }

    // 玩家取走结果槽物品时，通知菜单消费材料或处理拆解状态。
    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        if (this.takenMode != null) {
            this.menu.resultSlotTaken(this.takenMode);
            this.takenMode = null;
        }
    }
}
