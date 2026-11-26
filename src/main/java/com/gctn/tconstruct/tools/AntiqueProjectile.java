/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import com.gctn.tconstruct.tools.traits.AntiqueTraits;
import com.gctn.tconstruct.tools.traits.TinkerEffects;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.function.Predicate;

/** 原版箭实体负责碰撞、保护事件、同步和掉落；附带工具数据用于材料词条。 */
public class AntiqueProjectile extends AbstractArrow implements net.minecraft.world.entity.projectile.ItemSupplier {
    public static final ResourceKey<DamageType> ENDERFERENCE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID, "enderference_projectile"));
    private int groundedLife;
    private int flightTicks;
    private double launchSpeed;
    private static final net.minecraft.network.syncher.EntityDataAccessor<ItemStack> TOOL =
            net.minecraft.network.syncher.SynchedEntityData.defineId(AntiqueProjectile.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK);

    public AntiqueProjectile(EntityType<? extends AntiqueProjectile> type, Level level) { super(type, level); }

    public AntiqueProjectile(Level level, LivingEntity owner, ItemStack pickup, ItemStack tool) {
        this(level, owner, pickup, tool, ItemStack.EMPTY);
    }

    public AntiqueProjectile(Level level, LivingEntity owner, ItemStack pickup, ItemStack tool, ItemStack launcher) {
        // 合并词条的弹药只用于匠魂效果；原版武器来源必须是发射时的真实弓弩。
        super(ToolRanged.PROJECTILE.get(), owner, level, pickup, launcher.isEmpty() ? null : launcher);
        entityData.set(TOOL, tool.copy());
        ToolRanged.saveLauncher(this, launcher);
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

    void recordLaunchSpeed() { launchSpeed = getDeltaMovement().length(); }

    public boolean hurtTarget(EnderMan enderman, DamageSource source, Predicate<DamageSource> hurt) {
        boolean applied = !enderman.hasEffect(TinkerEffects.ENDERFERENCE) && AntiqueTraits.has(traitTool(), "enderference")
                && enderman.addEffect(new MobEffectInstance(TinkerEffects.ENDERFERENCE, 100, 1));
        if (!enderman.hasEffect(TinkerEffects.ENDERFERENCE)) { return hurt.test(source); }

        // 怀古保留弹体和发射者，但对受末影干扰的末影人绕开弹射物标签触发的闪避分支。
        boolean hit = hurt.test(damageSources().source(ENDERFERENCE_DAMAGE, this, source.getEntity()));
        if (hit) {
            // 原版箭遇到任何成功处理的末影人命中都会提前返回。真实命中仍要结算词条并移除弹体。
            if (projectileAction() != ToolDefinition.Action.SHURIKEN) { enderman.setArrowCount(enderman.getArrowCount() + 1); }
            doPostHurtEffects(enderman);
            playSound(net.minecraft.sounds.SoundEvents.ARROW_HIT, 1F, 1F);
            discard();
        } else if (applied) {
            enderman.removeEffect(TinkerEffects.ENDERFERENCE);
        }
        return hit;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide && getOwner() instanceof LivingEntity attacker && hit.getEntity() instanceof LivingEntity target) {
            AntiqueTraits.shockingHit(traitTool(), attacker, target);
        }
        double speed = getDeltaMovement().length();
        if (launchSpeed <= 0 || speed <= 0 || level().isClientSide) {
            super.onHitEntity(hit);
            return;
        }
        // 怀古按发射时的蓄力计算伤害。原版碰撞仍会乘以当前速度，所以先抵消这个倍率。
        // 满弓的暴击标记用于粒子，伤害只在射手满足怀古的坠落判定时增加。
        double normalDamage = getBaseDamage();
        boolean fullDraw = isCritArrow();
        double critical = 1;
        if (getOwner() instanceof LivingEntity owner && owner.fallDistance > 0 && !owner.onGround()
                && !owner.onClimbable() && !owner.isInWater()
                && !owner.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS) && !owner.isPassenger()) {
            critical = 1.5;
        }
        setBaseDamage(normalDamage * launchSpeed / speed * critical);
        setCritArrow(false);
        try {
            super.onHitEntity(hit);
        } finally {
            setBaseDamage(normalDamage);
            setCritArrow(fullDraw);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!level().isClientSide && getOwner() instanceof LivingEntity owner) {
            ToolModifiers.applyExtraKnockback(traitTool(), target, owner);
            ToolTraits.forEach(traitTool(), (trait, level) -> trait.afterHit(traitTool(), level, target, owner));
            ToolModifiers.afterHit(traitTool(), target, owner);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TinkerGroundedLife", groundedLife);
        if (launchSpeed > 0) { tag.putDouble("TinkerLaunchSpeed", launchSpeed); }
        if (!traitTool().isEmpty()) { tag.put("TinkerTool", traitTool().save(registryAccess())); }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        groundedLife = Math.max(0, tag.contains("TinkerGroundedLife") ? tag.getInt("TinkerGroundedLife") : tag.getShort("life"));
        launchSpeed = tag.getDouble("TinkerLaunchSpeed");
        entityData.set(TOOL, ItemStack.parseOptional(registryAccess(), tag.getCompound("TinkerTool")));
    }

    @Override
    public void tick() {
        boolean flight = !inGround && !isNoPhysics();
        boolean wet = isInWater();
        double gravity = isNoGravity() ? 0 : getGravity();
        Vec3 incoming = getDeltaMovement();
        super.tick();
        if (inGround || isRemoved()) {
            flightTicks = 0;
            return;
        }
        groundedLife = 0;
        if (flight && incoming.dot(getDeltaMovement()) > 0) {
            flightTicks++;
            updateFlight(wet, gravity);
        } else {
            flightTicks = 0;
        }
        if (!inGround && !isRemoved()) {
            ToolTraits.forEach(traitTool(), (trait, level) -> trait.onProjectileTick(traitTool(), level, this));
        }
    }

    private void updateFlight(boolean wet, double gravity) {
        boolean hovering = AntiqueTraits.has(traitTool(), "hovering", "onProjectileTick");
        boolean endspeed = AntiqueTraits.has(traitTool(), "endspeed", "onProjectileTick");
        double nativeDrag = wet ? getWaterInertia() : .99F;
        if (endspeed) {
            setDeltaMovement(getDeltaMovement().scale(1 / nativeDrag).add(0, -getDefaultGravity() / 250, 0));
            if (flightTicks > 1) {
                advanceEndspeed();
            }
        } else if (hovering || !wet && slowdown() != .01) {
            double drag = hovering ? .99 : 1 - slowdown();
            double fall = hovering ? gravity * .05 : gravity;
            setDeltaMovement(getDeltaMovement().add(0, gravity, 0).scale(drag / nativeDrag).add(0, -fall, 0));
        }
    }

    private void advanceEndspeed() {
        double traveledSquared = 0;
        double particleTravel = 0;
        while (!inGround && !isRemoved() && traveledSquared < 40) {
            Vec3 start = position();
            Vec3 step = getDeltaMovement();
            Vec3 end = start.add(step);
            double distanceSquared = step.lengthSqr();
            // 沿实际弹道逐段碰撞，保留各段的原版投射物事件，且不为加速飞行加载区块。
            if (distanceSquared < .001 || !level().hasChunkAt(BlockPos.containing(end))) {
                break;
            }
            HitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            EntityHitResult entityHit = findHitEntity(start, hit.getType() == HitResult.Type.MISS ? end : hit.getLocation());
            if (entityHit != null) {
                if (entityHit.getEntity() instanceof Player victim && getOwner() instanceof Player owner && !owner.canHarmPlayer(victim)) {
                    break;
                }
                hit = entityHit;
            }
            if (hit.getType() != HitResult.Type.MISS) {
                if (EventHooks.onProjectileImpact(this, hit)) {
                    setPos(end);
                    break;
                }
                ProjectileDeflection deflection = hitTargetOrDeflectSelf(hit);
                hasImpulse = true;
                if (inGround) {
                    setPos(position().add(getDeltaMovement()));
                }
                if (deflection != ProjectileDeflection.NONE || inGround || isRemoved() || step.dot(getDeltaMovement()) <= 0) {
                    break;
                }
            }
            setPos(end);
            checkInsideBlocks();
            setDeltaMovement(getDeltaMovement().add(0, -getDefaultGravity() / 250, 0));
            traveledSquared += distanceSquared;
            particleTravel += distanceSquared;
            if (level().isClientSide && particleTravel > .3) {
                level().addParticle(ParticleTypes.PORTAL, getX(), getY(), getZ(), 0, 0, 0);
                particleTravel = 0;
            }
        }
        updateRotation();
    }

    private ToolDefinition.Action projectileAction() {
        return traitTool().getItem() instanceof AntiqueTool tool ? tool.definition().action() : ToolDefinition.Action.ARROW;
    }

    private double slowdown() {
        return switch (projectileAction()) {
            case BOLT -> .015;
            case SHURIKEN -> .05;
            default -> .01;
        };
    }

    @Override
    protected double getDefaultGravity() {
        return switch (projectileAction()) {
            case BOLT -> .065;
            case SHURIKEN -> (tickCount / 10) * .04;
            default -> super.getDefaultGravity();
        };
    }

    @Override protected void tickDespawn() {
        int limit = com.gctn.tconstruct.Config.PROJECTILE_DESPAWN.get();
        if (ToolModifiers.level(traitTool(), "reinforced") >= 5) { limit = Math.max(1, limit / 10); }
        if (++groundedLife >= limit) { discard(); }
    }

    @Override
    protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
        super.onHitBlock(result);
        ToolTraits.forEach(traitTool(), (trait, level) -> trait.onProjectileHitBlock(traitTool(), level, this, result));
    }

    @Override
    protected float getWaterInertia() {
        return (float) (ToolModifiers.level(traitTool(), "fins") > 0 ? 1 - slowdown() * .8 : (1 - slowdown()) * .6);
    }

    @Override
    protected boolean tryPickup(Player player) {
        ItemStack recovered = getPickupItemStackOrigin();
        if (pickup != Pickup.ALLOWED || !(recovered.getItem() instanceof AntiqueTool ammo)) {
            return super.tryPickup(player);
        }
        var expected = TinkerDataComponents.getMaterialTag(recovered);
        if (restoreAmmo(player.getMainHandItem(), ammo, expected) || restoreAmmo(player.getOffhandItem(), ammo, expected)) {
            return true;
        }
        for (ItemStack candidate : player.getInventory().items) {
            if (restoreAmmo(candidate, ammo, expected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean restoreAmmo(ItemStack candidate, AntiqueTool ammo, CompoundTag expected) {
        if (!candidate.is(ammo) || !candidate.isDamaged()) {
            return false;
        }
        var actual = TinkerDataComponents.getMaterialTag(candidate);
        if (!expected.getCompound("Modifiers").equals(actual.getCompound("Modifiers"))) {
            return false;
        }
        for (ToolDefinition.Part part : ammo.definition().parts()) {
            if (!expected.getString(part.key()).equals(actual.getString(part.key()))) {
                return false;
            }
        }
        // 修复次数和材料定义缓存会变化，但不会改变弹药属于哪一组部件和强化。
        candidate.setDamageValue(Math.max(0, candidate.getDamageValue() - ToolRanged.AMMO_DURABILITY));
        return true;
    }
}
