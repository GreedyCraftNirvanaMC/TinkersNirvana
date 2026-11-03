/* SPDX-License-Identifier: LGPL-3.0-only; native frame lifecycle with Antique appearance. */
package com.gctn.tconstruct.gadgets.entity;

import com.gctn.tconstruct.gadgets.FancyFrames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FancyItemFrame extends ItemFrame {
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(FancyItemFrame.class, EntityDataSerializers.INT);
    public FancyItemFrame(EntityType<? extends ItemFrame> type, Level level) { super(type, level); }
    public FancyItemFrame(Level level, BlockPos pos, Direction direction, FancyFrames.Kind kind) {
        super(FancyFrames.ENTITY.get(), level, pos, direction); entityData.set(KIND, kind.ordinal());
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(KIND, 0); }
    public FancyFrames.Kind kind() { return FancyFrames.Kind.values()[Math.floorMod(entityData.get(KIND), FancyFrames.Kind.values().length)]; }
    @Override protected ItemStack getFrameItemStack() { return new ItemStack(FancyFrames.ITEMS.get(kind()).get()); }
    @Override public Component getName() { return hasCustomName() ? getCustomName() : getFrameItemStack().getHoverName(); }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("Frame", kind().ordinal()); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { entityData.set(KIND, Math.floorMod(tag.getInt("Frame"), FancyFrames.Kind.values().length)); super.readAdditionalSaveData(tag); }
}
