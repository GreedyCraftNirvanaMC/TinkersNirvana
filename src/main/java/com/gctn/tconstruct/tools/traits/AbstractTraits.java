package com.gctn.tconstruct.tools.traits;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Immutable definition. Runtime levels and state belong to the tool, never the registry. */
public abstract class AbstractTraits {
    public final String identifier;
    public final int maxLevel;
    public final int level;
    public final int color;
    public final int strengthenNumber;

    protected AbstractTraits(String identifier, int maxLevel, int level, int strengthenNumber, int color) {
        if (identifier == null || identifier.isBlank() || maxLevel < 1 || level < 1 || level > maxLevel) {
            throw new IllegalArgumentException("Invalid trait definition: " + identifier);
        }
        this.identifier = identifier;
        this.maxLevel = maxLevel;
        this.level = level;
        this.strengthenNumber = strengthenNumber;
        this.color = color;
    }

    /** Ordinary traits do not stack when several parts supply the same trait. */
    public int addLevel(int existingLevel) {
        return Math.clamp(Math.max(existingLevel, level), 1, maxLevel);
    }

    public int mergeLevel(int existingLevel) { return addLevel(existingLevel); }

    public Component getDisplayName(int toolLevel) {
        var name = Component.translatable("traits." + identifier + ".name");
        return (maxLevel > 1 ? Component.translatable("tooltip.tconstruct.trait.level", name, toolLevel) : name)
                .withColor(color);
    }

    public Component getDescription() {
        return Component.translatable("traits." + identifier + ".description").withColor(color);
    }

    /** Higher priorities run first; ties use the identifier for a stable order. */
    public int getPriority() { return 100; }

    public int modifyDurability(ItemStack tool, int level, int durability) { return durability; }
    public int onToolDamage(ItemStack tool, int level, int original, int amount, LivingEntity entity) { return amount; }
    public int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) { return amount; }
    public void onRepair(ItemStack tool, int level, int amount) {}
    public void onUpdate(ItemStack tool, int level, Level world, Entity entity, int slot, boolean selected) {}
    public void miningSpeed(ItemStack tool, int level, PlayerEvent.BreakSpeed event) {}
    public void beforeBlockBreak(ItemStack tool, int level, BlockEvent.BreakEvent event) {}
    public void afterBlockBreak(ItemStack tool, int level, Level world, BlockState state, BlockPos pos,
                                LivingEntity entity, boolean effective) {}
    public void afterHit(ItemStack tool, int level, LivingEntity target, LivingEntity attacker) {}

    public record TraitPosPair(String pos, AbstractTraits... traits) {
        public static TraitPosPair of(String pos, AbstractTraits... traits) {
            return new TraitPosPair(pos, traits);
        }
    }
}
