# Smeltery

The Smeltery is available in the **Smeltery** creative tab on Minecraft 1.21.1 / NeoForge.
It uses TinkersAntique's structure rules, fluid quantities, heating and casting rules,
GUI textures, block textures, and sound assets.

Build a rectangular seared-block floor, then build walls around an empty interior.
The inside can be 1–9 blocks wide and 1–9 blocks deep. Put one Smeltery Controller
and at least one Seared Tank, Gauge, or Window in the walls. The controller must
face outward. Corners, the outer rim under the walls, and a roof are optional.
The controller can be above the bottom wall layer; its floor search reaches down
64 blocks. Glass, drains, and seared ladders work in the walls; only full seared
stone/brick variants work in the floor.

Each interior block adds one single-item input slot and 1,152 mB of shared fluid
capacity. Fill a wall tank with lava or blazing blood. Right-click the active
controller to open the Smeltery. The left inventory scrolls; the main tank shows
each fluid separately. Click a fluid layer to make it the drain's default output.
Hold Shift over a fluid to display bucket quantities. The per-slot bars show
heating, missing recipes, missing fuel, insufficient heat, or insufficient space.

Place a Faucet against a Smeltery Drain, with a Casting Table or Casting Basin
directly below. Right-click the faucet to pour; right-click again to stop after
the current transaction. A rising redstone signal also activates the faucet.
Basins cast blocks. Tables cast ingots, nuggets, gems, and the tool parts already
provided by this project. Pour gold, aluminum brass, or brass over a suitable item
or part to create a reusable cast. Clay can create disposable tool-part casts.
Casting blocks accept hopper input and allow completed outputs to be extracted.
Seared tanks retain their fluid when broken and can also hold fluid as items.

Channels route fluid downward first, then split it among their side outputs.
Click an arm to cycle its direction; Shift-click cycles backward. Click the center
to toggle the bottom output. Attached levers/buttons can control the bottom gate.

Configuration is in `config/tconstruct-common.toml`, under `[smeltery]`:

| Key | Default |
| --- | --- |
| `heatItemsTickrateSmeltery` | `4` |
| `liquidTransferRate` | `6` |
| `oreToIngotRatio` | `2.0` |
| `obsidianAlloy` | `true` |
| `claycasts` | `true` |
| `allowBrickCasting` | `true` |
| `steelAlloy` | `true` |
| `drainGaseousFluids` | `true` |
| `minFluidHeight` | `3` |
| `maxSmelteryItemRenders` | `-1` (unlimited) |
| `temperatureCelsius` | `true` |

`oredictMeltingIgnore` contains item IDs or `#item_tag` IDs that may be ignored
when deriving melting recipes from crafting ingredients. Its defaults allow
wood, sticks, string, redstone, and chests to be omitted, matching Antique's
crafting-to-melting scan. Derived recipes are rebuilt after datapack reloads.

For mod integrations, initialize `SmelteryRecipes` after item/fluid registration,
then append melting, casting, fuel, alloy, or entity-melting entries to its public
registries. Common `c:ingots`, `c:nuggets`, `c:storage_blocks`, and `c:ores` material
tags are recognized, and casting can resolve common plate/gear tags. Subscribe to
`SmelteryEvents.Melting`, `Casting`, and `Casted` on the NeoForge game event bus to
customize results or reject a melting/casting operation. These callbacks execute
on the server; a rejected operation retains its inputs.
