package com.gctn.tconstruct.datagen;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.init.BlockRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

@EventBusSubscriber(modid = "tconstruct", value = Dist.CLIENT)
public class TCBlockStateProvider extends BlockStateProvider {
    public TCBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, "tconstruct", existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation exampleTexture = modLoc("block/tconstruct_texture");
        ResourceLocation bottomTexture = modLoc("block/tconstruct_texture_bottom");
        ResourceLocation topTexture = modLoc("block/tconstruct_texture_top");
        ResourceLocation sideTexture = modLoc("block/tconstruct_texture_front");
        ResourceLocation frontTexture = modLoc("block/tconstruct_texture_front");

        for (DeferredBlock<?> TCentries : BlockRegister.simpleEntries()) {
            Block block = TCentries.get();

            ModelFile TCModel = models().withExistingParent(BuiltInRegistries.BLOCK.getKey(block).getPath(), this.mcLoc("block/cobblestone"));

            simpleBlock(block);

            simpleBlockItem(block, TCModel);

            TinkersNirvana.LOGGER.info("test to: {}", block);
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
