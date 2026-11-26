/* SPDX-License-Identifier: LGPL-3.0-only; placement follows the native HangingEntityItem. */
package com.gctn.tconstruct.gadgets.item;

import com.gctn.tconstruct.gadgets.FancyFrames;
import com.gctn.tconstruct.gadgets.entity.FancyItemFrame;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

public class FancyFrameItem extends HangingEntityItem {
    private final FancyFrames.Kind kind;
    public FancyFrameItem(FancyFrames.Kind kind) { super(FancyFrames.ENTITY.get(), new Properties()); this.kind = kind; }
    @Override public InteractionResult useOn(UseOnContext context) {
        var face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) { return InteractionResult.FAIL; }
        var pos = context.getClickedPos().relative(face);
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(pos, face, stack)) { return InteractionResult.FAIL; }
        var level = context.getLevel();
        var frame = new FancyItemFrame(level, pos, face, kind);
        var data = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (!data.isEmpty()) { EntityType.updateCustomEntityTag(level, player, frame, data); }
        if (!frame.survives()) { return InteractionResult.CONSUME; }
        if (!level.isClientSide) {
            if (!level.addFreshEntity(frame)) { return InteractionResult.FAIL; }
            frame.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, frame.position());
        }
        stack.shrink(1); return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
