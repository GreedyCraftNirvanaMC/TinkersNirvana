package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

/**
 * 根据原始恢复量计算 Antique 廉价词条的额外治疗，结果向下取整。
 * 使用原始值可避免重复放大前面词条的加成，使用 long 运算防止整数溢出。
 */
public final class TraitCheap extends AbstractTraits { // 定义 TraitCheap 的实现类型，具体职责见上方说明。
    public TraitCheap() { // 定义 TraitCheap 的调用入口，参数和具体处理见下方实现。
        super("cheap", 1, -1, 0xFF555555); // 向当前声明或调用传入 super("cheap", 1, -1, 0xFF555555)。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int onToolHeal(ItemStack tool, int level, int original, int amount, LivingEntity entity) { // 定义 onToolHeal 的调用入口，参数和具体处理见下方实现。
        return (int) Math.min(Integer.MAX_VALUE, (long) amount + original * 5L / 100); // 返回 (int) Math.min(Integer.MAX_VALUE, (long) amount + original * 5L / 100)，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
