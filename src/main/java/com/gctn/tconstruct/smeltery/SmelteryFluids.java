package com.gctn.tconstruct.smeltery;

import com.gctn.tconstruct.TinkersNirvana;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 注册只供储罐使用的源流体、流动流体及其流体类型。
 * 这些流体没有可放置方块或流体桶，机器之间通过流体能力传输。
 * 客户端的纹理与着色注册集中在 SmelteryClientEvents 中。
 */
public final class SmelteryFluids {
    private static final DeferredRegister<FluidType> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, TinkersNirvana.MODID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TinkersNirvana.MODID);
    private static final List<Entry> ENTRIES = new ArrayList<>();
    public static final Entry IRON = fluid("molten_iron", 0xFFA81212, 769);
    public static final Entry GOLD = fluid("molten_gold", 0xFFF6D609, 532);
    public static final Entry COPPER = fluid("molten_copper", 0xFFED9F07, 542);
    public static final Entry COBALT = fluid("molten_cobalt", 0xFF2882D4, 950);
    public static final Entry GLASS = fluid("molten_glass", 0xCCB8D8E8, 750);
    public static final Entry OBSIDIAN = fluid("molten_obsidian", 0xFF2C0D59, 1000);
    public static final Entry STONE = fluid("seared_stone", 0xFF777777, 800);
    public static final Entry BLOOD = fluid("blood", 0xFF7B0000, 310);

    public record Entry(String name, int color, Supplier<? extends FluidType> type, Supplier<FlowingFluid> source, Supplier<FlowingFluid> flowing) {}
    private static Entry fluid(String name, int color, int temperature) {
        var type = TYPES.register(name, () -> new FluidType(FluidType.Properties.create().density(2000).viscosity(6000)
                .temperature(temperature).lightLevel(name.equals("blood") ? 0 : 10).descriptionId("fluid.tconstruct." + name)) {});
        Supplier<FlowingFluid> source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties(name, type)));
        Supplier<FlowingFluid> flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(properties(name, type)));
        var entry = new Entry(name, color, type, source, flowing);
        ENTRIES.add(entry);
        return entry;
    }
    private static BaseFlowingFluid.Properties properties(String name, Supplier<? extends FluidType> type) {
        return new BaseFlowingFluid.Properties(type,
                () -> (FlowingFluid) FLUIDS.getEntries().stream().filter(e -> e.getId().getPath().equals(name)).findFirst().orElseThrow().get(),
                () -> (FlowingFluid) FLUIDS.getEntries().stream().filter(e -> e.getId().getPath().equals("flowing_" + name)).findFirst().orElseThrow().get());
    }
    public static List<Entry> entries() { return List.copyOf(ENTRIES); }
    public static void register(IEventBus bus) { TYPES.register(bus); FLUIDS.register(bus); }
}
