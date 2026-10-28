package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import net.minecraft.core.component.DataComponentType; // 引入 net.minecraft.core.component.DataComponentType，供本文件使用。
import net.minecraft.core.component.DataComponents; // 引入 net.minecraft.core.component.DataComponents，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.component.CustomData; // 引入 net.minecraft.world.item.component.CustomData，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredHolder; // 引入 net.neoforged.neoforge.registries.DeferredHolder，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

/**
 * 为 Minecraft 1.21.1 定义可持久化、可网络同步的物品材料与词条组件。
 * 将 CustomData 视为不可变数据：读取时返回副本，修改时替换整个组件。
 * 此版本中，持久化编解码器也会用于生成默认的网络编解码器。
 */
public final class TinkerDataComponents { // 定义 TinkerDataComponents 的实现类型，具体职责见上方说明。
    public static final DeferredRegister.DataComponents COMPONENTS = // 设置 COMPONENTS 为 。
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TinkersNirvana.MODID); // 调用 createDataComponents，执行当前流程所需的处理。

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TINKER_MATERIAL = // 在 TinkerDataComponents 中补充当前声明或表达式的 public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TINKER_MATERIAL = 部分。
            COMPONENTS.registerComponentType("tinker_material", builder -> builder // 定义回调或映射规则，在对应条目被处理时执行。
                    .persistent(CustomData.CODEC)); // 调用 persistent，执行当前流程所需的处理。

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TINKER_TRAITS = // 在 TinkerDataComponents 中补充当前声明或表达式的 public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TINKER_TRAITS = 部分。
            COMPONENTS.registerComponentType("tinker_traits", builder -> builder // 定义回调或映射规则，在对应条目被处理时执行。
                    .persistent(CustomData.CODEC)); // 调用 persistent，执行当前流程所需的处理。

    private TinkerDataComponents() {} // 定义 TinkerDataComponents 的调用入口；此处无需额外处理。

    /** 返回材料 Tag 的副本；新组件缺失时兼容旧 CUSTOM_DATA，不自动写回。 */
    public static CompoundTag getMaterialTag(ItemStack stack) { // 定义 getMaterialTag 的调用入口，参数和具体处理见下方实现。
        CustomData data = stack.get(TINKER_MATERIAL.get()); // 设置 data 为 stack.get(TINKER_MATERIAL.get())。
        if (data == null) { // 检查 data == null；按条件决定是否执行本分支。
            data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY); // 设置 data 为 stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)。
        } // 结束当前作用域。
        return data.copyTag(); // 返回 data.copyTag()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    /** 返回词条数据副本；运行状态请通过 ToolTraits.updateTraitData 修改并写回。 */
    public static CompoundTag getTraitsTag(ItemStack stack) { // 定义 getTraitsTag 的调用入口，参数和具体处理见下方实现。
        CustomData data = stack.get(TINKER_TRAITS.get()); // 设置 data 为 stack.get(TINKER_TRAITS.get())。
        return data == null ? new CompoundTag() : data.copyTag(); // 返回 data == null ? new CompoundTag() : data.copyTag()，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        COMPONENTS.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
