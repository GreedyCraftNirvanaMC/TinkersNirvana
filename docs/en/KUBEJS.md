# KubeJS materials and traits

[README](../../README.md) · [Java addons](TOOL_API.md) · [简体中文](../zh-CN/KUBEJS.md)

For Minecraft 1.21.1, NeoForge and KubeJS 2101.7.2, verified with build.381. Install KubeJS and its declared dependencies, then place scripts in `kubejs/startup_scripts/`. Material and trait definitions are startup content. Restart the client and server after editing them and use matching startup scripts on both sides.

## Register materials

```js
StartupEvents.registry('tconstruct:material', event => {
    event.create('kubejs:amethyst')
        .color(0xA77CE3)
        .craftable(true)
        .head(1000, 8.0, 10.0, 8)
        .handle(1.1, 100)
        .extra(50)
        .item('minecraft:amethyst_shard', 1)
        .trait('kubejs:reinforced_edge', 1)
})
```

Cross-references are resolved after registration. Materials may reference traits in other files and items registered by the same batch of startup scripts.

| Method | Arguments |
| --- | --- |
| `head(durability, speed, attack, tier)` | Head durability, mining speed, attack damage, mining level |
| `handle(multiplier, durability)` | Handle durability multiplier and bonus |
| `extra(durability)` | Extra durability bonus |
| `bow(drawSpeed, range, damage)` | Draw speed, range multiplier, damage bonus |
| `bowString(multiplier)` | Bowstring multiplier |
| `shaft(multiplier, bonusAmmo)` | Shaft ammo multiplier and bonus |
| `fletching(accuracy, multiplier)` | Accuracy (0–1), ammo multiplier |
| `craftable(boolean)` | Enable part-builder crafting; default false |
| `castable(boolean)` | Set the casting flag; default false |
| `castingFluid(id)` | Bind a registered fluid and install part melting plus metal/clay cast recipes |
| `item(id, value)` | Material units supplied by one input item |
| `itemTag(id, value)` | Material units supplied by one item matching the tag |
| `representativeItem(id)` | Book icon and name fallback |
| `requireItems(...tags)` | Show the material when every specified item tag is nonempty |
| `trait(id, level)` | Default material trait |
| `partTrait(stat, id, level)` | Trait for an explicit part stat |
| `headTrait/handleTrait/extraTrait/bowTrait(id, level)` | Part-specific shortcuts |

Stat names are `Head`, `Handle`, `Extra`, `Bow`, `BowString`, `Shaft` and `Fletching`. An explicit part trait list overrides the default list for that part.

## Create traits

```js
StartupEvents.registry('tconstruct:trait', event => {
    event.create('kubejs:reinforced_edge')
        .displayName('Reinforced Edge')
        .description('Increases attack damage and reduces durability consumption')
        .color(0xA77CE3)
        .maxLevel(3)
        .priority(100)
        .stacking('sum')
        .modifyStats(ctx => {
            ctx.stats.attack += ctx.level * 1.5
        })
        .onToolDamage(ctx => {
            ctx.amount = Math.max(0, ctx.amount - ctx.level)
        })
        .inventoryTick(ctx => {
            if (ctx.selected) {
                ctx.data.putInt('SelectedTicks', ctx.data.getInt('SelectedTicks') + 1)
            }
        })
})
```

Defaults are maximum level 1, priority 100 and `max` stacking. Higher priority executes first, with internal ID breaking ties.

| Stacking mode | Behavior |
| --- | --- |
| `max` | Keep the highest contributed level |
| `sum` | Add all contributions |
| `distinct` | Add each distinct initial strength once |

Stacking applies during assembly and part merging; existing tools retain their saved level records. The result is capped at `maxLevel`. Edits to existing traits inherit their original stacking behavior.

## Modify or remove traits

```js
TinkerEvents.modifyTraits(event => {
    event.modify('tconstruct:fractured', trait => {
        trait.color(0xFFAA55)
        trait.replaceHandler('modifyStats', ctx => {
            ctx.stats.attack += 3
        })
        trait.appendHandler('onToolDamage', ctx => {
            ctx.amount = Math.max(0, ctx.amount - 1)
        })
    })

    event.remove('tconstruct:slimey_blue')
})
```

`replaceHandler(name, callback)` replaces one hook. `appendHandler` runs after existing behavior, `clearHandler(name)` clears a hook, and `clearHandlers()` clears all behavior.

Some builtins also have native rules outside lifecycle callbacks, such as Squeaky's Silk Touch and modifier restrictions or Splitting's extra projectile. `clearHandler('rules')` disables those rules. The rules hook accepts clearing only; implement new effects with the relevant lifecycle callbacks.

