package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.library.toolparts.Binding; // 引入 com.gctn.tconstruct.library.toolparts.Binding，供本文件使用。
import com.gctn.tconstruct.library.toolparts.PickaxeHead; // 引入 com.gctn.tconstruct.library.toolparts.PickaxeHead，供本文件使用。
import com.gctn.tconstruct.library.toolparts.ToolRod; // 引入 com.gctn.tconstruct.library.toolparts.ToolRod，供本文件使用。
import com.gctn.tconstruct.tools.tools.Pickaxe; // 引入 com.gctn.tconstruct.tools.tools.Pickaxe，供本文件使用。
import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.network.chat.Component; // 引入 net.minecraft.network.chat.Component，供本文件使用。
import net.minecraft.world.item.CreativeModeTab; // 引入 net.minecraft.world.item.CreativeModeTab，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。
import net.minecraft.world.item.Items; // 引入 net.minecraft.world.item.Items，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredBlock; // 引入 net.neoforged.neoforge.registries.DeferredBlock，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.Collection; // 引入 java.util.Collection，供本文件使用。
import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

import static com.gctn.tconstruct.library.TinkerMaterials.*; // 引入 com.gctn.tconstruct.library.TinkerMaterials.*，供本文件使用。

/**
 * 物品注册完成后，使用带有材料组件的物品栈构建创造模式分类页。
 * 未初始化的工具或部件没有材料信息；创造模式中的变体必须携带与合成结果一致的数据。
 */
public class TinkerRegistry { // 定义 TinkerRegistry 的实现类型，具体职责见上方说明。
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = // 设置 CREATIVE_MODE_TABS 为 。
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersNirvana.MODID); // 调用 create，执行当前流程所需的处理。

    // 部件
    public static final Supplier<CreativeModeTab> PARTS_TAB = // 设置 PARTS_TAB 为 。
            CREATIVE_MODE_TABS.register("tinker_tool_parts", () -> CreativeModeTab.builder() // 定义回调或映射规则，在对应条目被处理时执行。
                    .icon(() -> new ItemStack(Items.STICK)) // 调用 icon，执行当前流程所需的处理。
                    .title(Component.translatable("tinker_tool_parts_tab")) // 调用 title，执行当前流程所需的处理。
                    .displayItems(((itemDisplayParameters, output) -> { // 调用 displayItems，执行当前流程所需的处理。
                        Collection<ItemStack> tempStacks = new ArrayList<>(); // 设置 tempStacks 为 new ArrayList<>()。
                        ToolRod.getAllParts(tempStacks); // 调用 getAllParts，执行当前流程所需的处理。
                        Binding.getAllColoredParts(tempStacks); // 调用 getAllColoredParts，执行当前流程所需的处理。
                        PickaxeHead.getAllColoredParts(tempStacks); // 调用 getAllColoredParts，执行当前流程所需的处理。
                        for (ItemStack itemStack : tempStacks) { // 遍历 ItemStack itemStack : tempStacks，逐项执行当前处理。
                            output.accept(itemStack); // 调用 accept，执行当前流程所需的处理。
                        } // 结束当前作用域。
                    })) // 结束当前作用域并完成本次表达式或声明。
                    .build()); // 调用 build，执行当前流程所需的处理。

    // 工具
    public static final Supplier<CreativeModeTab> TOOLS_TAB = // 设置 TOOLS_TAB 为 。
            CREATIVE_MODE_TABS.register("tinker_tools", () -> CreativeModeTab.builder() // 定义回调或映射规则，在对应条目被处理时执行。
                    .icon(() -> new ItemStack(Items.WOODEN_PICKAXE)) // 调用 icon，执行当前流程所需的处理。
                    .title(Component.translatable("tinker_tools_tab")) // 调用 title，执行当前流程所需的处理。
                    .displayItems(((itemDisplayParameters, output) -> { // 调用 displayItems，执行当前流程所需的处理。
                        output.accept(Pickaxe.initPickaxe(wood, stone, wood)); // 调用 accept，执行当前流程所需的处理。
                    })) // 结束当前作用域并完成本次表达式或声明。
                    .build()); // 调用 build，执行当前流程所需的处理。

    // 方块
    public static final Supplier<CreativeModeTab> BLOCKS_TAB = // 设置 BLOCKS_TAB 为 。
            CREATIVE_MODE_TABS.register("tinker_blocks", () -> CreativeModeTab.builder() // 定义回调或映射规则，在对应条目被处理时执行。
                    .icon(() -> new ItemStack(Items.COBBLESTONE)) // 调用 icon，执行当前流程所需的处理。
                    .title(Component.translatable("tinker_blocks_tab")) // 调用 title，执行当前流程所需的处理。
                    .displayItems(((itemDisplayParameters, output) -> { // 调用 displayItems，执行当前流程所需的处理。
                        for (DeferredBlock<?> block : BlockRegister.entries()) { // 遍历 DeferredBlock<?> block : BlockRegister.entries()，逐项执行当前处理。
                            output.accept(block.get()); // 读取指定条目或延迟注册对象。
                        } // 结束当前作用域。
                        for (DeferredBlock<?> block : BlockRegister.simpleEntries()) { // 遍历 DeferredBlock<?> block : BlockRegister.simpleEntries()，逐项执行当前处理。
                            output.accept(block.get()); // 读取指定条目或延迟注册对象。
                        } // 结束当前作用域。
                        output.accept(SmelteryRegistration.SEARED_BRICK.get()); // 读取指定条目或延迟注册对象。
                        output.accept(SmelteryRegistration.INGOT_CAST.get()); // 读取指定条目或延迟注册对象。
                        output.accept(SmelteryRegistration.COBALT_INGOT.get()); // 读取指定条目或延迟注册对象。
                        output.accept(SmelteryRegistration.COBALT_NUGGET.get()); // 读取指定条目或延迟注册对象。
                    })) // 结束当前作用域并完成本次表达式或声明。
                    .build()); // 调用 build，执行当前流程所需的处理。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        // 创造模式分类页。
        CREATIVE_MODE_TABS.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
