package com.gctn.tconstruct.api;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tools.TinkerTools;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 附属模组的工具升级入口，无需注册，也不依赖 KubeJS。
 * 经验、升级条件及提示由调用方处理；修改操作必须在逻辑服务端线程执行。
 */
public final class ToolUpgradeApi {
    private static final String LEVEL_KEY = "UpgradeLevel";

    private ToolUpgradeApi() {}

    /** 是否为支持升级的匠魂工具，包括继承 TinkerTools 的附属工具。 */
    public static boolean isSupported(ItemStack tool) {
        return !tool.isEmpty() && tool.getItem() instanceof TinkerTools;
    }

    /** 初始等级为 0；旧工具及不支持的物品返回 0。每级贡献一个独立强化槽。 */
    public static int getLevel(ItemStack tool) {
        return isSupported(tool) ? Math.max(0, TinkerDataComponents.getMaterialTag(tool).getInt(LEVEL_KEY)) : 0;
    }

    /**
     * 原地升一级并新增一个强化槽，成功返回 true。
     * 不支持的物品、非单件堆叠或等级/槽位达到整数上限时返回 false，不修改工具。
     * 调用方应仅在成功后扣除升级成本；对同一工具重复调用会继续升级。
     */
    public static boolean tryUpgrade(ItemStack tool) {
        if (!isSupported(tool) || tool.getCount() != 1) { return false; }
        int level = getLevel(tool);
        if (level == Integer.MAX_VALUE) { return false; }

        TinkerTools item = (TinkerTools) tool.getItem();
        int previousSlots = item.getTotalModifierSlots(tool);
        if (previousSlots == Integer.MAX_VALUE) { return false; }

        // 材料组件返回副本，只改等级以保留材料、强化、词条运行数据与未知扩展字段。
        // 先在副本上确认实际新增一槽，避免槽位钳制导致升级成功却没有奖励。
        var data = TinkerDataComponents.getMaterialTag(tool);
        data.putInt(LEVEL_KEY, level + 1);
        CustomData upgradedData = CustomData.of(data);
        ItemStack preview = tool.copy();
        preview.set(TinkerDataComponents.TINKER_MATERIAL.get(), upgradedData);
        if (item.getTotalModifierSlots(preview) != previousSlots + 1) { return false; }

        tool.set(TinkerDataComponents.TINKER_MATERIAL.get(), upgradedData);
        return true;
    }
}
