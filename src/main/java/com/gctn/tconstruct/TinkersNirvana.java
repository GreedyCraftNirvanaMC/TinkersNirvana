package com.gctn.tconstruct;

import com.gctn.tconstruct.init.ToolPartsRegistryBus;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.init.ToolsRegistryBus;
import com.gctn.tconstruct.library.MaterialList;
import com.gctn.tconstruct.init.TableRegistryBus;
import com.gctn.tconstruct.init.TinkerRegistry;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// 此处标识符必须与 META-INF/neoforge.mods.toml 中的模组条目一致。
/**
 * NeoForge 模组入口：向模组事件总线注册共享定义和延迟注册器。
 * 注册阶段只登记对象的创建函数，必须等对应的注册事件完成后才能取得注册对象。
 * 客户端相关逻辑放在限定为 Dist.CLIENT 的事件订阅类中，避免专用服务端加载渲染类。
 */
@Mod(TinkersNirvana.MODID)
public class TinkersNirvana {
    // 集中定义模组标识符，供各处共用。
    public static final String MODID = "tconstruct";
    // 直接使用 SLF4J 日志接口。
    public static final Logger LOGGER = LogUtils.getLogger();

    public TinkersNirvana(IEventBus modEventBus, ModContainer modContainer) {
        // 材料初始化会引用词条条目，因此先注册词条定义。
        // 后续内容注册只登记对象供应器，不立即解析实际对象。
        TinkerTraits.registerTraits();
        TinkerDataComponents.register(modEventBus);
        TableRegistryBus.register(modEventBus);
        SmelteryRegistration.register(modEventBus);
        ToolPartsRegistryBus.register(modEventBus);
        ToolsRegistryBus.register(modEventBus);
        TinkerRegistry.register(modEventBus);
        BlockRegister.register(modEventBus);
        ItemRegister.register(modEventBus);
        // 修复原料初始化也会触发静态材料目录初始化，
        // 无需额外创建不会被使用的 TinkerMaterials 实例。
        MaterialList.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    
}
