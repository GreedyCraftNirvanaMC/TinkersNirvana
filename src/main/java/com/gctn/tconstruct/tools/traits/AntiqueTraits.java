/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.common.slime.SlimeMobModule;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tools.*;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** 共享生命周期承载 Antique 材料词条；所有运行状态属于工具或效果实例。 */
public final class AntiqueTraits {
    private AntiqueTraits() {}
    public static void register() {
        for (String id : new String[]{"alien", "aquadynamic", "aridiculous", "autosmelt", "baconlicious", "coldblooded",
                "crude", "crumbling", "dense", "depthdigger", "duritos", "enderference", "established", "flammable", "fractured",
                "heavy", "hellish", "holy", "insatiable", "jagged", "lightweight", "magnetic", "momentum", "petramor",
                "poisonous", "prickly", "raging", "sharp", "shocking", "slimey_green", "slimey_blue", "spiky",
                "splintering", "splinters", "squeaky", "superheat", "stiff", "stonebound", "tasty", "unnatural", "writable",
                "breakable", "endspeed", "freezing", "hovering", "splitting"}) {
            TinkerTraits.registerTrait(new Builtin(id));
        }
    }

    public static AbstractTraits.TraitEntry entry(String id, int level) {
        return new AbstractTraits.TraitEntry(TinkerTraits.getTrait(id), level);
    }
    public static boolean has(ItemStack tool, String id) { return level(tool, id) > 0; }
    public static int level(ItemStack tool, String id) {
        return ToolTraits.getTraits(tool).stream().filter(entry -> entry.trait().identifier.equals(id)).mapToInt(ToolTraits.Entry::level).findFirst().orElse(0);
    }

    private static int traitColor(String id) {
        return switch (id) {
            case "prickly", "spiky" -> 0xff00aa00;
            case "enderference" -> 0xff00aaaa;
            case "lightweight" -> 0xff00ff00;
            case "crude" -> 0xff424242;
            case "stonebound" -> 0xff555555;
            case "momentum" -> 0xff5555ff;
            case "slimey_blue", "slimey_green", "splinters" -> 0xff55ff55;
            case "aquadynamic", "jagged" -> 0xff55ffff;
            case "aridiculous" -> 0xffaa0000;
            case "insatiable" -> 0xffaa00aa;
            case "raging" -> 0xffc70000;
            case "magnetic" -> 0xffdddddd;
            case "coldblooded", "crumbling", "hellish" -> 0xffff0000;
            case "autosmelt" -> 0xffff5500;
            case "petramor", "tasty" -> 0xffff5555;
            case "duritos", "unnatural" -> 0xffff55ff;
            case "baconlicious" -> 0xffffaaaa;
            case "alien", "squeaky" -> 0xffffff55;
            case "breakable", "dense", "depthdigger", "endspeed", "established", "flammable", "freezing", "heavy", "holy", "hovering", "poisonous", "sharp", "shocking", "splintering", "splitting", "stiff", "superheat" -> 0xffffffff;
            default -> 0xffffffff;
        };
    }

    private static final class Builtin extends LeveledTraits {
        Builtin(String id) { super(id, switch (id) { case "crude", "magnetic", "raging", "writable" -> 3; default -> 1; }, -1, traitColor(id)); }
        @Override public int getPriority() { return identifier.equals("squeaky") ? Integer.MIN_VALUE : 100; }

        @Override
        public void onProjectileLaunch(ItemStack tool, int level, net.minecraft.world.entity.projectile.AbstractArrow arrow) {
            if (identifier.equals("hovering")) { arrow.setDeltaMovement(arrow.getDeltaMovement().scale(.5)); }
            else if (identifier.equals("endspeed")) { arrow.setNoGravity(true); }
        }

        @Override
        public void onProjectileTick(ItemStack tool, int level, net.minecraft.world.entity.projectile.AbstractArrow arrow) {
            if (identifier.equals("hovering")) { arrow.setDeltaMovement(arrow.getDeltaMovement().add(0, .0475, 0)); }
            else if (identifier.equals("endspeed") && arrow.tickCount > 1 && arrow.getDeltaMovement().lengthSqr() > .001) {
                arrow.setDeltaMovement(arrow.getDeltaMovement().normalize().scale(6));
            }
        }

        @Override
        public void modifyStats(ItemStack tool, int level, ToolStats stats) {
            switch (identifier) {
                case "lightweight" -> stats.attackSpeed *= 1.1F;
                case "fractured" -> stats.attack += 1.5F;
                case "alien" -> {
                    CompoundTag data = TinkerDataComponents.getTraitsTag(tool).getCompound(identifier);
                    stats.growthAttack += data.getFloat("Attack");
                    stats.growthSpeed += data.getFloat("Speed");
                    stats.growthDurability += data.getInt("Durability");
                }
                default -> { }
            }
        }

