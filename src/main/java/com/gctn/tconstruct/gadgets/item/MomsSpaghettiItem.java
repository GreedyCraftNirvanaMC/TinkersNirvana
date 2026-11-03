/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's ItemMomsSpaghetti. */
package com.gctn.tconstruct.gadgets.item;

import com.gctn.tconstruct.init.TinkerDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class MomsSpaghettiItem extends Item {
    private static final FoodProperties[] FOOD = new FoodProperties[3];
    static { for (int i = 0; i < FOOD.length; i++) { int nutrition = i == 2 ? 3 : 2; FOOD[i] = new FoodProperties(nutrition, nutrition * (i == 0 ? .2F : .4F) * 2, false, .5F, java.util.Optional.empty(), List.of()); } }
    public MomsSpaghettiItem() { super(new Properties().durability(100).food(FOOD[0])); }
    public static int seasoning(ItemStack stack) {
        var data = TinkerDataComponents.getMaterialTag(stack).getCompound("Modifiers");
        return data.getInt("spaghetti_meat") > 0 ? 2 : data.getInt("spaghetti_sauce") > 0 ? 1 : 0;
    }
    @Override public FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) { return FOOD[seasoning(stack)]; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return stack.getDamageValue() >= stack.getMaxDamage() ? InteractionResultHolder.fail(stack) : super.use(level, player, hand);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (stack.getDamageValue() < stack.getMaxDamage()) {
            entity.eat(level, stack.copyWithCount(1), getFoodProperties(stack, entity));
            stack.setDamageValue(stack.getDamageValue() + 1);
        }
        return stack;
    }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public boolean isValidRepairItem(ItemStack stack, ItemStack repair) { return false; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tconstruct.spaghetti.uses", Math.max(0, stack.getMaxDamage() - stack.getDamageValue()), stack.getMaxDamage()));
        if (seasoning(stack) > 0) { tooltip.add(Component.translatable("modifier.tconstruct.spaghetti_sauce")); }
        if (seasoning(stack) > 1) { tooltip.add(Component.translatable("modifier.tconstruct.spaghetti_meat")); }
        tooltip.add(Component.translatable("tooltip.tconstruct.spaghetti"));
    }
}
