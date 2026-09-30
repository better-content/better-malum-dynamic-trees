package com.bettercontent.bettermalumdynamictrees.mixin;

import com.bettercontent.bettermalumdynamictrees.MalumDistrictPolicy;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes the selection before Dynamic Trees checks soil/chance or writes a rooty block.
 * Non-district positions delegate to the original biome selector unchanged.
 */
@Mixin(value = DynamicTreeFeature.class, remap = false)
abstract class DynamicTreeFeatureMixin {
    private static final TagKey<Biome> RUNEWOOD = biomeTag("has_runewood");
    private static final TagKey<Biome> RARE_RUNEWOOD = biomeTag("has_rare_runewood");
    private static final TagKey<Biome> AZURE_RUNEWOOD = biomeTag("has_azure_runewood");
    private static final TagKey<Biome> RARE_AZURE_RUNEWOOD = biomeTag("has_rare_azure_runewood");

    @Shadow
    protected abstract BiomePropertySelectors.SpeciesSelector getSpeciesSelector(BiomeDatabase.EntryReader entry);

    @Redirect(
        method = "generateTree",
        at = @At(value = "INVOKE", target = "Lcom/ferreusveritas/dynamictrees/worldgen/DynamicTreeFeature;getSpeciesSelector(Lcom/ferreusveritas/dynamictrees/worldgen/BiomeDatabase$EntryReader;)Lcom/ferreusveritas/dynamictrees/api/worldgen/BiomePropertySelectors$SpeciesSelector;"),
        require = 1
    )
    private BiomePropertySelectors.SpeciesSelector selectDistrictSpecies(
        DynamicTreeFeature ignoredFeature, BiomeDatabase.EntryReader ignoredEntry,
        LevelContext context, BiomeDatabase.EntryReader entry, PoissonDisc ignoredDisc,
        BlockPos ignoredOrigin, BlockPos ignoredGround, SafeChunkBounds ignoredBounds
    ) {
        BiomePropertySelectors.SpeciesSelector fallback = getSpeciesSelector(entry);
        Long seed = context.seed();
        if (seed == null) {
            return fallback;
        }
        return (position, state, random) -> districtSelection(fallback, context, seed, position, state, random);
    }

    private static BiomePropertySelectors.SpeciesSelection districtSelection(
        BiomePropertySelectors.SpeciesSelector fallback, LevelContext context, long seed,
        BlockPos position, BlockState state, RandomSource random
    ) {
        var biome = context.accessor().getBiome(position);
        if (biome.is(AZURE_RUNEWOOD) || biome.is(RARE_AZURE_RUNEWOOD)) {
            if (MalumDistrictPolicy.shouldSelectRunewood(seed, position.getX(), position.getZ(), true,
                biome.value().getBaseTemperature())) {
                return new BiomePropertySelectors.SpeciesSelection(TreeRegistry.findSpecies("better_malum_dynamic_trees:azure_runewood"));
            }
        } else if ((biome.is(RUNEWOOD) || biome.is(RARE_RUNEWOOD))
            && MalumDistrictPolicy.shouldSelectRunewood(seed, position.getX(), position.getZ(), false,
                biome.value().getBaseTemperature())) {
            return new BiomePropertySelectors.SpeciesSelection(TreeRegistry.findSpecies("better_malum_dynamic_trees:runewood"));
        }
        return fallback.getSpecies(position, state, random);
    }

    private static TagKey<Biome> biomeTag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.BIOME, new ResourceLocation("malum", path));
    }
}
