package com.bettercontent.bettermalumdynamictrees.gametest;

import com.bettercontent.bettermalumdynamictrees.Dtmalum;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(Dtmalum.MODID)
@PrefixGameTestTemplate(false)
@Mod.EventBusSubscriber(modid = Dtmalum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DtmalumGameTests {
    private DtmalumGameTests() {
    }

    @SubscribeEvent
    public static void register(final RegisterGameTestsEvent event) {
        event.register(DtmalumGameTests.class);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "better_malum_dynamic_trees_resources", timeoutTicks = 80)
    public static void dynamicRunewoodResourcesLoad(final GameTestHelper helper) {
        assertDynamicTree(helper, "runewood", "malum:runewood_sapling");
        assertDynamicTree(helper, "azure_runewood", "malum:azure_runewood_sapling");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "better_malum_dynamic_trees_replacement", timeoutTicks = 80)
    public static void malumStaticTreeFeaturesAreCancelledForReplacement(final GameTestHelper helper) {
        assertCancels(helper, "malum:runewood_tree");
        assertCancels(helper, "malum:azure_runewood_tree");
        assertDoesNotCancel(helper, "malum:soulwood_tree");
        helper.succeed();
    }

    private static void assertDynamicTree(final GameTestHelper helper, final String tree, final String primitiveSapling) {
        Species species = Species.REGISTRY.get(Dtmalum.location(tree));
        helper.assertTrue(species.isValid(), "species should load: " + tree);
        helper.assertTrue(species.getFamily().isValid(), "family should load for " + tree);
        helper.assertTrue(species.getLeavesProperties().isValid(), "leaves properties should load for " + tree);
        helper.assertTrue(species.getPrimitiveLeaves().isPresent(), "primitive leaves should resolve for " + tree);
        helper.assertTrue(species.hasSeed(), "generated seed should be registered for " + tree);
        helper.assertTrue(species.getSapling().isPresent(), "generated dynamic sapling should be registered for " + tree);
        helper.assertTrue(
                ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(primitiveSapling)),
                "primitive sapling should exist: " + primitiveSapling
        );
    }

    private static void assertCancels(final GameTestHelper helper, final String configuredFeatureId) {
        FeatureCanceller canceller = FeatureCanceller.REGISTRY.get(Dtmalum.location("runewood_tree"));
        helper.assertTrue(canceller != FeatureCanceller.NULL_CANCELLER, "runewood canceller should be registered");
        helper.assertTrue(
                canceller.shouldCancel(configuredFeature(helper, configuredFeatureId), cancellationForMalumNamespace(canceller)),
                "runewood canceller should remove static feature " + configuredFeatureId
        );
    }

    private static void assertDoesNotCancel(final GameTestHelper helper, final String configuredFeatureId) {
        FeatureCanceller canceller = FeatureCanceller.REGISTRY.get(Dtmalum.location("runewood_tree"));
        helper.assertFalse(
                canceller.shouldCancel(configuredFeature(helper, configuredFeatureId), cancellationForMalumNamespace(canceller)),
                "runewood canceller should not remove unrelated feature " + configuredFeatureId
        );
    }

    private static ConfiguredFeature<?, ?> configuredFeature(final GameTestHelper helper, final String id) {
        ResourceLocation location = new ResourceLocation(id);
        return helper.getLevel()
                .registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, location))
                .value();
    }

    private static BiomePropertySelectors.NormalFeatureCancellation cancellationForMalumNamespace(final FeatureCanceller canceller) {
        BiomePropertySelectors.NormalFeatureCancellation cancellation = new BiomePropertySelectors.NormalFeatureCancellation();
        cancellation.cancelUsing(canceller);
        cancellation.cancelWithNamespace("malum");
        return cancellation;
    }
}
