package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。

import java.util.Objects; // 引入 java.util.Objects，供本文件使用。

/**
 * 共享且不可变的词条定义，保存标识符、等级上限和显示信息。
 * TraitEntry 保存材料贡献的初始等级，实际工具等级与运行状态保存在物品栈组件中。
 * 普通词条合并时取最高等级，LeveledTraits 则显式启用等级相加规则。
 */
public abstract class AbstractTraits implements ITraits { // 定义 AbstractTraits 的实现类型，具体职责见上方说明。
    public final String identifier; // 声明 identifier，保存后续流程使用的状态或依赖。
    public final int maxLevel; // 声明 maxLevel，保存后续流程使用的状态或依赖。
    public final int color; // 声明 color，保存后续流程使用的状态或依赖。
    // -1 代表非强化升级
    public final int strengthenNumber; // 声明 strengthenNumber，保存后续流程使用的状态或依赖。
    public AbstractTraits(String identifier, int maxLevel, int strengthenNumber, int color) { // 定义 AbstractTraits 的调用入口，参数和具体处理见下方实现。
        if (identifier == null || identifier.isBlank() || maxLevel < 1) { // 检查 identifier == null || identifier.isBlank() || maxLevel < 1；按条件决定是否执行本分支。
            throw new IllegalArgumentException("Invalid trait definition: " + identifier); // 抛出异常并终止当前无效操作，避免继续使用不合法状态。
        } // 结束当前作用域。
        this.identifier = identifier; // 设置 this.identifier 为 identifier。
        this.maxLevel = maxLevel; // 设置 this.maxLevel 为 maxLevel。
        this.strengthenNumber = strengthenNumber; // 设置 this.strengthenNumber 为 strengthenNumber。
        this.color = color; // 设置 this.color 为 color。
    } // 结束当前作用域。

    /** 根据原等级和新增等级计算结果，不保存工具的等级状态。 */
    public int addLevel(int currentLevel, int amount) { // 定义 addLevel 的调用入口，参数和具体处理见下方实现。
        return Math.clamp(Math.max(currentLevel, amount), 1, maxLevel); // 返回 Math.clamp(Math.max(currentLevel, amount), 1, maxLevel)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 根据原等级和待合并等级计算结果，不保存工具的等级状态。 */
    public int mergeLevel(int currentLevel, int incomingLevel) { // 定义 mergeLevel 的调用入口，参数和具体处理见下方实现。
        return addLevel(currentLevel, incomingLevel); // 返回 addLevel(currentLevel, incomingLevel)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 优先级越高越早执行；同优先级按 identifier 排序。 */
    public int getPriority() { return 100; } // 定义 getPriority 的调用入口；返回 100，将当前分支的结果交给调用者。

    public Component getDisplayName(int toolLevel) { // 定义 getDisplayName 的调用入口，参数和具体处理见下方实现。
        var name = Component.translatable("traits." + identifier + ".name"); // 设置 name 为 Component.translatable("traits." + identifier + ".name")。
        return (maxLevel > 1 ? Component.translatable("tooltip.tconstruct.trait.level", name, toolLevel) : name) // 返回 (maxLevel > 1 ? Component.translatable("tooltip.tconstruct.trait.level", name, toolLevel) : name)，将当前分支的结果交给调用者。
                .withColor(color); // 调用 withColor，执行当前流程所需的处理。
    } // 结束当前作用域。

    public Component getDescription() { // 定义 getDescription 的调用入口，参数和具体处理见下方实现。
        return Component.translatable("traits." + identifier + ".description").withColor(color); // 返回 Component.translatable("traits." + identifier + ".description").withColor(color)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public String getTranslatedName() { // 定义 getTranslatedName 的调用入口，参数和具体处理见下方实现。
        return Component.translatable("traits." + this.identifier + ".name").getString(); // 返回 Component.translatable("traits." + this.identifier + ".name").getString()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 材料赋予的词条及初始等级；等级不属于共享的词条定义。 */
    public record TraitEntry(AbstractTraits trait, int level) { // 定义 TraitEntry 的数据记录，具体职责见上方说明。
        public TraitEntry { // 在 AbstractTraits 中补充当前声明或表达式的 public TraitEntry { 部分。
            Objects.requireNonNull(trait, "trait"); // 调用 requireNonNull，执行当前流程所需的处理。
            if (level < 1 || level > trait.maxLevel) { // 检查 level < 1 || level > trait.maxLevel；按条件决定是否执行本分支。
                throw new IllegalArgumentException("Invalid initial level for " + trait.identifier + ": " + level); // 抛出异常并终止当前无效操作，避免继续使用不合法状态。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public record TraitPosPair(String pos, TraitEntry... traits) { // 定义 TraitPosPair 的数据记录，具体职责见上方说明。
        public static TraitPosPair of(String pos, TraitEntry... traits) { // 定义 of 的调用入口，参数和具体处理见下方实现。
            return new TraitPosPair(pos, traits); // 返回 new TraitPosPair(pos, traits)，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
