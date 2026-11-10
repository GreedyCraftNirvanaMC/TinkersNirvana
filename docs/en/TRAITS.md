# Trait system

[README](../../README.md) · [简体中文](../zh-CN/TRAITS.md)

## Data model

- `AbstractTraits` is an immutable behavior definition registered once. It stores neither material starting levels nor per-tool state.
- `AbstractTraits.TraitEntry(trait, level)` identifies a material trait and its starting level, with range validation.
- The tool component `tconstruct:tinker_traits` stores actual `Level`, `StrengthenNumber`, and per-trait state. Existing saves retain their saved traits and stats.
- `ToolTraits` assembles and reads traits. Unknown identifiers, invalid types, and nonpositive levels are retained in the save but skipped at runtime. Runtime levels are capped at `maxLevel`.

## Add a trait

1. Extend `AbstractTraits`, register a definition through `TinkerContent.registerTrait`, and expose starting `TraitEntry` values as needed. Materials with different starting levels reuse the same definition.
2. Define built-in material traits in `TinkerMaterials.registerToolMaterialTraits()`. Null is the default for all positions. An explicit position list replaces the default list. Do not repeat an identifier in one position.
3. Ordinary traits take the highest level through `addLevel(currentLevel, amount)`. `mergeLevel(currentLevel, incomingLevel)` follows that rule by default, and embossing also merges through `ToolTraits.addTraits`. Extend `LeveledTraits` to stack levels. By default each distinct starting level contributes once, so 1+1 gives 1 and 1+2 gives 3, capped by `maxLevel`. Return true from `stacksRepeatedLevels()` to count every part. `AppliedLevels` stores deduplication data.
4. Override only the necessary `ITraits` callbacks. Dispatch uses descending priority, then ascending identifier. Durability, damage, and healing pass the running value in order. Damage and healing also receive the original value.
5. Translate `traits.<identifier>.name` and `.description`. Tools and parts show levels and display descriptions with Alt.

## State and client/server rules

- The inventory callback uses `inventoryTick(..., CompoundTag traitTag)`. This tag is a separate copy. The dispatcher writes it back only when changed and retains other traits and unknown data. Read `Level` for the effective level and let the dispatcher write the state.
- In other server callbacks, use `ToolTraits.updateTraitData(tool, identifier, data -> ...)` to save cooldowns and counters. Editing a copy returned by `getTraitsTag()` does not persist it. Avoid recursive writes to the same trait.
- Inventory, wear, attack, and block interaction effects run on the server. Mining speed is calculated on both sides and must leave world and persistent state untouched.
- `beforeBlockBreak` runs during the block-break event. Another subscriber may still cancel it. `afterBlockBreak` runs in the common tool's `mineBlock` before wear, and `afterHit` runs after a hit and before wear. Broken tools skip these mining and attack traits.
- Material repair works on a tool copy. `onRepair` receives actual healing and updates trait state carried by the result. Preview and commit can both calculate, so `onToolHeal` and `onRepair` must be deterministic and leave world, player, registries, and input inventory untouched. The entity passed to `onToolHeal` is null for material repair. `onRepair` should only update the copy's trait state. Zero healing consumes nothing, and the repair loop stops if no progress is made.

## Tools, modifiers, and extensions

See the [tool catalog](TOOLS.md) and the [registration API with KubeJS examples](TOOL_API.md).

- `modifyStats` changes one recalculation's `ToolStats` and must be deterministic. It must leave inventory and shared materials untouched.
- `modifyAttack`, `onBlockDrops`, and `onDamageTaken` use native attack, drop, and defense events.
- `onProjectileLaunch` handles launch parameters and `onProjectileTick` updates custom projectiles.
- Bow limbs merge Bow/Head, crossbow bodies merge Handle/Extra, and arrowheads merge Head/Projectile. They fall back to defaults only when all explicit lists are missing.
- Built-in traits include Ecological, Cheap, Cheapskate, Momentum, Magnetic, Sharp, Poisonous, Insatiable, Alien, Aridiculous, Aquadynamic, Inscribed, Stonebound, Softy, Splitting, and Hovering.
- Transient charge, cooldown, and moss XP changes do not retrigger hand animations or reset mining. Material, modifier, and broken-state changes still refresh.

## Wood and stone baseline

- All wooden parts grant Ecological. Each inventory tick has a 1-in-400 chance to restore one durability. In-use and broken tools are skipped.
- Stone heads grant Cheapskate. New tools get 80% of final durability, rounded down with a minimum of 1.
- Other stone parts grant Cheap. Each repair gains an extra 5% of its original amount, rounded down. Duplicate sources do not stack.

## Manual regression checks

- A new all-wood pickaxe has one level of Ecological. A pickaxe with only a wooden binding still has Ecological.
- A new all-wood pickaxe has 75 maximum durability. An all-stone pickaxe has one level each of Cheapskate and Cheap, and 80% of `(120 + 20) × 0.5 - 50 = 20`, giving 16 durability.
- A damaged wooden pickaxe slowly heals in inventory. Full, in-use, and broken tools do not passively heal.
- Tool Station repair receives the Cheap bonus. Repeated preview refreshes preserve the input and materials until the result is taken.
- Tools, handles, heads, and bindings show the relevant traits and Alt descriptions. Shift still shows stats.
- Unknown traits or illegal levels do not crash a tool. Unknown data survives other trait updates.
- In multiplayer, repair, inventory healing, mining, and attack callbacks execute once. Existing tools keep saved stats.
