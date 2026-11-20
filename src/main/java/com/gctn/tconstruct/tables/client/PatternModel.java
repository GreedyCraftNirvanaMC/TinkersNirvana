package com.gctn.tconstruct.tables.client;

import com.gctn.tconstruct.TinkersNirvana;
import com.gctn.tconstruct.tools.PatternItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PatternModel extends BakedModelWrapper<BakedModel> {
    private static final ResourceLocation BLANK_TEXTURE = ResourceLocation.fromNamespaceAndPath(TinkersNirvana.MODID,
            "item/toolsupplies/pattern");
    private final Map<String, BakedModel> variants = new HashMap<>();
    private final ItemOverrides overrides = new ItemOverrides() {
        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
            if (PatternItem.isBlank(stack)) {
                return originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
            }
            return variants.computeIfAbsent(PatternItem.selected(stack).id(), PatternModel.this::variant);
        }
    };

    PatternModel(BakedModel original) {
        super(original);
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private BakedModel variant(String partId) {
        ResourceLocation id = ResourceLocation.tryParse(partId.indexOf(':') >= 0
                ? partId : TinkersNirvana.MODID + ":" + partId);
        if (id == null) {
            return originalModel;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(id.withPrefix("item/pattern/"));
        if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            return originalModel;
        }
        return new ImprintedModel(originalModel, sprite);
    }

    private static final class ImprintedModel extends BakedModelWrapper<BakedModel> {
        private final TextureAtlasSprite sprite;
        private final Map<BakedQuad, BakedQuad> remapped = new HashMap<>();

        private ImprintedModel(BakedModel original, TextureAtlasSprite sprite) {
            super(original);
            this.sprite = sprite;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            originalModel.applyTransform(context, pose, leftHand);
            return this;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(this);
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            return applyImprint(originalModel.getQuads(state, side, random));
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data, RenderType type) {
            return applyImprint(originalModel.getQuads(state, side, random, data, type));
        }

        private List<BakedQuad> applyImprint(List<BakedQuad> quads) {
            // 刻痕沿用空白模板的形状，保留模型原有的边缘、其他图层和物品变换。
            return quads.stream().map(quad -> quad.getSprite().contents().name().equals(BLANK_TEXTURE)
                    ? remapped.computeIfAbsent(quad, this::imprint) : quad).toList();
        }

        private BakedQuad imprint(BakedQuad quad) {
            TextureAtlasSprite original = quad.getSprite();
            int[] vertices = quad.getVertices().clone();
            int stride = vertices.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int uv = vertex * stride + 4;
                float u = (Float.intBitsToFloat(vertices[uv]) - original.getU0()) / (original.getU1() - original.getU0());
                float v = (Float.intBitsToFloat(vertices[uv + 1]) - original.getV0()) / (original.getV1() - original.getV0());
                vertices[uv] = Float.floatToRawIntBits(sprite.getU(u));
                vertices[uv + 1] = Float.floatToRawIntBits(sprite.getV(v));
            }
            return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite, quad.isShade(), quad.hasAmbientOcclusion());
        }
    }
}
