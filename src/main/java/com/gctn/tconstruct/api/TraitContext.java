package com.gctn.tconstruct.api;

import com.gctn.tconstruct.tools.ToolStats;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** One invocation. Only data passed to inventoryTick is automatically persisted. */
public final class TraitContext {
    public final ItemStack tool;
    public final int level;
    public ToolStats stats;
    public Level world;
    public Entity entity;
    public LivingEntity attacker;
    public LivingEntity target;
    public AbstractArrow projectile;
    public Object event;
    public CompoundTag data;
    public BlockPos pos;
    public BlockState state;
    public int slot;
    public boolean selected;
    public boolean effective;
    public int originalAmount;
    public int amount;
    public float originalDamage;
    public float damage;

    public void attribute(String modifierId, String attributeId, double amount, String operation, String slot) {
        if (stats == null) { throw new IllegalStateException("Attributes can only be added in modifyStats"); }
        TinkerContent.attribute(stats, modifierId, attributeId, amount, operation, slot);
    }

    public TraitContext(ItemStack tool, int level) {
        this.tool = tool;
        this.level = level;
    }
}
