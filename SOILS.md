# Soils and slime crystals

The Slime tab includes Green Slimy Mud, Blue Slimy Mud, Slimy Magma Mud,
Graveyard Soil, Consecrated Soil, and green, blue, and magma slime crystals.
Grout remains in the Smeltery tab with its existing registry identity.

All six soils retain Antique's hardness 3, blast resistance 3, friction 0.8,
sand sounds, and shovel efficiency. They drop without a shovel and do not fall
like sand. Walking on green or blue mud multiplies horizontal motion by 0.4,
preserves vertical motion, and briefly applies Weakness I. Magma mud has no
additional walking effect in Antique. Graveyard Soil heals undead mobs by one
health point per walking callback. Consecrated Soil deals one magic damage and
ignites undead mobs for one second; players and other mobs are excluded.

Crafting recipes:

| Output | Ingredients |
| --- | --- |
| 3 Grout | Clay ball, sand, gravel |
| 12 Grout | Clay block, 4 sand, 4 gravel |
| Green Slimy Mud | 4 vanilla slimeballs, sand, dirt |
| Blue Slimy Mud | 4 blue slimeballs, sand, dirt |
| Green/Blue Slimy Mud | Matching congealed slime block, sand, dirt |
| Slimy Magma Mud | 2 magma slimeballs, 2 magma cream, soul sand, netherrack |
| Graveyard Soil | Dirt, rotten flesh, bone meal |

Sand uses `c:sands`, and grout gravel uses `c:gravels`. The green/blue recipes accept dirt, coarse dirt, and podzol
through `tconstruct:soil_recipe_dirt`, matching the old dirt block's variants.
The Graveyard Soil recipe requires ordinary dirt.

Smelt Graveyard Soil for 200 ticks to obtain Consecrated Soil and 0.1 XP.
Smelt each slime mud for 200 ticks to obtain its matching crystal and 0.75 XP.
These are ordinary smelting recipes, so native furnaces and the Seared Furnace
can use them. Crystals expose `c:slime_crystals/<color>` item tags for integration.
The remaining slime tool statistics and traits are separate ports.

Green and blue mud reject ordinary plants. The empty block tag
`tconstruct:slime_plants` provides their dedicated plant-support extension point
for the future world module. Adding these soils does not register slime plants,
trees, mobs, or world generation.
