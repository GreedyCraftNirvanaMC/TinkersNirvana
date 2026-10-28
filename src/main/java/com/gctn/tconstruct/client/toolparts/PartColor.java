package com.gctn.tconstruct.client.toolparts; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.utils.Tags; // 引入 com.gctn.tconstruct.library.utils.Tags，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import net.minecraft.client.color.item.ItemColor; // 引入 net.minecraft.client.color.item.ItemColor，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent; // 引入 net.neoforged.neoforge.client.event.RegisterColorHandlersEvent，供本文件使用。

import static com.gctn.tconstruct.library.toolparts.Binding.BINDING; // 引入 com.gctn.tconstruct.library.toolparts.Binding.BINDING，供本文件使用。
import static com.gctn.tconstruct.library.toolparts.PickaxeHead.PICKAXEHEAD; // 引入 com.gctn.tconstruct.library.toolparts.PickaxeHead.PICKAXEHEAD，供本文件使用。
import static com.gctn.tconstruct.library.toolparts.ToolRod.TOOL_ROD; // 引入 com.gctn.tconstruct.library.toolparts.ToolRod.TOOL_ROD，供本文件使用。

/**
 * 只在客户端为共享的材料部件模型注册着色处理器。
 * 存档中的材料标识符无法识别时使用白色，避免物品无法渲染。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public class PartColor { // 定义 PartColor 的实现类型，具体职责见上方说明。
    public static final ItemColor PART_COLOR = (stack, tintIndex) -> { // 设置 PART_COLOR 为 (stack, tintIndex) -> {。
        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL); // 设置 materialId 为 TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL)。
        Material material = TinkerMaterials.getMaterial(materialId); // 设置 material 为 TinkerMaterials.getMaterial(materialId)。
        return material == null ? 0xFFFFFFFF : material.color; // 返回 material == null ? 0xFFFFFFFF : material.color，将当前分支的结果交给调用者。
    }; // 结束当前作用域并完成本次表达式或声明。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    private static void registerItemColors(RegisterColorHandlersEvent.Item event) { // 定义 registerItemColors 的调用入口，参数和具体处理见下方实现。
        event.register(PART_COLOR, BINDING.get()); // 登记对象工厂或事件处理器。
        event.register(PART_COLOR, PICKAXEHEAD.get()); // 登记对象工厂或事件处理器。
        event.register(PART_COLOR, TOOL_ROD.get()); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。

} // 结束当前作用域。
