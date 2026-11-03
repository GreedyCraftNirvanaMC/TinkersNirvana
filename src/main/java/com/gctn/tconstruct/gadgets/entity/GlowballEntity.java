/*
 * 根据 TinkersAntique 的 EntityThrowball 光球行为移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.gadgets.entity;

import com.gctn.tconstruct.gadgets.GadgetModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** 沿用原生投射物同步、重力与碰撞事件，在命中位置尝试生成光斑。 */
public final class GlowballEntity extends ThrowableItemProjectile {
    public GlowballEntity(EntityType<? extends GlowballEntity> type, Level level) {
        super(type, level);
    }

    public GlowballEntity(Level level, LivingEntity shooter) {
        super(GadgetModule.GLOWBALL_ENTITY.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return GadgetModule.GLOWBALL.get();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide) {
            if (getItem().is(GadgetModule.EFLN.get()) && level() instanceof net.minecraft.server.level.ServerLevel server) {
                com.gctn.tconstruct.gadgets.EflnBlast.start(server, this); discard(); return;
            }
            BlockPos pos = null;
            Direction facing = Direction.DOWN;
            if (hit instanceof BlockHitResult blockHit) {
                pos = blockHit.getBlockPos().relative(blockHit.getDirection());
                facing = blockHit.getDirection().getOpposite();
            } else if (hit instanceof EntityHitResult entityHit) {
                // 命中实体不会造成伤害，只在其脚下的位置寻找光斑支撑。
                pos = entityHit.getEntity().blockPosition();
            }
            if (pos != null) {
                GadgetModule.GLOW.get().addGlow(level(), pos, facing);
            }
            discard();
        }
    }
}
