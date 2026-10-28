// SPDX-License-Identifier: LGPL-3.0-only
// 改编自 TinkersAntique 的冶炼炉检测器，来源与许可说明见 THIRD_PARTY_NOTICES.md。
package com.gctn.tconstruct.smeltery; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.block.SmelteryControllerBlock; // 引入 com.gctn.tconstruct.smeltery.block.SmelteryControllerBlock，供本文件使用。
import com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.SmelteryComponentBlockEntity，供本文件使用。
import net.minecraft.core.BlockPos; // 引入 net.minecraft.core.BlockPos，供本文件使用。
import net.minecraft.core.Direction; // 引入 net.minecraft.core.Direction，供本文件使用。
import net.minecraft.world.level.Level; // 引入 net.minecraft.world.level.Level，供本文件使用。

import java.util.ArrayList; // 引入 java.util.ArrayList，供本文件使用。
import java.util.List; // 引入 java.util.List，供本文件使用。

/**
 * 检测围绕矩形空气内部空间的 Antique 式炉底和无框炉壁。
 * 首个无效炉壁层决定可用高度，结构不要求四角或顶盖。
 * 未加载区块会使检测暂停，不会被当作缺失方块，也不会被强制加载。
 */
public record SmelteryStructure(BlockPos min, BlockPos max, List<BlockPos> members, List<BlockPos> tanks) { // 定义 SmelteryStructure 的数据记录，具体职责见上方说明。
    public static final int MAX_INNER_SIZE = 9; // 设置 MAX_INNER_SIZE 为 9。
    public int width() { return max.getX() - min.getX() + 1; } // 定义 width 的调用入口；返回 max.getX() - min.getX() + 1，将当前分支的结果交给调用者。
    public int height() { return max.getY() - min.getY() + 1; } // 定义 height 的调用入口；返回 max.getY() - min.getY() + 1，将当前分支的结果交给调用者。
    public int depth() { return max.getZ() - min.getZ() + 1; } // 定义 depth 的调用入口；返回 max.getZ() - min.getZ() + 1，将当前分支的结果交给调用者。
    public int volume() { return width() * height() * depth(); } // 定义 volume 的调用入口；返回 width() * height() * depth()，将当前分支的结果交给调用者。
    public boolean loaded(Level level) { return level.hasChunksAt(min.offset(-1, -1, -1), max.offset(1, 0, 1)); } // 定义 loaded 的调用入口；返回 level.hasChunksAt(min.offset(-1, -1, -1), max.offset(1, 0, 1))，将当前分支的结果交给调用者。

    public record Detection(SmelteryStructure structure, boolean unloaded) {} // 定义 Detection 的数据记录，具体职责见上方说明。

    public static Detection detect(Level level, BlockPos controller) { // 定义 detect 的调用入口，参数和具体处理见下方实现。
        var scan = new Scan(level, controller); // 设置 scan 为 new Scan(level, controller)。
        SmelteryStructure result = scan.detect(); // 设置 result 为 scan.detect()。
        return new Detection(result, scan.unloaded); // 返回 new Detection(result, scan.unloaded)，将当前分支的结果交给调用者。
    } // 结束当前作用域。

