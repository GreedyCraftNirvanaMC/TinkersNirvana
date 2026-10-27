package com.gctn.tconstruct.tables.recipe;

import com.gctn.tconstruct.library.TinkerMaterials;
import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.library.utils.Tags;
import com.gctn.tconstruct.library.materials.Material;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class ToolAssemblyRecipe {
    private final PartRequirement[] requirements;
    private final Function<Material[], ItemStack> resultFactory;

    ToolAssemblyRecipe(PartRequirement[] requirements, Function<Material[], ItemStack> resultFactory) {
        this.requirements = requirements;
        this.resultFactory = resultFactory;
    }

    public Item getRequiredItem(int inputSlot) {
        for (PartRequirement requirement : this.requirements) {
            if (requirement.inputSlot() == inputSlot) {
                return requirement.requiredItem();
            }
        }
        return null;
    }

    public ItemStack createResult(Container container) {
        Material[] materials = new Material[this.requirements.length];
        for (int index = 0; index < this.requirements.length; index++) {
            PartRequirement requirement = this.requirements[index];
            ItemStack stack = container.getItem(requirement.inputSlot());
            if (!stack.is(requirement.requiredItem())) {
                return ItemStack.EMPTY;
            }

            Material material = getMaterial(stack, requirement.requiredStat());
            if (material == null) {
                return ItemStack.EMPTY;
            }
            materials[index] = material;
        }

        return this.resultFactory.apply(materials);
    }

    public void consumeInputs(Container container) {
        for (PartRequirement requirement : this.requirements) {
            container.removeItem(requirement.inputSlot(), 1);
        }
    }

    // 返回stack对应的材料，同时验证材料是否含有对应属性
    private static Material getMaterial(ItemStack stack, String requiredStat) {
        String materialId = TinkerDataComponents.getMaterialTag(stack).getString(Tags.PART_MATERIAL);
        Material material = TinkerMaterials.getMaterial(materialId);
        return material != null && material.hasStats(requiredStat) ? material : null;
    }

    record PartRequirement(int inputSlot, Supplier<? extends Item> item, String requiredStat) {
        Item requiredItem() {
            return this.item.get();
        }
    }
}
