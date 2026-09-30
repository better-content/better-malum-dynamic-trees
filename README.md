# Dynamic Trees for Malum (`better_malum_dynamic_trees`)

Forge 1.20.1 addon that provides Dynamic Trees integration for Malum Runewood trees.

`OccultDistricts` is a pure seed-and-coordinate provider for later flora and Well consumers. It has no biome, mineral worldgen, mutable region storage, or chunk writes. Its field contains ordinary gaps and continuous fringe, interior, and core bands. The native Dynamic Trees selection route wraps its selector before soil, chance, or block placement: normal Runewood is concentrated in district interiors/cores with sparse fringe clues, and Azure additionally requires a biome base temperature of at most `0.40`.

## References used (structure + API)

- `DynamicTreesTeam/DynamicTrees-BWG` (branch `1.20.1`) as primary structural reference.
- `Groupix05/DynamicTrees-Template` (branch `1.20.1`) for minimal addon bootstrap.
- `DynamicTreesTeam/DynamicTreesPlus` for worldgen canceller JSON shape.

This project follows the same addon layout pattern:

- `src/main/resources/trees/<modid>/families/*.json`
- `src/main/resources/trees/<modid>/leaves_properties/*.json`
- `src/main/resources/trees/<modid>/species/*.json`
- `src/main/resources/trees/<modid>/world_gen/default.json`
- `src/main/resources/trees/<modid>/world_gen/feature_cancellers.json`

Main mod bootstrap follows current 1.20.1 pattern with:

- `RegistryHandler.setup(MODID)`
- `GatherDataHelper.gatherAllData(...)`

## Versions tested against

- Minecraft: `1.20.1`
- Forge: `47.4.13`
- Dynamic Trees dependency in build: `1.4.10`
- Malum jar inspected for IDs: `malum-1.20.1-1.6.7.jar`

## Trees included

- Runewood
- Azure Runewood

Soulwood is intentionally not implemented in v1.

## Dependency stance

The addon requires only Dynamic Trees and Malum.

## Verified Malum IDs used

- `malum:runewood_log`
- `malum:stripped_runewood_log`
- `malum:runewood_leaves`
- `malum:runewood_sapling`
- `malum:azure_runewood_leaves`
- `malum:azure_runewood_sapling`

Note: Malum `1.6.7` does not provide `azure_runewood_log`; Azure Runewood worldgen in Malum itself uses `runewood_log` with azure leaves.

## Worldgen replacement strategy

`better_malum_dynamic_trees` wraps the DT species selector plus scoped feature cancellers:

- At eligible Malum biome tags, a seed-coordinate selector chooses the dynamic species only when the district policy admits it; every other site delegates to the unmodified Dynamic Trees selector:
  - `#malum:has_runewood`
  - `#malum:has_rare_runewood`
  - `#malum:has_azure_runewood`
  - `#malum:has_rare_azure_runewood`
- Cancels Malum's custom `malum:runewood_tree` configured feature type only in those same biome tags and only for the `malum` namespace.

## Tuning worldgen

Tune `MalumDistrictPolicy` only with fixed-seed coverage tests. The underlying Dynamic Trees biome selection remains the fallback, so this addon does not assign a replacement density or chance to the biome database.

## Known limitations

- Azure Runewood currently shares the Runewood primitive log because that is how Malum `1.6.7` is configured.
- Soulwood integration is deferred.

## Verification

- `./gradlew verifyFast`
- `./gradlew verifyFull`

`verifyFast` runs the unit-test lane. `verifyFull` adds the headless Forge GameTest pass.

## Community and support

For modpack and mod discussion, playtest feedback, and bug reports, join the [Better Content Discord](https://discord.gg/EkRnZbzqS9).

## Identity

The clean-break canonical identity is repository/artifact `better-malum-dynamic-trees`, mod ID and resource namespace `better_malum_dynamic_trees`, and Maven group `com.bettercontent`. Legacy `dtmalum` worlds and configs are not migrated.
