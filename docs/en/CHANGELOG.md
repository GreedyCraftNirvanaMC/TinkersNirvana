# Changelog

[README](../../README.md) · [简体中文](../zh-CN/CHANGELOG.md)

## Unreleased

- Preserve the actual projectile launcher separately from merged ammunition traits, including split projectiles, and expose `ToolRanged.launcherFor` for server-side integrations.
- Add `tools.initialModifierSlots` to configure the base modifier slot count for new and existing tools (default 3).

## 1.21.1-1.0.3

- Expose `ToolUpgradeApi` for addons to level tools, granting one persistent modifier slot per successful upgrade.

## 1.21.1-1.0.1

- Add optional KubeJS material and trait builders, definition edits and removal, translatable name fallbacks, saved-tool refresh, and Java addon documentation.
- Fix island slime spawning and add Tinkers-only spawn settings, liquid breathing, and off-island cleanup.
- Generate 1–3 slime trees per new island and block natural vanilla trees on slime soil.
- Fix ingot cast/fluid rendering, add placed-fluid translations, and align Overworld ore replacement rules.

## 1.21.1-1.0.0

- Restore direct main-index entries for the Tinker Tank and Seared Furnace,
  reusing their existing introduction, construction and animated structure pages
  without changing saved page IDs or duplicating book content. Use the original
  image dimensions for pages with text below an illustration.

- Restore the active Antique book definition and Mantle page templates: six
  chapters, original registration order, one material per leaf, finished-tool
  sidebars, colored stats and hover descriptions, icon overviews, and ranged
  material groups of three. Remove added search/detail pages and restore the
  original title, image, assembly, modifier, and structure-animation placement.

- Prefer ingots for material book icons instead of registry-first storage blocks,
  and expose explicit representative item overrides for Java and KubeJS addons.

- Restore Antique's ingredient-tag visibility conditions for optional materials,
  expose material availability predicates, provide fallback book icons for addon
  materials, and round book-page clipping outwards at fractional GUI origins.

- Fix opening Materials and You crashing while building the embossment page;
  derive its example parts from the registered pickaxe definition.

- Rebuild Materials and You around Mantle's cover, facing paper pages, icon
  contents, tool assembly diagrams, material columns, and modifier ingredient
  layouts; paginate long text, retain inline styles/tooltips, save each book's
  reading position, and add zoomable animated structure previews without a Mantle dependency.

- Add the Stencil Table, Part Builder, persistent Crafting Station, pattern/part
  chests, shared workshop navigation, adjacent inventory access, material variants,
  tool naming and part replacement, crafting-grid kit repair, and offhand harvesting.
- Port slime islands, underground pools, trees and plants, configurable ore
  generation, and workshops/smelteries for all five native village styles.
- Add drying racks, jerky and slime foods, slime boots and slings, piggyback packs,
  six-face slime channels, seven fancy frames, EFLN, and the spaghetti progression.
- Restore the illustrated guidebook with searchable translated pages, live material
  and modifier catalogs, resource-pack pages, and rotatable layered multiblocks.
- Expose native drying recipes, station processing, weighted modifier ingredients,
  native equipment attributes, configurable beheading drops, and entity melting.
- Align modifier units, fortification, necrotic healing, secondary fiery damage,
  ingredient remainders, and creative-only modifier limits with native mechanics.
- Restore blank-pattern naming and validation, mattock/kama right-click roles,
  gravel-to-flint crafting, optional native anvil/breaking rules, projectile expiry,
  adjacent-inventory exclusions, and autosmelt experience with complete stack output.
- Restyle the added workshop, world, gadget, gear, and book artwork through GIMP
  while retaining Antique silhouettes and adapting models to modern block states.
- Format tool pattern names with their selected part instead of displaying an
  untranslated `%s` placeholder.
- Drop the Tool Station item when its block is broken in survival.
- Port Antique's complete registered tool catalog: harvesting tools, six melee
  weapons, bows/crossbows, arrows/bolts, and shurikens, with shared part definitions,
  native tool actions, damage, repair, ammunition, and material components.
- Add the full part catalog, pattern-based material crafting, complete part casts,
  coated bolt cores, Tool Forge progression, and survival crafting entries.
- Restore source material stats and traits, add combat/drop/projectile lifecycle
  callbacks, tool modifiers, embossing, fortification, moss, and soulbound handling.
- Keep one set of physical menu slots across tool modes, preserve complete output
  batches, and avoid reequip/mining resets from transient trait state.
- Add public Java/KubeJS material, ingredient, stat, trait, modifier, and casting
  registration APIs without adding a runtime KubeJS dependency.
- Restyle 169 Antique textures and draw seven new icons in GIMP, retaining source
  silhouettes, tool layers, damaged states, and bow/crossbow draw stages; include
  Antique tool sounds and Chinese/English text.

