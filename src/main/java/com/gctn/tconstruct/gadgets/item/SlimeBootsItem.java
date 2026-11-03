/* SPDX-License-Identifier: LGPL-3.0-only; Antique's zero-armor, unbreakable slime boots. */
package com.gctn.tconstruct.gadgets.item;

import com.gctn.tconstruct.gadgets.SlimeGear;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class SlimeBootsItem extends ArmorItem {
    public final String color;
    public SlimeBootsItem(String color) { super(SlimeGear.SLIME_ARMOR, Type.BOOTS, new Properties().stacksTo(1)); this.color = color; }
    @Override public ItemAttributeModifiers getDefaultAttributeModifiers() { return ItemAttributeModifiers.EMPTY; }
}
