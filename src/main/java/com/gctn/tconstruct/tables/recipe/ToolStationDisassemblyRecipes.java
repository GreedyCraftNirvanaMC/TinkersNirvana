package com.gctn.tconstruct.tables.recipe; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.toolparts.Binding; // 引入 com.gctn.tconstruct.library.toolparts.Binding，供本文件使用。
import com.gctn.tconstruct.library.toolparts.PickaxeHead; // 引入 com.gctn.tconstruct.library.toolparts.PickaxeHead，供本文件使用。
import com.gctn.tconstruct.library.toolparts.ToolRod; // 引入 com.gctn.tconstruct.library.toolparts.ToolRod，供本文件使用。
import com.gctn.tconstruct.tools.tools.Pickaxe; // 引入 com.gctn.tconstruct.tools.tools.Pickaxe，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。

/**
 * 将受支持且已完全修复的镐还原为三个对应材料的部件。
 * 本类只描述输出，取消预览及源工具的一次性消耗由方块实体负责。
 */
public final class ToolStationDisassemblyRecipes { // 定义 ToolStationDisassemblyRecipes 的实现类型，具体职责见上方说明。
    private ToolStationDisassemblyRecipes() { // 定义 ToolStationDisassemblyRecipes 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    public static boolean canDisassemble(ItemStack stack) { // 定义 canDisassemble 的调用入口，参数和具体处理见下方实现。
        return !createParts(stack).isEmpty(); // 返回 !createParts(stack).isEmpty()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static PartList createParts(ItemStack stack) { // 定义 createParts 的调用入口，参数和具体处理见下方实现。
        if (!isFullDurabilityTool(stack)) { // 检查 !isFullDurabilityTool(stack)；按条件决定是否执行本分支。
            return PartList.EMPTY; // 返回 PartList.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        return switch (stack.getItem()) { // 返回 switch (stack.getItem()) {，将当前分支的结果交给调用者。
            case net.minecraft.world.item.Item item when item == Pickaxe.PICKAXE.get() -> createPickaxeParts(stack); // 定义 case net.minecraft.world.item.Item item when item == Pickaxe.PICKAXE.get() 分支，读取指定条目或延迟注册对象。
            default -> PartList.EMPTY; // 定义 default 分支，产生此情况对应的结果。
        }; // 结束当前作用域并完成本次表达式或声明。
    } // 结束当前作用域。

    private static boolean isFullDurabilityTool(ItemStack stack) { // 定义 isFullDurabilityTool 的调用入口，参数和具体处理见下方实现。
        return stack.isDamageableItem() && stack.getDamageValue() == 0; // 返回 stack.isDamageableItem() && stack.getDamageValue() == 0，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static PartList createPickaxeParts(ItemStack stack) { // 定义 createPickaxeParts 的调用入口，参数和具体处理见下方实现。
        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack); // 设置 materials 为 TinkerDataComponents.getMaterialTag(stack)。
        Material handle = getMaterial(materials, "Handle", "Handle"); // 设置 handle 为 getMaterial(materials, "Handle", "Handle")。
        Material head = getMaterial(materials, "Head", "Head"); // 设置 head 为 getMaterial(materials, "Head", "Head")。
        Material binding = getMaterial(materials, "Binding", "Extra"); // 设置 binding 为 getMaterial(materials, "Binding", "Extra")。
        if (handle == null || head == null || binding == null) { // 检查 handle == null || head == null || binding == null；按条件决定是否执行本分支。
            return PartList.EMPTY; // 返回 PartList.EMPTY，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        return new PartList(new ItemStack[] { // 返回 new PartList(new ItemStack[] {，将当前分支的结果交给调用者。
                ToolRod.getPart(handle), // 调用 getPart，执行当前流程所需的处理。
                PickaxeHead.getColoredPart(head), // 调用 getColoredPart，执行当前流程所需的处理。
                Binding.getColoredPart(binding) // 调用 getColoredPart，执行当前流程所需的处理。
        }); // 结束当前作用域并完成本次表达式或声明。
    } // 结束当前作用域。

    private static Material getMaterial(CompoundTag materials, String materialKey, String requiredStat) { // 定义 getMaterial 的调用入口，参数和具体处理见下方实现。
        String materialId = materials.getString(materialKey); // 设置 materialId 为 materials.getString(materialKey)。
        Material material = TinkerMaterials.getMaterial(materialId); // 设置 material 为 TinkerMaterials.getMaterial(materialId)。
        return material != null && material.hasStats(requiredStat) ? material : null; // 返回 material != null && material.hasStats(requiredStat) ? material : null，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public record PartList(ItemStack[] parts) { // 定义 PartList 的数据记录，具体职责见上方说明。
        public static final PartList EMPTY = new PartList(new ItemStack[0]); // 设置 EMPTY 为 new PartList(new ItemStack[0])。

        public boolean isEmpty() { // 定义 isEmpty 的调用入口，参数和具体处理见下方实现。
            return this.parts.length == 0; // 返回 this.parts.length == 0，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
