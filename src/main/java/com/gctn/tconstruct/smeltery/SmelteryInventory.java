/*
 * 根据 TinkersAntique 的多方块物品保存行为移植，并适配现代物品组件。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源说明见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.smeltery;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** 原生容器只保存一个字节的槽号，大型多方块必须使用整数避免覆盖其他物品。 */
public final class SmelteryInventory {
    private SmelteryInventory() {
    }

    public static void save(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", slot);
                list.add(stack.save(registries, entry));
            }
        }
        tag.put("Items", list);
    }

    public static void load(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        for (Tag value : tag.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (!entry.contains("Slot", Tag.TAG_ANY_NUMERIC)) {
                continue;
            }
            // 兼容旧存档的无符号字节槽号，包括保存为负数的 128～255 槽。
            int slot = entry.contains("Slot", Tag.TAG_BYTE) ? entry.getByte("Slot") & 255 : entry.getInt("Slot");
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.parseOptional(registries, entry));
            }
        }
    }
}
