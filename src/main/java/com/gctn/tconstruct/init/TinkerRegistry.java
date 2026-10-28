package com.gctn.tconstruct.init;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.library.toolparts.Binding;
import com.gctn.tconstruct.library.toolparts.PickaxeHead;
import com.gctn.tconstruct.library.toolparts.ToolRod;
import com.gctn.tconstruct.tools.tools.Pickaxe;
import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import static com.gctn.tconstruct.library.TinkerMaterials.*;

public class TinkerRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TinkersNirvana.MODID);

    // 部件
    public static final Supplier<CreativeModeTab> PARTS_TAB =
            CREATIVE_MODE_TABS.register("tinker_tool_parts", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.STICK))
                    .title(Component.translatable("tinker_tool_parts_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        Collection<ItemStack> tempStacks = new ArrayList<>();
                        ToolRod.getAllParts(tempStacks);
                        Binding.getAllColoredParts(tempStacks);
                        PickaxeHead.getAllColoredParts(tempStacks);
                        int i = 0;
                        for (ItemStack itemStack : tempStacks) {
                            output.accept(itemStack);
                        }
                    }))
                    .build());

    // 工具
    public static final Supplier<CreativeModeTab> TOOLS_TAB =
            CREATIVE_MODE_TABS.register("tinker_tools", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.WOODEN_PICKAXE))
                    .title(Component.translatable("tinker_tools_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(Pickaxe.initPickaxe(wood, stone, wood));
                    }))
                    .build());

    // 方块
    public static final Supplier<CreativeModeTab> BLOCKS_TAB =
            CREATIVE_MODE_TABS.register("tinker_blocks", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.COBBLESTONE))
                    .title(Component.translatable("tinker_blocks_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        for (DeferredBlock<?> block : BlockRegister.entries()) {
                            output.accept(block.get());
                        }
                        for (DeferredBlock<?> block : BlockRegister.simpleEntries()) {
                            output.accept(block.get());
                        }
                        output.accept(SmelteryRegistration.SEARED_BRICK.get());
                        output.accept(SmelteryRegistration.INGOT_CAST.get());
                        output.accept(SmelteryRegistration.COBALT_INGOT.get());
                        output.accept(SmelteryRegistration.COBALT_NUGGET.get());
                    }))
                    .build());

    public static void register(IEventBus eventBus) {
        // Creative mode tabs
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
