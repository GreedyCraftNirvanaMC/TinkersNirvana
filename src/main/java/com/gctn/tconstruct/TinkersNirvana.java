package com.gctn.tconstruct; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.init.ToolPartsRegistryBus; // 引入 com.gctn.tconstruct.init.ToolPartsRegistryBus，供本文件使用。
import com.gctn.tconstruct.init.TinkerDataComponents; // 引入 com.gctn.tconstruct.init.TinkerDataComponents，供本文件使用。
import com.gctn.tconstruct.init.ToolsRegistryBus; // 引入 com.gctn.tconstruct.init.ToolsRegistryBus，供本文件使用。
import com.gctn.tconstruct.library.MaterialList; // 引入 com.gctn.tconstruct.library.MaterialList，供本文件使用。
import com.gctn.tconstruct.init.TableRegistryBus; // 引入 com.gctn.tconstruct.init.TableRegistryBus，供本文件使用。
import com.gctn.tconstruct.init.TinkerRegistry; // 引入 com.gctn.tconstruct.init.TinkerRegistry，供本文件使用。
import com.gctn.tconstruct.tools.traits.TinkerTraits; // 引入 com.gctn.tconstruct.tools.traits.TinkerTraits，供本文件使用。
import com.gctn.tconstruct.init.BlockRegister; // 引入 com.gctn.tconstruct.init.BlockRegister，供本文件使用。
import com.gctn.tconstruct.init.ItemRegister; // 引入 com.gctn.tconstruct.init.ItemRegister，供本文件使用。
import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import com.mojang.logging.LogUtils; // 引入 com.mojang.logging.LogUtils，供本文件使用。
import net.neoforged.bus.api.IEventBus; // 引入 net.neoforged.bus.api.IEventBus，供本文件使用。
import net.neoforged.fml.ModContainer; // 引入 net.neoforged.fml.ModContainer，供本文件使用。
import net.neoforged.fml.common.Mod; // 引入 net.neoforged.fml.common.Mod，供本文件使用。
import net.neoforged.fml.config.ModConfig; // 引入 net.neoforged.fml.config.ModConfig，供本文件使用。
import org.slf4j.Logger; // 引入 org.slf4j.Logger，供本文件使用。

// 此处标识符必须与 META-INF/neoforge.mods.toml 中的模组条目一致。
/**
 * NeoForge 模组入口：向模组事件总线注册共享定义和延迟注册器。
 * 注册阶段只登记对象的创建函数，必须等对应的注册事件完成后才能取得注册对象。
 * 客户端相关逻辑放在限定为 Dist.CLIENT 的事件订阅类中，避免专用服务端加载渲染类。
 */
@Mod(TinkersNirvana.MODID) // 通过 @Mod(TinkersNirvana.MODID) 声明框架需要读取的类型或生命周期信息。
public class TinkersNirvana { // 定义 TinkersNirvana 的实现类型，具体职责见上方说明。
    // 集中定义模组标识符，供各处共用。
    public static final String MODID = "tconstruct"; // 设置 MODID 为 "tconstruct"。
    // 直接使用 SLF4J 日志接口。
    public static final Logger LOGGER = LogUtils.getLogger(); // 设置 LOGGER 为 LogUtils.getLogger()。

    public TinkersNirvana(IEventBus modEventBus, ModContainer modContainer) { // 定义 TinkersNirvana 的调用入口，参数和具体处理见下方实现。
        // 材料初始化会引用词条条目，因此先注册词条定义。
        // 后续内容注册只登记对象供应器，不立即解析实际对象。
        TinkerTraits.registerTraits(); // 调用 registerTraits，执行当前流程所需的处理。
        TinkerDataComponents.register(modEventBus); // 登记对象工厂或事件处理器。
        TableRegistryBus.register(modEventBus); // 登记对象工厂或事件处理器。
        SmelteryRegistration.register(modEventBus); // 登记对象工厂或事件处理器。
        ToolPartsRegistryBus.register(modEventBus); // 登记对象工厂或事件处理器。
        ToolsRegistryBus.register(modEventBus); // 登记对象工厂或事件处理器。
        TinkerRegistry.register(modEventBus); // 登记对象工厂或事件处理器。
        BlockRegister.register(modEventBus); // 登记对象工厂或事件处理器。
        ItemRegister.register(modEventBus); // 登记对象工厂或事件处理器。
        // 修复原料初始化也会触发静态材料目录初始化，
        // 无需额外创建不会被使用的 TinkerMaterials 实例。
        MaterialList.init(); // 调用 init，执行当前流程所需的处理。
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC); // 调用 registerConfig，执行当前流程所需的处理。
    } // 结束当前作用域。
    
} // 结束当前作用域。
