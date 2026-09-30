package com.bettercontent.bettermalumdynamictrees.mixin;

import com.bettercontent.bettermalumdynamictrees.WeepingWellDistrictPolicy;
import com.sammy.malum.common.worldgen.WeepingWellStructure;
import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Rejects only out-of-district candidates before Malum evaluates cave and terrain validity. */
@Mixin(WeepingWellStructure.class)
abstract class WeepingWellStructureMixin {
    @Inject(method = "findGenerationPoint", at = @At("HEAD"), cancellable = true, require = 1)
    private void admitOnlyDistrictCandidates(Structure.GenerationContext context,
        CallbackInfoReturnable<Optional<Structure.GenerationStub>> callback) {
        ChunkPos candidate = context.chunkPos();
        if (!WeepingWellDistrictPolicy.admits(context.seed(), candidate.getMinBlockX(), candidate.getMinBlockZ())) {
            callback.setReturnValue(Optional.empty());
        }
    }
}
