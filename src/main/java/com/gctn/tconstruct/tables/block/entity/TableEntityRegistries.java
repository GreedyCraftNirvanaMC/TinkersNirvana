package com.gctn.tconstruct.tables.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.TinkersNirvana; // 引入 com.gctn.tconstruct.TinkersNirvana，供本文件使用。
import com.gctn.tconstruct.tables.block.TableBlocks; // 引入 com.gctn.tconstruct.tables.block.TableBlocks，供本文件使用。
import net.minecraft.core.registries.Registries; // 引入 net.minecraft.core.registries.Registries，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntityType; // 引入 net.minecraft.world.level.block.entity.BlockEntityType，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.neoforge.registries.DeferredRegister; // 引入 net.neoforged.neoforge.registries.DeferredRegister，供本文件使用。

import java.util.function.Supplier; // 引入 java.util.function.Supplier，供本文件使用。

/**
 * 为工具台方块注册对应的方块实体工厂。
 * 通过延迟供应器等待注册生命周期允许访问方块后，再取得实际方块对象。
 */
public class TableEntityRegistries { // 定义 TableEntityRegistries 的实现类型，具体职责见上方说明。
    public static final DeferredRegister<BlockEntityType<?>> TABLE_ENTITY_TYPES = // 设置 TABLE_ENTITY_TYPES 为 。
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TinkersNirvana.MODID); // 调用 create，执行当前流程所需的处理。

    public static final Supplier<BlockEntityType<?>> TOOL_STATION_BLOCK_ENTITY = // 设置 TOOL_STATION_BLOCK_ENTITY 为 。
            TABLE_ENTITY_TYPES.register("toolstation_blockentity", () -> BlockEntityType.Builder // 定义回调或映射规则，在对应条目被处理时执行。
                    .of(ToolStationBlockEntity::new, TableBlocks.TOOL_STATION.get()) // 读取指定条目或延迟注册对象。
                    .build(null)); // 调用 build，执行当前流程所需的处理。

    public static void register(IEventBus eventBus) { // 定义 register 的调用入口，参数和具体处理见下方实现。
        TABLE_ENTITY_TYPES.register(eventBus); // 登记对象工厂或事件处理器。
    } // 结束当前作用域。
} // 结束当前作用域。
