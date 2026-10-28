package com.gctn.tconstruct.library.stats;

/**
 * 头部属性决定挖掘等级、挖掘速度、攻击加成及按头部材料计算的修复量。
 * 攻击数值以生命值点数为单位，玩家的基础攻击力由 Minecraft 另外叠加。
 */
public class HeadMaterialStats extends AbstractMaterialStats {
    public final int durability; // 通常介于 1 至 1000 之间。
    public final int harvestLevel; // 对应等级定义见 HarvestLevels。
    public final float attack; // 通常为 0 至 10 个生命值点；每点等于半颗心，除以 2 即为心数。
    public final float miningspeed;

    public HeadMaterialStats(int durability, int harvestLevel, float attack,  float miningspeed) {
        super("Head");
        this.durability = durability;
        this.harvestLevel = harvestLevel;
        this.attack= attack;
        this.miningspeed = miningspeed;
    }
}
