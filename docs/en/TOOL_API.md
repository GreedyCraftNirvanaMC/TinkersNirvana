# Tool extension API

[README](../../README.md) · [简体中文](../zh-CN/TOOL_API.md)

The entry point is `com.gctn.tconstruct.api.TinkerContent`. Register materials, traits, and modifiers at startup and keep the client and server definitions in sync. Restart after editing startup scripts. Global definitions should not change during player interactions or server ticks.

## KubeJS material example

Place this example in `kubejs/startup_scripts/tinker_materials.js`. It uses KubeJS's `StartupEvents.postInit` and the existing vanilla amethyst shard. This mod does not require KubeJS at build time.

```js
const TinkerContent = Java.loadClass('com.gctn.tconstruct.api.TinkerContent')

StartupEvents.postInit(event => {
  const amethyst = TinkerContent.registerMaterial('kubejs:amethyst', 0xA77CE3, true, false)
  // Durability, mining speed, attack, and Antique harvest level.
  TinkerContent.head(amethyst, 480, 6.5, 4.5, 3)
  TinkerContent.handle(amethyst, 0.9, 40)
  TinkerContent.extra(amethyst, 90)
  TinkerContent.bow(amethyst, 0.8, 1.1, 2)
  TinkerContent.item(amethyst, 'minecraft:amethyst_shard', 1)
  TinkerContent.trait(amethyst, null, 'lightweight', 1)
  TinkerContent.trait(amethyst, 'Head', 'jagged', 1)
})
```

Add a resource-pack translation for `material.kubejs:amethyst.name`. Supported stats include `Head`, `Handle`, `Extra`, `Bow`, `BowString`, `Shaft`, and `Fletching`. Use `Material.addMaterialStats` for additional stat types.

For `item(material, id, value)` and `tag(material, id, value)`, the value is material units per ingredient. One unit equals 144 mB in the Smeltery. Tags are resolved during matching and support reloads. Parts can also supply material, except coated bolt cores.

Registered materials are visible by default. `material.requireItems('c:ingots/my_metal')` requires that tag to be nonempty before the material appears in the book and creative catalog. When passing multiple tags, all must be populated. Reopen the catalog after a tag reload. Java add-ons can use `material.setAvailability(BooleanSupplier)` for other conditions. Built-in compatibility materials such as lead, silver, and electrum use Antique's ingredient conditions while keeping their registry data available. The book reads live stats and traits when opened.

The book prefers a matching ingot as the representative icon, followed by a one-unit ingredient and then another ingredient or part. Override it with `TinkerContent.representativeItem(material, 'mymod:my_ingot')` or supply a stack with components using `material.setRepresentativeItem(() -> new ItemStack(...))`. The icon does not become a crafting ingredient.

`HarvestLevels` uses 0 for wood, 1 for stone, 2 for iron, 3 for diamond, and 4 or above for the native netherite-tier restriction. Player-facing names follow Antique. Bow limbs need `Bow` and `Head`, and crossbow bodies need `Handle` and `Extra`. Provide the corresponding stats for usable bow, crossbow, and ammunition parts.

## Casting materials

If this or another mod has already registered the fluid, add the following to a startup script.

```js
TinkerContent.castingMaterial(material, 'kubejs:molten_amethyst')
```

This registers metal cast, disposable clay cast, and remelting recipes for eligible parts. The fluid must already exist. Use the public `SmelteryRecipes` registry for alloys, fuels, and more complex casting.

## Java traits

Extend `AbstractTraits` or `LeveledTraits` and register through `TinkerContent.registerTrait`. Namespace the identifier to avoid conflicts.

| Callback | Purpose and constraints |
| --- | --- |
| `modifyStats` | Modify the current `ToolStats` during assembly or recalculation. Must be deterministic and have no world side effects. |
| `modifyDurability` | Adjust final base durability. |
| `onToolDamage` / `onToolHeal` | Adjust damage or healing in sequence. The entity is null for material repair. |
| `onRepair` | Update trait state on the preview copy only. |
| `miningSpeed` | Runs on both sides and reads the tool and world. |
| `beforeBlockBreak` / `afterBlockBreak` | Check before a player breaks a block or apply effects after success. |
| `modifyAttack` / `afterHit` | Handle direct attacks and projectiles that carry tool data. |
| `afterDamage` | Read actual damage after armor and absorption. |
| `onBlockDrops` | Modify the native drop event. |
| `onDamageTaken` | Apply defense effects while holding or using a tool. |
| `onProjectileLaunch` / `onProjectileTick` | Set native arrow launch parameters and update custom tool projectiles. |
| `inventoryTick` | Update per-trait state on the server. |

`ToolTraits.updateTraitData` copies and writes the selected trait state, preserving other traits and unknown data. Keep cooldowns and counters off shared trait definitions. Translate `traits.<identifier>.name` and `traits.<identifier>.description`.

Set part-specific traits with `trait(material, position, id, level)`. Null denotes the default list. Parts with several stat positions merge explicit lists and use the default only when all explicit lists are missing. Ordinary traits take the highest level. By default, `LeveledTraits` counts each distinct starting level once.

