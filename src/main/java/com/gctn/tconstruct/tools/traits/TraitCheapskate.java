package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

/**
 * 对组装后的最终耐久应用一次 Antique 吝啬词条效果。
 * 耐久取原值的 80% 并向下取整，最低保留 1 点，保证物品栈仍是有效工具。
 */
public final class TraitCheapskate extends AbstractTraits { // 定义 TraitCheapskate 的实现类型，具体职责见上方说明。
    public TraitCheapskate() { // 定义 TraitCheapskate 的调用入口，参数和具体处理见下方实现。
        super("cheapskate", 1, -1, 0xFFAAAAAA); // 向当前声明或调用传入 super("cheapskate", 1, -1, 0xFFAAAAAA)。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int modifyDurability(ItemStack tool, int level, int durability) { // 定义 modifyDurability 的调用入口，参数和具体处理见下方实现。
        return Math.max(1, (int) (durability * 80L / 100)); // 返回 Math.max(1, (int) (durability * 80L / 100))，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
