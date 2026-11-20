/* SPDX-License-Identifier: LGPL-3.0-only */
package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.world.VillagePools;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class VillageSmelteryLimitMixin {
    @Shadow @Final private List<? super PoolElementStructurePiece> pieces;

    @WrapOperation(method = "tryPlacingChildren", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getShuffledTemplates(Lnet/minecraft/util/RandomSource;)Ljava/util/List;"),
            require = 2)
    private List<StructurePoolElement> tconstruct$limitSmelteries(StructureTemplatePool pool, RandomSource random,
                                                                Operation<List<StructurePoolElement>> original) {
        List<StructurePoolElement> candidates = original.call(pool, random);
        if (candidates.stream().noneMatch(VillagePools::isSmeltery)) {
            return candidates;
        }
        // 原版拼图放置器已经保存当前村庄接受的组件。只在成功放置冶炼建筑后过滤后续候选。
        for (Object piece : pieces) {
            if (piece instanceof PoolElementStructurePiece element && VillagePools.isSmeltery(element.getElement())) {
                return candidates.stream().filter(candidate -> !VillagePools.isSmeltery(candidate)).toList();
            }
        }
        return candidates;
    }
}
