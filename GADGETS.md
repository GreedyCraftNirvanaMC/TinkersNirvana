# Gadgets

The Gadgets creative tab contains the ported utilities and decorative blocks.
All added item names and tooltips support English and Simplified Chinese.

Craft a Wooden Hopper from five planks surrounding a wooden chest in the normal
hopper pattern. It holds one stack and transfers items at half the normal hopper
rate. It collects dropped items and pulls from inventories above, including
sided inventories, and outputs downward or horizontally. Redstone does not lock
it. Its screen keeps Antique's centered single slot and original texture. A
comparator reads the stored stack, breaking it drops its contents, and it can be
used as furnace fuel for 300 ticks.

Five sugar canes in an X-shaped recipe make three Punji Sticks. Attach them to
any solid face, including walls and ceilings. Living entities touching them take
damage and briefly receive Slowness II; falling contact is more dangerous. Nearby
sticks on the same face connect visually, including diagonal neighbors. Removing
the supporting face drops the sticks as items.

Wooden Rails use the normal rail shape, speed, curves, and slopes, with wooden
sounds and lower hardness. Six planks around a wooden rod make four rails.
Replace the rod with a wooden trapdoor to craft four Wooden Rails with a
Trapdoor. While a hopper minecart is on a trapdoor rail, it can unload one item
per tick into the inventory below, respecting that inventory's top-side item
handler. Plain rails and chest minecarts do not perform this unloading. Full or
unavailable inventories keep the cargo in the cart.

Firewood, Lavawood, and Mud Bricks each have full blocks, slabs, and stairs.
Craft Firewood from Lavawood and two blaze powders. Three full blocks make six
slabs; the stair-shaped recipe using six blocks makes four stairs. Two Mud Bricks
(items) can also make one Mud Bricks Slab. Double slabs drop two slabs.

Firewood and Lavawood blocks and slabs emit light level 7; their stairs retain
Antique's unlit stair behavior. All six wood variants burn for 300 ticks. Axes
work on the wood families, while shovels work on Mud Bricks. The ported metal
storage blocks require a pickaxe of any tier and can form beacon bases. Full
copper blocks retain beacon support through oxidation and waxing.

Two stone or cobblestone blocks in a vertical crafting column make four Stone
Rods. A rod below coal or charcoal makes four Stone Torches. Torches can stand on
the ground or attach to a wall and produce light level 14 with normal flame
particles. The ladder-shaped recipe using seven Stone Rods makes three Stone
Ladders. Stone torches and ladders use stone placement/breaking sounds and native
Minecraft attachment rules. Stone Rods are exposed through `c:rods/stone`.

Brownstone has all twelve Antique styles, each with full blocks, slabs, and
stairs. Walking on them boosts horizontal movement while retaining vertical
movement; the original water-specific multiplier is preserved. No lasting
movement attribute is added. Three Rough Brownstone blocks are crafted from
four sandstone blocks and two redstone dust. Smelt rough blocks into smooth
blocks, then use the original shapeless decoration cycle to reach the other
styles. Smelting bricks produces cracked bricks. Three blocks make six slabs;
six blocks in the stair recipe make four stairs. Double slabs drop two slabs.

The native modern blocks provide stair corners, slab merging, waterlogging,
support removal, and torch/ladder placement. These building blocks are separate
from the seared blocks used in Smeltery and Tinker Tank structures.
