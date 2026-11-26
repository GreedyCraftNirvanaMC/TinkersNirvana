/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

/** 每个额外方块都走原版玩家破坏流程，包括领地保护、掉落、事件和耐久。 */
@EventBusSubscriber(modid = TinkersNirvana.MODID)
public final class ToolHarvest {
    private static final ThreadLocal<Boolean> HARVESTING = ThreadLocal.withInitial(() -> false);
    private static final Map<ServerPlayer, ArrayDeque<TreeHarvest>> TREES = new IdentityHashMap<>();
    private static final ThreadLocal<net.minecraft.core.BlockPos> REPLANTING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SEED_RESERVED = ThreadLocal.withInitial(() -> false);
    private ToolHarvest() {}
    public static void afterBreak(AntiqueTool item, ItemStack tool, Level level, BlockState original, BlockPos pos, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || HARVESTING.get() || REPLANTING.get() != null
                || !minesArea(tool) || !canExpand(tool, original)) {
            return;
        }
        var action = item.definition().action();
        boolean expandedWidth = ToolModifiers.level(tool, "harvestwidth") > 0;
        boolean expandedHeight = ToolModifiers.level(tool, "harvestheight") > 0;
        HARVESTING.set(true);
        try {
            if (action == ToolDefinition.Action.LUMBERAXE && isTree(level, pos)) {
                InteractionHand hand = DualToolHarvest.active(player) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
                var task = new TreeHarvest(player.serverLevel(), tool, hand, expandedWidth || expandedHeight ? 4 : 1);
                task.addBranches(pos);
                if (!task.queue.isEmpty()) {
                    TREES.computeIfAbsent(player, ignored -> new ArrayDeque<>()).addLast(task);
                }
                return;
            }
            HitResult hit = player.pick(player.blockInteractionRange(), 1, false);
            BlockHitResult blockHit = hit instanceof BlockHitResult block ? block : null;
            for (BlockPos next : extraBlocks(player, tool, pos, original, blockHit)) {
                harvest(player, tool, next, pos, original);
            }
        } finally {
            HARVESTING.remove();
        }
    }

    static boolean supportsExpanders(ToolDefinition.Action action) {
        return switch (action) {
            case PICKAXE, SHOVEL, AXE, MATTOCK, KAMA, HAMMER, EXCAVATOR, LUMBERAXE, SCYTHE -> true;
            default -> false;
        };
    }

    public static boolean minesArea(ItemStack stack) {
        // 怀古的鹤嘴锹可以扩展铺路或耕地，左键每次只挖一格。
        return stack.getItem() instanceof AntiqueTool tool && supportsExpanders(tool.definition().action())
                && tool.definition().action() != ToolDefinition.Action.MATTOCK;
    }

    private static boolean canExpand(ItemStack stack, BlockState origin) {
        return stack.getItem() instanceof AntiqueTool tool && supportsExpanders(tool.definition().action())
                && !TinkerTools.isBroken(stack) && !origin.isAir() && DualToolHarvest.effective(stack, origin);
    }

    public static List<BlockPos> extraBlocks(Player player, ItemStack stack, BlockPos origin, BlockState original, BlockHitResult blockHit) {
        if (!canExpand(stack, original)) { return List.of(); }
        var action = ((AntiqueTool) stack.getItem()).definition().action();
        boolean wide = ToolModifiers.level(stack, "harvestwidth") > 0;
        boolean tall = ToolModifiers.level(stack, "harvestheight") > 0;
        boolean large = switch (action) {
            case HAMMER, EXCAVATOR, LUMBERAXE, SCYTHE -> true;
            default -> false;
        };
        if (!large && !wide && !tall) { return List.of(); }
        int width = (large ? 3 : 1) + (wide ? (large ? 2 : 1) : 0);
        int height = (large ? 3 : 1) + (tall ? (large ? 2 : 1) : 0);
        if (action == ToolDefinition.Action.MATTOCK) { width = height = width + height - 1; }
        int depth = action == ToolDefinition.Action.LUMBERAXE || action == ToolDefinition.Action.SCYTHE ? 3 : 1;
        int distance = large && (wide || tall) ? 3 : 0;
        if (blockHit != null && (blockHit.getType() != HitResult.Type.BLOCK || !blockHit.getBlockPos().equals(origin))) {
            blockHit = null;
        }
        Direction side = blockHit == null ? player.getDirection().getOpposite() : blockHit.getDirection();
        Vec3 offset = blockHit == null ? new Vec3(.5, .5, .5) : blockHit.getLocation().subtract(Vec3.atLowerCornerOf(origin));
        Level level = player.level();
        List<BlockPos> extra = new ArrayList<>();
        for (BlockPos next : harvestBounds(origin, side, offset, player.getDirection(), width, height, depth)) {
            if (next.equals(origin) || distance > 0 && next.distManhattan(origin) > distance || !level.hasChunkAt(next)) {
                continue;
            }
            BlockState state = level.getBlockState(next);
            if (!state.isAir() && DualToolHarvest.effective(stack, state)) { extra.add(next); }
        }
        return extra;
    }

    private static Iterable<BlockPos> harvestBounds(BlockPos origin, Direction side, Vec3 offset, Direction horizontal,
                                                    int width, int height, int depth) {
        int x = side.getAxis() == Direction.Axis.X ? -side.getStepX() * depth
                : side.getAxis() == Direction.Axis.Y ? horizontal.getStepX() * height + horizontal.getStepZ() * width : width;
        int y = side.getAxis() == Direction.Axis.Y ? -side.getStepY() * depth : height;
        int z = side.getAxis() == Direction.Axis.Z ? -side.getStepZ() * depth
                : side.getAxis() == Direction.Axis.Y ? horizontal.getStepX() * width + horizontal.getStepZ() * height : width;
        // 偶数宽高没有中心格，按玩家击中的半侧决定向哪一边扩展。
        int startX = side.getAxis() == Direction.Axis.X ? 0 : startOffset(x, offset.x);
        int startY = side.getAxis() == Direction.Axis.Y ? 0 : startOffset(y, offset.y);
        int startZ = side.getAxis() == Direction.Axis.Z ? 0 : startOffset(z, offset.z);
        List<BlockPos> positions = new ArrayList<>();
        // 保留怀古面向顺序，耐久不足时先处理排在前面的额外方块。
        for (int dx = 0; dx != x; dx += Integer.signum(x)) {
            for (int dy = 0; dy != y; dy += Integer.signum(y)) {
                for (int dz = 0; dz != z; dz += Integer.signum(z)) {
                    positions.add(origin.offset(startX + dx, startY + dy, startZ + dz));
                }
            }
        }
        return positions;
    }

    private static int startOffset(int size, double hit) {
        int step = Integer.signum(size);
        boolean nearEnd = step > 0 ? hit > .5 : hit < .5;
        return -size / 2 + (size % 2 == 0 && nearEnd ? step : 0);
    }

    private static boolean isTree(Level level, BlockPos origin) {
        BlockPos aboveTrunk = null;
        ArrayDeque<BlockPos> candidates = new ArrayDeque<>();
        candidates.push(origin);
        while (!candidates.isEmpty()) {
            BlockPos candidate = candidates.pop();
            if ((aboveTrunk == null || candidate.getY() > aboveTrunk.getY()) && isLog(level, candidate)) {
                aboveTrunk = candidate.above();
                while (isLog(level, aboveTrunk)) {
                    aboveTrunk = aboveTrunk.above();
                }
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    candidates.push(aboveTrunk.relative(direction));
                }
            }
        }
        if (aboveTrunk == null) {
            return false;
        }
        // 树冠附近至少有五片树叶才启动连锁砍伐，避免把建筑中的整排原木当树。
        int leaves = 0;
        for (BlockPos pos : BlockPos.betweenClosed(aboveTrunk.offset(-1, -1, -1), aboveTrunk.offset(1, 1, 1))) {
            if (level.hasChunkAt(pos) && level.getBlockState(pos).is(BlockTags.LEAVES) && ++leaves >= 5) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLog(Level level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.hasChunkAt(pos) && level.getBlockState(pos).is(BlockTags.LOGS);
    }

    public static boolean modifyGround(AntiqueTool item, UseOnContext context, ItemAbility ability) {
        if (context.getClickedFace() == Direction.DOWN) { return false; }
        var level = context.getLevel();
        var player = context.getPlayer();
        var stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        boolean path = ability == ItemAbilities.SHOVEL_FLATTEN;
        var hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside());
        var extras = !path || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH)
                ? extraBlocks(player, stack, pos, state, hit) : List.<BlockPos>of();
        boolean changed = modifyGroundAt(item, context, ability);
        for (BlockPos extra : extras) {
            if (TinkerTools.isBroken(stack) || stack.isEmpty()) { break; }
            if (path && !level.getBlockState(extra).is(Blocks.GRASS_BLOCK)) { continue; }
            var extraHit = new BlockHitResult(hit.getLocation().add(extra.getX() - pos.getX(), extra.getY() - pos.getY(), extra.getZ() - pos.getZ()),
                    hit.getDirection(), extra, hit.isInside());
            if (!player.mayInteract(level, extra) || !player.mayUseItemAt(extra, hit.getDirection(), stack)) { continue; }
            // 中心格已由原版交互派发右键事件，附带方块也要让保护模组有机会拒绝。
            if (!level.isClientSide) {
                var event = CommonHooks.onRightClickBlock(player, context.getHand(), extra, extraHit);
                if (event.isCanceled() || event.getUseItem() == TriState.FALSE) { continue; }
            }
            changed |= modifyGroundAt(item, new UseOnContext(level, player, context.getHand(), stack, extraHit), ability);
        }
        return changed;
    }

    private static boolean modifyGroundAt(AntiqueTool item, UseOnContext context, ItemAbility ability) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        boolean path = ability == ItemAbilities.SHOVEL_FLATTEN;
        if (path && !level.isEmptyBlock(pos.above())) { return false; }
        var state = level.getBlockState(pos);
        var next = state.getToolModifiedState(context, ability, level.isClientSide);
        if (next == null || next == state) { return false; }
        if (!level.isClientSide) {
            if (!level.setBlock(pos, next, 11)) { return false; }
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), next));
            item.damage(context.getItemInHand(), 1, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
        }
        level.playSound(context.getPlayer(), pos, path ? SoundEvents.SHOVEL_FLATTEN : SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public static boolean harvestCrops(AntiqueTool item, ItemStack tool, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        BlockState center = level.getBlockState(origin);
        if (context.getPlayer() == null || !isCrop(center) && !item.matchesHarvestCategory(center)) {
            return false;
        }
        boolean scythe = item.definition().action() == ToolDefinition.Action.SCYTHE;
        boolean wide = ToolModifiers.level(tool, "harvestwidth") > 0;
        boolean tall = ToolModifiers.level(tool, "harvestheight") > 0;
        int width = scythe ? (wide ? 5 : 3) : (wide ? 2 : 1);
        int height = scythe ? (tall ? 5 : 3) : (tall ? 2 : 1);
        Vec3 offset = context.getClickLocation().subtract(Vec3.atLowerCornerOf(origin));
        var positions = new ArrayList<BlockPos>();
        for (BlockPos candidate : harvestBounds(origin, context.getClickedFace(), offset, context.getHorizontalDirection(), width, height, scythe ? 3 : 1)) {
            if (!candidate.equals(origin) && (!scythe || (!wide && !tall) || candidate.distManhattan(origin) <= 3)
                    && level.hasChunkAt(candidate) && canHarvestCrop(level, candidate, level.getBlockState(candidate))) {
                positions.add(candidate);
            }
        }
        if (canHarvestCrop(level, origin, center)) { positions.add(origin); }
        if (positions.isEmpty()) { return false; }
        if (level.isClientSide) { return true; }
        if (!(context.getPlayer() instanceof ServerPlayer player)) { return false; }
        java.util.function.Supplier<Boolean> action = () -> {
            boolean harvested = false;
            for (BlockPos candidate : positions) {
                harvested |= harvestCrop(player, item, tool, candidate);
            }
            return harvested;
        };
        boolean harvested = context.getHand() == InteractionHand.OFF_HAND ? DualToolHarvest.withOffhandTool(player, action) : action.get();
        if (harvested) {
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1, 1);
            player.sweepAttack();
        }
        return harvested;
    }

    private static boolean isCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock || state.is(Blocks.NETHER_WART) || state.is(Blocks.SUGAR_CANE);
    }

    private static boolean canHarvestCrop(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.is(Blocks.NETHER_WART)) {
            return state.getValue(NetherWartBlock.AGE) == 3;
        }
        return state.is(Blocks.SUGAR_CANE) && level.getBlockState(pos.below()).is(Blocks.SUGAR_CANE);
    }

    private static boolean harvestCrop(ServerPlayer player, AntiqueTool item, ItemStack tool, BlockPos pos) {
        Level level = player.level();
        if (TinkerTools.isBroken(tool) || player.getMainHandItem() != tool || !level.hasChunkAt(pos)
                || !player.mayInteract(level, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (!canHarvestCrop(level, pos, state)) {
            return false;
        }
        REPLANTING.set(pos);
        SEED_RESERVED.set(false);
        try {
            if (!player.gameMode.destroyBlock(pos) || !level.isEmptyBlock(pos)) {
                return false;
            }
            if (state.getDestroySpeed(level, pos) == 0) {
                item.damage(tool, 1, player, EquipmentSlot.MAINHAND);
            }
            if (SEED_RESERVED.get()) {
                BlockState seedling = state.getBlock() instanceof CropBlock crop ? crop.getStateForAge(0)
                        : state.setValue(NetherWartBlock.AGE, 0);
                if (seedling.canSurvive(level, pos)) {
                    level.setBlockAndUpdate(pos, seedling);
                } else {
                    net.minecraft.world.level.block.Block.popResource(level, pos, seedling.getBlock().getCloneItemStack(level, pos, seedling));
                }
            }
            return true;
        } finally {
            REPLANTING.remove();
            SEED_RESERVED.remove();
        }
    }

    public static void reserveSeed(net.neoforged.neoforge.event.level.BlockDropsEvent event) {
        if (!event.getPos().equals(REPLANTING.get())) {
            return;
        }
        ItemStack seed;
        if (event.getState().getBlock() instanceof CropBlock crop) {
            seed = crop.getCloneItemStack(event.getLevel(), event.getPos(), event.getState());
        } else if (event.getState().is(Blocks.NETHER_WART)) {
            seed = new ItemStack(net.minecraft.world.item.Items.NETHER_WART);
        } else {
            return;
        }
        for (var iterator = event.getDrops().iterator(); iterator.hasNext();) {
            ItemStack drop = iterator.next().getItem();
            if (drop.is(seed.getItem()) && !drop.isEmpty()) {
                drop.shrink(1);
                if (drop.isEmpty()) { iterator.remove(); }
                SEED_RESERVED.set(true);
                return;
            }
        }
    }
    @SubscribeEvent
    private static void tickTrees(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var tasks = TREES.get(player);
        if (tasks == null) {
            return;
        }
        TreeHarvest task = tasks.getFirst();
        if (task.level != player.level() || !player.isAlive() || player.isSpectator()
                || player.getItemInHand(task.hand) != task.tool || TinkerTools.isBroken(task.tool) || task.tool.isEmpty()) {
            tasks.removeFirst();
        } else {
            // 每刻处理有限数量的原木。副手伐木也沿用原版破坏事件和已有的副手耐久逻辑。
            HARVESTING.set(true);
            try {
                if (task.hand == InteractionHand.OFF_HAND) {
                    DualToolHarvest.withOffhandTool(player, () -> {
                        task.step(player);
                        return null;
                    });
                } else {
                    task.step(player);
                }
            } finally {
                HARVESTING.remove();
            }
            if (task.queue.isEmpty()) {
                tasks.removeFirst();
            }
        }
        if (tasks.isEmpty()) {
            TREES.remove(player);
        }
    }

    @SubscribeEvent
    private static void stopTrees(PlayerEvent.PlayerLoggedOutEvent event) {
        TREES.remove(event.getEntity());
    }

    @SubscribeEvent
    private static void stopTrees(ServerStoppingEvent event) {
        TREES.keySet().removeIf(player -> player.getServer() == event.getServer());
    }

    private static final class TreeHarvest {
        private final ServerLevel level;
        private final ItemStack tool;
        private final InteractionHand hand;
        private final int blocksPerTick;
        private final ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        private final Set<BlockPos> visited = new HashSet<>();

        private TreeHarvest(ServerLevel level, ItemStack tool, InteractionHand hand, int blocksPerTick) {
            this.level = level;
            this.tool = tool;
            this.hand = hand;
            this.blocksPerTick = blocksPerTick;
        }

        private void addBranches(BlockPos origin) {
            visited.add(origin);
            for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-1, 0, -1), origin.offset(1, 1, 1))) {
                if (isLog(level, candidate)) {
                    BlockPos pos = candidate.immutable();
                    if (visited.add(pos)) {
                        queue.addLast(pos);
                    }
                }
            }
        }

        private void step(ServerPlayer player) {
            for (int i = 0; i < blocksPerTick && !queue.isEmpty() && !TinkerTools.isBroken(tool) && !tool.isEmpty(); i++) {
                BlockPos pos = queue.removeFirst();
                if (isLog(level, pos) && harvest(player, tool, pos, pos, level.getBlockState(pos))) {
                    addBranches(pos);
                }
            }
        }
    }

    private static boolean harvest(ServerPlayer player, ItemStack tool, BlockPos pos, BlockPos origin, BlockState original) {
        if (TinkerTools.isBroken(tool) || player.getMainHandItem() != tool || !player.level().hasChunkAt(pos)) { return false; }
        BlockState state = player.level().getBlockState(pos);
        float hardness = state.getDestroySpeed(player.level(), pos);
        if (state.isAir() || state.hasBlockEntity() || hardness < 0 || !DualToolHarvest.effective(tool, state)
                || !state.canHarvestBlock(player.level(), pos, player) || !player.mayInteract(player.level(), pos)) {
            return false;
        }
        // 比较相对挖掘时间，避免低速工具借范围挖掘秒破远比中心方块难挖的方块。
        if (hardness > 0 && original.getDestroyProgress(player, player.level(), origin)
                > 10 * state.getDestroyProgress(player, player.level(), pos)) {
            return false;
        }
        return player.gameMode.destroyBlock(pos);
    }
}
