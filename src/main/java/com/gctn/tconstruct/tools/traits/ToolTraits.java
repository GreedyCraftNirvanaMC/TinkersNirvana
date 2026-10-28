package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.nbt.Tag; // 引入 net.minecraft.nbt.Tag，供本文件使用。
import net.minecraft.world.entity.Entity; // 引入 net.minecraft.world.entity.Entity，供本文件使用。
import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.component.CustomData; // 引入 net.minecraft.world.item.component.CustomData，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.Arrays; // 引入 java.util.Arrays，供本文件使用。
import java.util.Comparator; // 引入 java.util.Comparator，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。
import java.util.function.BiConsumer; // 引入 java.util.function.BiConsumer，供本文件使用。
import java.util.function.Consumer; // 引入 java.util.function.Consumer，供本文件使用。

/**
 * 以稳定的优先级顺序组装、校验和分发各个物品栈的词条。
 * 保留但不执行未知或无效的存档条目，以便兼容后续版本的数据。
 * 状态修改采用写时复制，保证不可变 CustomData 组件可以安全地持久化和同步。
 */
public final class ToolTraits { // 定义 ToolTraits 的实现类型，具体职责见上方说明。
    private ToolTraits() {} // 定义 ToolTraits 的调用入口；此处无需额外处理。

