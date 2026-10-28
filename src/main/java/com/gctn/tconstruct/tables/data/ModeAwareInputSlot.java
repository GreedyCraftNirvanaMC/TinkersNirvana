package com.gctn.tconstruct.tables.data;

import com.gctn.tconstruct.tables.menu.ToolStationMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * One mode-specific view over a physical station input slot.
 * Inactive views reject pickup/insertion, while taking a preview part commits disassembly.
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
