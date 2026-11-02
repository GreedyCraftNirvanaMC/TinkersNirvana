# Changelog

## Unreleased

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
