# Gadgets

[README](../../README.md) · [简体中文](../zh-CN/GADGETS.md)

The Gadgets creative tab contains the ported utilities and decorative blocks.
All added item names and tooltips support English and Simplified Chinese.

Eight snowballs surrounding glowstone dust make eight Glowballs. They stack to
sixteen and throw with Antique's speed, scatter, and snowball sound. A successful
impact places a thin, noncolliding light patch on the struck face; an entity hit
tries to place one at the entity's position without dealing damage. The patch
emits light level 14. It requires a solid supporting face, tries another face if
the preferred face is unavailable, and cannot replace water or a solid block.
Removing support removes the patch. It drops nothing; picking it gives a
Glowball. The projectile uses native impact events, so a cancelled impact does
not place a patch or consume the flying projectile. Creative use preserves the
held stack; ordinary use consumes one ball.

## Drying and food

Item Racks and Drying Racks attach to floors, walls, or ceilings. Right-click to place one item and again to retrieve it. Drying Racks make ten jerkies, five slime drops, leather, dried clay, and dried clay bricks. Jerky and slime drops take five minutes and clay balls take two. Hoppers and pipes insert a single item into an empty rack and extract finished goods from a Drying Rack. Ready goods emit comparator strength 15. Set `gadgets.dryingTimeMultiplier` for duration and `gadgets.dryLeather` for drying cooked meat into leather.

## Slime gear

Slime Slings come in five standard colors. Stand on the ground, aim at a block, hold use to charge, and release to launch away from it. Slime Boots prevent ordinary bounce-related fall damage. Sneaking stops the bounce and reduces fall damage. The gear grants no extra armor.

Use a Piggybackpack on a mob to carry it. Each passenger requires one pack, with a default limit of three. Carrying passengers slows movement. Removing packs releases passengers one at a time. `gadgets.limitPiggybackpack` restricts targets to players and leashable mobs, and `gadgets.maxPiggybackPassengers` sets the limit. A rejected interaction consumes nothing.

## Slime channels and EFLN

Smelt one congealed slime into three channels of the same color. Placement selects one of eight flow directions, and sneak-placement reverses it. Channels form inner and outer corners between floors, walls, and ceilings. They carry mobs and items, and redstone stops the flow. Channels extinguish fire and prevent drowning and fall damage. Expiring item entities in a channel get extra lifetime.

Flint and either gunpowder or sulfur make one EFLN. It progressively breaks blocks in a sphere subject to blast resistance, preserves ordinary drops, and pushes nearby entities. It uses native explosion cancellation and area events and processes batches without force-loading chunks. Dispensers can throw Glowballs and EFLN.

## Fancy frames and spaghetti

Fancy Frames come in jeweled, aluminum brass, cobalt, Ardite, Manyullyn, gold, and clear variants. They retain vanilla item rotation, maps, and removal. The Clear Frame hides its border when occupied and shows it again when emptied.

Hard Spaghetti is rare loot in desert pyramids, jungle temples, and stronghold corridors. Soak it with three buckets of water for fifteen minutes on a Casting Table, dry it for fifteen minutes, then smelt it into Mom's Spaghetti. It has one hundred servings and stays as an item when exhausted. Refill one serving at a time with wheat in Tool Station repair mode. Beetroot soup adds sauce and returns the bowl. Then add meat with three different cooked meats. Spaghetti items are hidden from creative tabs, as in Antique.

Data packs and KubeJS can add drying recipes. See the [tool extension API](TOOL_API.md).

## Transport and building

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
