package com.gctn.tconstruct.smeltery; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.world.level.material.FlowingFluid; // 引入 net.minecraft.world.level.material.FlowingFluid，供本文件使用。
import net.minecraft.world.level.material.Fluid; // 引入 net.minecraft.world.level.material.Fluid，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.fluids.BaseFlowingFluid; // 引入 net.neoforged.neoforge.fluids.BaseFlowingFluid，供本文件使用。
import net.neoforged.neoforge.fluids.FluidType; // 引入 net.neoforged.neoforge.fluids.FluidType，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。
import net.neoforged.neoforge.registries.NeoForgeRegistries; // 引入 net.neoforged.neoforge.registries.NeoForgeRegistries，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。
import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

/**
 * 注册只供储罐使用的源流体、流动流体及其流体类型。
 * 这些流体没有可放置方块或流体桶，机器之间通过流体能力传输。
 * 客户端的纹理与着色注册集中在 SmelteryClientEvents 中。
 */
public final class SmelteryFluids { // 定义 SmelteryFluids 的实现类型，具体职责见上方说明。
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TinkersNirvana.MODID); // 设置 TYPES 为 DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TinkersNirvana.MODID)。
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TinkersNirvana.MODID); // 设置 FLUIDS 为 DeferredRegister.create(Registries.FLUID, TinkersNirvana.MODID)。
    private static final List<Entry> ENTRIES = new ArrayList<>(); // 设置 ENTRIES 为 new ArrayList<>()。
    public static final Entry IRON = fluid("molten_iron", 0xFFA81212, 769); // 设置 IRON 为 fluid("molten_iron", 0xFFA81212, 769)。
    public static final Entry GOLD = fluid("molten_gold", 0xFFF6D609, 532); // 设置 GOLD 为 fluid("molten_gold", 0xFFF6D609, 532)。
    public static final Entry COPPER = fluid("molten_copper", 0xFFED9F07, 542); // 设置 COPPER 为 fluid("molten_copper", 0xFFED9F07, 542)。
    public static final Entry COBALT = fluid("molten_cobalt", 0xFF2882D4, 950); // 设置 COBALT 为 fluid("molten_cobalt", 0xFF2882D4, 950)。
    public static final Entry GLASS = fluid("molten_glass", 0xCCB8D8E8, 750); // 设置 GLASS 为 fluid("molten_glass", 0xCCB8D8E8, 750)。
    public static final Entry OBSIDIAN = fluid("molten_obsidian", 0xFF2C0D59, 1000); // 设置 OBSIDIAN 为 fluid("molten_obsidian", 0xFF2C0D59, 1000)。
    public static final Entry STONE = fluid("seared_stone", 0xFF777777, 800); // 设置 STONE 为 fluid("seared_stone", 0xFF777777, 800)。
    public static final Entry BLOOD = fluid("blood", 0xFF7B0000, 310); // 设置 BLOOD 为 fluid("blood", 0xFF7B0000, 310)。

    public record Entry(String name, int color, Supplier<? extends FluidType> type, Supplier<FlowingFluid> source, Supplier<FlowingFluid> flowing) {} // 定义 Entry 的数据记录，具体职责见上方说明。
    private static Entry fluid(String name, int color, int temperature) { // 定义 fluid 的调用入口，参数和具体处理见下方实现。
        var type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create().density(2000).viscosity(6000) // 设置 type 为 TYPES.register(name, () -> new FluidType(FluidType.Properties.create().density(2000).viscosity(6000)。
                .temperature(temperature).lightLevel(name.equals("blood") ? 0 : 10).descriptionId("fluid.tconstruct." + name)) {}); // 调用 temperature，执行当前流程所需的处理。
        Supplier<FlowingFluid> source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties(name, type))); // 设置 source 为 FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties(name, type)))。
        Supplier<FlowingFluid> flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(properties(name, type))); // 设置 flowing 为 FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(properties(name, type)))。
        var entry = new Entry(name, color, type, source, flowing); // 设置 entry 为 new Entry(name, color, type, source, flowing)。
        ENTRIES.add(entry); // 向集合或构建器追加条目。
        return entry; // 返回 entry，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    private static BaseFlowingFluid.Properties properties(String name, Supplier<? extends FluidType> type) { // 定义 properties 的调用入口，参数和具体处理见下方实现。
        return new BaseFlowingFluid.Properties(type, // 返回 new BaseFlowingFluid.Properties(type,，将当前分支的结果交给调用者。
                () -> (FlowingFluid) FLUIDS.getEntries().stream().filter(e -> e.getId().getPath().equals(name)).findFirst().orElseThrow().get(), // 定义回调或映射规则，在对应条目被处理时执行。
                () -> (FlowingFluid) FLUIDS.getEntries().stream().filter(e -> e.getId().getPath().equals("flowing_" + name)).findFirst().orElseThrow().get()); // 定义回调或映射规则，在对应条目被处理时执行。
    } // 结束当前作用域。
    public static List<Entry> entries() { return List.copyOf(ENTRIES); } // 定义 entries 的调用入口；返回 List.copyOf(ENTRIES)，将当前分支的结果交给调用者。
    public static void register(IEventBus bus) { TYPES.register(bus); FLUIDS.register(bus); } // 定义 register 的调用入口；登记对象工厂或事件处理器。
} // 结束当前作用域。
