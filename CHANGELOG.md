# Changelog

## Unreleased

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
