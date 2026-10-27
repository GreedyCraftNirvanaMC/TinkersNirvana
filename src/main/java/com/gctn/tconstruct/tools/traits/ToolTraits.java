package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.init.TinkerDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/** Single reader/dispatcher for persisted traits. Unknown data is preserved but not executed. */
public final class ToolTraits {
    private ToolTraits() {}

    public static void addTraits(CompoundTag tag, List<AbstractTraits> traits) {
        for (AbstractTraits trait : traits) {
            CompoundTag data = tag.getCompound(trait.identifier);
            if (trait instanceof LeveledTraits) {
                // Antique applies each distinct strength once, even when several parts supply it.
                int[] applied = data.getIntArray("AppliedLevels");
                if (java.util.Arrays.stream(applied).anyMatch(value -> value == trait.level)) {
                    continue;
                }
                int[] updated = java.util.Arrays.copyOf(applied, applied.length + 1);
                updated[applied.length] = trait.level;
                data.putIntArray("AppliedLevels", updated);
            }
            data.putInt("Level", trait.addLevel(data.getInt("Level")));
            data.putInt("StrengthenNumber", trait.strengthenNumber);
            tag.put(trait.identifier, data);
        }
    }

    public static List<Entry> getTraits(ItemStack tool) {
        CompoundTag tag = TinkerDataComponents.getTraitsTag(tool);
        List<Entry> entries = new ArrayList<>();
        for (String id : tag.getAllKeys()) {
            AbstractTraits trait = TinkerTraits.getTrait(id);
            if (trait == null || !tag.contains(id, Tag.TAG_COMPOUND)) {
                continue;
            }
            int level = tag.getCompound(id).getInt("Level");
            if (level > 0) {
                entries.add(new Entry(trait, Math.min(level, trait.maxLevel)));
            }
        }
        entries.sort(Comparator.<Entry>comparingInt(entry -> entry.trait().getPriority()).reversed()
                .thenComparing(entry -> entry.trait().identifier));
        return List.copyOf(entries);
    }

    public static void forEach(ItemStack tool, BiConsumer<AbstractTraits, Integer> action) {
        for (Entry entry : getTraits(tool)) {
            action.accept(entry.trait(), entry.level());
        }
    }

    public static int modifyDurability(ItemStack tool, int durability) {
        for (Entry entry : getTraits(tool)) {
            durability = entry.trait().modifyDurability(tool, entry.level(), durability);
        }
        return Math.max(1, durability);
    }

    public static int modifyHealing(ItemStack tool, int amount, LivingEntity entity) {
        int original = amount;
        for (Entry entry : getTraits(tool)) {
            amount = entry.trait().onToolHeal(tool, entry.level(), original, amount, entity);
        }
        return Math.max(0, amount);
    }

    public record Entry(AbstractTraits trait, int level) {}
}
