package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.library.utils.ToolHelper;
import com.gctn.tconstruct.tools.traits.AbstractTraits;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

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
     * Returns the number of modifier slots that can still be used on this tool.
     */
    public final int getAvailableModifierSlots(ItemStack stack) {
        int totalSlots = getBaseModifierSlots(stack) + getMaterialModifierSlotAdjustment(stack);
        return Math.max(0, totalSlots - Math.max(0, getUsedModifierSlots(stack)));
    }

    /** Override for tool types whose base modifier slot count is not 3. */
    protected int getBaseModifierSlots(ItemStack stack) {
        return DEFAULT_MODIFIER_SLOTS;
    }

    /** Hook for modifier slots added or removed by material traits. */
    protected int getMaterialModifierSlotAdjustment(ItemStack stack) {
        return 0;
    }

    /** Hook for the number of slots consumed by applied modifiers. */
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
        if (amount <= 0 || isBroken(stack)) {
            return;
        }

        int originalCount = stack.getCount();
        int maxDamage = stack.getMaxDamage();
        stack.hurtAndBreak(amount, entity, slot);
        if (stack.isEmpty()) {
            stack.setCount(originalCount);
            stack.setDamageValue(maxDamage);
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
        for (Material material : TinkerMaterials.materials) {
            if (material.identifier.equals(materialId)
                    && material.getStats() != null
                    && material.getStats().containsKey(requiredStat)) {
                return material;
            }
        }
        return null;
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
        if(!(stack.getItem() instanceof TinkerTools)) { return; }
        CompoundTag traitsTag = TinkerDataComponents.getTraitsTag(stack);
        for (String traitId : traitsTag.getAllKeys()) {
            AbstractTraits trait = TinkerTraits.getTrait(traitId);
            if (trait != null) {continue;}
            CompoundTag traitTag = traitsTag.getCompound(traitId);
            int traitLevel = traitTag.getInt("Level");
            trait.inventoryTick(stack, level, entity, slotId, isSelected, traitTag);
        }
    }

}
