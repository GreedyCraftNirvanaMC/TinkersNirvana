package com.gctn.tconstruct.tools.traits;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TraitEcological extends AbstractTraits{
    public TraitEcological(){
        super("ecological", 1, -1, 0xff8e661b);
    }
    @Override
    public int addLevel(int currentLevel, int amount){
        // 生态词条固定为一级，不随叠加提升等级。
        return maxLevel;
    }
    @Override
    public int mergeLevel(int currentLevel, int incomingLevel){
        return maxLevel;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CompoundTag traitTag) {

    }
}
