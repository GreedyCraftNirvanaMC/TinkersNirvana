package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 采用等级相加规则的词条基类，区别于仅取最高等级的普通词条。
 * 默认情况下，每种不同的初始强度只贡献一次，与 Antique 的可叠加词条行为一致。
 */
public abstract class LeveledTraits extends AbstractTraits { // 定义 LeveledTraits 的实现类型，具体职责见上方说明。
    protected LeveledTraits(String identifier, int maxLevel, int strengthenNumber, int color) { // 定义 LeveledTraits 的调用入口，参数和具体处理见下方实现。
        super(identifier, maxLevel, strengthenNumber, color); // 向当前声明或调用传入 super(identifier, maxLevel, strengthenNumber, color)。
    } // 结束当前作用域。

    /** 覆盖为 true 时，每个部件的等级都会累加，包括相同强度。 */
    public boolean stacksRepeatedLevels() { return false; } // 定义 stacksRepeatedLevels 的调用入口；返回 false，将当前分支的结果交给调用者。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int addLevel(int currentLevel, int amount) { // 定义 addLevel 的调用入口，参数和具体处理见下方实现。
        return (int) Math.clamp((long) currentLevel + amount, 1L, maxLevel); // 返回 (int) Math.clamp((long) currentLevel + amount, 1L, maxLevel)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
