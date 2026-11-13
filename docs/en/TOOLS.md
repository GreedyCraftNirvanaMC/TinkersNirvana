# Tools and parts

[README](../../README.md) · [简体中文](../zh-CN/TOOLS.md)

For Minecraft 1.21.1 / NeoForge. Tools share the material components, trait system, Tool Station, and Smeltery. The existing pickaxe ID is `tconstruct:pickaxe/pickaxe`.

## Crafting workflow

1. Arrange two sticks and two planks in a checkerboard to make four blank patterns. Use a pattern to cycle parts or sneak-use to cycle backward.
2. Craft a Stencil Table, Part Builder, and Tool Station from blank patterns. Select a part at the Stencil Table, then put its pattern and material in the Part Builder. Taking a part preserves the pattern and turns spare material into shards. The Tool Station's repair mode also supports the existing batch pattern processing.
3. Wood, stone, flint, bone, and cactus can be worked directly. For metal parts, pour molten metal into a cast on the Casting Table. Make the metal cast over an existing part. Clay casts are consumed on use.
4. In an assembly mode, place parts where the slot icons show and take the finished tool. Three Seared Bricks blocks, four metal blocks, and a Tool Station make a Tool Forge. Fourteen metal types, including iron, are supported.

Processing uses the full material value. High-value ingredients such as logs can produce multiple parts. If the value cannot be divided evenly, the recipe leaves the input intact. Use smaller ingredients such as planks or shards. Repair mode puts the tool in the center. Assembly slots follow each tool's part layout.

## Workshops, storage, and repair

The Crafting Station has nine inputs, a vanilla recipe book, and access to adjacent storage. Use the top tabs to switch between adjacent stations. The Pattern Chest stores one of each pattern or cast. The Part Chest separates parts by material and component. Both chests have 256 slots and pagination.

With a Pattern Chest attached, left-click a pattern in the Part Builder sidebar to swap it with the current pattern. The Tool Station can access a connected Part Chest. By default, breaking a chest retains its inventory, controlled by `general.chestsKeepInventory`. Workshop wood and metal finishes follow the crafting ingredients and persist through placement, drops, and pick block.

In repair mode, you can replace parts while retaining wear, separate modifiers, and compatible trait state. Use the name field to name a new tool or rename an existing one. A sharpening kit plus flint can raise or lower harvest level. A kit alone can repair a tool in the crafting grid.

When a main-hand harvesting tool is unsuitable for a block, the game can use an effective offhand tool. The tool actually used determines durability loss, drops, and traits. A suitable main-hand tool still takes priority. `client.renderInventoryInWorld` controls items displayed on workshop surfaces. The three `client.listAll*` settings control material variants in creative tabs.

See [Materials and You](BOOK.md), [gadgets](GADGETS.md), and [world generation](WORLD.md) for related features.

All modes share six physical input slots and one result slot. Switching modes keeps the inputs. Leftover items that do not fit the new recipe block assembly. Right-clicking a batch output takes the full batch. If quick pickup only partly fits in inventory, the rest drops to the player as with vanilla crafting.

## Tool catalog

The table lists parts in material-data order. In-game slot icons indicate where each part goes.

| Tool | Parts | Station | Use or special action |
| --- | --- | --- | --- |
| Pickaxe | Tool rod, pickaxe head, binding | Tool Station | Mining |
| Shovel | Tool rod, shovel head, binding | Tool Station | Digging and paths |
| Hatchet | Tool rod, axe head, binding | Tool Station | Chopping, stripping, scraping, and dewaxing |
| Mattock | Tool rod, axe head, shovel head | Tool Station | Axe/shovel harvesting and tilling |
| Kama | Tool rod, kama head, binding | Tool Station | Harvesting mature crops and shearing |
| Hammer | Tough tool rod, hammer head, two large plates | Tool Forge | Area mining and bonus damage to undead |
| Excavator | Tough tool rod, excavator head, large plate, tough binding | Tool Forge | Area digging |
| Lumber Axe | Tough tool rod, broad axe head, large plate, tough binding | Tool Forge | Chops connected logs |
| Scythe | Tough tool rod, scythe head, tough binding, tough tool rod | Tool Forge | Area harvesting, shearing, and attacks |
| Broadsword | Tool rod, sword blade, wide guard | Tool Station | Vanilla sweeping attack |
| Longsword | Tool rod, sword blade, hand guard | Tool Station | Hold and release use to lunge, except while gliding |
| Rapier | Tool rod, sword blade, cross guard | Tool Station | Fast attacks, partial armor piercing, and use to backstep |
| Frying Pan | Tool rod, pan head | Tool Station | Charge and release use for knockback |
| Battlesign | Tool rod, sign head | Tool Station | Block and reflect projectiles from the front |
| Cleaver | Tough tool rod, large sword blade, large plate, tough tool rod | Tool Forge | Heavy hits, built-in beheading, and offhand restrictions |
| Shortbow | Two bow limbs, bowstring | Tool Station | Material-dependent draw and range |
| Longbow | Two bow limbs, large plate, bowstring | Tool Forge | Higher-powered bow |
| Crossbow | Tough tool rod, bow limb, tough binding, bowstring | Tool Forge | Stores ammunition when loaded and fires on the next use |
| Arrow | Arrow shaft, arrowhead, fletching | Tool Station | Repairable ammunition |
| Bolt | Coated bolt core, fletching | Tool Forge | Repairable ammunition |
| Shuriken | Four knife blades | Tool Forge | Throwable and recoverable |

