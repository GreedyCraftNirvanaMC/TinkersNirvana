package com.gctn.tconstruct.tables.recipe; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.utils.Tags; // 引入 com.gctn.tconstruct.library.utils.Tags，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import net.minecraft.world.Container; // 引入 net.minecraft.world.Container，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

import java.util.function.Function; // 引入 java.util.function.Function，供本文件使用。
import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

/**
 * 定义固定的部件槽位要求，以及根据材料创建工具的工厂。
 * 生成预览时只校验所有材料，不消耗输入；原料消耗在独立的确认步骤中执行。
 */
public class ToolAssemblyRecipe { // 定义 ToolAssemblyRecipe 的实现类型，具体职责见上方说明。
    private final PartRequirement[] requirements; // 声明 requirements，保存后续流程使用的状态或依赖。
    private final Function<Material[], ItemStack> resultFactory; // 在 ToolAssemblyRecipe 中补充当前声明或表达式的 private final Function<Material[], ItemStack> resultFactory 部分。

    ToolAssemblyRecipe(PartRequirement[] requirements, Function<Material[], ItemStack> resultFactory) { // 调用 ToolAssemblyRecipe，执行当前流程所需的处理。
        this.requirements = requirements; // 设置 this.requirements 为 requirements。
        this.resultFactory = resultFactory; // 设置 this.resultFactory 为 resultFactory。
    } // 结束当前作用域。

    public Item getRequiredItem(int inputSlot) { // 定义 getRequiredItem 的调用入口，参数和具体处理见下方实现。
        for (PartRequirement requirement : this.requirements) { // 遍历 PartRequirement requirement : this.requirements，逐项执行当前处理。
            if (requirement.inputSlot() == inputSlot) { // 检查 requirement.inputSlot() == inputSlot；按条件决定是否执行本分支。
                return requirement.requiredItem(); // 返回 requirement.requiredItem()，将当前分支的结果交给调用者。
            } // 结束当前作用域。
        } // 结束当前作用域。
        return null; // 返回 null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public ItemStack createResult(Container container) { // 定义 createResult 的调用入口，参数和具体处理见下方实现。
        Material[] materials = new Material[this.requirements.length]; // 设置 materials 为 new Material[this.requirements.length]。
        for (int index = 0; index < this.requirements.length; index++) { // 遍历 int index = 0; index < this.requirements.length; index++，逐项执行当前处理。
            PartRequirement requirement = this.requirements[index]; // 设置 requirement 为 this.requirements[index]。
            ItemStack stack = container.getItem(requirement.inputSlot()); // 设置 stack 为 container.getItem(requirement.inputSlot())。
            if (!stack.is(requirement.requiredItem())) { // 检查 !stack.is(requirement.requiredItem())；按条件决定是否执行本分支。
                return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
            } // 结束当前作用域。

            Material material = getMaterial(stack, requirement.requiredStat()); // 设置 material 为 getMaterial(stack, requirement.requiredStat())。
            if (material == null) { // 检查 material == null；按条件决定是否执行本分支。
                return ItemStack.EMPTY; // 返回 ItemStack.EMPTY，将当前分支的结果交给调用者。
            } // 结束当前作用域。
            materials[index] = material; // 设置 materials[index] 为 material。
        } // 结束当前作用域。

        return this.resultFactory.apply(materials); // 返回 this.resultFactory.apply(materials)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public void consumeInputs(Container container) { // 定义 consumeInputs 的调用入口，参数和具体处理见下方实现。
        for (PartRequirement requirement : this.requirements) { // 遍历 PartRequirement requirement : this.requirements，逐项执行当前处理。
            container.removeItem(requirement.inputSlot(), 1); // 从指定槽位移除已确认的物品数量。
        } // 结束当前作用域。
    } // 结束当前作用域。

    // 返回stack对应的材料，同时验证材料是否含有对应属性
    private static Material getMaterial(ItemStack stack, String requiredStat) { // 定义 getMaterial 的调用入口，参数和具体处理见下方实现。
        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL); // 设置 materialId 为 TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL)。
        Material material = TinkerMaterials.getMaterial(materialId); // 设置 material 为 TinkerMaterials.getMaterial(materialId)。
        return material != null && material.hasStats(requiredStat) ? material : null; // 返回 material != null && material.hasStats(requiredStat) ? material : null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    record PartRequirement(int inputSlot, Supplier<? extends Item> item, String requiredStat) { // 定义 PartRequirement 的数据记录，具体职责见上方说明。
        Item requiredItem() { // 调用 requiredItem，执行当前流程所需的处理。
            return this.item.get(); // 返回 this.item.get()，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
