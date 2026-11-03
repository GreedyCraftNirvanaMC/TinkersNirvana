/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's DualToolHarvestUtils. */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.tools.tools.AntiqueTool;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Supplier;

/** Scope native harvesting to the effective offhand tool, restoring hands even on cancellation or exceptions. */
public final class DualToolHarvest {
    private static final ThreadLocal<Set<Player>> ACTIVE = new ThreadLocal<>();
    private DualToolHarvest() {}
    private static boolean effective(ItemStack stack, BlockState state) {
        if (stack.isEmpty() || TinkerTools.isBroken(stack)) { return false; }
        if (ToolModifiers.level(stack, "blasting") > 0 && !state.requiresCorrectToolForDrops() && state.getFluidState().isEmpty()) { return true; }
        return stack.getDestroySpeed(state) > 1 || stack.isCorrectToolForDrops(state);
    }
    public static boolean shouldUseOffhand(Player player, BlockState state) {
        ItemStack main = player.getMainHandItem();
        return main.getItem() instanceof AntiqueTool tool && !tool.definition().effectiveBlocks().isEmpty()
                && !effective(main, state) && effective(player.getOffhandItem(), state);
    }
    public static boolean active(Player player) { var active = ACTIVE.get(); return active != null && active.contains(player); }
    public static <T> T withTool(Player player, BlockState state, Supplier<T> action) {
        if (player == null || active(player) || !shouldUseOffhand(player, state)) { return action.get(); }
        int selected = player.getInventory().selected;
        ItemStack main = player.getInventory().items.get(selected), off = player.getInventory().offhand.getFirst();
        if (ACTIVE.get() == null) { ACTIVE.set(Collections.newSetFromMap(new IdentityHashMap<>())); }
        ACTIVE.get().add(player);
        player.getInventory().items.set(selected, off); player.getInventory().offhand.set(0, main);
        try { return action.get(); }
        finally {
            ItemStack used = player.getInventory().items.get(selected), other = player.getInventory().offhand.getFirst();
            player.getInventory().items.set(selected, other); player.getInventory().offhand.set(0, used);
            var active = ACTIVE.get(); active.remove(player); if (active.isEmpty()) { ACTIVE.remove(); }
        }
    }
    public static <T> T withTool(Player player, BlockPos pos, Supplier<T> action) { return player == null ? action.get() : withTool(player, player.level().getBlockState(pos), action); }
}
