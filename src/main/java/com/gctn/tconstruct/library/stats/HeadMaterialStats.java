package com.gctn.tconstruct.library.stats; // 声明此类所属的包，使其与对应功能模块保持一致。

/**
 * 头部属性决定挖掘等级、挖掘速度、攻击加成及按头部材料计算的修复量。
 * 攻击数值以生命值点数为单位，玩家的基础攻击力由 Minecraft 另外叠加。
 */
public class HeadMaterialStats extends AbstractMaterialStats { // 定义 HeadMaterialStats 的实现类型，具体职责见上方说明。
    public final int durability; // 通常介于 1 至 1000 之间。
    public final int harvestLevel; // 对应等级定义见 HarvestLevels。
    public final float attack; // 通常为 0 至 10 个生命值点；每点等于半颗心，除以 2 即为心数。
    public final float miningspeed; // 声明 miningspeed，保存后续流程使用的状态或依赖。

    public HeadMaterialStats(int durability, int harvestLevel, float attack,  float miningspeed) { // 定义 HeadMaterialStats 的调用入口，参数和具体处理见下方实现。
        super("Head"); // 向当前声明或调用传入 super("Head")。
        this.durability = durability; // 设置 this.durability 为 durability。
        this.harvestLevel = harvestLevel; // 设置 this.harvestLevel 为 harvestLevel。
        this.attack= attack; // 设置 this.attack 为 attack。
        this.miningspeed = miningspeed; // 设置 this.miningspeed 为 miningspeed。
    } // 结束当前作用域。
} // 结束当前作用域。