        @Override
        public int onToolDamage(ItemStack tool, int level, int original, int amount, LivingEntity entity) {
            if (entity == null) { return amount; }
            switch (identifier) {
                case "duritos" -> {
                    float random = entity.getRandom().nextFloat();
                    return random < .1F ? amount + original : random < .5F ? Math.max(0, amount - original) : amount;
                }
                case "dense" -> {
                    if (entity.getRandom().nextFloat() < .75F * tool.getDamageValue() / Math.max(1F, tool.getMaxDamage())) {
                        return Math.max(0, amount - Math.max(original / 2, 1));
                    }
                }
                case "insatiable" -> { return amount + TinkerEffects.level(entity, TinkerEffects.INSATIABLE) / 3; }
                default -> { }
            }
            return amount;
        }

        @Override
        public float modifyAttack(ItemStack tool, int level, LivingEntity attacker, LivingEntity target, float original, float damage) {
            return switch (identifier) {
                case "coldblooded" -> damage + (target.getHealth() >= target.getMaxHealth() ? original / 2 : 0);
                case "crude" -> damage + (target.getArmorValue() == 0 ? original * .05F * level : 0);
                case "hellish" -> damage + (target.fireImmune() ? 0 : 4);
                case "holy" -> damage + (target.getType().is(EntityTypeTags.UNDEAD) ? 5 : 0);
                case "jagged" -> damage + (float) (Math.log(tool.getDamageValue() / 72D + 1) * 2);
                case "superheat" -> damage + (target.isOnFire() ? original * .35F : 0);
                case "raging" -> damage + 2.5F * level * (1 - attacker.getHealth() / attacker.getMaxHealth());
                case "insatiable" -> damage + TinkerEffects.level(attacker, TinkerEffects.INSATIABLE) / 3F;
                case "splintering" -> damage + .3F * TinkerEffects.level(target, TinkerEffects.SPLINTER);
                case "aridiculous" -> damage + 2 * aridity(attacker.level(), attacker.blockPosition());
                case "shocking" -> damage + (TinkerDataComponents.getTraitsTag(tool).getCompound(identifier).getFloat("Charge") >= 100 ? 5 : 0);
                case "squeaky" -> 0;
                default -> damage;
            };
        }

        @Override
        public void miningSpeed(ItemStack tool, int level, PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();
            BlockPos pos = event.getPosition().orElse(player.blockPosition());
            float original = event.getOriginalSpeed();
            float speed = event.getNewSpeed();
            switch (identifier) {
                case "aquadynamic" -> {
                    float coefficient = 1 + (player.isInWater() ? 5.5F : 0);
                    if (player.level().isRaining()) { coefficient += player.level().getBiome(pos).value().getModifiedClimateSettings().downfall() / 1.6F; }
                    if (coefficient != 1) { speed += original * coefficient; }
                }
                case "aridiculous" -> speed += original * aridity(player.level(), pos) / 10;
                case "depthdigger" -> speed += Math.max(0, 72 - pos.getY()) / 30F;
                case "stonebound" -> { if (tool.isCorrectToolForDrops(event.getState())) { speed += (float) (Math.log(tool.getDamageValue() / 72D + 1) * 2); } }
                case "momentum" -> speed += original * TinkerEffects.level(player, TinkerEffects.MOMENTUM) / 80F;
                case "lightweight" -> speed += original * .1F;
                case "crumbling" -> { if (!event.getState().requiresCorrectToolForDrops()) { speed *= ToolBuilder.savedStats(tool).getFloat("MiningSpeed") * .5F; } }
                case "unnatural" -> {
                    int required = event.getState().is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL) ? 4 : event.getState().is(BlockTags.INCORRECT_FOR_IRON_TOOL) ? 3
                            : event.getState().is(BlockTags.INCORRECT_FOR_STONE_TOOL) ? 2 : event.getState().is(BlockTags.INCORRECT_FOR_WOODEN_TOOL) ? 1 : 0;
                    if (tool.isCorrectToolForDrops(event.getState())) { speed += Math.max(0, ToolBuilder.savedStats(tool).getInt("HarvestLevel") - required); }
                }
                default -> { }
            }
            event.setNewSpeed(Math.max(0, speed));
        }

