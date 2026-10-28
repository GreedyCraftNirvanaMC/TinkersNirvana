package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.tools.TinkerTools;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EcologicalTraits extends AbstractTraits {
    public EcologicalTraits(int level) {
        super("ecological", 1, level, -1, 0xFF55FF55);
    }

    @Override
    public void onUpdate(ItemStack tool, int level, Level world, Entity entity, int slot, boolean selected) {
        // Antique: 1/400 per inventory tick, except while using this tool. Broken tools need repair first.
        if (!world.isClientSide && entity instanceof LivingEntity living && living.getUseItem() != tool
                && world.random.nextInt(400) == 0) {
            TinkerTools.healTool(tool, 1, living);
        }
    }
}
