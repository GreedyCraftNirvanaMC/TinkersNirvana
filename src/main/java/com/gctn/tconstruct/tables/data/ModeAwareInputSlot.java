package com.gctn.tconstruct.tables.data;

import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 物理输入槽在特定工具台模式下的一个视图。
 * 未激活的视图拒绝放入和取出；取走拆解预览中的部件时确认拆解。
 */
public class ModeAwareInputSlot extends Slot {
    private final ToolStationMenu menu;
    private final Mode slotMode;
    private final int slotIndex;

    public ModeAwareInputSlot(ToolStationMenu menu, Container container, int slot, int x, int y, Mode slotMode, int slotIndex) {
        super(container, slot, x, y);
        this.menu = menu;
        this.slotMode = slotMode;
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean isActive() {
        return this.menu.isInputSlotAvailable(this.slotMode, this.slotIndex);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.isActive()
                && super.mayPlace(stack)
                && this.menu.canPlaceInput(this.slotMode, this.slotIndex, stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.isActive() && super.mayPickup(player);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        this.menu.disassemblyPartTaken();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.menu.inputsChanged();
    }
}
