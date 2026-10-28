# Changelog

## Unreleased

- Complete the material-trait lifecycle for wood and stone, including Ecological
  inventory healing, Cheap repair bonuses, and Cheapskate assembled durability.
- Apply default traits to bindings and deduplicate repeated material traits.
- Keep trait definitions immutable, validate saved levels, and dispatch callbacks
  in a stable priority order without modifying unknown saved trait data.
- Show trait levels and Alt descriptions on tools and all supported parts.


### Port review improvements

- Restore Antique wood/stone stats and head-based pickaxe repair inputs/amounts.
  Repair efficiency counts distinct materials rather than occupied slots, so
  splitting a stack no longer increases the bonus. Restore plank/log/stick and
  stone/cobblestone material values; remove repair-preview debug log spam.
- Revalidate crafting outputs before pickup, refresh previews after automation
  changes, and check active-slot insertion rules during shift-click merges.
- Preserve translatable tool/part names, add Ctrl part details and Chinese UI
  translations, and read saved mining speed/attack bonuses for Shift tooltips.
- Index material lookup, reject invalid tool combinations, and consistently hide
  hidden materials from creative part lists.
- Add block loot tables and mining tags; retain Antique's Cobalt-tier ore gate
  and hardness. Correct the sample datagen model that replaced ore with cobblestone.

### Remaining port gaps identified

- Antique's repair-history diminishing returns, modifier repair penalties, and
  original/modified durability scaling are not yet ported. The current repair
  station uses head material units plus trait healing callbacks.
- Recipe acquisition/world generation, higher-tier materials, reinforcement and
  other modifiers, additional tools, projectile systems, and their traits remain
  unported. Shovel/axe/debug station modes have no recipes.
- Existing pre-trait/pre-stat-fix tools retain saved data; no automatic migration
  is performed. Older literal names stay literal until a tool is rebuilt.

- Port the Smeltery multiblock to NeoForge 1.21.1, including seared construction blocks, lava tanks, variable inventory, melting, layered fluid storage, drain selection, fluid automation, faucets, and casting.
- Add survival recipes, cobalt melting/casting products, data-pack machine recipes, Chinese player-facing text, and adapted TinkersAntique assets.
- Preserve stored fluids across structure invalidation/resizing and pause access while required chunks are unloaded.

