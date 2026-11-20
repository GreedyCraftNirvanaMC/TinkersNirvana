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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class VillageBuildingLimitsMixin {
    @Shadow @Final private List<? super PoolElementStructurePiece> pieces;
    @Unique private int tconstruct$workshopLimit = -1;

    @WrapOperation(method = "tryPlacingChildren", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getShuffledTemplates(Lnet/minecraft/util/RandomSource;)Ljava/util/List;"),
            require = 2)
    private List<StructurePoolElement> tconstruct$limitBuildings(StructureTemplatePool pool, RandomSource random,
                                                                Operation<List<StructurePoolElement>> original) {
        List<StructurePoolElement> candidates = original.call(pool, random);
        boolean hasWorkshops = candidates.stream().anyMatch(VillagePools::isWorkshop);
        boolean hasSmelteries = candidates.stream().anyMatch(VillagePools::isSmeltery);
        if (!hasWorkshops && !hasSmelteries) { return candidates; }
        if (hasWorkshops && tconstruct$workshopLimit < 0) {
            // 怀古的工坊上限为村庄 size + nextInt(4)。新版原版村庄没有 size 参数，按旧版默认 size=0 抽取一次。
            tconstruct$workshopLimit = random.nextInt(4);
        }
        int workshops = 0;
        boolean smelteryPlaced = false;
        for (Object piece : pieces) {
            if (piece instanceof PoolElementStructurePiece element) {
                if (hasWorkshops && VillagePools.isWorkshop(element.getElement())) { workshops++; }
                if (hasSmelteries && VillagePools.isSmeltery(element.getElement())) { smelteryPlaced = true; }
            }
        }
        boolean workshopCapReached = hasWorkshops && workshops >= tconstruct$workshopLimit;
        if (!workshopCapReached && !smelteryPlaced) { return candidates; }
        boolean smelteryCapReached = smelteryPlaced;
        return candidates.stream().filter(candidate -> !(workshopCapReached && VillagePools.isWorkshop(candidate))
                && !(smelteryCapReached && VillagePools.isSmeltery(candidate))).toList();
    }
}
