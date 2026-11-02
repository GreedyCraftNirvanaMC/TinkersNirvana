# Seared Furnace

The Seared Furnace uses ordinary Minecraft smelting recipes and shared liquid
fuel to cook several batches at once. Its controller is in the Smeltery creative
tab. Craft it with eight Seared Bricks (items) surrounding a vanilla furnace.

Build a closed seared cuboid with an empty interior 1–9 blocks wide and deep,
and at least one block tall. Full seared blocks form the inner floor, side walls,
and vertical corner columns. The roof also accepts bottom-half seared slabs and
stairs. The outer rims around the floor and roof are optional; the vertical
corner columns are required. A fuel tank must be in an outer frame position,
such as a vertical corner, rather than a flat wall panel. The controller faces
outward in a wall below the roof. Glass and drains cannot replace wall panels.

Each interior block adds three inventory slots to the nine base slots. A minimum
3×3×3 furnace has 12 slots; a 5×5×5 furnace has 90. Each slot accepts up to 16
items. Fill a frame tank/gauge/window with lava or blazing blood, then open the
controller. Its original screen has a scrolling input panel, fuel gauge, and
remaining-fuel flame. Status bars distinguish heating, unsupported inputs,
missing fuel, insufficient heat, oversized outputs, and completed batches.

The entire batch is replaced by its cooked result in the same slot. Finished
outputs stay there until removed or changed, including after saving/reloading;
stone will therefore remain stone rather than automatically becoming smooth
stone. Food receives Antique's faster cooking factor. Each heating step consumes
one unit of buffered fuel regardless of how many slots are heating. A slot whose
entire output would exceed 16 items keeps its input and reports insufficient room.

Hoppers and other item handlers can insert or extract items through the
controller. Fuel tanks keep their separate fluid contents and normal tank-item
behavior. Replacing walls or drains in a formed structure restores ownership
without requiring a resize. On structural failure, the first nine inventory
slots remain in the controller; overflow drops outside it. Repairing the shell
restores the full inventory size. Removing the controller drops all stored items.

Configuration is in `config/tconstruct-common.toml`, under `[smeltery]`:
`heatItemsTickrateSearedFurnace` defaults to `4`, matching Antique. It is independent
of the Smeltery heating interval. English and Simplified Chinese translations are
provided for the new screen, item, status messages, and configuration label.

`HeatingContainer` provides common menu/fuel synchronization. `SmelteryMaster`
manages structure membership; `FluidSmelteryMaster` additionally supplies shared
fluids for drains. The Seared Furnace implements membership and heating without
exposing shared molten storage. Large inventories use integer saved slot indices
while retaining compatibility with older unsigned-byte indices.
Inventories exceeding vanilla's signed-short network slot range synchronize
only the visible seven rows. Scrolling, whole-inventory shift insertion, and
double-click collection retain access to the full backing inventory.