    private static class Scan { // 定义 Scan 的实现类型，具体职责见上方说明。
        private final Level level; // 声明 level，保存后续流程使用的状态或依赖。
        private final BlockPos controller; // 声明 controller，保存后续流程使用的状态或依赖。
        private boolean unloaded; // 声明 unloaded，保存后续流程使用的状态或依赖。
        Scan(Level level, BlockPos controller) { this.level = level; this.controller = controller; } // 调用 Scan，执行当前流程所需的处理。
        private boolean loaded(BlockPos pos) { // 定义 loaded 的调用入口，参数和具体处理见下方实现。
            if (level.isOutsideBuildHeight(pos)) return false; // 检查 level.isOutsideBuildHeight(pos)；按条件决定是否执行本分支。
            if (!level.hasChunkAt(pos)) { unloaded = true; return false; } // 检查 !level.hasChunkAt(pos)；按条件决定是否执行本分支。
            return true; // 返回 true，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        private boolean air(BlockPos pos) { return loaded(pos) && level.getBlockState(pos).isAir(); } // 定义 air 的调用入口；返回 loaded(pos) && level.getBlockState(pos).isAir()，将当前分支的结果交给调用者。
        private boolean member(BlockPos pos, boolean floor) { // 定义 member 的调用入口，参数和具体处理见下方实现。
            if (!loaded(pos)) return false; // 检查 !loaded(pos)；按条件决定是否执行本分支。
            if (pos.equals(controller)) return !floor; // 检查 pos.equals(controller)；按条件决定是否执行本分支。
            return level.getBlockState(pos).is(floor ? SmelteryRegistration.FLOOR : SmelteryRegistration.WALL) // 返回 level.getBlockState(pos).is(floor ? SmelteryRegistration.FLOOR : SmelteryRegistration.WALL)，将当前分支的结果交给调用者。
                    && level.getBlockEntity(pos) instanceof SmelteryComponentBlockEntity part && part.canJoin(controller); // 调用 getBlockEntity，执行当前流程所需的处理。
        } // 结束当前作用域。
        private BlockPos edge(BlockPos start, Direction direction) { // 定义 edge 的调用入口，参数和具体处理见下方实现。
            BlockPos cursor = start; // 设置 cursor 为 start。
            for (int i = 0; i <= MAX_INNER_SIZE && air(cursor); i++) cursor = cursor.relative(direction); // 遍历 int i = 0; i <= MAX_INNER_SIZE && air(cursor); i++) cursor = cursor.relative(direction，逐项执行当前处理。
            return cursor; // 返回 cursor，将当前分支的结果交给调用者。
        } // 结束当前作用域。
        SmelteryStructure detect() { // 调用 detect，执行当前流程所需的处理。
            var state = level.getBlockState(controller); // 设置 state 为 level.getBlockState(controller)。
            BlockPos inside = controller.relative(state.getValue(SmelteryControllerBlock.FACING).getOpposite()); // 设置 inside 为 controller.relative(state.getValue(SmelteryControllerBlock.FACING).getOpposite())。
            if (!air(inside)) return null; // 检查 !air(inside)；按条件决定是否执行本分支。
            // 世界高度边界和 loaded() 已限制扫描范围，额外设置 64 格
            // 下降上限会错误拒绝向上扫描原本允许的高大结构。
            while (air(inside.below())) inside = inside.below(); // 在 air(inside.below())) inside = inside.below( 成立时继续处理。
            int minX = edge(inside, Direction.WEST).getX() + 1; // 设置 minX 为 edge(inside, Direction.WEST).getX() + 1。
            int maxX = edge(inside, Direction.EAST).getX() - 1; // 设置 maxX 为 edge(inside, Direction.EAST).getX() - 1。
            int minZ = edge(inside, Direction.NORTH).getZ() + 1; // 设置 minZ 为 edge(inside, Direction.NORTH).getZ() + 1。
            int maxZ = edge(inside, Direction.SOUTH).getZ() - 1; // 设置 maxZ 为 edge(inside, Direction.SOUTH).getZ() - 1。
            int width = maxX - minX + 1, depth = maxZ - minZ + 1; // 设置 width 为 maxX - minX + 1, depth = maxZ - minZ + 1。
            if (width < 1 || width > MAX_INNER_SIZE || depth < 1 || depth > MAX_INNER_SIZE) return null; // 检查 width < 1 || width > MAX_INNER_SIZE || depth < 1 || depth > MAX_INNER_SIZE；按条件决定是否执行本分支。
            // 控制器必须位于侧壁，不能放在被忽略的角落或炉底。
            boolean onX = (controller.getX() == minX - 1 || controller.getX() == maxX + 1) && controller.getZ() >= minZ && controller.getZ() <= maxZ; // 设置 onX 为 (controller.getX() == minX - 1 || controller.getX() == maxX + 1) && controller.getZ() >= minZ && controller…。
            boolean onZ = (controller.getZ() == minZ - 1 || controller.getZ() == maxZ + 1) && controller.getX() >= minX && controller.getX() <= maxX; // 设置 onZ 为 (controller.getZ() == minZ - 1 || controller.getZ() == maxZ + 1) && controller.getX() >= minX && controller…。
            if (!onX && !onZ) return null; // 检查 !onX && !onZ；按条件决定是否执行本分支。
            List<BlockPos> members = new ArrayList<>(), tanks = new ArrayList<>(); // 设置 members 为 new ArrayList<>(), tanks = new ArrayList<>()。
            int bottom = inside.getY(); // 设置 bottom 为 inside.getY()。
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) { // 遍历 int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++，逐项执行当前处理。
                BlockPos pos = new BlockPos(x, bottom - 1, z); // 设置 pos 为 new BlockPos(x, bottom - 1, z)。
                if (!member(pos, true)) return null; // 检查 !member(pos, true)；按条件决定是否执行本分支。
                members.add(pos); // 向集合或构建器追加条目。
            } // 结束当前作用域。
            int top = bottom - 1; // 设置 top 为 bottom - 1。
            for (int y = bottom; y < level.getMaxBuildHeight(); y++) { // 遍历 int y = bottom; y < level.getMaxBuildHeight(); y++，逐项执行当前处理。
                List<BlockPos> layer = new ArrayList<>(); // 设置 layer 为 new ArrayList<>()。
                boolean valid = true; // 设置 valid 为 true。
                for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) valid &= air(new BlockPos(x, y, z)); // 遍历 int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) valid &= air(new BlockPos(x, y, z)，逐项执行当前处理。
                for (int x = minX; x <= maxX; x++) { layer.add(new BlockPos(x, y, minZ - 1)); layer.add(new BlockPos(x, y, maxZ + 1)); } // 遍历 int x = minX; x <= maxX; x++) { layer.add(new BlockPos(x, y, minZ - 1)); layer.add(new BlockPos(x, y, maxZ …，逐项执行当前处理。
                for (int z = minZ; z <= maxZ; z++) { layer.add(new BlockPos(minX - 1, y, z)); layer.add(new BlockPos(maxX + 1, y, z)); } // 遍历 int z = minZ; z <= maxZ; z++) { layer.add(new BlockPos(minX - 1, y, z)); layer.add(new BlockPos(maxX + 1, y…，逐项执行当前处理。
                for (BlockPos pos : layer) valid &= member(pos, false); // 遍历 BlockPos pos : layer) valid &= member(pos, false，逐项执行当前处理。
                if (!valid) break; // 检查 !valid；按条件决定是否执行本分支。
                members.addAll(layer); // 合并当前批次的全部条目。
                for (BlockPos pos : layer) if (level.getBlockState(pos).is(SmelteryRegistration.SEARED_TANK.get())) tanks.add(pos); // 遍历 BlockPos pos : layer) if (level.getBlockState(pos).is(SmelteryRegistration.SEARED_TANK.get())) tanks.add(pos，逐项执行当前处理。
                top = y; // 设置 top 为 y。
            } // 结束当前作用域。
            if (top < controller.getY() || tanks.isEmpty() || unloaded) return null; // 检查 top < controller.getY() || tanks.isEmpty() || unloaded；按条件决定是否执行本分支。
            return new SmelteryStructure(new BlockPos(minX, bottom, minZ), new BlockPos(maxX, top, maxZ), List.copyOf(members), List.copyOf(tanks)); // 返回 new SmelteryStructure(new BlockPos(minX, bottom, minZ), new BlockPos(maxX, top, maxZ), List.copyOf(members)…，将当前分支的结果交给调用者。
        } // 结束当前作用域。
    } // 结束当前作用域。
} // 结束当前作用域。