    public static void addTraits(CompoundTag tag, List<AbstractTraits.TraitEntry> traits) { // 定义 addTraits 的调用入口，参数和具体处理见下方实现。
        for (AbstractTraits.TraitEntry entry : traits) { // 遍历 AbstractTraits.TraitEntry entry : traits，逐项执行当前处理。
            AbstractTraits trait = entry.trait(); // 设置 trait 为 entry.trait()。
            CompoundTag data = tag.getCompound(trait.identifier); // 设置 data 为 tag.getCompound(trait.identifier)。
            if (trait instanceof LeveledTraits leveled && !leveled.stacksRepeatedLevels()) { // 检查 trait instanceof LeveledTraits leveled && !leveled.stacksRepeatedLevels()；按条件决定是否执行本分支。
                int[] applied = data.getIntArray("AppliedLevels"); // 设置 applied 为 data.getIntArray("AppliedLevels")。
                if (Arrays.stream(applied).anyMatch(value -> value == entry.level())) { // 检查 Arrays.stream(applied).anyMatch(value -> value == entry.level())；按条件决定是否执行本分支。
                    continue; // 跳过当前条目，继续处理下一条。
                } // 结束当前作用域。
                int[] updated = Arrays.copyOf(applied, applied.length + 1); // 设置 updated 为 Arrays.copyOf(applied, applied.length + 1)。
                updated[applied.length] = entry.level(); // 设置 updated[applied.length] 为 entry.level()。
                data.putIntArray("AppliedLevels", updated); // 写入整型数组，保留各槽位数据。
            } // 结束当前作用域。
            int combinedLevel = trait.addLevel(data.getInt("Level"), entry.level()); // 设置 combinedLevel 为 trait.addLevel(data.getInt("Level"), entry.level())。
            data.putInt("Level", Math.clamp(combinedLevel, 1, trait.maxLevel)); // 写入整型存档字段。
            data.putInt("StrengthenNumber", trait.strengthenNumber); // 写入整型存档字段。
            tag.put(trait.identifier, data); // 按键保存对应数据。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public static List<Entry> getTraits(ItemStack tool) { // 定义 getTraits 的调用入口，参数和具体处理见下方实现。
        CompoundTag tag = TinkerDataComponents.getTraitsTag(tool); // 设置 tag 为 TinkerDataComponents.getTraitsTag(tool)。
        List<Entry> entries = new ArrayList<>(); // 设置 entries 为 new ArrayList<>()。
        for (String id : tag.getAllKeys()) { // 遍历 String id : tag.getAllKeys()，逐项执行当前处理。
            AbstractTraits trait = TinkerTraits.getTrait(id); // 设置 trait 为 TinkerTraits.getTrait(id)。
            if (trait == null || !tag.contains(id, Tag.TAG_COMPOUND)) { // 检查 trait == null || !tag.contains(id, Tag.TAG_COMPOUND)；按条件决定是否执行本分支。
                continue; // 跳过当前条目，继续处理下一条。
            } // 结束当前作用域。
            CompoundTag data = tag.getCompound(id); // 设置 data 为 tag.getCompound(id)。
            if (data.contains("Level", Tag.TAG_INT) && data.getInt("Level") > 0) { // 检查 data.contains("Level", Tag.TAG_INT) && data.getInt("Level") > 0；按条件决定是否执行本分支。
                entries.add(new Entry(trait, Math.min(data.getInt("Level"), trait.maxLevel))); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } // 结束当前作用域。
        entries.sort(Comparator.<Entry>comparingInt(entry -> entry.trait().getPriority()).reversed() // 定义回调或映射规则，在对应条目被处理时执行。
                .thenComparing(entry -> entry.trait().identifier)); // 调用 thenComparing，执行当前流程所需的处理。
        return List.copyOf(entries); // 返回 List.copyOf(entries)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static void forEach(ItemStack tool, BiConsumer<AbstractTraits, Integer> action) { // 定义 forEach 的调用入口，参数和具体处理见下方实现。
        for (Entry entry : getTraits(tool)) { // 遍历 Entry entry : getTraits(tool)，逐项执行当前处理。
            action.accept(entry.trait(), entry.level()); // 调用 accept，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    /**
     * 修改指定词条的独立状态并写回。回调结束后重新读取根 Tag，保留嵌套回调对其他词条的修改。
     * 不在共享定义上保存计时器等状态；客户端只读回调不能调用本方法。
     */
    public static void updateTraitData(ItemStack tool, String id, Consumer<CompoundTag> action) { // 定义 updateTraitData 的调用入口，参数和具体处理见下方实现。
        CompoundTag tag = TinkerDataComponents.getTraitsTag(tool); // 设置 tag 为 TinkerDataComponents.getTraitsTag(tool)。
        if (!tag.contains(id, Tag.TAG_COMPOUND)) { // 检查 !tag.contains(id, Tag.TAG_COMPOUND)；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        CompoundTag original = tag.getCompound(id); // 设置 original 为 tag.getCompound(id)。
        CompoundTag updated = original.copy(); // 设置 updated 为 original.copy()。
        action.accept(updated); // 调用 accept，执行当前流程所需的处理。
        if (!original.equals(updated)) { // 检查 !original.equals(updated)；按条件决定是否执行本分支。
            CompoundTag latest = TinkerDataComponents.getTraitsTag(tool); // 设置 latest 为 TinkerDataComponents.getTraitsTag(tool)。
            latest.put(id, updated); // 按键保存对应数据。
            tool.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(latest)); // 设置目标槽位、组件或属性的值。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public static void inventoryTick(ItemStack tool, Level world, Entity entity, int slot, boolean selected) { // 定义 inventoryTick 的调用入口，参数和具体处理见下方实现。
        if (world.isClientSide) { // 检查 world.isClientSide；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        for (Entry entry : getTraits(tool)) { // 遍历 Entry entry : getTraits(tool)，逐项执行当前处理。
            updateTraitData(tool, entry.trait().identifier, data -> { // 定义回调或映射规则，在对应条目被处理时执行。
                data.putInt("Level", entry.level()); // 写入整型存档字段。
                entry.trait().inventoryTick(tool, world, entity, slot, selected, data); // 调用 trait，执行当前流程所需的处理。
            }); // 结束当前作用域并完成本次表达式或声明。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public static int modifyDurability(ItemStack tool, int durability) { // 定义 modifyDurability 的调用入口，参数和具体处理见下方实现。
        for (Entry entry : getTraits(tool)) { // 遍历 Entry entry : getTraits(tool)，逐项执行当前处理。
            durability = entry.trait().modifyDurability(tool, entry.level(), durability); // 设置 durability 为 entry.trait().modifyDurability(tool, entry.level(), durability)。
        } // 结束当前作用域。
        return Math.max(1, durability); // 返回 Math.max(1, durability)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static int modifyDamage(ItemStack tool, int amount, LivingEntity entity) { // 定义 modifyDamage 的调用入口，参数和具体处理见下方实现。
        if (amount <= 0) { // 检查 amount <= 0；按条件决定是否执行本分支。
            return 0; // 返回 0，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        int original = amount; // 设置 original 为 amount。
        for (Entry entry : getTraits(tool)) { // 遍历 Entry entry : getTraits(tool)，逐项执行当前处理。
            amount = entry.trait().onToolDamage(tool, entry.level(), original, amount, entity); // 设置 amount 为 entry.trait().onToolDamage(tool, entry.level(), original, amount, entity)。
        } // 结束当前作用域。
        return Math.max(0, amount); // 返回 Math.max(0, amount)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static int modifyHealing(ItemStack tool, int amount, LivingEntity entity) { // 定义 modifyHealing 的调用入口，参数和具体处理见下方实现。
        if (amount <= 0) { // 检查 amount <= 0；按条件决定是否执行本分支。
            return 0; // 返回 0，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        int original = amount; // 设置 original 为 amount。
        for (Entry entry : getTraits(tool)) { // 遍历 Entry entry : getTraits(tool)，逐项执行当前处理。
            amount = entry.trait().onToolHeal(tool, entry.level(), original, amount, entity); // 设置 amount 为 entry.trait().onToolHeal(tool, entry.level(), original, amount, entity)。
        } // 结束当前作用域。
        return Math.max(0, amount); // 返回 Math.max(0, amount)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public record Entry(AbstractTraits trait, int level) {} // 定义 Entry 的数据记录，具体职责见上方说明。
} // 结束当前作用域。
