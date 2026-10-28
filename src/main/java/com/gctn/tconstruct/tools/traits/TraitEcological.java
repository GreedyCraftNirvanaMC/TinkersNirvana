package com.gctn.tconstruct.tools.traits; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.tools.TinkerTools; // 引入 com.gctn.tconstruct.tools.TinkerTools，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.entity.Entity; // 引入 net.minecraft.world.entity.Entity，供本文件使用。
import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。

/**
 * 工具由生物携带时，尝试缓慢恢复其耐久。
 * 只由服务端进行随机判定；跳过正在使用的工具，healTool 也会拒绝恢复已损坏工具。
 */
public final class TraitEcological extends AbstractTraits { // 定义 TraitEcological 的实现类型，具体职责见上方说明。
    public TraitEcological() { // 定义 TraitEcological 的调用入口，参数和具体处理见下方实现。
        super("ecological", 1, -1, 0xff8e661b); // 向当前声明或调用传入 super("ecological", 1, -1, 0xff8e661b)。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CompoundTag traitTag) { // 定义 inventoryTick 的调用入口，参数和具体处理见下方实现。
        // 每个背包 tick 有 1/400 概率恢复 1 点；正在使用或已损坏的工具不能被动修复。
        if (!level.isClientSide && entity instanceof LivingEntity living && living.getUseItem() != stack // 检查 !level.isClientSide && entity instanceof LivingEntity living && living.getUseItem() != stack；按条件决定是否执行本分支。
                && level.random.nextInt(400) == 0) { // 调用 nextInt，执行当前流程所需的处理。
            TinkerTools.healTool(stack, 1, living); // 调用 healTool，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
