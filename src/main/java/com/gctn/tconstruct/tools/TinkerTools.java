package com.gctn.tconstruct.tools; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.utils.ToolHelper; // 引入 com.gctn.tconstruct.library.utils.ToolHelper，供本文件使用。
import com.gctn.tconstruct.tools.traits.ToolTraits; // 引入 com.gctn.tconstruct.tools.traits.ToolTraits，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.world.entity.Entity; // 引入 net.minecraft.world.entity.Entity，供本文件使用。
import net.minecraft.world.entity.EquipmentSlot; // 引入 net.minecraft.world.entity.EquipmentSlot，供本文件使用。
import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 基于数据组件的工具共用的生命周期逻辑，耐久耗尽后仍保留工具以供修复。
 * 原版损耗流程处理附魔与创造模式免损，耗尽的物品栈随后恢复为损坏状态。
 * 组装时一次性写入属性，避免材料定义变化后悄悄重建存档中的旧工具。
 */
public abstract class TinkerTools extends Item { // 定义 TinkerTools 的实现类型，具体职责见上方说明。
    public static final int DEFAULT_MODIFIER_SLOTS = 3; // 设置 DEFAULT_MODIFIER_SLOTS 为 3。

    public static final DeferredRegister.Items ITEMS = // 设置 ITEMS 为 。
            DeferredRegister.createItems(TinkersNirvana.MODID); // 调用 createItems，执行当前流程所需的处理。

    public TinkerTools() { // 定义 TinkerTools 的调用入口，参数和具体处理见下方实现。
        super(new Item.Properties().stacksTo(1)); // 调用 Properties，执行当前流程所需的处理。
    } // 结束当前作用域。

