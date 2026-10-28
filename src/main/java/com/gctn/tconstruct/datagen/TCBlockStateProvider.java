package com.gctn.tconstruct.datagen;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * 生成简单立方体方块的状态文件、方块模型和物品模型。
 * 只处理 simpleEntries；机器方块保留手工编写的组合模型或方向模型。
 */
@EventBusSubscriber(modid = "tconstruct", value = Dist.CLIENT)
public class TCBlockStateProvider extends BlockStateProvider {
    public TCBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, "tconstruct", existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (DeferredBlock<?> TCentries : BlockRegister.simpleEntries()) {
            Block block = TCentries.get();

            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            simpleBlockWithItem(block, models().cubeAll(name, modLoc("block/" + name)));
        }
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(
                event.includeClient(),
                new TCBlockStateProvider(output, existingFileHelper)
        );
    }
}
