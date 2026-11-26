/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.compat.jei;

import com.gctn.tconstruct.Config;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

final class JeiEntityPreview {
    private final EntityType<?> type;
    private final String id;
    private ClientLevel previewLevel;
    private LivingEntity preview;
    private List<? extends String> scaleConfig;
    private float scaleFactor = 1F;

    JeiEntityPreview(EntityType<?> type) {
        this.type = type;
        id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    Component name() { return type.getDescription(); }

    void draw(GuiGraphics graphics, int x, int y) {
        var client = Minecraft.getInstance();
        if (client.level == null) { return; }
        if (previewLevel != client.level) {
            previewLevel = client.level;
            preview = type == EntityType.PLAYER ? null
                    : type.create(previewLevel) instanceof LivingEntity living ? living : null;
        }
        var entity = type == EntityType.PLAYER ? client.player : preview;
        if (entity == null) { return; }
        if (entity == preview) { entity.hurtTime = (Util.getMillis() / 500) % 4 == 0 ? 10 : 0; }
        float naturalScale = entity.getBbWidth() > entity.getBbHeight()
                ? 13F / entity.getBbWidth() : 32F / entity.getBbHeight();
        // JEI 已把绘制原点移到配方内。原版跟随鼠标的入口会用屏幕绝对坐标裁剪模型。
        InventoryScreen.renderEntityInInventory(graphics, x, y, Math.max(1, naturalScale * scaleFactor()) / entity.getScale(),
                new Vector3f(0F, entity.getBbHeight() / 2F, 0F),
                new Quaternionf().rotateZ((float) Math.PI).rotateY(3.45F).rotateX(.08F), null, entity);
    }

    private float scaleFactor() {
        var config = Config.JEI_ENTITY_SCALES.get();
        if (!config.equals(scaleConfig)) {
            scaleConfig = List.copyOf(config);
            scaleFactor = 1F;
            for (String entry : config) {
                int split = entry.indexOf(';');
                if (id.equalsIgnoreCase(entry.substring(0, split))) {
                    scaleFactor = Float.parseFloat(entry.substring(split + 1));
                    break;
                }
            }
        }
        return scaleFactor;
    }
}
