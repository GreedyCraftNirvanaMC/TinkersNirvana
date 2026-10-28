package com.gctn.tconstruct.smeltery.block.entity; // 声明此类所属的包，使其与对应功能模块保持一致。

import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.HolderLookup; // 引入 net.minecraft.core.HolderLookup，供本文件使用。
import net.minecraft.nbt.CompoundTag; // 引入 net.minecraft.nbt.CompoundTag，供本文件使用。
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket; // 引入 net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntity; // 引入 net.minecraft.world.level.block.entity.BlockEntity，供本文件使用。
import net.minecraft.world.level.block.entity.BlockEntityType; // 引入 net.minecraft.world.level.block.entity.BlockEntityType，供本文件使用。
import net.minecraft.world.level.block.state.BlockState; // 引入 net.minecraft.world.level.block.state.BlockState，供本文件使用。

/**
 * 为较小的冶炼炉构件共用脏标记和方块实体数据包同步逻辑。
 * 这些构件的更新标签复用存档状态，大型控制器则单独覆盖为仅发送可视数据。
 */
public abstract class SyncedBlockEntity extends BlockEntity { // 定义 SyncedBlockEntity 的实现类型，具体职责见上方说明。
    protected SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); } // 定义 SyncedBlockEntity 的调用入口；向当前声明或调用传入 super(type, pos, state)。
    public void sync() { // 定义 sync 的调用入口，参数和具体处理见下方实现。
        setChanged(); // 标记数据已变化，供区块保存或菜单同步使用。
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2); // 检查 level != null && !level.isClientSide；按条件决定是否执行本分支。
    } // 结束当前作用域。
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); } // 实现父类或接口约定；定义 getUpdatePacket 的调用入口；返回 ClientboundBlockEntityDataPacket.create(this)，将当前分支的结果交给调用者。
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); } // 实现父类或接口约定；定义 getUpdateTag 的调用入口；返回 saveWithoutMetadata(registries)，将当前分支的结果交给调用者。
} // 结束当前作用域。