        @Override
        public void afterBlockBreak(ItemStack tool, int level, Level world, BlockState state, BlockPos pos, LivingEntity entity, boolean effective) {
            switch (identifier) {
                case "momentum" -> entity.addEffect(new MobEffectInstance(TinkerEffects.MOMENTUM,
                        Math.max(1, (int) (300 / Math.max(.1F, ToolBuilder.savedStats(tool).getFloat("MiningSpeed")))),
                        Math.min(31, TinkerEffects.level(entity, TinkerEffects.MOMENTUM)), false, false));
                case "magnetic" -> entity.addEffect(new MobEffectInstance(TinkerEffects.MAGNETIC, 30, level, false, false));
                case "petramor" -> { if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) && entity.getRandom().nextFloat() < .1F) { TinkerTools.healTool(tool, 5, entity); } }
                case "baconlicious" -> { if (entity.getRandom().nextFloat() < .005F) { world.addFreshEntity(new ItemEntity(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, new ItemStack(ToolSupplies.item("bacon")))); } }
                case "slimey_green", "slimey_blue" -> { if (effective) { slime(world, pos, entity); } }
                case "splinters" -> { if (entity.getRandom().nextInt(150) == 0) { TraitDamage.hurt(entity, null, "splinters", .1F); } }
                case "shocking" -> charge(tool, entity, true);
                default -> { }
            }
        }

        @Override
        public void afterHit(ItemStack tool, int level, LivingEntity target, LivingEntity attacker) {
            switch (identifier) {
                case "poisonous" -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 101));
                case "holy" -> { if (target.getType().is(EntityTypeTags.UNDEAD)) { target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50)); } }
                case "sharp" -> target.addEffect(new MobEffectInstance(TinkerEffects.BLEED, 121));
                case "insatiable" -> attacker.addEffect(new MobEffectInstance(TinkerEffects.INSATIABLE, 100, Math.min(9, TinkerEffects.level(attacker, TinkerEffects.INSATIABLE)), false, false));
                case "splintering" -> target.addEffect(new MobEffectInstance(TinkerEffects.SPLINTER, 40, Math.min(5, TinkerEffects.level(target, TinkerEffects.SPLINTER)), false, false));
                case "enderference" -> { if (target instanceof net.minecraft.world.entity.monster.EnderMan) { target.addEffect(new MobEffectInstance(TinkerEffects.ENDERFERENCE, 100, 1)); } }
                case "prickly" -> TraitDamage.hurt(target, attacker, "prickly", Math.max(0, .5F + (float) attacker.getRandom().nextGaussian() * .75F));
                case "magnetic" -> attacker.addEffect(new MobEffectInstance(TinkerEffects.MAGNETIC, 30, level, false, false));
                case "freezing" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, Math.min(4, TinkerEffects.level(target, MobEffects.MOVEMENT_SLOWDOWN))));
                case "shocking" -> charge(tool, attacker, false);
                case "slimey_green", "slimey_blue" -> { if (!target.isAlive()) { slime(target.level(), target.blockPosition(), attacker); } }
                case "baconlicious" -> { if (!target.isAlive() && attacker.getRandom().nextFloat() < .05F) { target.spawnAtLocation(ToolSupplies.item("bacon")); } }
                case "splinters" -> { if (attacker.getRandom().nextInt(150) == 0) { TraitDamage.hurt(attacker, null, "splinters", .1F); } }
                default -> { }
            }
        }

        @Override
        public void onDamageTaken(ItemStack tool, int level, LivingIncomingDamageEvent event) {
            LivingEntity player = event.getEntity();
            if (identifier.equals("flammable")) {
                if (event.getSource().getEntity() != null) { event.getSource().getEntity().igniteForSeconds(3); }
                if (player.isBlocking() && event.getSource().is(DamageTypeTags.IS_FIRE)) {
                    if (tool.getItem() instanceof AntiqueTool item) { item.damage(tool, 3, player, LivingEntity.getSlotForHand(player.getUsedItemHand())); }
                    event.setCanceled(true);
                }
            } else if (identifier.equals("spiky") && event.getSource().getDirectEntity() instanceof LivingEntity attacker
                    && attacker != player && !event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
                    attacker.hurt(player.damageSources().thorns(player), Math.max(.1F, ToolBuilder.savedStats(tool).getFloat("Attack") * .5F));
                }
            } else if (identifier.equals("stiff") && player.isBlocking()) { event.setAmount(Math.max(1, event.getAmount() - 1)); }
        }

        @Override
        public void onBlockDrops(ItemStack tool, int level, BlockDropsEvent event) {
            if (identifier.equals("autosmelt")) { ToolModifiers.smeltDrops(event); }
            else if (identifier.equals("established") && event.getLevel().random.nextFloat() < (event.getDroppedExperience() > 0 ? .33F : .03F)) {
                event.setDroppedExperience(event.getDroppedExperience() + 1);
            }
        }

        @Override
        public void inventoryTick(ItemStack tool, Level world, Entity entity, int slot, boolean selected, CompoundTag data) {
            if (TinkerTools.isBroken(tool)) { return; }
            if (identifier.equals("tasty") && selected && entity instanceof Player player && player.getFoodData().needsFood()
                    && tool.getMaxDamage() - tool.getDamageValue() >= 5 && tool.getItem() instanceof AntiqueTool item) {
                float chance = .01F + (player.getHealth() < player.getMaxHealth() ? .02F : 0)
                        + Math.max(0, 10 - player.getFoodData().getFoodLevel()) * .0025F;
                if (player.getRandom().nextFloat() < chance) { player.getFoodData().eat(1, 0); item.damage(tool, 5, player, EquipmentSlot.MAINHAND); player.playSound(SoundEvents.GENERIC_EAT, .8F, 1); }
            } else if (identifier.equals("shocking") && selected && world.getGameTime() % 5 == 0) {
                if (data.contains("X")) {
                    double distance = Math.sqrt(entity.distanceToSqr(data.getDouble("X"), data.getDouble("Y"), data.getDouble("Z")));
                    float before = data.getFloat("Charge");
                    float charge = Math.min(100, before + (float) Math.min(5, distance) * 2);
                    data.putFloat("Charge", charge);
                    if (before < 100 && charge >= 100 && entity instanceof net.minecraft.server.level.ServerPlayer player) {
                        player.playNotifySound(ToolSounds.CHARGED.get(), SoundSource.PLAYERS, .8F, 1);
                    }
                }
                data.putDouble("X", entity.getX()); data.putDouble("Y", entity.getY()); data.putDouble("Z", entity.getZ());
            } else if (identifier.equals("alien") && entity.tickCount % 72 == 0 && entity instanceof Player player
                    && !(player instanceof net.neoforged.neoforge.common.util.FakePlayer) && !player.isUsingItem()
                    && tool.getItem() instanceof AntiqueTool && data.getInt("Points") < 800) {
                if (!data.contains("Pool")) {
                    int[] pool = new int[3];
                    for (int point = 0; point < 800; point++) { pool[player.getRandom().nextInt(3)]++; }
                    data.putIntArray("Pool", pool);
                }
                int[] pool = data.getIntArray("Pool");
                if (pool.length != 3) { return; }
                boolean changed = false;
                if (entity.tickCount % 216 == 0 && data.getInt("AttackPoints") < pool[2]) {
                    data.putInt("AttackPoints", data.getInt("AttackPoints") + 1);
                    data.putFloat("Attack", data.getFloat("Attack") + .005F);
                    changed = true;
                } else if (entity.tickCount % 144 == 0 && data.getInt("SpeedPoints") < pool[1]) {
                    data.putInt("SpeedPoints", data.getInt("SpeedPoints") + 1);
                    data.putFloat("Speed", data.getFloat("Speed") + .007F);
                    changed = true;
                } else if (data.getInt("Durability") < pool[0]) {
                    data.putInt("Durability", data.getInt("Durability") + 1);
                    changed = true;
                }
                if (!changed) { return; }
                data.putInt("Points", data.getInt("Points") + 1);
                // 分发器保存状态后由下一次物品更新统一重算。
                data.putBoolean("Rebuild", true);
            }
        }

        private void charge(ItemStack tool, LivingEntity entity, boolean mining) {
            ToolTraits.updateTraitData(tool, identifier, data -> {
                float charge = data.getFloat("Charge");
                if (charge >= 100) {
                    entity.addEffect(new MobEffectInstance(mining ? MobEffects.DIG_SPEED : MobEffects.MOVEMENT_SPEED, 50, mining ? 2 : 5));
                    data.putFloat("Charge", 0);
                    entity.level().playSound(null, entity.blockPosition(), ToolSounds.DISCHARGE.get(), SoundSource.PLAYERS, 1, 1);
                } else { data.putFloat("Charge", Math.min(100, charge + 15)); }
            });
        }
        private void slime(Level world, BlockPos pos, LivingEntity entity) {
            if (entity.getRandom().nextFloat() >= .0033F) { return; }
            Slime slime = identifier.equals("slimey_blue") ? SlimeMobModule.BLUE.get().create(world) : EntityType.SLIME.create(world);
            if (slime != null) { slime.setSize(1, true); slime.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5); world.addFreshEntity(slime); }
        }
        private static float aridity(Level world, BlockPos pos) {
            var biome = world.getBiome(pos).value();
            float rainfall = biome.getModifiedClimateSettings().downfall();
            return (float) (Math.pow(1.25, 3 * (.5F + biome.getBaseTemperature() - rainfall)) - 1.25) - (world.isRaining() ? rainfall / 2 : 0);
        }
    }
}