## Modifiers and tool definitions

`TinkerContent.registerModifier(new ToolModifiers.Definition(id, maxLevel, unitsPerLevel, ingredient, modifyStats))` adds a modifier. The ingredient is a `Predicate<ItemStack>`. The `BiConsumer<ToolStats, Integer>` receives the total units invested. Preview and commit can both recalculate, so callbacks must not consume items, use randomness, or change the world.

`ToolDefinition`, `ToolBuilder.assemble`, and `AntiqueTool` let Java add-ons share material calculations, components, durability, and traits. Register new items with your own DeferredRegister before item registration closes. Call `AntiqueParts.register` and `AntiqueTools.register` before registry closure as well. A different tool UI needs its own menu. Built-in station modes are defined in `Mode`.

Material and modifier state use `tconstruct:tinker_material` and `tconstruct:tinker_traits`. Recalculation preserves unknown components. Changes to built-in materials affect new or explicitly recalculated tools. They do not reset old tools at login.

## Native attributes and modifier ingredients

`ToolStats.attribute(holder, id, amount, operation, slotGroup)` adds a native item attribute. Java takes `Holder<Attribute>`, `AttributeModifier.Operation`, and `EquipmentSlotGroup`. KubeJS can use string arguments.

```js
const ToolModifiers = Java.loadClass('com.gctn.tconstruct.tools.ToolModifiers')
const Items = Java.loadClass('net.minecraft.world.item.Items')
StartupEvents.postInit(event => {
  TinkerContent.registerModifier(new ToolModifiers.Definition(
    'kubejs:long_reach', 1, 1,
    stack => stack.is(Items.AMETHYST_SHARD),
    (stats, units) => TinkerContent.attribute(stats, 'kubejs:long_reach',
      'minecraft:player.block_interaction_range', units, 'ADD_VALUE', 'MAINHAND')
  ))
  TinkerContent.modifierItem('haste', 'kubejs:compressed_redstone', 81)
  TinkerContent.modifierTag('sharpness', 'kubejs:quartz_dusts', 1)
})
```

The attribute must already exist. Namespace the modifier ID and keep it unique within each attribute and equipment slot group. Operations are `ADD_VALUE`, `ADD_MULTIPLIED_BASE`, and `ADD_MULTIPLIED_TOTAL`. Attributes persist and sync through tool components and are regenerated when recalculated. Removing a modifier also removes its attributes. Ingredient value is the number of cumulative units per item. Tags support reloads.

## Drying and station processing

Drying uses the native recipe manager and accepts data-pack and KubeJS recipes.

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'tconstruct:drying',
    ingredient: { item: 'minecraft:apple' },
    result: { id: 'kubejs:dried_apple', count: 1 },
    time: 1200
  })
})
```

`time` is the base duration in ticks, scaled by `gadgets.dryingTimeMultiplier`. Set `leather: true` to honor the leather-drying toggle.

Use `com.gctn.tconstruct.tables.recipe.StationProcessingRecipes.register(id, itemPredicate, planner)` to process non-tools at the Tool Station. Slot 0 is the target and slots 1–5 are ingredients. A pure planner returns `Plan(result, int[6], byproducts)` or null. The first consumption count must be 1. Preview must leave inputs and world untouched. Commit revalidates, consumes inputs, returns native container items and byproducts, and drops overflow beside the station.

If an add-on calls `Plan.consume` with a plain `Container`, handle the returned overflow list. Only a station block entity with a world drops overflow automatically.

Workstations and racks store their finish in `tconstruct:table_material`. A `tconstruct:table` recipe's `material` ingredient identifies the input that provides the finish. Tags can include add-on wood. See `data/tconstruct/recipe/workshop` for examples.

## Beheading and entity melting

`config/tconstruct-common.toml` supports these lists.

```toml
[tools]
mobHeadDrops = ["minecraft:zombie;true;minecraft:zombie_head", "#kubejs:stone_mobs;false;minecraft:cobblestone;3"]
[smeltery]
entityMelting = ["minecraft:iron_golem;true;tconstruct:iron;18", "#kubejs:stone_mobs;false;tconstruct:stone;50"]
```

The first field is an entity ID or `#entity_type_tag`. The second controls class-subtype matching. Tags always match dynamically. For head drops, the optional fourth field is a maximum count, randomized from 1 to that value. One matching drop is chosen at random. Player heads retain the profile, and an empty list disables configured drops. Add Java rules with `TinkerContent.headDrop(entityPredicate, outputFunction)`.

For entity melting, the last field is mB per two damage points. Later matches take precedence, and an empty list or unmatched entity falls back to blood. Java rules in `SmelteryRecipes.ENTITY_MELTING` take priority. Legacy names such as `snowman` and `villager_golem` have aliases. Use full modern IDs in extensions.

See [world generation](WORLD.md) for tree extensions and [Materials and You](BOOK.md) for custom pages and multiblock previews.
