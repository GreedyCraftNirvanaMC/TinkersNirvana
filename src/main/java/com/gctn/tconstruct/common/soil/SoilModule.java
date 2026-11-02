/*
 * 根据 TinkersAntique 的土壤与史莱姆水晶注册移植。
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package com.gctn.tconstruct.common.soil;

import com.gctn.tconstruct.init.BlockRegister;
import com.gctn.tconstruct.init.ItemRegister;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/** 砖泥沿用冶炼模块的物品身份，其余土壤与水晶在这里注册。 */
public final class SoilModule {
    public static final Map<String, DeferredBlock<AntiqueSoilBlock>> BLOCKS = new LinkedHashMap<>();
    public static final Map<String, Supplier<Item>> CRYSTALS = new LinkedHashMap<>();

    static {
        register("slimy_mud_green", AntiqueSoilBlock.Kind.SLIMY_MUD_GREEN);
        register("slimy_mud_blue", AntiqueSoilBlock.Kind.SLIMY_MUD_BLUE);
        register("graveyard_soil", AntiqueSoilBlock.Kind.GRAVEYARD);
        register("consecrated_soil", AntiqueSoilBlock.Kind.CONSECRATED);
        register("slimy_mud_magma", AntiqueSoilBlock.Kind.SLIMY_MUD_MAGMA);
        for (String color : new String[]{"green", "blue", "magma"}) {
            CRYSTALS.put(color, ItemRegister.ITEMS.register("slimecrystal_" + color, () -> new Item(new Item.Properties())));
        }
    }

    private SoilModule() {
    }

    private static void register(String name, AntiqueSoilBlock.Kind kind) {
        BLOCKS.put(name, BlockRegister.registerBlockWithItem(name, () ->
                new AntiqueSoilBlock(kind, AntiqueSoilBlock.soilProperties())));
    }

    public static Collection<DeferredBlock<?>> entries() {
        return new ArrayList<>(BLOCKS.values());
    }
}
