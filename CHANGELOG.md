# Changelog

## Unreleased

- Complete the material-trait lifecycle for wood and stone, including Ecological
  inventory healing, Cheap repair bonuses, and Cheapskate assembled durability.
- Apply default traits to bindings and deduplicate repeated material traits.
- Keep trait definitions immutable, validate saved levels, and dispatch callbacks
  in a stable priority order without modifying unknown saved trait data.
- Show trait levels and Alt descriptions on tools and all supported parts.

- Port the Smeltery multiblock to NeoForge 1.21.1, including seared construction blocks, lava tanks, variable inventory, melting, layered fluid storage, drain selection, fluid automation, faucets, and casting.
- Add survival recipes, cobalt melting/casting products, data-pack machine recipes, Chinese player-facing text, and adapted TinkersAntique assets.
- Preserve stored fluids across structure invalidation/resizing and pause access while required chunks are unloaded.
