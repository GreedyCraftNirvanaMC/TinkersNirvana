/*
 * 根据 TinkersAntique 的工具、部件与材料行为适配 NeoForge。
 * SPDX-License-Identifier: LGPL-3.0-only
 * 来源与许可见 third_party/NOTICE.md。
 */
package com.gctn.tconstruct.tools;

import com.gctn.tconstruct.library.toolparts.AntiqueParts;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;

/** 无客户端依赖的工具规格；扩展模组可用相同规格注册自己的 AntiqueTool。 */
public record ToolDefinition(String id, List<Part> parts, List<TagKey<Block>> effectiveBlocks,
                             float durabilityMultiplier, float damageMultiplier, float bonusDamage,
                             float attackSpeed, Action action) {
    public ToolDefinition {
        Objects.requireNonNull(id);
        parts = List.copyOf(parts);
        effectiveBlocks = List.copyOf(effectiveBlocks);
        if (parts.isEmpty() || parts.size() > 6 || durabilityMultiplier <= 0 || attackSpeed <= 0 || damageMultiplier < 0
                || !Float.isFinite(durabilityMultiplier) || !Float.isFinite(damageMultiplier) || !Float.isFinite(bonusDamage) || !Float.isFinite(attackSpeed)) {
            throw new IllegalArgumentException("Invalid tool definition: " + id);
        }
        if (parts.stream().map(Part::key).distinct().count() != parts.size()) {
            throw new IllegalArgumentException("Duplicate material key: " + id);
        }
    }

    public enum Action { PICKAXE, SHOVEL, AXE, MATTOCK, KAMA, HAMMER, EXCAVATOR, LUMBERAXE,
        SCYTHE, BROADSWORD, LONGSWORD, RAPIER, FRYPAN, BATTLESIGN, CLEAVER,
        SHORTBOW, LONGBOW, CROSSBOW, ARROW, BOLT, SHURIKEN }

    public record Part(String key, String part, String stat) {
        public Part {
            Objects.requireNonNull(key);
            Objects.requireNonNull(part);
            Objects.requireNonNull(stat);
        }
        public AntiqueParts.Definition definition() { return AntiqueParts.get(part); }
        public List<String> traitStats(Action action) {
            if (stat.equals("Bow")) { return List.of("Bow", "Head"); }
            if (stat.equals("Head") && (action == Action.ARROW || action == Action.BOLT)) { return List.of("Head", "Projectile"); }
            if (stat.equals("Handle") && action == Action.CROSSBOW) { return List.of("Handle", "Extra"); }
            return List.of(stat);
        }
    }
}
