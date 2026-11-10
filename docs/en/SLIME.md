# Slime blocks, items, and mobs

[README](../../README.md) · [简体中文](../zh-CN/SLIME.md)

The Slime creative tab contains blue, purple, blood, and magma slime blocks,
five congealed slime colors, and five edible slimeballs. Vanilla supplies the
green slime block and green slimeball. Pink blocks are registered but hidden
from the creative tab, as in TinkersAntique.

Pack four same-color slimeballs in a square to make congealed slime, or nine
in a full crafting grid to make a slime block. Craft a block by itself to recover
four or nine slimeballs. Pink blocks have no ordinary packing or unpacking
recipes. Antique's fallback recipes turn a vanilla slimeball plus blue, purple, or orange dye into the corresponding blue,
purple, or magma slimeball. Blood slimeballs can be cast from blood.

Ordinary slime blocks use vanilla slime bounce, careful walking, and piston
behavior. Colored slime blocks stick to other movable blocks and each other;
they follow modern vanilla slime's separation from honey blocks. Congealed slime
has a full-block appearance and support surface, with a 10/16-height collision
surface, friction 0.5, and no piston adhesion. Living entities and dropped items
landing faster than 0.25 blocks per tick rebound at 1.2 times their downward
speed, including while sneaking. Weaker landings stop. XP orbs and other entity
types do not gain rebound speed. All entities receive no fall damage from
congealed slime.

Congealed slime supports native leaves. This uses the modern block-only
`minecraft:logs` tag; the corresponding item is excluded from wood ingredient
tags, and the block is excluded from the axe mining tag. Native leaf-distance
updates handle support removal.

All edible slimeballs take the normal 32 ticks to eat and can be eaten at full
hunger. Nutrition, saturation, and effects match Antique:

| Color | Nutrition | Saturation restored | Effects |
| --- | ---: | ---: | --- |
| Blue | 1 | 2 | Slowness III 45s; Jump Boost III 60s |
| Purple | 1 | 4 | Bad Luck 45s; Luck 60s |
| Blood | 1 | 3 | Poison III 45s; Health Boost 60s |
| Magma | 2 | 4 | Weakness 45s; Wither 15s; Fire Resistance 60s |
| Pink | 1 | 2 | Nausea III 10s |

Tooltips use translated effect names. New game text supports English and
Simplified Chinese.

In `tconstruct-common.toml`, `general.matchVanillaSlimeblock` defaults to `false`:
nine mixed slimeballs make a vanilla slime block. Set it to `true` to make the
hidden pink slime block instead. Uniform blue, purple, blood, and magma grids
always use their own block recipes. Uniform green remains the vanilla result;
uniform pink only matches the mixed fallback when this option is enabled.

Green, blue, purple, and blood slimeballs melt to 250 mB of their matching fluid.
Their congealed blocks melt to 1,000 mB; an empty casting basin casts a congealed
block from that amount in 100 ticks. An empty table casts one slimeball from
250 mB in 50 ticks. Blue and purple full blocks have explicit 2,250 mB melting
recipes, alongside the existing vanilla green block recipe. Magma and pink do
not receive invented fluid equivalents.

For integration, colored balls use `c:slime_balls/<color>` and `c:slime_balls`;
full blocks use `c:storage_blocks/slime/<color>` and
`c:storage_blocks/slime`. Packing recipes accept the matching color tags.

The Slime tab also includes the original blue and purple slime spawn eggs.
Their English entity names remain Antique's **Sky Slime** and **Squire Slime**.
Both retain size-dependent native slime health, movement, damage, splitting,
and AI, with Antique's 0.63 jump power, 10–19 tick jump delay, 0.8 squish damping,
and fall-damage immunity. Jump Boost does not increase their fixed jump power.
The body uses the original gray texture and ARGB tints `FF67F0F5` and `EFD236FF`,
including the purple gel's original alpha. Landing particles use the matching
colored slimeball item texture.

Only size-one mobs drop slimeballs: zero to two of the matching color, plus the
original random Looting bonus. Splitting creates mobs of the same color and
retains the parent's custom name, persistence, and AI flag. Native entity data
stores their type, size, health, and other state. In the Smeltery, either colored
mob produces 25 mB of its matching slime fluid per heart of melting damage.

Spawn predicates recognize their own source or flowing slime fluid and the
block tags `tconstruct:slime_spawnable/blue` and
`tconstruct:slime_spawnable/purple`. Blue's tag corresponds to non-purple slime grass, and purple's tag corresponds to purple slime grass. See [world generation](WORLD.md) for natural spawning. Spawn eggs and summon commands are also available.
