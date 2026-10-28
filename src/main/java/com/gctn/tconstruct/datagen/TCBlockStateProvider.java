package com.gctn.tconstruct.datagen; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.init.BlockRegister; // 引入 com.gctn.tconstruct.init.BlockRegister，供本文件使用。
import net.minecraft.core.registries.BuiltInRegistries; // 引入 net.minecraft.core.registries.BuiltInRegistries，供本文件使用。
import net.minecraft.data.DataGenerator; // 引入 net.minecraft.data.DataGenerator，供本文件使用。
import net.minecraft.data.PackOutput; // 引入 net.minecraft.data.PackOutput，供本文件使用。
import net.minecraft.world.level.block.Block; // 引入 net.minecraft.world.level.block.Block，供本文件使用。
import net.neoforged.api.distmarker.Dist; // 引入 net.neoforged.api.distmarker.Dist，供本文件使用。
import net.neoforged.bus.api.SubscribeEvent; // 引入 net.neoforged.bus.api.SubscribeEvent，供本文件使用。
import net.neoforged.fml.common.EventBusSubscriber; // 引入 net.neoforged.fml.common.EventBusSubscriber，供本文件使用。
import net.neoforged.neoforge.client.model.generators.BlockStateProvider; // 引入 net.neoforged.neoforge.client.model.generators.BlockStateProvider，供本文件使用。
import net.neoforged.neoforge.common.data.ExistingFileHelper; // 引入 net.neoforged.neoforge.common.data.ExistingFileHelper，供本文件使用。
import net.neoforged.neoforge.data.event.GatherDataEvent; // 引入 net.neoforged.neoforge.data.event.GatherDataEvent，供本文件使用。
import net.neoforged.neoforge.registries.DeferredBlock; // 引入 net.neoforged.neoforge.registries.DeferredBlock，供本文件使用。

/**
 * 生成简单立方体方块的状态文件、方块模型和物品模型。
 * 只处理 simpleEntries；机器方块保留手工编写的组合模型或方向模型。
 */
@EventBusSubscriber(modid = "tconstruct", value = Dist.CLIENT) // 通过 @EventBusSubscriber(modid 声明框架需要读取的类型或生命周期信息。
public class TCBlockStateProvider extends BlockStateProvider { // 定义 TCBlockStateProvider 的实现类型，具体职责见上方说明。
    public TCBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) { // 定义 TCBlockStateProvider 的调用入口，参数和具体处理见下方实现。
        super(output, "tconstruct", existingFileHelper); // 向当前声明或调用传入 super(output, "tconstruct", existingFileHelper)。
    } // 结束当前作用域。

    @Override // 覆盖父类或接口的方法，保持原有调用契约。
    protected void registerStatesAndModels() { // 定义 registerStatesAndModels 的调用入口，参数和具体处理见下方实现。
        for (DeferredBlock<?> TCentries : BlockRegister.simpleEntries()) { // 遍历 DeferredBlock<?> TCentries : BlockRegister.simpleEntries()，逐项执行当前处理。
            Block block = TCentries.get(); // 设置 block 为 TCentries.get()。

            String name = BuiltInRegistries.BLOCK.getKey(block).getPath(); // 设置 name 为 BuiltInRegistries.BLOCK.getKey(block).getPath()。
            simpleBlockWithItem(block, models().cubeAll(name, modLoc("block/" + name))); // 调用 simpleBlockWithItem，执行当前流程所需的处理。
        } // 结束当前作用域。
    } // 结束当前作用域。

    @SubscribeEvent // 将此方法声明为事件处理入口。
    public static void gatherData(GatherDataEvent event) { // 定义 gatherData 的调用入口，参数和具体处理见下方实现。
        DataGenerator generator = event.getGenerator(); // 设置 generator 为 event.getGenerator()。
        PackOutput output = generator.getPackOutput(); // 设置 output 为 generator.getPackOutput()。
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper(); // 设置 existingFileHelper 为 event.getExistingFileHelper()。

        generator.addProvider( // 调用 addProvider，执行当前流程所需的处理。
                event.includeClient(), // 调用 includeClient，执行当前流程所需的处理。
                new TCBlockStateProvider(output, existingFileHelper) // 创建 new TCBlockStateProvider(output, existingFileHelper)，作为当前对象或调用的组成部分。
        ); // 结束当前参数列表或表达式并完成本次调用。
    } // 结束当前作用域。
} // 结束当前作用域。
