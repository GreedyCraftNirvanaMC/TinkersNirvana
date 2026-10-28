package com.gctn.tconstruct.tools.tools;

import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.stats.ExtraMaterialStats;
import com.gctn.tconstruct.library.stats.HandleMaterialStats;
import com.gctn.tconstruct.library.stats.HeadMaterialStats;
import com.gctn.tconstruct.library.utils.HarvestLevels;
import com.gctn.tconstruct.tools.TinkerTools;
import com.gctn.tconstruct.tools.traits.ToolTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public class Pickaxe extends TinkerTools {
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("pickaxe/pickaxe",
            Pickaxe::new);

    /** Forces this class's static registration entry to be initialized. */
    public static void init() {}

    public Pickaxe() {}

    // Material order: handle, head, binding.
    public static ItemStack initPickaxe(Material matHandle, Material matHead, Material matBinding) {
        ItemStack stack = new ItemStack(PICKAXE.get());

        CompoundTag materialTag = new CompoundTag();
        materialTag.putString("Handle", matHandle.identifier);
        materialTag.putString("Head", matHead.identifier);
        materialTag.putString("Binding", matBinding.identifier);
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(materialTag));

        CompoundTag traitsTag = new CompoundTag();
        ToolTraits.addTraits(traitsTag, matHead.getTraits("Head"));
        ToolTraits.addTraits(traitsTag, matHandle.getTraits("Handle"));
        ToolTraits.addTraits(traitsTag, matBinding.getTraits("Extra"));
        stack.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(traitsTag));

        HeadMaterialStats headStats = (HeadMaterialStats) matHead.getStats().get("Head");
        if (headStats != null) {
            stack.set(DataComponents.TOOL, getPickaxeInfo(headStats));
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, getPickaxeAttibute(headStats));
            stack.set(DataComponents.MAX_DAMAGE,
                    ToolTraits.modifyDurability(stack, getPickaxeDurability(matHandle, matHead, matBinding)));
            stack.set(DataComponents.DAMAGE, 0);
        }

        stack.set(DataComponents.ITEM_NAME, Component.literal(
                        Component.translatable(
                                "material." + matHead.identifier + ".name").getString()
                                + " "
                                + Component.translatable("item.tconstruct.pickaxe.name").getString()
                )
        );
        return stack;
    }

    private static int getPickaxeDurability(Material matHandle, Material matHead, Material matBinding) {
        HandleMaterialStats handleStats = (HandleMaterialStats) matHandle.getStats().get("Handle");
        ExtraMaterialStats bindingStats = (ExtraMaterialStats) matBinding.getStats().get("Extra");
        HeadMaterialStats headStats = (HeadMaterialStats) matHead.getStats().get("Head");

        float handleModifier = handleStats != null ? handleStats.modifer : 1.0F;
        int handleDurability = handleStats != null ? handleStats.durability : 0;
        int bindingDurability = bindingStats != null ? bindingStats.durability : 0;

        return Math.max(1, Math.round((headStats.durability + bindingDurability) * handleModifier) + handleDurability);
    }

    private static Tool getPickaxeInfo(HeadMaterialStats headStats) {
        return new Tool(List.of(
                Tool.Rule.deniesDrops(HarvestLevels.getIncorrectBlocksForDrops(headStats.harvestLevel)),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, headStats.miningspeed)
        ), 1.0F, 1);
    }

    private static ItemAttributeModifiers getPickaxeAttibute(HeadMaterialStats headStats) {
        double attackSpeed = 1.2;
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_ID,
                                headStats.attack,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Item.BASE_ATTACK_SPEED_ID,
                                attackSpeed - 4.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            if (!isBroken(stack)) {
                boolean effective = isCorrectToolForDrops(stack, state);
                ToolTraits.forEach(stack, (trait, traitLevel) ->
                        trait.afterBlockBreak(stack, traitLevel, level, state, pos, miningEntity, effective));
            }
            damageTool(stack, 1, miningEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && !isBroken(stack)) {
            ToolTraits.forEach(stack, (trait, traitLevel) -> trait.afterHit(stack, traitLevel, target, attacker));
            damageTool(stack, 2, attacker, EquipmentSlot.MAINHAND);
        }
    }

    @Override
    public List<RepairInfo> getRepairInfo(ItemStack stack) {
        List<RepairInfo> repairInfos = new ArrayList<>();
        Material head = getMaterial(stack, "Head", "Head");
        Material handle = getMaterial(stack, "Handle", "Handle");
        if (head == null || handle == null) {
            return List.of();
        }
        int headDurability = ((HeadMaterialStats) head.getStats().get("Head")).durability;
        int handleDurability = ((HandleMaterialStats) handle.getStats().get("Handle")).durability;
        repairInfos.add(new RepairInfo(head, 1.0F, headDurability));
        repairInfos.add(new RepairInfo(handle, 0.8F, handleDurability));

        return repairInfos;
    }

}