    public List<RepairInfo> getRepairInfo(ItemStack stack) { // 定义 getRepairInfo 的调用入口，参数和具体处理见下方实现。
        return List.of(); // 返回 List.of()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /**
     * 返回工具当前仍可使用的强化槽数量。
     */
    public final int getAvailableModifierSlots(ItemStack stack) { // 定义 getAvailableModifierSlots 的调用入口，参数和具体处理见下方实现。
        int totalSlots = getBaseModifierSlots(stack) + getMaterialModifierSlotAdjustment(stack); // 设置 totalSlots 为 getBaseModifierSlots(stack) + getMaterialModifierSlotAdjustment(stack)。
        return Math.max(0, totalSlots - Math.max(0, getUsedModifierSlots(stack))); // 返回 Math.max(0, totalSlots - Math.max(0, getUsedModifierSlots(stack)))，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 基础强化槽数量不是 3 的工具可以覆盖此方法。 */
    protected int getBaseModifierSlots(ItemStack stack) { // 定义 getBaseModifierSlots 的调用入口，参数和具体处理见下方实现。
        return DEFAULT_MODIFIER_SLOTS; // 返回 DEFAULT_MODIFIER_SLOTS，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 供材料词条增加或减少强化槽数量的扩展点。 */
    protected int getMaterialModifierSlotAdjustment(ItemStack stack) { // 定义 getMaterialModifierSlotAdjustment 的调用入口，参数和具体处理见下方实现。
        return 0; // 返回 0，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 用于统计已应用强化占用的槽位数量。 */
    protected int getUsedModifierSlots(ItemStack stack) { // 定义 getUsedModifierSlots 的调用入口，参数和具体处理见下方实现。
        return 0; // 返回 0，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static boolean isBroken(ItemStack stack) { // 定义 isBroken 的调用入口，参数和具体处理见下方实现。
        return stack.isDamageableItem() // 返回 stack.isDamageableItem()，将当前分支的结果交给调用者。
                && stack.getMaxDamage() > 0 // 调用 getMaxDamage，执行当前流程所需的处理。
                && stack.getDamageValue() >= stack.getMaxDamage(); // 调用 getDamageValue，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public float getDestroySpeed(ItemStack stack, BlockState state) { // 定义 getDestroySpeed 的调用入口，参数和具体处理见下方实现。
        return isBroken(stack) ? 1.0F : super.getDestroySpeed(stack, state); // 返回 isBroken(stack) ? 1.0F : super.getDestroySpeed(stack, state)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) { // 定义 isCorrectToolForDrops 的调用入口，参数和具体处理见下方实现。
        return !isBroken(stack) && super.isCorrectToolForDrops(stack, state); // 返回 !isBroken(stack) && super.isCorrectToolForDrops(stack, state)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    protected final void damageTool(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) { // 定义 damageTool 的调用入口，参数和具体处理见下方实现。
        if (entity.level().isClientSide || amount <= 0 || isBroken(stack)) { // 检查 entity.level().isClientSide || amount <= 0 || isBroken(stack)；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。
        amount = ToolTraits.modifyDamage(stack, amount, entity); // 设置 amount 为 ToolTraits.modifyDamage(stack, amount, entity)。
        if (amount <= 0) { // 检查 amount <= 0；按条件决定是否执行本分支。
            return; // 结束当前方法，后续步骤不再执行。
        } // 结束当前作用域。

        int originalCount = stack.getCount(); // 设置 originalCount 为 stack.getCount()。
        int maxDamage = stack.getMaxDamage(); // 设置 maxDamage 为 stack.getMaxDamage()。
        stack.hurtAndBreak(amount, entity, slot); // 调用 hurtAndBreak，执行当前流程所需的处理。
        if (stack.isEmpty()) { // 检查 stack.isEmpty()；按条件决定是否执行本分支。
            // 原版损耗修正和损坏通知已经执行，此时恢复
            // 同一个物品栈并保留其组件，使工具台仍能修复该工具。
            stack.setCount(originalCount); // 设置物品栈数量。
            stack.setDamageValue(maxDamage); // 写入当前工具的耐久损耗。
        } // 结束当前作用域。
    } // 结束当前作用域。

    /** 被动治疗不能复活损坏工具；材料修复走组装台的独立模拟流程。 */
    public static void healTool(ItemStack stack, int amount, LivingEntity entity) { // 定义 healTool 的调用入口，参数和具体处理见下方实现。
        if (!entity.level().isClientSide && amount > 0 && stack.isDamaged() && !isBroken(stack)) { // 检查 !entity.level().isClientSide && amount > 0 && stack.isDamaged() && !isBroken(stack)；按条件决定是否执行本分支。
            int healed = ToolTraits.modifyHealing(stack, amount, entity); // 设置 healed 为 ToolTraits.modifyHealing(stack, amount, entity)。
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - healed)); // 写入当前工具的耐久损耗。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean isBarVisible(ItemStack stack) { // 定义 isBarVisible 的调用入口，参数和具体处理见下方实现。
        return isBroken(stack) || super.isBarVisible(stack); // 返回 isBroken(stack) || super.isBarVisible(stack)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int getBarWidth(ItemStack stack) { // 定义 getBarWidth 的调用入口，参数和具体处理见下方实现。
        return isBroken(stack) ? 13 : super.getBarWidth(stack); // 返回 isBroken(stack) ? 13 : super.getBarWidth(stack)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public int getBarColor(ItemStack stack) { // 定义 getBarColor 的调用入口，参数和具体处理见下方实现。
        return isBroken(stack) ? 0xFFFF0000 : super.getBarColor(stack); // 返回 isBroken(stack) ? 0xFFFF0000 : super.getBarColor(stack)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static Component shiftTip = Component.translatable( // 设置 shiftTip 为 Component.translatable(。
            "tooltip.tconstruct.hold_for_stats", // 向当前声明或调用传入 "tooltip.tconstruct.hold_for_stats"。
            Component.literal("Shift").withColor(0xFFFFFF55) // 调用 literal，执行当前流程所需的处理。
    ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
    public static Component controlTip = Component.translatable( // 设置 controlTip 为 Component.translatable(。
            "tooltip.tconstruct.hold_for_more", // 向当前声明或调用传入 "tooltip.tconstruct.hold_for_more"。
            Component.literal("Ctrl").withColor(0xFF55FFFF) // 调用 literal，执行当前流程所需的处理。
    ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。

    protected static Material getMaterial(ItemStack stack, String materialKey, String requiredStat) { // 定义 getMaterial 的调用入口，参数和具体处理见下方实现。
        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(materialKey); // 设置 materialId 为 TinkerDataComponents.getMaterialTag(stack).getString(materialKey)。
        Material material = TinkerMaterials.getMaterial(materialId); // 设置 material 为 TinkerMaterials.getMaterial(materialId)。
        return material != null && material.hasStats(requiredStat) ? material : null; // 返回 material != null && material.hasStats(requiredStat) ? material : null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static Component getDurabilityTip(ItemStack stack) { // 定义 getDurabilityTip 的调用入口，参数和具体处理见下方实现。
        int maxDurability = stack.getMaxDamage(); // 设置 maxDurability 为 stack.getMaxDamage()。
        int remainingDurability = Math.max(0, maxDurability - stack.getDamageValue()); // 设置 remainingDurability 为 Math.max(0, maxDurability - stack.getDamageValue())。
        Component durabilityTip = Component.translatable( // 设置 durabilityTip 为 Component.translatable(。
                "tooltip.tconstruct.tool.durability", // 向当前声明或调用传入 "tooltip.tconstruct.tool.durability"。
                Component.literal(String.valueOf(remainingDurability)).withColor(ToolHelper.getDurabilityColor(stack)), // 调用 literal，执行当前流程所需的处理。
                Component.literal(String.valueOf(maxDurability)).withColor(0xFF47CC47) // 调用 literal，执行当前流程所需的处理。
        ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
        Component brokenTip = Component.translatable( // 设置 brokenTip 为 Component.translatable(。
                "tooltip.tconstruct.tool.durability.broken", // 向当前声明或调用传入 "tooltip.tconstruct.tool.durability.broken"。
                Component.translatable("tooltip.tconstruct.tool.broken").withColor(0xFFCC4747) // 调用 translatable，执行当前流程所需的处理。
        ).withColor(0xFFAAAAAA); // 调用 withColor，执行当前流程所需的处理。
        return isBroken(stack) ? brokenTip : durabilityTip; // 返回 isBroken(stack) ? brokenTip : durabilityTip，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        ITEMS.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。

    public record RepairInfo(Material material, float partEfficient, int durability) { // 定义 RepairInfo 的数据记录，具体职责见上方说明。
    } // 结束当前作用域。

    // 词条触发器部分
    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) { // 定义 inventoryTick 的调用入口，参数和具体处理见下方实现。
        super.inventoryTick(stack, level, entity, slotId, isSelected); // 调用 inventoryTick，执行当前流程所需的处理。
        ToolTraits.inventoryTick(stack, level, entity, slotId, isSelected); // 调用 inventoryTick，执行当前流程所需的处理。
    } // 结束当前作用域。

} // 结束当前作用域。
