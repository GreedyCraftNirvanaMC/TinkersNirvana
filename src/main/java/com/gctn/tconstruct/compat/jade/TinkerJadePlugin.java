package com.gctn.tconstruct.compat.jade;

import com.gctn.tconstruct.gadgets.block.RackBlock;
import com.gctn.tconstruct.gadgets.block.entity.RackBlockEntity;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.block.entity.SearedFurnaceBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity;
import com.gctn.tconstruct.smeltery.block.entity.TinkerTankBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class TinkerJadePlugin implements IWailaPlugin {
    @Override public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MachineStatus.INSTANCE, SmelteryBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatus.INSTANCE, SearedFurnaceBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatus.INSTANCE, TinkerTankBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatus.INSTANCE, SmelteryComponentBlockEntity.class);
        registration.registerBlockDataProvider(MachineStatus.INSTANCE, RackBlockEntity.class);
    }

    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineStatus.INSTANCE, SmelteryBlock.class);
        registration.registerBlockComponent(MachineStatus.INSTANCE, RackBlock.class);
    }
}
