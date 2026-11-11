# World generation

[README](../../README.md) · [简体中文](../zh-CN/WORLD.md)

New Overworld chunks can contain slime islands with green, blue, or purple slime soil, colored grass, slime lakes, trees, and vines. Magma slime islands can appear over Nether lava seas. Blue and purple slimes spawn in the matching island regions, and magma islands use magma cubes.

Plant slime saplings on slime soil. Bone meal grows saplings, grass, and ferns. Harvest leaves with shears or Silk Touch. Blue and purple vines grow downward through three stages. Slime leaves and vines can also supply fletching or shaft materials.

Tool workshops and smelteries can generate in all five vanilla village styles. Chests generate random loot on first opening. Buildings use vanilla jigsaw pools and coexist with other mods that add structures to those pools.

## Configuration

Edit `config/tconstruct-common.toml`. World generation settings affect future chunks and do not rewrite existing structures or terrain.

| Key | Default | Purpose |
| --- | --- | --- |
| `world.generateSlimeIslands` | `true` | Slime and magma islands |
| `world.slimeIslandRate` / `world.magmaIslandRate` | `730` / `100` | Average chunks per island attempt |
| `world.islandSlimeSpawnInterval` | `40` | Ticks between extra Tinkers slime spawn attempts per nearby ordinary island |
| `world.islandSlimeCap` | `16` | Local limit for naturally spawning Tinkers slimes per ordinary slime island |
| `world.slimeOutsideIslandSeconds` | `30` | Seconds a Tinkers slime can remain away from an island before removal |
| `world.generateIslandsInSuperflat` | `false` | Slime islands in superflat worlds |
| `world.generateSlimePools` | `false` | Underground slime and magma pools |
| `world.slimePoolRate` / `world.magmaPoolRate` | `30` | Underground pool attempt frequency |
| `world.slimePoolHeightMax` | `64` | Maximum pool attempt height |
| `world.generateVillageStructures` | `true` | Both village structure families |
| `world.villageWorkshopWeight` / `world.villageSmelteryWeight` | `30` / `9` | Village structure weights |

Islands and pools have separate dimension ID lists, allowlist or denylist settings, and surface-dimension restrictions. Use resource IDs. Empty lists are valid. Copper, tin, aluminum, cobalt, and Ardite each have an ore toggle and attempt count. Overworld ores also have minimum and maximum height settings. A count of zero disables attempts.

Each newly generated slime island has 1–3 slime trees. Blue and purple slimes can breathe and swim in liquid slime. After the configured time away from an island, they are removed without drops, experience or splitting. Returning to any ordinary slime island resets the timer, which is saved with the entity.

## Data packs

Configured and placed features live under `data/tconstruct/worldgen/configured_feature` and `placed_feature`. Biome modifiers live under `data/tconstruct/neoforge/biome_modifier`. Override those resources to change generation stages, biomes, or blocks.

The `tconstruct:slime_tree` configuration takes `trunk` and `leaves` block states, plus optional `vine`, `min_height`, `height_range`, and `seek_height`. Add-ons can reuse it for their own trees.

Village templates are under `data/tconstruct/structure/village/<biome>`. Additional pools and loot use vanilla data-pack formats. See [Materials and You](BOOK.md) for book extensions.
