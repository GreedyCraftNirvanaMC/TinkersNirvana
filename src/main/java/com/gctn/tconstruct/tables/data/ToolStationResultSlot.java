package com.gctn.tconstruct.tables.data;

import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ToolStationResultSlot extends Slot {
    private final ToolStationMenu menu;
    private Mode takenMode;
    private ItemStack crafted = ItemStack.EMPTY;

    // 绑定工具台菜单，让结果槽把放入、取出事件交回菜单处理。
    public ToolStationResultSlot(ToolStationMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    // 默认页空置时，允许从结果槽放入完整工具以拆解。
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
            this.crafted = getItem().copy();
        }
        return mayPickup;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        // 手动放入时每次只能拆一个，领取合成产物仍可取走整批。
        return this.menu.canPlaceResultSlot(stack) ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public ItemStack remove(int amount) {
        // 与原版 ResultContainer 一致，右键领取也取走完整配方批次。
        return super.remove(this.getItem().getCount());
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
            this.menu.resultSlotTaken(this.takenMode, player, crafted);
            this.takenMode = null;
            this.crafted = ItemStack.EMPTY;
        }
    }
}
