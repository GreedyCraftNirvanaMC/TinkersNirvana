/*
 * 根据 TinkersAntique 的 ItemThrowball 光球行为移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.gadgets.item;

import com.gctn.tconstruct.gadgets.entity.GlowballEntity;
import com.gctn.tconstruct.gadgets.GadgetModule;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;

/** 保留雪球物品的类型关系，使用原版光球的投掷速度与散布。 */
public final class GlowballItem extends SnowballItem {
    public GlowballItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            var projectile = new GlowballEntity(level, player);
            projectile.setItem(stack.copyWithCount(1));
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 2.1F, 0.5F);
            level.addFreshEntity(projectile);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        var projectile = new GlowballEntity(GadgetModule.GLOWBALL_ENTITY.get(), level);
        projectile.setPos(pos.x(), pos.y(), pos.z());
        projectile.setItem(stack.copyWithCount(1));
        return projectile;
    }
}
