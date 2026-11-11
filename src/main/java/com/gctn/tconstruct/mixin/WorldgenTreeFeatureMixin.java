package com.gctn.tconstruct.mixin;

import com.gctn.tconstruct.world.WorldModule;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public abstract class WorldgenTreeFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void tconstruct$skipSlimeSoil(FeaturePlaceContext<TreeConfiguration> context, CallbackInfoReturnable<Boolean> callback) {
        if (context.level() instanceof WorldGenRegion && WorldModule.soil(context.level().getBlockState(context.origin().below()))) {
            callback.setReturnValue(false);
        }
    }
}
