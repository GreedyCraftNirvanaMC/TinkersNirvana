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

import java.util.List;

/**
 * Assembles the current handle/head/binding tool and implements mining/combat wear.
 * Head, extra and handle stats are combined in Antique order, then material traits modify final durability.
 * Minecraft Tool rules handle mining and drops, while the attribute component supplies combat bonuses.
 */
public class Pickaxe extends TinkerTools {
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("pickaxe/pickaxe",
            Pickaxe::new);

    /** Forces this class's static registration entry to be initialized. */
    public static void init() {}

    public Pickaxe() {}

    // Material order: handle, head, binding.
    public static ItemStack initPickaxe(Material matHandle, Material matHead, Material matBinding) {
        if (matHandle == null || matHead == null || matBinding == null) {
            return ItemStack.EMPTY;
        }
        HandleMaterialStats handleStats = matHandle.getStats("Handle", HandleMaterialStats.class);
        HeadMaterialStats headStats = matHead.getStats("Head", HeadMaterialStats.class);
        ExtraMaterialStats bindingStats = matBinding.getStats("Extra", ExtraMaterialStats.class);
        // Unsupported or incompletely ported materials must not produce tools
        // whose missing MAX_DAMAGE component makes them accidentally unbreakable.
        if (handleStats == null || headStats == null || bindingStats == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(PICKAXE.get());

        // IDs reconstruct materials; generated tool/attribute components keep a
        // snapshot of stats so a material-definition change cannot rewrite saves.
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

        stack.set(DataComponents.TOOL, getPickaxeInfo(headStats));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, getPickaxeAttributes(headStats));
        stack.set(DataComponents.MAX_DAMAGE, ToolTraits.modifyDurability(stack,
                getPickaxeDurability(handleStats, headStats, bindingStats)));
        stack.set(DataComponents.DAMAGE, 0);

        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_name",
                Component.translatable("material." + matHead.identifier + ".name"),
                Component.translatable("item.tconstruct.pickaxe.name")));
        return stack;
    }

    private static int getPickaxeDurability(HandleMaterialStats handleStats, HeadMaterialStats headStats,
                                            ExtraMaterialStats bindingStats) {
        // Match Antique ToolNBT: combine head/extra, round the handle product,
        // add the flat handle bonus, and only then clamp the final durability.
        return Math.max(1, Math.round((headStats.durability + bindingStats.durability) * handleStats.modifer)
                + handleStats.durability);
    }

    private static Tool getPickaxeInfo(HeadMaterialStats headStats) {
        // Rules are ordered: deny under-tier drops before the pickaxe rule grants
        // drops. The speed rule still applies to blocks above the harvest tier.
        return new Tool(List.of(
                Tool.Rule.deniesDrops(HarvestLevels.getIncorrectBlocksForDrops(headStats.harvestLevel)),
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, headStats.miningspeed)
        ), 1.0F, 1);
    }

    private static ItemAttributeModifiers getPickaxeAttributes(HeadMaterialStats headStats) {
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
                boolean effective = state.is(BlockTags.MINEABLE_WITH_PICKAXE);
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
        // Antique pickaxes repair from the head only. Handle/binding durability
        // changes maximum wear, but does not make those materials repair inputs.
        Material head = getMaterial(stack, "Head", "Head");
        if (head == null) {
            return List.of();
        }
        HeadMaterialStats stats = head.getStats("Head", HeadMaterialStats.class);
        return stats == null ? List.of() : List.of(new RepairInfo(head, 1.0F, stats.durability));
    }

}
