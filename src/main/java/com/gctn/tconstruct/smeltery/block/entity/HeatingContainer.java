/*
 * 根据 TinkersAntique 的 TileHeatingStructure、TileHeatingStructureFuelTank 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block.entity;

import net.minecraft.world.Container;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/** 两种加热结构共用菜单同步，配方、储液和成品处理仍由各自控制器负责。 */
public interface HeatingContainer extends Container, SmelteryMaster {
    boolean active();

    boolean heating();

    boolean isRemoved();

    int heat(int slot);

    int required(int slot);

    int fuel();

    int fuelQuality();

    int temperature();

    FuelDisplay fuelDisplay();

    record FuelDisplay(FluidStack fluid, int amount, int capacity, int temperature) {
        public static FuelDisplay of(Level level, List<BlockPos> tanks, FluidStack currentFuel, int fuel) {
            FluidStack display = fuel > 0 && !currentFuel.isEmpty() ? currentFuel.copyWithAmount(1) : FluidStack.EMPTY;
            int capacity = fuel > 0 ? currentFuel.getAmount() : 0;
            int amount = 0;
            for (BlockPos pos : tanks) {
                if (level != null && level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                    FluidStack fluid = component.tank.getFluid();
                    if (!fluid.isEmpty()) {
                        if (display.isEmpty()) {
                            display = fluid.copyWithAmount(1);
                        }
                        if (FluidStack.isSameFluidSameComponents(display, fluid)) {
                            amount += fluid.getAmount();
                            capacity += component.tank.getCapacity();
                        }
                    }
                }
            }
            return new FuelDisplay(display, amount, capacity, display.isEmpty() ? 0 : display.getFluidType().getTemperature(display));
        }
    }
}
