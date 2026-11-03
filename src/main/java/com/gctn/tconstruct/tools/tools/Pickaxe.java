package com.gctn.tconstruct.tools.tools;

import com.gctn.tconstruct.library.materials.Material;
import com.gctn.tconstruct.tools.AntiqueTools;
import com.gctn.tconstruct.tools.ToolBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import java.util.List;

/** 保留原物品 ID 和组装方法，已有工具继续使用原来的材料键。 */
public class Pickaxe extends AntiqueTool {
    public static final DeferredItem<Item> PICKAXE = ITEMS.register("pickaxe/pickaxe", Pickaxe::new);
    public static void init() {}
    public Pickaxe() { super(AntiqueTools.PICKAXE); }
    public static ItemStack initPickaxe(Material handle, Material head, Material binding) {
        return ToolBuilder.assemble(PICKAXE.get(), AntiqueTools.PICKAXE, List.of(handle, head, binding));
    }
}
