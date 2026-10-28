package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.utils.ToolHelper;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * 基于数据组件的工具共用的生命周期逻辑，耐久耗尽后仍保留工具以供修复。
 * 原版损耗流程处理附魔与创造模式免损，耗尽的物品栈随后恢复为损坏状态。
 * 组装时一次性写入属性，避免材料定义变化后悄悄重建存档中的旧工具。
 */
public abstract class TinkerTools extends Item {
    public static final int DEFAULT_MODIFIER_SLOTS = 3;

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TinkersNirvana.MODID);

    public TinkerTools() {
        super(new Item.Properties().stacksTo(1));
    }

    public List<RepairInfo> getRepairInfo(ItemStack stack) {
        return List.of();
    }

    /**
     * 返回工具当前仍可使用的强化槽数量。
     */
    public final int getAvailableModifierSlots(ItemStack stack) {
        int totalSlots = getBaseModifierSlots(stack) + getMaterialModifierSlotAdjustment(stack);
        return Math.max(0, totalSlots - Math.max(0, getUsedModifierSlots(stack)));
    }

    /** 基础强化槽数量不是 3 的工具可以覆盖此方法。 */
    protected int getBaseModifierSlots(ItemStack stack) {
        return DEFAULT_MODIFIER_SLOTS;
    }

    /** 供材料词条增加或减少强化槽数量的扩展点。 */
    protected int getMaterialModifierSlotAdjustment(ItemStack stack) {
        return 0;
    }

    /** 用于统计已应用强化占用的槽位数量。 */
    protected int getUsedModifierSlots(ItemStack stack) {
        return 0;
    }

    public static boolean isBroken(ItemStack stack) {
        return stack.isDamageableItem()
                && stack.getMaxDamage() > 0
                && stack.getDamageValue() >= stack.getMaxDamage();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isBroken(stack) ? 1.0F : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return !isBroken(stack) && super.isCorrectToolForDrops(stack, state);
    }

    protected final void damageTool(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot) {
        if (entity.level().isClientSide || amount <= 0 || isBroken(stack)) {
            return;
        }
        amount = ToolTraits.modifyDamage(stack, amount, entity);
        if (amount <= 0) {
            return;
        }

        int originalCount = stack.getCount();
        int maxDamage = stack.getMaxDamage();
        stack.hurtAndBreak(amount, entity, slot);
        if (stack.isEmpty()) {
            // 原版损耗修正和损坏通知已经执行，此时恢复
            // 同一个物品栈并保留其组件，使工具台仍能修复该工具。
            stack.setCount(originalCount);
            stack.setDamageValue(maxDamage);
        }
    }

    /** 被动治疗不能复活损坏工具；材料修复走组装台的独立模拟流程。 */
    public static void healTool(ItemStack stack, int amount, LivingEntity entity) {
        if (!entity.level().isClientSide && amount > 0 && stack.isDamaged() && !isBroken(stack)) {
            int healed = ToolTraits.modifyHealing(stack, amount, entity);
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - healed));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isBroken(stack) || super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return isBroken(stack) ? 13 : super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return isBroken(stack) ? 0xFFFF0000 : super.getBarColor(stack);
    }

    public static Component shiftTip = Component.translatable(
            "tooltip.tconstruct.hold_for_stats",
            Component.literal("Shift").withColor(0xFFFFFF55)
    ).withColor(0xFFAAAAAA);
    public static Component controlTip = Component.translatable(
            "tooltip.tconstruct.hold_for_more",
            Component.literal("Ctrl").withColor(0xFF55FFFF)
    ).withColor(0xFFAAAAAA);

    protected static Material getMaterial(ItemStack stack, String materialKey, String requiredStat) {
        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(materialKey);
        Material material = TinkerMaterials.getMaterial(materialId);
        return material != null && material.hasStats(requiredStat) ? material : null;
    }

    public static Component getDurabilityTip(ItemStack stack) {
        int maxDurability = stack.getMaxDamage();
        int remainingDurability = Math.max(0, maxDurability - stack.getDamageValue());
        Component durabilityTip = Component.translatable(
                "tooltip.tconstruct.tool.durability",
                Component.literal(String.valueOf(remainingDurability)).withColor(ToolHelper.getDurabilityColor(stack)),
                Component.literal(String.valueOf(maxDurability)).withColor(0xFF47CC47)
        ).withColor(0xFFAAAAAA);
        Component brokenTip = Component.translatable(
                "tooltip.tconstruct.tool.durability.broken",
                Component.translatable("tooltip.tconstruct.tool.broken").withColor(0xFFCC4747)
        ).withColor(0xFFAAAAAA);
        return isBroken(stack) ? brokenTip : durabilityTip;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public record RepairInfo(Material material, float partEfficient, int durability) {
    }

    // 词条触发器部分
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        ToolTraits.inventoryTick(stack, level, entity, slotId, isSelected);
    }

}