- Add Ardite Ore with Antique-based GIMP artwork, original default hardness and
  harvest tier, block/item tags, self drops, Smeltery integration, and bilingual names.
- Allow cobalt-tier assembled tools to harvest blocks requiring netherite-tier tools.
- Refine metal-nugget highlights and shadows using the modern vanilla gold nugget
  as a style reference while retaining each Antique silhouette and detail.
- Restyle eleven metals' ingots, nuggets, storage blocks, and ported ores in GIMP
  from their corresponding Antique artwork, preserving individual silhouettes,
  patterns, and mineral layouts while refining palettes for modern Minecraft.
- Use the requested Chinese blue/purple slime names for mobs and spawn eggs.
- Apply the supplied Chinese tool-part names to parts and their metal/clay casts.
- Apply the supplied Chinese metal glossary to ingot, nugget, and storage-block
  names, and add the missing bilingual Cobalt Ore name.
- Port blue/purple slime mobs and spawn eggs, original jumping/squish/fall rules,
  colored particles and loot, same-color splitting, source tints and texture,
  spawn predicates, and type-specific Smeltery fluids.
- Port copper, tin, and aluminum ore blocks, original physical/rendering values,
  stone-pickaxe self drops, integration tags, and existing Smeltery ore yields.
- Port Glowballs and six-face light patches, native projectile synchronization
  and impact cancellation, original assets, crafting, and bilingual text.
- Restore Antique's three/twelve-item grout crafting outputs and large-batch recipe.
- Port the remaining five soil variants, their walking/undead effects, slime mud
  recipes, three slime crystals and smelting, and original soil physical values.
- Port colored slime blocks, congealed slime, edible slimeballs, original effects,
  bounce and support behavior, piston hooks, reversible recipes, mixed-slime
  configuration, fluid recipes, original assets, and Chinese/English text.
- Port all sixteen stained clear glass colors, connected textures, original
  tint/beacon palette, dye recipes, glass tags, melting, and Silk Touch configuration.
- Restore standalone glass borders, transparent-block physical properties, and
  full seared-wall light blocking and redstone conduction.
- Complete Firewood, Lavawood, and Mud Bricks building families, recipes, slab
  drops, native shape behavior, original lighting/fuel, and mining-tool tags.
- Convert Antique's blast-resistance values to modern units; restore seared
  metal sounds, casting stone sounds, and common-metal mining/beacon behavior.
- Port Wooden Rails and trapdoor rails, native curves/slopes, hopper-minecart
  unloading, inventory rejection/rollback handling, recipes, and original textures.
- Port Punji Sticks with six-face placement, connected models, support removal,
  contact/falling damage, Slowness II, crafting, and Chinese/English text.
- Complete missing Chinese names for existing materials, tools, parts, tabs,
  tool-station controls, and stat tooltips.
- Port the single-slot Wooden Hopper, slower native transfers, redstone-independent
  operation, original centered-slot screen, crafting, fuel value, and translations.
- Port the Seared Furnace multiblock, shared liquid fuel, sixteen-item batches,
  retained cooked outputs, original screen/status bars, automation, and configuration.
- Save multiblock inventory indices as integers, retaining compatibility with
  older byte indices and preventing collisions above slot 255.
- Page very large heating inventories within vanilla network slot limits while
  preserving full-inventory shift transfers and double-click collection.
- Restore component ownership when replacing walls or drains without changing
  a formed structure's dimensions.
- Port brownstone's twelve styles, slabs, stairs, walking boost, decoration cycles,
  and smelting recipes, plus stone rods, floor/wall torches, and ladders.
- Add a Gadgets creative tab and Chinese/English names and tooltips for the new items.

- Port TinkersAntique's enclosed Tinker Tank, shared drain access, ordered storage,
  original fluid-only screen, controller window, and multiblock fluid rendering.
- Add all twelve seared slab and stair styles, including bottom-half tank roofs,
  crafting, loot, and Chinese/English names. Double slabs count as full seared blocks.

- Port TinkersAntique's Smeltery structure, heating, ordered fluid storage,
  alloying, drains, tanks, faucets, channels, and casting to NeoForge 1.21.1.
- Add the original Smeltery GUI assets, scrolling input inventory, heating-state
  tooltips, fluid selection, block/fluid rendering, and heating/pouring sounds.
- Add construction recipes, casts, material casting outputs, automation support,
  fuel-retaining tank items, configuration, and integration events.

- Complete the material-trait lifecycle for wood and stone, including Ecological
  inventory healing, Cheap repair bonuses, and Cheapskate assembled durability.
- Apply default traits to bindings and deduplicate repeated material traits.
- Keep trait definitions immutable, validate saved levels, and dispatch callbacks
  in a stable priority order without modifying unknown saved trait data.
- Show trait levels and Alt descriptions on tools and all supported parts.
