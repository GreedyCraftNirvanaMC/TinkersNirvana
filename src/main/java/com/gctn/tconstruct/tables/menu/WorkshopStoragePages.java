/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.tables.menu;

import java.util.function.IntPredicate;

/** 打开菜单时将物品集中显示，保留方块实体的原始槽位。 */
final class WorkshopStoragePages {
    private final int[] slots;

    WorkshopStoragePages(int size, IntPredicate occupied) {
        slots = new int[size];
        int used = 0;
        int empty = size;
        for (int slot = 0; slot < size; slot++) {
            if (occupied.test(slot)) { slots[used++] = slot; }
            else { slots[--empty] = slot; }
        }
        // 空槽也按原来的顺序呈现，方便按正常的槽位顺序插入物品。
        for (int left = used, right = size - 1; left < right; left++, right--) {
            int slot = slots[left];
            slots[left] = slots[right];
            slots[right] = slot;
        }
    }

    int index(int displaySlot) { return slots[displaySlot]; }

    int pageCount(int pageSize, IntPredicate occupied) {
        int occupiedCount = 0;
        int last = 0;
        for (int index = 0; index < slots.length; index++) {
            if (occupied.test(slots[index])) {
                occupiedCount++;
                last = index;
            }
        }
        int pages = last / pageSize + 1;
        return occupiedCount == pages * pageSize && occupiedCount < slots.length ? pages + 1 : pages;
    }
}
