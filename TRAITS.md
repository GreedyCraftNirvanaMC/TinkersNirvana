# Material traits

Trait definitions are immutable and registered once in `TinkerTraits`. The tool's
`tconstruct:tinker_traits` data component stores the effective level and remains
compatible with the component introduced in b69581b. Missing components mean no
traits; unknown or invalid entries are ignored during dispatch, without deleting
saved data. Existing tools retain their saved traits and stats; newly assembled
tools receive the current material assignments.

To add a trait:

1. Extend `AbstractTraits` and register the definition in `TinkerTraits.registerTraits()`.
   Never store tool-specific state on the definition; use the tool's components.
2. Add material assignments in `registerToolMaterialTraits()`. A `null` position
   supplies the default traits for all stats. A position-specific list replaces
   that default (stone heads use Cheapskate; other stone parts use Cheap).
3. Ordinary traits do not stack. Extend `LeveledTraits` when distinct strengths
   should add together, capped at `maxLevel`; repeated equal strengths count once.
   Register the lowest-strength definition for lookup and use the same max level
   for every strength of the same identifier.
4. Override the relevant callbacks: final assembled durability, inventory tick,
   mining speed, before/after block break, successful melee hit, tool damage,
   healing, or material repair. `ToolTraits` dispatches higher priorities first,
   with identifiers breaking ties. Callbacks receive the tool's effective level.
   World mutations must remain server-side; mining-speed calculation also runs
   on the client. Repair callbacks operate only on a simulated result stack and
   must not mutate the world, player, registry, or source inventory.
5. Add `traits.<identifier>.name` and `.description` translations. Tool and part
   tooltips show names and Alt descriptions through the same definitions.

Currently functional materials are wood and stone. Their supported traits are
Ecological (all wood parts), Cheapskate (stone heads), and Cheap (other stone
parts). Ecological uses Antique's 1/400 chance each inventory tick, skips the
actively used stack, and cannot revive broken tools. Cheap adds 5% of each
healing amount, rounded down. Cheapskate reduces final assembled durability by
20%, rounded down, with a minimum of one.
