# Smeltery

Build a rectangular, open-top Smeltery using a seared brick or seared stone floor, one Smeltery Controller facing outward, and at least one Seared Tank in a wall. The inside must be air. Interior width and depth can each be 1–9 blocks, including rectangular and even sizes; the walls must be at least one block high. Corners, a frame around the floor, and a ceiling are not required. Seared Glass and Smeltery Drains can replace wall blocks. The controller can be above the lowest wall layer.

Craft grout from sand, gravel, and a clay ball, then smelt it into Seared Bricks. Craft the controller, tanks, drains, faucets, casting table, and basin using the recipes in the recipe book. Right-click a Seared Tank with a lava bucket to fuel it. Each tank holds four buckets. Fuel is spent only while processing items, alloys, or living entities.

Open the controller to insert meltable items, one per slot. The inventory and fluid capacity grow with the interior volume. Use the arrows to browse inventory pages. Click a liquid layer in the fluid display to select it for draining. Items dropped inside are also collected when they have a melting recipe. A fueled Smeltery damages living entities inside and collects blood.

Place a faucet against the outside of a drain and a casting table or basin directly below the faucet. Right-click the faucet, or give it a redstone pulse, to pour. A second right-click stops it. Casting tables accept a single item as a mold; an empty-hand click removes it while no fluid is present. Make an Ingot Cast by pouring two ingots' worth of gold over any ingot; the template ingot is consumed. Reusable Ingot Casts turn molten metal into ingots. Empty basins cast storage blocks and other block recipes. Take cooled results by right-clicking, or extract them with a hopper.

Default melting/casting covers iron, gold, copper, cobalt, glass, obsidian, and seared stone. Ores and raw iron/gold/copper produce two ingots, and cobalt ore produces two cobalt ingots. One ingot is 144 mB. Adding water and lava through a drain alloys them into obsidian. Metal fluids are held in tanks and transferred through drains and fluid handlers; this port does not register placeable molten-fluid blocks or buckets.

Drains expose NeoForge fluid capabilities. The controller accepts item automation, Seared Tanks accept lava through fluid handlers, and casting blocks expose mold/input and result slots. Automation can insert molds and extract finished results; it cannot extract reusable molds. Casting blocks reject incompatible fluids and lock the mold while fluid is present. Partial pours can be recovered through their fluid capability.

Changing the structure updates its inventory and capacity. Items that no longer fit are dropped at the controller. Stored liquid is retained even when the structure becomes smaller or invalid; excess liquid must be drained before more can be inserted. Inventories larger than 256 slots use integer slot indices for world saves. Processing pauses when required chunks are unloaded. A component cannot belong to two controllers at once. Breaking the controller drops its remaining items and destroys its stored liquid.

## Data packs

Recipes live in `data/<namespace>/recipe/` and use the `tconstruct:smeltery` recipe type. `operation` is one of `MELTING`, `ALLOY`, `CASTING_TABLE`, or `CASTING_BASIN`.

```json
{
  "type": "tconstruct:smeltery",
  "operation": "MELTING",
  "ingredient": { "tag": "c:ingots/iron" },
  "fluid_result": { "id": "tconstruct:molten_iron", "amount": 144 },
  "temperature": 769,
  "time": 100
}
```

`MELTING` needs an ingredient and `fluid_result`. `ALLOY` needs a `fluids` list and `fluid_result`; all inputs must fit one atomic transaction. Casting needs one entry in `fluids` and an item-stack `result`, with an optional `ingredient` for the mold. Omit `ingredient` for an empty basin or table. Set `consume_input` to consume the mold/template on completion. Casting `time` is cooling time in ticks; melting `time` is heating time in ticks. The current lava fuel supports recipes up to temperature 1000. Ingredients support the standard NeoForge ingredient codecs, including tags.

Data-pack reloads take effect on the next processing tick. In-progress heating/cooling restarts when its loaded recipe definitions change; item inputs, molds, stored fluids, and remaining fuel are retained.

Add full-cube Smeltery component blocks to `tconstruct:smeltery_floor` and/or `tconstruct:smeltery_wall` block tags. Custom structure blocks also need a compatible component block entity to participate in ownership tracking.

## In-game verification

Check formation with different controller orientations, 1×1 and 9×9 interiors, rectangular footprints, missing corners, a raised controller, missing fuel tanks, and blocked interiors. Check that an adjacent second controller cannot share components.

Run a complete lava → iron melting → drain selection → ingot/block casting cycle. Check full-tank rejection, incompatible casting fluid rejection, stopping/restarting a pour, partial-fluid recovery, shift-click and hopper transfers, simultaneous viewers on different pages, resizing, and breaking components. Save/reload the world and cross chunk boundaries while items and several fluid layers are stored. Verify the GUI, molten layers behind glass, tank fluid, casting items, and faucet flow on a real client.