Make a bolt core by coating an arrow shaft with two units of castable material on a Casting Table. Assembly and disassembly preserve both shaft and coating materials. Sharpening kits and shards are also listed in the parts catalog.

Sneak to harvest one block with an area tool. Each extra block uses the vanilla player break flow and respects protection events and drops. Unsuitable or unbreakable blocks, block entities, and unloaded chunks are skipped. Connected-log harvesting has distance and count limits.

Ammunition is stored as durability, and each shot uses ten points. Recovery first refills a stack with the same materials and modifiers. Otherwise, it gives a new item with one shot remaining. Extra split projectiles cannot be recovered. Vanilla and tipped arrows use vanilla arrow entities.

## Repair, modifiers, and disassembly

- In default mode, put the tool in the center and materials or modifiers in the other slots. Preview changes a copy. Inputs are consumed when taking the output.
- Common modifiers use redstone, quartz, lapis, diamonds, emeralds, blaze powder, pistons, fermented spider eyes, Consecrated Soil, and similar ingredients. Lapis retains staged progress. Silk Touch and Luck are mutually exclusive.
- Reinforcements, silky jewels, lava crystals, and expanders have crafting recipes. Wither skeletons drop necrotic bones. Coat a bone in blood to make a bloodbone.
- Use a moss ball on a bookshelf or another enchanting-power block. It costs five XP levels to turn it into Mending Moss. An upgraded tool stores repair energy from XP and repairs gradually in the hotbar or offhand.
- A sharpening kit plus flint sets a higher or lower harvest level while keeping the original material traits.
- Emboss once using a part, green, blue, and magma slime crystals, and a gold block. It adds the trait for that part. Repeated or incompatible embossing is rejected.
- Disassemble a fully repaired tool to recover parts. Taking the first part commits the operation. Changing modes before that returns the original tool. A broken tool remains as an item and can be repaired.
- Soulbound returns the tool after death. Other drops follow vanilla rules.

Hold Shift for stats, materials, and free modifier slots. Hold Alt for trait descriptions. Stats follow Antique definitions. Existing tools retain saved stats and unknown components survive repair, modification, and recalculation.

## Extensions and resources

- [Java and KubeJS API](TOOL_API.md)
- [Trait lifecycle](TRAITS.md)
- [Manual playtest](PLAYTEST_TOOLS.md)
- Run `scripts/generate_antique_tool_resources.py --antique <Antique-source>` to generate models, recipes, translations, and texture provenance.
- Run `scripts/gimp_restyle_tools.py` in the GIMP MCP PyGObject context and call `restyle_tools(antiqueRoot, outputRoot, start, end)` to export textures in batches.
- Run `scripts/audit_antique_tool_resources.py --antique <Antique-source>` to check resource references, PNGs, source hashes, alpha silhouettes, and bilingual keys.

Textures retain Antique silhouettes, part layers, broken states, and draw stages. Modern iron-sword shading was a reference. The creative modifier crystal and status icons were drawn in GIMP. Tool sounds use Antique's original OGG files.

## Tool and crafting settings

Edit `config/tconstruct-common.toml`.

| Key | Default | Effect |
| --- | --- | --- |
| `general.craftCastableMaterials` | `false` | Allow castable materials in the Part Builder |
| `general.addFlintRecipe` | `true` | Craft one flint from three gravel after recipe reload |
| `general.craftingStationBlacklist` | `[]` | Exclude adjacent storage by block ID, block-entity type ID, or full class name |
| `tools.deconstructTools` | `true` | Allow disassembly |
| `tools.initialModifierSlots` | `3` | Base modifier slots for new and existing tools; range 0 to 2147483647. Material traits, extra-slot modifiers and tool levels are added separately. |
| `tools.vanillaToolBreaking` | `false` | Destroy tools when durability runs out |
| `tools.oldMattockAndKama` | `false` | By default, the mattock makes paths and the kama tills. Enable to make the mattock till instead. |
| `tools.repairToolsOnAnvils` | `false` | Allow tools in vanilla anvils |
| `tools.autosmeltFortuneInteraction` | `false` | Apply Antique's extra Fortune yield to autosmelting |
| `tools.despawnProjectile` | `1200` | Ticks before grounded custom ammunition despawns. Non-destructible ammunition uses one tenth of this time. |

Changing the base slot count does not remove installed modifiers or reset tool levels. If the total falls below the slots already used, available slots are shown as zero and further slot-consuming modifiers are rejected. Keep this common setting identical on the server and clients so tooltips match server validation; restart after editing it.

Select a part before using a blank mold for processing. See the [tool extension API](TOOL_API.md) for beheading drops and entity melting.
