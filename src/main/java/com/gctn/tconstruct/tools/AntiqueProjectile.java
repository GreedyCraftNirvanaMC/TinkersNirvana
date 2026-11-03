/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** 原版箭实体负责碰撞、保护事件、同步和掉落；附带工具数据用于材料词条。 */
public class AntiqueProjectile extends AbstractArrow implements net.minecraft.world.entity.projectile.ItemSupplier {
    private int groundedLife;
    private static final net.minecraft.network.syncher.EntityDataAccessor<ItemStack> TOOL =
            net.minecraft.network.syncher.SynchedEntityData.defineId(AntiqueProjectile.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK);

    public AntiqueProjectile(EntityType<? extends AntiqueProjectile> type, Level level) { super(type, level); }

    public AntiqueProjectile(Level level, LivingEntity owner, ItemStack pickup, ItemStack tool) {
        super(ToolRanged.PROJECTILE.get(), owner, level, pickup, tool.isEmpty() ? null : tool);
        entityData.set(TOOL, tool.copy());
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TOOL, ItemStack.EMPTY);
    }

    public ItemStack traitTool() { return entityData.get(TOOL); }
    @Override
    public ItemStack getItem() { return traitTool(); }
    @Override
    protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW); }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!level().isClientSide && getOwner() instanceof LivingEntity owner) {
            ToolTraits.forEach(traitTool(), (trait, level) -> trait.afterHit(traitTool(), level, target, owner));
            ToolModifiers.afterHit(traitTool(), target, owner);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TinkerGroundedLife", groundedLife);
        if (!traitTool().isEmpty()) { tag.put("TinkerTool", traitTool().save(registryAccess())); }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        groundedLife = Math.max(0, tag.contains("TinkerGroundedLife") ? tag.getInt("TinkerGroundedLife") : tag.getShort("life"));
        entityData.set(TOOL, ItemStack.parseOptional(registryAccess(), tag.getCompound("TinkerTool")));
    }

    @Override
    public void tick() {
        super.tick();
        if (!inGround) { groundedLife = 0; }
        if (!inGround) { ToolTraits.forEach(traitTool(), (trait, level) -> trait.onProjectileTick(traitTool(), level, this)); }
    }

    @Override protected void tickDespawn() {
        int limit = com.gctn.tconstruct.Config.PROJECTILE_DESPAWN.get();
        if (ToolModifiers.level(traitTool(), "reinforced") >= 5) { limit = Math.max(1, limit / 10); }
        if (++groundedLife >= limit) { discard(); }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide && com.gctn.tconstruct.tools.traits.AntiqueTraits.has(traitTool(), "breakable") && random.nextBoolean()) { discard(); }
    }

    @Override
    protected float getWaterInertia() { return ToolModifiers.level(traitTool(), "fins") > 0 ? .992F : super.getWaterInertia(); }

    @Override
    protected boolean tryPickup(net.minecraft.world.entity.player.Player player) {
        if (pickup != Pickup.ALLOWED || !(getPickupItemStackOrigin().getItem() instanceof com.gctn.tconstruct.tools.tools.AntiqueTool)) {
            return super.tryPickup(player);
        }
        ItemStack recovered = getPickupItemStackOrigin();
        var expected = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(recovered);
        expected.remove("Stats");
        for (ItemStack candidate : player.getInventory().items) {
            if (candidate.getItem() != recovered.getItem() || !candidate.isDamaged()) { continue; }
            var actual = com.gctn.tconstruct.init.TinkerDataComponents.getMaterialTag(candidate);
            actual.remove("Stats");
            if (expected.equals(actual)) {
                candidate.setDamageValue(Math.max(0, candidate.getDamageValue() - 10));
                return true;
            }
        }
        return super.tryPickup(player);
    }
}
