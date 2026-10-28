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
 * 组装当前的手柄、头部和绑定结，并实现挖掘及战斗损耗。
 * 先按 Antique 顺序合并头部、额外部件和手柄属性，再由材料词条修改最终耐久。
 * Minecraft 的 Tool 规则处理挖掘与掉落，属性组件提供战斗加成。
 */
public class Pickaxe extends TinkerTools {
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("pickaxe/pickaxe",
            Pickaxe::new);

    /** 显式触发本类的静态注册条目初始化。 */
    public static void init() {}

    public Pickaxe() {}

    // 材料参数顺序：手柄、头部、绑定结。
    public static ItemStack initPickaxe(Material matHandle, Material matHead, Material matBinding) {
        if (matHandle == null || matHead == null || matBinding == null) {
            return ItemStack.EMPTY;
        }
        HandleMaterialStats handleStats = matHandle.getStats("Handle", HandleMaterialStats.class);
        HeadMaterialStats headStats = matHead.getStats("Head", HeadMaterialStats.class);
        ExtraMaterialStats bindingStats = matBinding.getStats("Extra", ExtraMaterialStats.class);
        // 不受支持或尚未完整移植的材料不能生成工具，
        // 否则缺失 MAX_DAMAGE 组件可能导致工具意外变得无法损坏。
        if (handleStats == null || headStats == null || bindingStats == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(PICKAXE.get());

        // 通过材料标识符还原材料，生成的工具和属性组件保存一份
        // 属性快照，避免材料定义变化后改写已有工具的存档属性。
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
        // 遵循 Antique 的 ToolNBT：合并头部与额外耐久，乘手柄系数并取整，
        // 再加上手柄固定耐久，最后限制最终耐久的最小值。
        return Math.max(1, Math.round((headStats.durability + bindingStats.durability) * handleStats.modifer)
                + handleStats.durability);
    }

    private static Tool getPickaxeInfo(HeadMaterialStats headStats) {
        // 按顺序应用规则：先拒绝超出采掘等级的掉落，再由镐规则允许
        // 其他掉落；挖掘速度规则仍可作用于超出采掘等级的方块。
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
        // Antique 的镐只使用头部材料修复，手柄与绑定结的耐久
        // 会改变最大可承受损耗，但不会使其材料成为有效修复原料。
        Material head = getMaterial(stack, "Head", "Head");
        if (head == null) {
            return List.of();
        }
        HeadMaterialStats stats = head.getStats("Head", HeadMaterialStats.class);
        return stats == null ? List.of() : List.of(new RepairInfo(head, 1.0F, stats.durability));
    }

}
