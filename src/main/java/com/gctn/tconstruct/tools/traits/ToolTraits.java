package com.gctn.tconstruct.tools.traits;

import com.gctn.tconstruct.init.TinkerDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** 工具词条的统一组装、读取和分发入口；未知存档数据保留但不执行。 */
public final class ToolTraits {
    private ToolTraits() {}

    public static void addTraits(CompoundTag tag, List<AbstractTraits.TraitEntry> traits) {
        for (AbstractTraits.TraitEntry entry : traits) {
            AbstractTraits trait = entry.trait();
            CompoundTag data = tag.getCompound(trait.identifier);
            if (trait instanceof LeveledTraits leveled && !leveled.stacksRepeatedLevels()) {
                int[] applied = data.getIntArray("AppliedLevels");
                if (Arrays.stream(applied).anyMatch(value -> value == entry.level())) {
                    continue;
                }
                int[] updated = Arrays.copyOf(applied, applied.length + 1);
                updated[applied.length] = entry.level();
                data.putIntArray("AppliedLevels", updated);
            }
            int combinedLevel = trait.addLevel(data.getInt("Level"), entry.level());
            data.putInt("Level", Math.clamp(combinedLevel, 1, trait.maxLevel));
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
            CompoundTag data = tag.getCompound(id);
            if (data.contains("Level", Tag.TAG_INT) && data.getInt("Level") > 0) {
                entries.add(new Entry(trait, Math.min(data.getInt("Level"), trait.maxLevel)));
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
                data.putInt("Level", entry.level());
                entry.trait().inventoryTick(tool, world, entity, slot, selected, data);
            });
        }
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
