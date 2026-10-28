package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 同步浇注口正在流动的流体种类，供客户端绘制流体束。
 * 本实体不持有传输中的实际体积，源端和目标端始终保存权威流体数量。
 */
public class FaucetBlockEntity extends SyncedBlockEntity {
    private FluidStack flowing = FluidStack.EMPTY;
    public FaucetBlockEntity(BlockPos pos, BlockState state) { super(SmelteryRegistration.FAUCET_ENTITY.get(), pos, state); }
    public FluidStack flowing() { return flowing; }
    public void setFlowing(FluidStack fluid) {
        if (flowing.isEmpty() && fluid.isEmpty() || !flowing.isEmpty() && !fluid.isEmpty() && FluidStack.isSameFluidSameComponents(flowing, fluid)) return;
        flowing = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(1);
        sync();
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Flowing", flowing.saveOptional(registries));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        flowing = FluidStack.parseOptional(registries, tag.getCompound("Flowing"));
    }
}
