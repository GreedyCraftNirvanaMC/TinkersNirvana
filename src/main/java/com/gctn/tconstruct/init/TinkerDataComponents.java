package com.gctn.tconstruct.init;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TinkerDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TinkersNirvana.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TINKER_MATERIAL =
            COMPONENTS.registerComponentType("tinker_material", builder -> builder
                    .persistent(CustomData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> TINKER_TRAITS =
            COMPONENTS.registerComponentType("tinker_traits", builder -> builder
                    .persistent(CustomData.CODEC));

    private TinkerDataComponents() {}

    /** 返回材料 Tag 的副本；新组件缺失时兼容旧 CUSTOM_DATA，不自动写回。 */
    public static CompoundTag getMaterialTag(ItemStack stack) {
        CustomData data = stack.get(TINKER_MATERIAL.get());
        if (data == null) {
            data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        }
        return data.copyTag();
    }

    /** 返回词条数据副本；运行状态请通过 ToolTraits.updateTraitData 修改并写回。 */
    public static CompoundTag getTraitsTag(ItemStack stack) {
        CustomData data = stack.get(TINKER_TRAITS.get());
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static void register(IEventBus eventBus) {
        COMPONENTS.register(eventBus);
    }
}
