package com.gctn.tconstruct.tools.tools; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.stats.ExtraMaterialStats; // 引入 com.gctn.tconstruct.library.stats.ExtraMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.stats.HandleMaterialStats; // 引入 com.gctn.tconstruct.library.stats.HandleMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.stats.HeadMaterialStats; // 引入 com.gctn.tconstruct.library.stats.HeadMaterialStats，供本文件使用。
import com.gctn.tconstruct.library.utils.HarvestLevels; // 引入 com.gctn.tconstruct.library.utils.HarvestLevels，供本文件使用。
import com.gctn.tconstruct.tools.TinkerTools; // 引入 com.gctn.tconstruct.tools.TinkerTools，供本文件使用。
import com.gctn.tconstruct.tools.traits.ToolTraits; // 引入 com.gctn.tconstruct.tools.traits.ToolTraits，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.component.DataComponents; // 引入 net.minecraft.core.component.DataComponents，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.tags.BlockTags; // 引入 net.minecraft.tags.BlockTags，供本文件使用。
import net.minecraft.world.entity.EquipmentSlot; // 引入 net.minecraft.world.entity.EquipmentSlot，供本文件使用。
import net.minecraft.world.entity.EquipmentSlotGroup; // 引入 net.minecraft.world.entity.EquipmentSlotGroup，供本文件使用。
import net.minecraft.world.entity.LivingEntity; // 引入 net.minecraft.world.entity.LivingEntity，供本文件使用。
import net.minecraft.world.entity.ai.attributes.AttributeModifier; // 引入 net.minecraft.world.entity.ai.attributes.AttributeModifier，供本文件使用。
import net.minecraft.world.entity.ai.attributes.Attributes; // 引入 net.minecraft.world.entity.ai.attributes.Attributes，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.component.CustomData; // 引入 net.minecraft.world.item.component.CustomData，供本文件使用。
import net.minecraft.world.item.component.ItemAttributeModifiers; // 引入 net.minecraft.world.item.component.ItemAttributeModifiers，供本文件使用。
import net.minecraft.world.item.component.Tool; // 引入 net.minecraft.world.item.component.Tool，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。
import net.neoforged.neoforge.registries.DeferredItem; // 引入 net.neoforged.neoforge.registries.DeferredItem，供本文件使用。

import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 组装当前的手柄、头部和绑定结，并实现挖掘及战斗损耗。
 * 先按 Antique 顺序合并头部、额外部件和手柄属性，再由材料词条修改最终耐久。
 * Minecraft 的 Tool 规则处理挖掘与掉落，属性组件提供战斗加成。
 */
public class Pickaxe extends TinkerTools { // 定义 Pickaxe 的实现类型，具体职责见上方说明。
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("pickaxe/pickaxe", // 设置 PICKAXE 为 ITEMS.register("pickaxe/pickaxe",。
            Pickaxe::new); // 向当前声明或调用传入 Pickaxe::new)。

    /** 显式触发本类的静态注册条目初始化。 */
    public static void init() {} // 定义 init 的调用入口；此处无需额外处理。

    public Pickaxe() {} // 定义 Pickaxe 的调用入口；此处无需额外处理。

