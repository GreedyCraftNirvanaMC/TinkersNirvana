/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools.tools;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.stats.HeadMaterialStats;
import com.gctn.tconstruct.tools.*;
import com.gctn.tconstruct.tools.ToolDefinition.Action;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;
import java.util.List;

public class AntiqueTool extends TinkerTools {
    private final ToolDefinition definition;
    public AntiqueTool(ToolDefinition definition) { this.definition = definition; }
    public ToolDefinition definition() { return definition; }
    public boolean isLauncher() {
        return switch (definition.action()) { case SHORTBOW, LONGBOW, CROSSBOW -> true; default -> false; };
    }
    public boolean isAmmo() {
        return switch (definition.action()) { case ARROW, BOLT, SHURIKEN -> true; default -> false; };
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        if (isBroken(stack)) { return false; }
        return switch (definition.action()) {
            case PICKAXE, HAMMER -> ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability);
            case SHOVEL, EXCAVATOR -> ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability);
            case AXE, LUMBERAXE -> ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability);
            case MATTOCK -> ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability) || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability);
            case KAMA, SCYTHE -> ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(ability);
            case BROADSWORD -> ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(ability);
            case BATTLESIGN -> ability == ItemAbilities.SHIELD_BLOCK;
            default -> false;
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (isBroken(stack) || context.getPlayer() == null) { return InteractionResult.PASS; }
        if ((definition.action() == Action.KAMA || definition.action() == Action.SCYTHE)
                && context.getHand() == InteractionHand.MAIN_HAND && ToolHarvest.harvestCrops(this, stack, context)) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        for (ItemAbility ability : new ItemAbility[]{ItemAbilities.AXE_STRIP, ItemAbilities.AXE_SCRAPE,
                ItemAbilities.AXE_WAX_OFF, ItemAbilities.SHOVEL_FLATTEN, ItemAbilities.HOE_TILL}) {
            if (!canPerformAction(stack, ability)) { continue; }
            if (ability == ItemAbilities.SHOVEL_FLATTEN && !context.getLevel().isEmptyBlock(context.getClickedPos().above())) { continue; }
            BlockState result = state.getToolModifiedState(context, ability, false);
            if (result == null) { continue; }
            Level level = context.getLevel();
            if (!level.isClientSide) {
                level.setBlock(context.getClickedPos(), result, 11);
                level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, context.getClickedPos(),
                        net.minecraft.world.level.gameevent.GameEvent.Context.of(context.getPlayer(), result));
                damageTool(stack, 1, context.getPlayer(), context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            level.playSound(context.getPlayer(), context.getClickedPos(),
                    ability == ItemAbilities.HOE_TILL ? SoundEvents.HOE_TILL : ability == ItemAbilities.SHOVEL_FLATTEN
                            ? SoundEvents.SHOVEL_FLATTEN : SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (isBroken(stack) || (definition.action() != Action.KAMA && definition.action() != Action.SCYTHE)) { return InteractionResult.PASS; }
        if (entity instanceof net.neoforged.neoforge.common.IShearable target && target.isShearable(player, stack, entity.level(), entity.blockPosition())) {
            var drops = target.onSheared(player, stack, entity.level(), entity.blockPosition());
            if (!entity.level().isClientSide) {
                for (ItemStack drop : drops) { target.spawnShearedDrop(entity.level(), entity.blockPosition(), drop); }
                damageTool(stack, 1, player, LivingEntity.getSlotForHand(hand));
            }
            entity.gameEvent(net.minecraft.world.level.gameevent.GameEvent.SHEAR, player);
            return InteractionResult.sidedSuccess(entity.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!isBroken(stack) && definition.action() == Action.BROADSWORD && state.is(net.minecraft.world.level.block.Blocks.COBWEB)) { return 15; }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return !isBroken(stack) && ((definition.action() == Action.BROADSWORD && state.is(net.minecraft.world.level.block.Blocks.COBWEB))
                || super.isCorrectToolForDrops(stack, state));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && !isBroken(stack) && state.getDestroySpeed(level, pos) > 0) {
            boolean effective = isCorrectToolForDrops(stack, state);
            ToolTraits.forEach(stack, (trait, traitLevel) -> trait.afterBlockBreak(stack, traitLevel, level, state, pos, entity, effective));
            damageTool(stack, 1, entity, EquipmentSlot.MAINHAND);
            ToolHarvest.afterBreak(this, stack, level, state, pos, entity);
        }
        return true;
    }
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { return true; }
    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && !isBroken(stack)) {
            ToolTraits.forEach(stack, (trait, traitLevel) -> trait.afterHit(stack, traitLevel, target, attacker));
            ToolCombat.afterHit(this, stack, target, attacker);
            damageTool(stack, definition.effectiveBlocks().isEmpty() ? 1 : 2, attacker, EquipmentSlot.MAINHAND);
        }
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isBroken(stack)) { return InteractionResultHolder.fail(stack); }
        if (definition.action() == Action.LONGSWORD && player.isFallFlying()) { return InteractionResultHolder.pass(stack); }
        if (isLauncher() || definition.action() == Action.SHURIKEN) { return ToolRanged.use(this, level, player, hand); }
        if (definition.action() == Action.RAPIER) {
            if (!level.isClientSide) { ToolCombat.release(this, stack, player, 0); }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (definition.action() == Action.LONGSWORD || definition.action() == Action.RAPIER
                || definition.action() == Action.FRYPAN || definition.action() == Action.BATTLESIGN) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (isBroken(stack)) { return UseAnim.NONE; }
        return definition.action() == Action.BATTLESIGN ? UseAnim.BLOCK
                : isLauncher() ? (definition.action() == Action.CROSSBOW ? UseAnim.CROSSBOW : UseAnim.BOW)
                : definition.action() == Action.FRYPAN || definition.action() == Action.LONGSWORD ? UseAnim.BOW : UseAnim.NONE;
    }
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 72000; }
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (definition.action() == Action.CROSSBOW) { ToolRanged.charge(this, stack, level, entity, 72000 - remaining); }
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (isBroken(stack)) { return; }
        int used = 72000 - remaining;
        if (isLauncher()) { ToolRanged.release(this, stack, level, entity, used); }
        else if (!level.isClientSide && entity instanceof Player player) { ToolCombat.release(this, stack, player, used); }
    }
    public void damage(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) { damageTool(stack, amount, entity, slot); }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ToolModifiers.level(stack, "incognito") == 0 && (super.isFoil(stack)
                || TinkerDataComponents.getTraitsTag(stack).getCompound("shocking").getFloat("Charge") >= 100);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack previous, ItemStack current, boolean slotChanged) {
        return slotChanged || shouldCauseBlockBreakReset(previous, current);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack previous, ItemStack current) {
        if (previous.getItem() != current.getItem() || isBroken(previous) != isBroken(current)) { return true; }
        var before = TinkerDataComponents.getMaterialTag(previous);
        var after = TinkerDataComponents.getMaterialTag(current);
        for (String transientKey : new String[]{"Stats", "MossXP", "MossLastHeal", "LoadedAmmo"}) { before.remove(transientKey); after.remove(transientKey); }
        return !before.equals(after) || !ToolTraits.getTraits(previous).equals(ToolTraits.getTraits(current));
    }
    @Override
    public List<RepairInfo> getRepairInfo(ItemStack stack) {
        List<RepairInfo> result = new ArrayList<>();
        var data = TinkerDataComponents.getMaterialTag(stack);
        for (ToolDefinition.Part part : definition.parts()) {
            Material material = TinkerMaterials.getMaterial(data.getString(part.key()));
            if (material != null && (part.stat().equals("Head") || part.stat().equals("Bow"))) {
                HeadMaterialStats stats = material.getStats("Head", HeadMaterialStats.class);
                if (stats != null) { result.add(new RepairInfo(material, .25F, stats.durability)); }
            }
        }
        return result;
    }
}
