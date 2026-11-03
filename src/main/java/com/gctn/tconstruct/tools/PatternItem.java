/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.init.ItemRegister;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class PatternItem extends Item {
    public static final net.neoforged.neoforge.registries.DeferredItem<PatternItem> PATTERN =
            ItemRegister.ITEMS.register("pattern", PatternItem::new);
    public PatternItem() { super(new Properties()); }
    public static void init() {}

    public static AntiqueParts.Definition selected(ItemStack stack) {
        String id = TinkerDataComponents.getMaterialTag(stack).getString("Pattern");
        AntiqueParts.Definition part = AntiqueParts.get(id);
        return part == null ? AntiqueParts.get("pickaxe_head") : part;
    }

    public static boolean isBlank(ItemStack stack) { return AntiqueParts.get(TinkerDataComponents.getMaterialTag(stack).getString("Pattern")) == null; }

    @Override
    public Component getName(ItemStack stack) {
        if (isBlank(stack)) { return Component.translatable("item.tconstruct.pattern.blank"); }
        return Component.translatable("item.tconstruct.pattern",
                Component.translatable("item.tconstruct." + selected(stack).id() + ".name"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            var parts = new ArrayList<>(AntiqueParts.entries().values());
            parts.removeIf(part -> part.id().equals("bolt_core"));
            int index = Math.floorMod(parts.indexOf(selected(stack)) + (player.isShiftKeyDown() ? -1 : 1), parts.size());
            var part = parts.get(index);
            CompoundTag data = TinkerDataComponents.getMaterialTag(stack);
            data.putString("Pattern", part.id());
            stack.set(TinkerDataComponents.TINKER_MATERIAL.get(), CustomData.of(data));
            player.displayClientMessage(Component.translatable("message.tconstruct.pattern_selected",
                    Component.translatable("item.tconstruct." + part.id() + ".name")), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!isBlank(stack)) { tooltip.add(Component.translatable("tooltip.tconstruct.pattern_part",
                Component.translatable("item.tconstruct." + selected(stack).id() + ".name"))); }
        tooltip.add(Component.translatable("tooltip.tconstruct.pattern_usage"));
    }
}
