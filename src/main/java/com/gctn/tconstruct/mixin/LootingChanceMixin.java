/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.tools.ToolModifiers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootItemRandomChanceWithEnchantedBonusCondition.class)
abstract class LootingChanceMixin {
    @WrapOperation(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int tconsnirvana$projectileLooting(Holder<Enchantment> enchantment, LivingEntity attacker,
                                              Operation<Integer> original, LootContext context) {
        return ToolModifiers.projectileLooting(enchantment, original.call(enchantment, attacker), context);
    }
}
