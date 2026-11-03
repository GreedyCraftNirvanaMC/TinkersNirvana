package com.gctn.tconstruct.tables.data;

import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModeAwareInputSlot extends Slot {
    private final ToolStationMenu menu;
    private final int slotIndex;

    public ModeAwareInputSlot(ToolStationMenu menu, Container container, int slot, int x, int y, int slotIndex) {
        super(container, slot, x, y);
        this.menu = menu;
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean isActive() {
        return this.menu.isInputSlotAvailable(this.menu.getMode(), this.slotIndex);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.isActive()
                && super.mayPlace(stack)
                && this.menu.canPlaceInput(this.menu.getMode(), this.slotIndex, stack);
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
