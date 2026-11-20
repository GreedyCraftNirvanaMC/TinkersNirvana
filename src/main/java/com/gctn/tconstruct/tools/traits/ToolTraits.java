package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.init.TinkerDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** 工具词条的统一组装、读取和分发入口；未知存档数据保留但不执行。 */
public final class ToolTraits {
    private ToolTraits() {}

    public static List<AbstractTraits.TraitEntry> entriesFrom(CompoundTag tag) {
        List<AbstractTraits.TraitEntry> entries = new ArrayList<>();
        for (String id : orderedIds(tag)) {
            AbstractTraits trait = TinkerTraits.getTrait(id);
            if (trait == null) { continue; }
            CompoundTag data = tag.getCompound(id);
            int[] levels = data.getIntArray("AppliedLevels");
            if (levels.length == 0) { levels = new int[]{data.getInt("Level")}; }
            for (int level : levels) {
                if (level > 0) { entries.add(new AbstractTraits.TraitEntry(trait, Math.min(level, trait.maxLevel))); }
            }
        }
        return List.copyOf(entries);
    }

    public static void addTraits(CompoundTag tag, List<AbstractTraits.TraitEntry> traits) {
        Set<String> order = orderedIds(tag);
        for (AbstractTraits.TraitEntry entry : traits) {
            AbstractTraits trait = TinkerTraits.getTrait(entry.trait().identifier);
            if (trait == null) { continue; }
            int level = Math.min(entry.level(), trait.maxLevel);
            CompoundTag data = tag.getCompound(trait.identifier);
            if (trait instanceof LeveledTraits leveled && !leveled.stacksRepeatedLevels()) {
                int[] applied = data.getIntArray("AppliedLevels");
                if (applied.length == 0 && data.getInt("Level") > 0) {
                    applied = new int[]{data.getInt("Level")};
                    data.putIntArray("AppliedLevels", applied);
                }
                if (Arrays.stream(applied).anyMatch(value -> value == level)) {
                    continue;
                }
                int[] updated = Arrays.copyOf(applied, applied.length + 1);
                updated[applied.length] = level;
                data.putIntArray("AppliedLevels", updated);
            }
            int combinedLevel = trait.addLevel(data.getInt("Level"), level);
            data.putInt("Level", Math.clamp(combinedLevel, 1, trait.maxLevel));
            data.putInt("StrengthenNumber", trait.strengthenNumber);
            tag.put(trait.identifier, data);
            order.add(trait.identifier);
        }
        saveOrder(tag, order);
    }

    private static void saveOrder(CompoundTag tag, Set<String> order) {
        if (order.isEmpty()) {
            tag.remove("Order");
            return;
        }
        ListTag stored = new ListTag();
        for (String id : order) { stored.add(StringTag.valueOf(id)); }
        tag.put("Order", stored);
    }

    private static Set<String> orderedIds(CompoundTag tag) {
        Set<String> order = new LinkedHashSet<>();
        for (Tag id : tag.getList("Order", Tag.TAG_STRING)) {
            if (tag.contains(id.getAsString(), Tag.TAG_COMPOUND)) { order.add(id.getAsString()); }
        }
        // 旧工具只保存了无序的复合标签，继续以字母顺序读取这些旧词条。
        tag.getAllKeys().stream().filter(id -> tag.contains(id, Tag.TAG_COMPOUND)).sorted().forEach(order::add);
        return order;
    }

    public static void replaceMaterialTraits(CompoundTag actual, CompoundTag previous, CompoundTag replacement,
                                             boolean legacyEmbossing) {
        Set<String> order = orderedIds(replacement);
        if (!legacyEmbossing) {
            for (String id : orderedIds(previous)) {
                if (!order.contains(id)) { actual.remove(id); }
            }
        }
        for (String id : order) {
            CompoundTag installed = replacement.getCompound(id);
            CompoundTag state = actual.getCompound(id).copy();
            state.merge(installed);
            if (!installed.contains("AppliedLevels")) { state.remove("AppliedLevels"); }
            actual.put(id, state);
        }
        order.addAll(orderedIds(actual));
        saveOrder(actual, order);
    }

    public static List<Entry> getTraits(ItemStack tool) {
        CompoundTag tag = TinkerDataComponents.getTraitsTag(tool);
        List<Entry> entries = new ArrayList<>();
        for (String id : orderedIds(tag)) {
            AbstractTraits trait = TinkerTraits.getTrait(id);
            if (trait == null || !tag.contains(id, Tag.TAG_COMPOUND)) {
                continue;
            }
            CompoundTag data = tag.getCompound(id);
            if (data.contains("Level", Tag.TAG_INT) && data.getInt("Level") > 0) {
                entries.add(new Entry(trait, Math.min(data.getInt("Level"), trait.maxLevel)));
            }
        }
        // 怀古对同优先级的词条保留安装顺序，例如挖掘速度的乘数和加数。
        entries.sort(Comparator.<Entry>comparingInt(entry -> entry.trait().getPriority()).reversed());
        return List.copyOf(entries);
    }

    public static void forEach(ItemStack tool, BiConsumer<AbstractTraits, Integer> action) {
        for (Entry entry : getTraits(tool)) {
            action.accept(entry.trait(), entry.level());
        }
    }

    /**
     * 修改指定词条的独立状态并写回。回调结束后重新读取根 Tag，保留嵌套回调对其他词条的修改。
     * 不在共享定义上保存计时器等状态；客户端只读回调不能调用本方法。
     */
    public static void updateTraitData(ItemStack tool, String id, Consumer<CompoundTag> action) {
        CompoundTag tag = TinkerDataComponents.getTraitsTag(tool);
        if (!tag.contains(id, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag original = tag.getCompound(id);
        CompoundTag updated = original.copy();
        action.accept(updated);
        if (!original.equals(updated)) {
            CompoundTag latest = TinkerDataComponents.getTraitsTag(tool);
            latest.put(id, updated);
            tool.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(latest));
        }
    }

    public static void inventoryTick(ItemStack tool, Level world, Entity entity, int slot, boolean selected) {
        if (world.isClientSide) {
            return;
        }
        for (Entry entry : getTraits(tool)) {
            updateTraitData(tool, entry.trait().identifier, data -> {
                int storedLevel = data.getInt("Level");
                data.putInt("Level", entry.level());
                entry.trait().inventoryTick(tool, world, entity, slot, selected, data);
                data.putInt("Level", storedLevel);
            });
        }
    }

    public static int modifyModifierSlots(ItemStack tool, int slots) {
        for (Entry entry : getTraits(tool)) {
            slots = entry.trait().modifyModifierSlots(tool, entry.level(), slots);
        }
        return slots;
    }

    public static int modifyDurability(ItemStack tool, int durability) {
        for (Entry entry : getTraits(tool)) {
            durability = entry.trait().modifyDurability(tool, entry.level(), durability);
        }
        return Math.max(1, durability);
    }

    public static int modifyDamage(ItemStack tool, int amount, LivingEntity entity) {
        if (amount <= 0) {
            return 0;
        }
        int original = amount;
        for (Entry entry : getTraits(tool)) {
            amount = entry.trait().onToolDamage(tool, entry.level(), original, amount, entity);
        }
        return Math.max(0, amount);
    }

    public static int modifyHealing(ItemStack tool, int amount, LivingEntity entity) {
        if (amount <= 0) {
            return 0;
        }
        int original = amount;
        for (Entry entry : getTraits(tool)) {
            amount = entry.trait().onToolHeal(tool, entry.level(), original, amount, entity);
        }
        return Math.max(0, amount);
    }

    public record Entry(AbstractTraits trait, int level) {}
}
