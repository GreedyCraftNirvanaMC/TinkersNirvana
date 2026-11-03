/* SPDX-License-Identifier: LGPL-3.0-only; replaces Antique's per-dimension island SavedData. */
package com.gctn.tconstruct.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

/** Stored on the origin chunk, including proto-chunks; worldgen threads never mutate shared world data. */
public record IslandBounds(List<BoundingBox> normal, List<BoundingBox> magma) {
    public static final IslandBounds EMPTY = new IslandBounds(List.of(), List.of());
    public static final Codec<IslandBounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BoundingBox.CODEC.listOf().optionalFieldOf("normal", List.of()).forGetter(IslandBounds::normal),
            BoundingBox.CODEC.listOf().optionalFieldOf("magma", List.of()).forGetter(IslandBounds::magma)).apply(instance, IslandBounds::new));
    public IslandBounds { normal = List.copyOf(normal); magma = List.copyOf(magma); }
    public static void add(WorldGenLevel level, BlockPos origin, BoundingBox box, boolean magma) {
        var chunk = level.getChunk(origin);
        IslandBounds old = chunk.getData(WorldModule.ISLAND_BOUNDS);
        List<BoundingBox> added = new ArrayList<>(magma ? old.magma : old.normal); added.add(box);
        chunk.setData(WorldModule.ISLAND_BOUNDS, magma ? new IslandBounds(old.normal, added) : new IslandBounds(added, old.magma));
        chunk.setUnsaved(true);
    }
}