Removed traits are skipped by material bindings and existing tools. Their saved state is retained so registering the same ID can restore it. Existing inventory tools rebuild cached properties on a server inventory tick after definitions change. Lower maximum levels cap effective levels while retaining saved levels.

## Modify materials

```js
TinkerEvents.modifyMaterials(event => {
    event.modify('tconstruct:wood', material => {
        material.removeTrait('tconstruct:ecological')
        material.addTrait('kubejs:reinforced_edge', 1)
        material.head(100, 3, 1, 1)
    })
})
```

The editor exposes material creation methods plus `addTrait`, `removeTrait` and `clearTraits`. Material binding changes affect subsequent assembly and part replacement. Existing tools retain their own trait records, which resolve current definitions. Builtins accept both `wood` and `tconstruct:wood` while preserving saved IDs.

## Callback contexts

Every context has `tool` and numeric `level`. Other fields depend on the hook and may be null. Set context fields to provide results; callbacks do not return numeric results.

| Hook | Fields and results |
| --- | --- |
| `modifyModifierSlots` | `originalAmount`, mutable material bonus slot `amount` |
| `onExperienceDrop` | `entity`, LivingExperienceDropEvent `event` |
| `onProjectileHitBlock` | `projectile`, `pos`, BlockHitResult `event` |
| `modifyStats` | Mutable `stats` |
| `modifyAttack` | `attacker`, `target`, `originalDamage`, mutable `damage` |
| `modifyDurability` | `originalAmount`, mutable maximum durability `amount` |
| `onToolDamage/onToolHeal` | `entity`, `originalAmount`, mutable `amount` |
| `onRepair` | Healed `amount`; operates on a preview tool |
| `miningSpeed` | `entity`, BreakSpeed `event`; call `event.setNewSpeed(...)` |
| `beforeBlockBreak` | `entity`, `pos`, `state`, BreakEvent `event` |
| `afterBlockBreak` | `world`, `entity`, `pos`, `state`, `effective` |
| `onBlockDrops` | `world`, `pos`, `state`, BlockDropsEvent `event` |
| `onDamageTaken` | `entity`, LivingIncomingDamageEvent `event` |
| `inventoryTick` | `world`, `entity`, `slot`, `selected`, `data` |
| `afterHit` | `attacker`, `target`; follows existing hit-hook timing |
| `afterDamage` | `attacker`, `target`, `damage`, LivingDamageEvent.Post `event` |
| `onProjectileLaunch/onProjectileTick` | `projectile` |

Inside `modifyStats`, `ctx.attribute(modifierId, attributeId, amount, operation, slot)` adds native attributes using string IDs. Operations are `add_value`, `add_multiplied_base` and `add_multiplied_total`; slots include `mainhand`, `offhand` and `hand`.

`stats` exposes `durability`, `harvestLevel`, `miningSpeed`, `attack`, `attackSpeed`, `drawSpeed`, `range`, `projectileDamage`, `accuracy` and growth fields.

Stat, durability and repair calculations may run repeatedly for previews. Keep calculations deterministic and perform world interactions in actual event hooks. Mining speed and projectile callbacks can run on both sides. Projectile contexts include `world`; check `ctx.world.isClientSide` before server-only world mutations. Inventory ticking runs on the server and automatically saves `data`, a CompoundTag specific to that tool and trait. Other contexts are temporary. Callback exceptions log the trait ID and hook and disable the failing callback until restart. Recoverable numeric, stat and independent data changes are restored.

## Resource-pack localization

Create language files at `assets/kubejs/lang/en_us.json` and `assets/kubejs/lang/zh_cn.json`.

```json
{
  "material.kubejs.amethyst.name": "Amethyst",
  "traits.kubejs.reinforced_edge.name": "Reinforced Edge",
  "traits.kubejs.reinforced_edge.description": "Increases attack damage and reduces durability consumption"
}
```

Colons and slashes in custom IDs become dots in default translation keys. Materials and traits accept `translationKey(key)`; traits also accept `descriptionKey(key)`. Existing builtin keys remain supported.

Material names resolve in this order: current-language material translation, explicit display name, representative item name, first valid `item` name, material ID. Item names remain translatable components. Set a representative item for tag-only inputs. Trait names use translation, default name, then ID. Descriptions use translation, default description, then an empty component.

## Other APIs

Existing `TinkerContent` calls remain available for older scripts. See the [Java addon API](TOOL_API.md). Native recipes such as drying can be added through `ServerEvents.recipes` and data packs. Material `castingFluid` derives melting and metal/clay casting rules from registered part costs.