    // 材料参数顺序：手柄、头部、绑定结。
    public static ItemStack initPickaxe(Material matHandle, Material matHead, Material matBinding) { // 定义 initPickaxe 的调用入口，参数和具体处理见下方实现。
        if (matHandle == null || matHead == null || matBinding == null) { // 检查 matHandle == null || matHead == null || matBinding == null；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        HandleMaterialStats handleStats = matHandle.getStats("Handle", HandleMaterialStats.class); // 设置 handleStats 为 matHandle.getStats("Handle", HandleMaterialStats.class)。
        HeadMaterialStats headStats = matHead.getStats("Head", HeadMaterialStats.class); // 设置 headStats 为 matHead.getStats("Head", HeadMaterialStats.class)。
        ExtraMaterialStats bindingStats = matBinding.getStats("Extra", ExtraMaterialStats.class); // 设置 bindingStats 为 matBinding.getStats("Extra", ExtraMaterialStats.class)。
        // 不受支持或尚未完整移植的材料不能生成工具，
        // 否则缺失 MAX_DAMAGE 组件可能导致工具意外变得无法损坏。
        if (handleStats == null || headStats == null || bindingStats == null) { // 检查 handleStats == null || headStats == null || bindingStats == null；按条件决定是否执行本分支。
            return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        ItemStack stack = new ItemStack(PICKAXE.get()); // 设置 stack 为 new ItemStack(PICKAXE.get())。

        // 通过材料标识符还原材料，生成的工具和属性组件保存一份
        // 属性快照，避免材料定义变化后改写已有工具的存档属性。
        CompoundTag materialTag = new CompoundTag(); // 设置 materialTag 为 new CompoundTag()。
        materialTag.putString("Handle", matHandle.identifier); // 写入字符串存档字段。
        materialTag.putString("Head", matHead.identifier); // 写入字符串存档字段。
        materialTag.putString("Binding", matBinding.identifier); // 写入字符串存档字段。
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(materialTag)); // 设置目标槽位、组件或属性的值。

        CompoundTag traitsTag = new CompoundTag(); // 设置 traitsTag 为 new CompoundTag()。
        ToolTraits.addTraits(traitsTag, matHead.getTraits("Head")); // 合并材料赋予的初始词条。
        ToolTraits.addTraits(traitsTag, matHandle.getTraits("Handle")); // 合并材料赋予的初始词条。
        ToolTraits.addTraits(traitsTag, matBinding.getTraits("Extra")); // 合并材料赋予的初始词条。
        stack.set(TinkerDataComponents.TINKER_TRAITS.get(), CustomData.of(traitsTag)); // 设置目标槽位、组件或属性的值。

        stack.set(DataComponents.TOOL, getPickaxeInfo(headStats)); // 设置目标槽位、组件或属性的值。
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, getPickaxeAttributes(headStats)); // 设置目标槽位、组件或属性的值。
        stack.set(DataComponents.MAX_DAMAGE, ToolTraits.modifyDurability(stack, // 设置目标槽位、组件或属性的值。
                getPickaxeDurability(handleStats, headStats, bindingStats))); // 调用 getPickaxeDurability，执行当前流程所需的处理。
        stack.set(DataComponents.DAMAGE, 0); // 设置目标槽位、组件或属性的值。

        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_name", // 设置目标槽位、组件或属性的值。
                Component.translatable("material." + matHead.identifier + ".name"), // 调用 translatable，执行当前流程所需的处理。
                Component.translatable("item.tconstruct.pickaxe.name"))); // 调用 translatable，执行当前流程所需的处理。
        return stack; // 返回 stack，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static int getPickaxeDurability(HandleMaterialStats handleStats, HeadMaterialStats headStats, // 定义 getPickaxeDurability 的调用入口，参数和具体处理见下方实现。
                                            ExtraMaterialStats bindingStats) { // 在 Pickaxe 中补充当前声明或表达式的 ExtraMaterialStats bindingStats) { 部分。
        // 遵循 Antique 的 ToolNBT：合并头部与额外耐久，乘手柄系数并取整，
        // 再加上手柄固定耐久，最后限制最终耐久的最小值。
        return Math.max(1, Math.round((headStats.durability + bindingStats.durability) * handleStats.modifer) // 返回 Math.max(1, Math.round((headStats.durability + bindingStats.durability) * handleStats.modifer)，将当前分支的结果交给调用者。
                + handleStats.durability); // 向当前声明或调用传入 + handleStats.durability)。
    } // 结束当前作用域。

    private static Tool getPickaxeInfo(HeadMaterialStats headStats) { // 定义 getPickaxeInfo 的调用入口，参数和具体处理见下方实现。
        // 按顺序应用规则：先拒绝超出采掘等级的掉落，再由镐规则允许
        // 其他掉落；挖掘速度规则仍可作用于超出采掘等级的方块。
        return new Tool(List.of( // 返回 new Tool(List.of(，将当前分支的结果交给调用者。
                Tool.Rule.deniesDrops(HarvestLevels.getIncorrectBlocksForDrops(headStats.harvestLevel)), // 调用 deniesDrops，执行当前流程所需的处理。
                Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, headStats.miningspeed) // 调用 minesAndDrops，执行当前流程所需的处理。
        ), 1.0F, 1); // 向当前声明或调用传入 ), 1.0F, 1)。
    } // 结束当前作用域。

    private static ItemAttributeModifiers getPickaxeAttributes(HeadMaterialStats headStats) { // 定义 getPickaxeAttributes 的调用入口，参数和具体处理见下方实现。
        double attackSpeed = 1.2; // 设置 attackSpeed 为 1.2。
        return ItemAttributeModifiers.builder() // 返回 ItemAttributeModifiers.builder()，将当前分支的结果交给调用者。
                .add( // 向集合或构建器追加条目。
                        Attributes.ATTACK_DAMAGE, // 向当前声明或调用传入 Attributes.ATTACK_DAMAGE。
                        new AttributeModifier( // 创建 new AttributeModifier(，作为当前对象或调用的组成部分。
                                Item.BASE_ATTACK_DAMAGE_ID, // 向当前声明或调用传入 Item.BASE_ATTACK_DAMAGE_ID。
                                headStats.attack, // 向当前声明或调用传入 headStats.attack。
                                AttributeModifier.Operation.ADD_VALUE // 在 Pickaxe 中补充当前声明或表达式的 AttributeModifier.Operation.ADD_VALUE 部分。
                        ), // 结束当前参数列表或表达式。
                        EquipmentSlotGroup.MAINHAND // 在 Pickaxe 中补充当前声明或表达式的 EquipmentSlotGroup.MAINHAND 部分。
                ) // 结束当前参数列表或表达式。
                .add( // 向集合或构建器追加条目。
                        Attributes.ATTACK_SPEED, // 向当前声明或调用传入 Attributes.ATTACK_SPEED。
                        new AttributeModifier( // 创建 new AttributeModifier(，作为当前对象或调用的组成部分。
                                Item.BASE_ATTACK_SPEED_ID, // 向当前声明或调用传入 Item.BASE_ATTACK_SPEED_ID。
                                attackSpeed - 4.0, // 向当前声明或调用传入 attackSpeed - 4.0。
                                AttributeModifier.Operation.ADD_VALUE // 在 Pickaxe 中补充当前声明或表达式的 AttributeModifier.Operation.ADD_VALUE 部分。
                        ), // 结束当前参数列表或表达式。
                        EquipmentSlotGroup.MAINHAND // 在 Pickaxe 中补充当前声明或表达式的 EquipmentSlotGroup.MAINHAND 部分。
                ) // 结束当前参数列表或表达式。
                .build(); // 调用 build，执行当前流程所需的处理。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) { // 定义 mineBlock 的调用入口，参数和具体处理见下方实现。
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) { // 检查 !level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F；按条件决定是否执行本分支。
            if (!isBroken(stack)) { // 检查 !isBroken(stack)；按条件决定是否执行本分支。
                boolean effective = state.is(BlockTags.MINEABLE_WITH_PICKAXE); // 设置 effective 为 state.is(BlockTags.MINEABLE_WITH_PICKAXE)。
                ToolTraits.forEach(stack, (trait, traitLevel) -> // 定义回调或映射规则，在对应条目被处理时执行。
                        trait.afterBlockBreak(stack, traitLevel, level, state, pos, miningEntity, effective)); // 调用 afterBlockBreak，执行当前流程所需的处理。
            } // 结束当前作用域。
            damageTool(stack, 1, miningEntity, EquipmentSlot.MAINHAND); // 调用 damageTool，执行当前流程所需的处理。
        } // 结束当前作用域。
        return true; // 返回 true，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { // 定义 hurtEnemy 的调用入口，参数和具体处理见下方实现。
        return true; // 返回 true，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { // 定义 postHurtEnemy 的调用入口，参数和具体处理见下方实现。
        if (!attacker.level().isClientSide && !isBroken(stack)) { // 检查 !attacker.level().isClientSide && !isBroken(stack)；按条件决定是否执行本分支。
            ToolTraits.forEach(stack, (trait, traitLevel) -> trait.afterHit(stack, traitLevel, target, attacker)); // 定义回调或映射规则，在对应条目被处理时执行。
            damageTool(stack, 2, attacker, EquipmentSlot.MAINHAND); // 调用 damageTool，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    public List<RepairInfo> getRepairInfo(ItemStack stack) { // 定义 getRepairInfo 的调用入口，参数和具体处理见下方实现。
        // Antique 的镐只使用头部材料修复，手柄与绑定结的耐久
        // 会改变最大可承受损耗，但不会使其材料成为有效修复原料。
        Material head = getMaterial(stack, "Head", "Head"); // 设置 head 为 getMaterial(stack, "Head", "Head")。
        if (head == null) { // 检查 head == null；按条件决定是否执行本分支。
            return List.of(); // 返回 List.of()，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        HeadMaterialStats stats = head.getStats("Head", HeadMaterialStats.class); // 设置 stats 为 head.getStats("Head", HeadMaterialStats.class)。
        return stats == null ? List.of() : List.of(new RepairInfo(head, 1.0F, stats.durability)); // 返回 stats == null ? List.of() : List.of(new RepairInfo(head, 1.0F, stats.durability))，将当前分支的结果交给调用者。
    } // 结束当前作用域。

} // 结束当前作用域。
