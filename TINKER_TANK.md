# Tinker Tank

The Tinker Tank stores multiple fluids without heating, melting, or alloying them.
Its controller and all twelve seared slab/stair styles are in the Smeltery creative
tab. Names and screen text support English and Simplified Chinese.

Craft the controller with eight Seared Bricks (items) surrounding an empty bucket.
Build a closed cuboid with an empty interior 1–9 blocks wide, 1–9 blocks deep, and
at least one block tall. Its outside dimensions are therefore at least 3×3×3.
Both the floor and roof need a complete outer frame; side corners are required.

- Walls and side corners accept full seared blocks, seared glass, seared ladders,
  drains, and seared tanks/gauges/windows.
- The floor accepts full seared blocks, seared glass, and drains. Its outer frame
  accepts full seared blocks and drains.
- The roof accepts wall materials and bottom-half seared stairs/slabs. Its outer
  frame accepts full seared blocks, drains, and bottom-half seared stairs/slabs.
  Top-half stairs/slabs cannot form a roof. Double slabs count as full seared blocks.
- Use exactly one controller facing outward in a wall, below the roof. A fuel tank
  is optional; any seared tank placed in the walls keeps its own separate contents.

Capacity is 4,000 mB per block of the entire outside cuboid, including its shell:
a 3×3×3 tank holds 108 buckets; a 5×5×5 tank holds 500 buckets. There is no heating
or fuel cost. Height is limited by the world's build height; the controller's
downward floor search reaches 64 blocks, matching Antique.

Use a Smeltery Drain to insert or extract fluid with containers, pipes, or faucets.
The controller itself opens the original 122×130 fluid-only screen and does not
expose a fluid capability. Click a layer to choose the default drain output. Hold
Shift in the screen to show bucket units. An empty active controller displays a
water-colored window as its formation indicator; this does not add water.

Breaking the shell closes the screen and disables drain access. Stored fluids
remain in the controller and become accessible after repairing the structure.
Shrinking the tank retains excess fluid and prevents further filling until space
is available. Removing the controller permanently removes its stored fluids.

Integrations can implement `SmelteryMaster` for controller ownership, revalidation,
and shared fluid access. Drains resolve their master on every operation, so a
cached capability cannot keep accessing a dismantled or replaced controller.
