package com.gctn.tconstruct.init; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.common.block.OreBlocks; // 引入 com.gctn.tconstruct.common.block.OreBlocks，供本文件使用。
import com.gctn.tconstruct.tables.block.TableBlocks; // 引入 com.gctn.tconstruct.tables.block.TableBlocks，供本文件使用。
import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import net.minecraft.world.item.BlockItem; // 引入 net.minecraft.world.item.BlockItem，供本文件使用。
import net.minecraft.world.item.Item; // 引入 net.minecraft.world.item.Item，供本文件使用。
import net.minecraft.world.level.block.Block; // 引入 net.minecraft.world.level.block.Block，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredBlock; // 引入 net.neoforged.neoforge.registries.DeferredBlock，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.Collection; // 引入 java.util.Collection，供本文件使用。
import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

/**
 * 统一注册方块及对应的物品形式，并保留已有的物品标识符。
 * 先遍历各分类以触发其静态注册，再将共享的延迟注册器挂到模组事件总线上。
 */
public class BlockRegister { // 定义 BlockRegister 的实现类型，具体职责见上方说明。
    public static final DeferredRegister.Blocks BLOCKS = // 设置 BLOCKS 为 。
        DeferredRegister.createBlocks(TinkersNirvana.MODID); // 调用 createBlocks，执行当前流程所需的处理。

    public static <T extends Block> DeferredBlock<T> registerBlockWithItem(String name, Supplier<T> block) { // 调用 registerBlockWithItem，执行当前流程所需的处理。
        return registerBlockWithItem(name, name, block); // 返回 registerBlockWithItem(name, name, block)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static <T extends Block> DeferredBlock<T> registerBlockWithItem(String blockName, String itemName, Supplier<T> block) { // 调用 registerBlockWithItem，执行当前流程所需的处理。
        DeferredBlock<T> registeredBlock = BLOCKS.register(blockName, block); // 设置 registeredBlock 为 BLOCKS.register(blockName, block)。
        ItemRegister.ITEMS.register(itemName, () -> new BlockItem(registeredBlock.get(), new Item.Properties())); // 定义回调或映射规则，在对应条目被处理时执行。
        return registeredBlock; // 返回 registeredBlock，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        entries(); // 调用 entries，执行当前流程所需的处理。
        simpleEntries(); // 调用 simpleEntries，执行当前流程所需的处理。
        BLOCKS.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。

    public static Collection<DeferredBlock<?>> entries() { // 定义 entries 的调用入口，参数和具体处理见下方实现。
        // 调用各分类的 entries 方法，以触发其静态注册条目初始化。
        Collection<DeferredBlock<?>> entries = new ArrayList<>(); // 设置 entries 为 new ArrayList<>()。
        entries.addAll(TableBlocks.entries()); // 合并当前批次的全部条目。
        entries.addAll(SmelteryRegistration.blocks()); // 合并当前批次的全部条目。
        return entries; // 返回 entries，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    public static Collection<DeferredBlock<?>> simpleEntries() { // 定义 simpleEntries 的调用入口，参数和具体处理见下方实现。
        Collection<DeferredBlock<?>> simpleEntries = new ArrayList<>(); // 设置 simpleEntries 为 new ArrayList<>()。

        simpleEntries.addAll(OreBlocks.entries()); // 合并当前批次的全部条目。
        return simpleEntries; // 返回 simpleEntries，将当前分支的结果交给调用者。
    } // 结束当前作用域。
} // 结束当前作用域。
