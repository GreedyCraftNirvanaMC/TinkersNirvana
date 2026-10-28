package com.gctn.tconstruct.client.tools.toolcolors; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.library.TinkerMaterials; // 引入 com.gctn.tconstruct.library.TinkerMaterials，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.library.materials.Material; // 引入 com.gctn.tconstruct.library.materials.Material，供本文件使用。
import net.minecraft.client.color.item.ItemColor; // 引入 net.minecraft.client.color.item.ItemColor，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.nbt.Tag; // 引入 net.minecraft.nbt.Tag，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent; // 引入 net.neoforged.neoforge.client.event.RegisterColorHandlersEvent，供本文件使用。

import static com.gctn.tconstruct.tools.tools.Pickaxe.PICKAXE; // 引入 com.gctn.tconstruct.tools.tools.Pickaxe.PICKAXE，供本文件使用。

/**
 * 将镐模型的三个图层映射到存档中的手柄、头部和绑定结材料。
 * 材料数据缺失或无法识别时安全回退，不修改或迁移物品栈数据。
 */
@EventBusSubscriber(modid = TinkersNirvana.MODID, value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public class PickaxeColor { // 定义 PickaxeColor 的实现类型，具体职责见上方说明。
    public static final ItemColor PICKAXE_COLOR = (stack, tintIndex) -> { // 设置 PICKAXE_COLOR 为 (stack, tintIndex) -> {。
        CompoundTag materials = TinkerDataComponents.getMaterialTag(stack); // 设置 materials 为 TinkerDataComponents.getMaterialTag(stack)。
        if (!materials.contains("Handle", Tag.TAG_STRING) // 检查 !materials.contains("Handle", Tag.TAG_STRING)；按条件决定是否执行本分支。
                || !materials.contains("Head", Tag.TAG_STRING) // 调用 contains，执行当前流程所需的处理。
                || !materials.contains("Binding", Tag.TAG_STRING)) { // 调用 contains，执行当前流程所需的处理。
            return 0xFFFFFFFF; // 返回 0xFFFFFFFF，将当前分支的结果交给调用者。
        } // 结束当前作用域。

        return switch (tintIndex) { // 返回 switch (tintIndex) {，将当前分支的结果交给调用者。
            case 0 -> // layer0 → Handle（手柄）
                    getMaterialColor(materials.getString("Handle")); // 读取字符串字段。
            case 1 -> // layer1 → Head（头部）
                    getMaterialColor(materials.getString("Head")); // 读取字符串字段。
            case 2 -> // layer2 → Binding（绑定）
                    getMaterialColor(materials.getString("Binding")); // 读取字符串字段。
            default -> // 异常索引，返回默认色
                    0xFFFFFFFF; // 在 PickaxeColor 中补充当前声明或表达式的 0xFFFFFFFF 部分。
        }; // 结束当前作用域并完成本次表达式或声明。
    }; // 结束当前作用域并完成本次表达式或声明。

    /**
     * 方法：根据材质ID获取颜色
     * @param materialId 材质标识符（如"iron"、"diamond"）
     * @return 材质对应的颜色，匹配不到返回默认白色
     */
    private static int getMaterialColor(String materialId) { // 定义 getMaterialColor 的调用入口，参数和具体处理见下方实现。
        Material material = TinkerMaterials.getMaterial(materialId); // 设置 material 为 TinkerMaterials.getMaterial(materialId)。
        return material == null ? 0xFFFFFFFF : material.color; // 返回 material == null ? 0xFFFFFFFF : material.color，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void registerPickaxeColors(RegisterColorHandlersEvent.Item event) { // 定义 registerPickaxeColors 的调用入口，参数和具体处理见下方实现。
        event.register(PICKAXE_COLOR, PICKAXE.get()); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
