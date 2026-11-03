/* SPDX-License-Identifier: LGPL-3.0-only; adapted from Antique's BakedTableModel. */
package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.init.TinkerDataComponents;
import com.gctn.tconstruct.tables.TableMaterial;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;

/** Swaps only the wood/metal quads, retaining the table's modern top texture and baked rotation. */
public final class TableMaterialModel extends BakedModelWrapper<BakedModel> {
    private record Key(BakedQuad quad, TextureAtlasSprite sprite) {}
    private final Cache<Key, BakedQuad> quads;
    private final Cache<BlockState, BakedModel> items;
    private final BlockState fixed;
    private final ItemOverrides overrides = new ItemOverrides() {
        @Override public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
            BlockState state = stack.get(TinkerDataComponents.TABLE_MATERIAL.get());
            if (state == null) { return TableMaterialModel.this; }
            BakedModel result = items.getIfPresent(state);
            if (result == null) { result = new TableMaterialModel(originalModel, state, quads, items); items.put(state, result); }
            return result;
        }
    };
    public TableMaterialModel(BakedModel original) { this(original, null, CacheBuilder.newBuilder().maximumSize(4096).build(), CacheBuilder.newBuilder().maximumSize(128).build()); }
    private TableMaterialModel(BakedModel original, BlockState fixed, Cache<Key, BakedQuad> quads, Cache<BlockState, BakedModel> items) {
        super(original); this.fixed = fixed; this.quads = quads; this.items = items;
    }
    @Override public ItemOverrides getOverrides() { return fixed == null ? overrides : ItemOverrides.EMPTY; }
    @Override public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) { originalModel.applyTransform(context, pose, leftHand); return this; }
    @Override public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) { return List.of(this); }
    @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
        return retexture(originalModel.getQuads(state, side, random), fixed);
    }
    @Override public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType type) {
        return retexture(originalModel.getQuads(state, side, random, data, type), fixed == null ? data.get(TableMaterial.MODEL) : fixed);
    }
    private List<BakedQuad> retexture(List<BakedQuad> original, BlockState state) {
        if (state == null || original.isEmpty()) { return original; }
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getParticleIcon();
        return original.stream().map(quad -> {
            String texture = quad.getSprite().contents().name().toString();
            if (!texture.equals("minecraft:block/oak_planks") && !texture.startsWith("minecraft:block/oak_log") && !texture.equals("minecraft:block/iron_block")) { return quad; }
            Key key = new Key(quad, sprite); BakedQuad result = quads.getIfPresent(key);
            if (result == null) {
                int[] vertices = quad.getVertices().clone(); int stride = vertices.length / 4;
                var old = quad.getSprite();
                for (int vertex = 0; vertex < 4; vertex++) {
                    int offset = vertex * stride;
                    float u = (Float.intBitsToFloat(vertices[offset + 4]) - old.getU0()) / (old.getU1() - old.getU0());
                    float v = (Float.intBitsToFloat(vertices[offset + 5]) - old.getV0()) / (old.getV1() - old.getV0());
                    vertices[offset + 4] = Float.floatToRawIntBits(sprite.getU(u)); vertices[offset + 5] = Float.floatToRawIntBits(sprite.getV(v));
                }
                result = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite, quad.isShade(), quad.hasAmbientOcclusion()); quads.put(key, result);
            }
            return result;
        }).toList();
    }
}
