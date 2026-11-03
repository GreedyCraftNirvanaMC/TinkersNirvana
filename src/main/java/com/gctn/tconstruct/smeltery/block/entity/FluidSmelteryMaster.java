/*
 * 根据 TinkersAntique 的 ISmelteryTankHandler 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryTank;

/** 只有冶炼炉和工匠储罐提供共享储液；焦黑熔炉仅管理燃料储罐。 */
public interface FluidSmelteryMaster extends SmelteryMaster {
    SmelteryTank liquids();
}
