package com.bettercontent.bettermalumdynamictrees;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEvent;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Dtmalum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MalumRunewoodFeatureCanceller {
    private static final ResourceLocation RUNEWOOD_TREE_FEATURE = new ResourceLocation("malum", "runewood_tree");

    private MalumRunewoodFeatureCanceller() {
    }

    @SubscribeEvent
    public static void registerFeatureCancellers(final RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().register(new FeatureCanceller(Dtmalum.location("runewood_tree")) {
            @Override
            public boolean shouldCancel(final ConfiguredFeature<?, ?> configuredFeature,
                                        final BiomePropertySelectors.NormalFeatureCancellation cancellation) {
                final ResourceLocation featureName = ForgeRegistries.FEATURES.getKey(configuredFeature.feature());
                return RUNEWOOD_TREE_FEATURE.equals(featureName)
                        && cancellation.shouldCancelNamespace(RUNEWOOD_TREE_FEATURE.getNamespace());
            }
        });
    }
}
