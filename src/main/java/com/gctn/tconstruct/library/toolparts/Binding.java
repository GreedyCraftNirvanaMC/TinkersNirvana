package com.gctn.tconstruct.library.toolparts; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.utils.Tags; // 引入 com.gctn.tconstruct.library.utils.Tags，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import com.gctn.tconstruct.library.materials.MaterialValue; // 引入 com.gctn.tconstruct.library.materials.MaterialValue，供本文件使用。
import net.minecraft.core.component.DataComponents; // 引入 net.minecraft.core.component.DataComponents，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.component.CustomData; // 引入 net.minecraft.world.item.component.CustomData，供本文件使用。
import net.neoforged.neoforge.registries.DeferredItem; // 引入 net.neoforged.neoforge.registries.DeferredItem，供本文件使用。

import java.util.Collection; // 引入 java.util.Collection，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 创建镐组装与拆解使用的绑定结部件。
 * 绑定结材料提供额外耐久和词条，采掘行为由头部材料决定。
 */
public class Binding extends ToolPart { // 定义 Binding 的实现类型，具体职责见上方说明。
    public Binding(int cost) { // 定义 Binding 的调用入口，参数和具体处理见下方实现。
        super(cost); // 向当前声明或调用传入 super(cost)。
    } // 结束当前作用域。

    public static final DeferredItem<Item> BINDING = ITEMS.register("parts/binding", // 设置 BINDING 为 ITEMS.register("parts/binding",。
            () -> new Binding(MaterialValue.VALUE_Ingot)); // 定义回调或映射规则，在对应条目被处理时执行。

    /** 显式触发本类的静态注册条目初始化。 */
    public static void init() { // 定义 init 的调用入口，参数和具体处理见下方实现。
    } // 结束当前作用域。

    public static void getAllColoredParts(Collection<ItemStack> coloredParts) { // 定义 getAllColoredParts 的调用入口，参数和具体处理见下方实现。
        List<Material> materials = TinkerMaterials.materials; // 设置 materials 为 TinkerMaterials.materials。
        for (Material material : materials) { // 遍历 Material material : materials，逐项执行当前处理。
            if (material.hasStats("Extra") && !material.isHidden()) { // 检查 material.hasStats("Extra") && !material.isHidden()；按条件决定是否执行本分支。
                coloredParts.add(getColoredPart(material)); // 向集合或构建器追加条目。
            } // 结束当前作用域。
        } // 结束当前作用域。
    } // 结束当前作用域。

    public static ItemStack getColoredPart(Material material) { // 定义 getColoredPart 的调用入口，参数和具体处理见下方实现。
        ItemStack stack = new ItemStack(BINDING.get()); // 设置 stack 为 new ItemStack(BINDING.get())。
        CompoundTag tag = new CompoundTag(); // 设置 tag 为 new CompoundTag()。
        tag.putString(Tags.PART_MATERIAL, material.identifier); // 写入字符串存档字段。
        stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(tag)); // 设置目标槽位、组件或属性的值。
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.tconstruct.material_name", // 设置目标槽位、组件或属性的值。
                Component.translatable("material." + material.identifier + ".name"), // 调用 translatable，执行当前流程所需的处理。
                Component.translatable("item.tconstruct.binding.name"))); // 调用 translatable，执行当前流程所需的处理。
        return stack; // 返回 stack，将当前分支的结果交给调用者。
    } // 结束当前作用域。

} // 结束当前作用域。
