/* SPDX-License-Identifier: LGPL-3.0-only; Antique's arbitrary wood and metal table textures. */
package com.gctn.tconstruct.tables;

import com.gctn.tconstruct.init.TinkerDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/** The appearance is a block state, so modded woods/metals need no hard-coded registrations. */
public final class TableMaterial {
    public static final ModelProperty<BlockState> MODEL = new ModelProperty<>();
    private BlockState state;
    public TableMaterial(BlockState fallback) { state = fallback; }
    public BlockState state() { return state; }
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("TableMaterial", 10)) { state = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("TableMaterial")); }
    }
    public void save(CompoundTag tag) { tag.put("TableMaterial", NbtUtils.writeBlockState(state)); }
    public void set(BlockState state) { this.state = state; }
    public void collect(DataComponentMap.Builder output) { output.set(TinkerDataComponents.TABLE_MATERIAL.get(), state); }
    public ModelData modelData() { return ModelData.builder().with(MODEL, state).build(); }
    public void apply(ItemStack stack) { stack.set(TinkerDataComponents.TABLE_MATERIAL.get(), state); }
    public static ItemStack pick(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, ItemStack stack) {
        var entity = level.getBlockEntity(pos);
        if (entity instanceof com.gctn.tconstruct.tables.block.entity.WorkshopBlockEntity workshop) { workshop.material.apply(stack); }
        else if (entity instanceof com.gctn.tconstruct.tables.block.entity.ToolStationBlockEntity station) { station.material.apply(stack); }
        else if (entity instanceof com.gctn.tconstruct.gadgets.block.entity.RackBlockEntity rack) { rack.material.apply(stack); }
        return stack;
    }
    public static void creative(net.minecraft.world.level.block.Block block, java.util.function.Consumer<ItemStack> output) {
        net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> wood = null;
        if (block instanceof com.gctn.tconstruct.tables.block.WorkshopBlock workshop) {
            wood = workshop.kind() == com.gctn.tconstruct.tables.WorkshopModule.Kind.PART_BUILDER ? net.minecraft.tags.BlockTags.LOGS
                    : workshop.kind() == com.gctn.tconstruct.tables.WorkshopModule.Kind.CRAFTING || workshop.kind() == com.gctn.tconstruct.tables.WorkshopModule.Kind.STENCIL ? net.minecraft.tags.BlockTags.PLANKS : null;
        }
        boolean forge = block == com.gctn.tconstruct.tables.block.TableBlocks.TOOL_FORGE.get();
        if (wood == null && !forge || !com.gctn.tconstruct.Config.LIST_ALL_TABLES.get()) { output.accept(new ItemStack(block)); return; }
        var states = new java.util.LinkedHashSet<BlockState>();
        if (wood != null) { for (var material : net.minecraft.core.registries.BuiltInRegistries.BLOCK) { if (material.defaultBlockState().is(wood)) { states.add(material.defaultBlockState()); } } }
        else { for (String metal : java.util.List.of("iron", "gold", "copper", "tin", "aluminum", "bronze", "steel", "cobalt", "ardite", "manyullyn", "knightslime", "pigiron", "alubrass", "alumite")) {
            var tag = net.minecraft.tags.TagKey.create(Registries.BLOCK, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + metal));
            for (var material : net.minecraft.core.registries.BuiltInRegistries.BLOCK) { if (material.defaultBlockState().is(tag)) { states.add(material.defaultBlockState()); break; } }
        } }
        if (states.isEmpty()) { output.accept(new ItemStack(block)); }
        else { for (BlockState state : states) { var stack = new ItemStack(block); stack.set(TinkerDataComponents.TABLE_MATERIAL.get(), state); output.accept(stack); } }
    }
}
