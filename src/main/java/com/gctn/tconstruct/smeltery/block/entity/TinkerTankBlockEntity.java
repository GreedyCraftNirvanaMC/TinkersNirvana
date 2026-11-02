/*
 * 根据 TinkersAntique 的 TileTinkerTank、TileMultiblock 移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery.block.entity;

import com.gctn.tconstruct.smeltery.SmelteryModule;
import com.gctn.tconstruct.smeltery.SmelteryTank;
import com.gctn.tconstruct.smeltery.block.SmelteryBlock;
import com.gctn.tconstruct.smeltery.menu.TinkerTankMenu;
import com.gctn.tconstruct.smeltery.multiblock.TinkerTankStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/** 仅存储液体；不运行冶炼炉的燃料、加热、合金或实体熔炼逻辑。 */
public class TinkerTankBlockEntity extends BlockEntity implements MenuProvider, SmelteryMaster {
    public final SmelteryTank liquids = new SmelteryTank(this::changed);
    private TinkerTankStructure structure;
    private Set<BlockPos> members = Set.of();
    private BlockPos min;
    private BlockPos max;
    private boolean active;
    private boolean needsCheck = true;
    private boolean dirty;
    private int tick;

    public TinkerTankBlockEntity(BlockPos pos, BlockState state) {
        super(SmelteryModule.TINKER_TANK_ENTITY.get(), pos, state);
    }

    public void tick() {
        // 组成方块变化会立即安排重检；定期重检也能发现内部放入的方块。
        if (needsCheck || tick % (active() ? 300 : 20) == 0) {
            needsCheck = false;
            checkStructure();
        }
        tick = (tick + 1) % 300;
        if (dirty) {
            dirty = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            if (structure != null) {
                for (BlockPos pos : structure.blocks()) {
                    if (level.getBlockState(pos).getBlock() instanceof SmelteryBlock block && block.kind == SmelteryBlock.Kind.DRAIN) {
                        level.updateNeighbourForOutputSignal(pos, block);
                    }
                }
            }
        }
    }

    private void checkStructure() {
        // 跨区块结构未全部加载时保留现有数据，不能因卸载邻区块而清空储液。
        if (min != null && max != null) {
            for (int x = (min.getX() - 1) >> 4; x <= (max.getX() + 1) >> 4; x++) {
                for (int z = (min.getZ() - 1) >> 4; z <= (max.getZ() + 1) >> 4; z++) {
                    if (!level.hasChunk(x, z)) {
                        return;
                    }
                }
            }
        }
        TinkerTankStructure found = TinkerTankStructure.detect(level, worldPosition,
                getBlockState().getValue(SmelteryBlock.FACING).getOpposite());
        if (found != null && active() && found.equals(structure)) {
            return;
        }
        if (found == null && !active && structure == null) {
            return;
        }
        releaseComponents();
        structure = found;
        active = found != null;
        members = found == null ? Set.of() : new HashSet<>(found.blocks());
        min = found == null ? null : found.min();
        max = found == null ? null : found.max();
        // 原版失效时使用空尺寸计算占位容量，但排液口必须停止访问这些储液。
        liquids.setCapacity(found == null ? 32000 : found.capacity());
        if (found != null) {
            for (BlockPos pos : found.blocks()) {
                if (level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                    component.assign(worldPosition);
                }
            }
        }
        level.setBlock(worldPosition, getBlockState().setValue(SmelteryBlock.ACTIVE, active), 3);
        changed();
    }

    public void releaseComponents() {
        if (structure != null && level != null && !level.isClientSide) {
            for (BlockPos pos : structure.blocks()) {
                if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity component) {
                    component.release(worldPosition);
                }
            }
        }
    }

    public boolean active() {
        return active && !isRemoved() && (level == null || level.isClientSide || structure != null);
    }

    @Override
    public boolean contains(BlockPos pos) {
        return active() && members.contains(pos);
    }

    @Override
    public void requestCheck() {
        needsCheck = true;
    }

    @Override
    public SmelteryTank liquids() {
        return liquids;
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }

    private void changed() {
        setChanged();
        dirty = true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.tinkertank.name");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TinkerTankMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Active", active);
        if (min != null && max != null) {
            tag.putLong("Min", min.asLong());
            tag.putLong("Max", max.asLong());
        }
        liquids.save(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        active = tag.getBoolean("Active");
        min = tag.contains("Min") ? BlockPos.of(tag.getLong("Min")) : null;
        max = tag.contains("Max") ? BlockPos.of(tag.getLong("Max")) : null;
        liquids.load(tag, registries);
        // 归属和尺寸从当前世界重建，不能信任存档中遗留的组成方块列表。
        structure = null;
        members = Set.of();
        needsCheck = true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
