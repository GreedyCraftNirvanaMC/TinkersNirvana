# Ores

[README](../../README.md) · [简体中文](../zh-CN/ORES.md)

The Blocks creative tab includes Antique's Tin Ore and Aluminum Ore at
`tconsnirvana:tin_ore` and `tconsnirvana:aluminum_ore`. Each uses artwork restyled from its Antique texture and cutout rendering,
hardness 3, stone sounds, and stone-pickaxe mining level. Their original common
ore class leaves blast resistance at zero. A valid pickaxe drops the ore block
itself; Fortune and Silk Touch keep that one-block quantity.

Copper uses vanilla ore generation and loot. Legacy `tconstruct:copper_ore`
blocks and items load as vanilla Copper Ore. Raw iron, gold, and copper blocks
melt for the same amount as nine individual raw ores. Oxidation and waxing do
not change copper recovery. Cut variants use the lowest copper cost available
through crafting or stonecutting.

The tab also includes `tconsnirvana:ardite_ore` (Ardite Ore / 阿迪特矿石). Its GIMP
artwork retains Antique's mineral layout over modern netherrack. It keeps
Antique's hardness 10, blast resistance 10, stone sounds, and default cobalt
harvest tier 4, represented by NeoForge's `needs_netherite_tool` tag. Native
netherite pickaxes and assembled cobalt-tier-or-higher pickaxes can harvest it;
diamond and lower tiers leave no drop. Cobalt Ore (`tconsnirvana:cobalt_ore`)
uses the same hardness, blast resistance, and tool requirements. Both ores
drop one ore block when harvested successfully, including with Fortune or Silk Touch.

To mine the first cobalt or ardite, use a Pickaxe Head Pattern and two obsidian
blocks in the Part Builder. The assembled obsidian-headed pickaxe already has
harvest tier 4. An assembled hammer uses the tier of its hammer head.
Assembled tools mine at hand speed below the required tier. Vanilla pickaxes
keep their native mining speed, even if they cannot collect the ore.

Tin, aluminum, cobalt, and ardite use matching `c:ores/<metal>` block and item
tags, `c:ores`, and `c:ore_rates/singular`. Tin and aluminum use
`c:ores_in_ground/stone`, while cobalt and ardite use `c:ores_in_ground/netherrack`.
Each of these four ores yields one ingot in a furnace. In a Smeltery it uses
`smeltery.oreToIngotRatio`, which defaults to 288 mB of matching molten metal per ore.

[World generation settings](WORLD.md) control ore placement. Data packs can override placement and furnace recipes. In-game names support English and Simplified Chinese.
