# Ores

The Blocks creative tab includes Antique's Copper Ore, Tin Ore, and Aluminum
Ore at `tconstruct:copper_ore`, `tconstruct:tin_ore`, and
`tconstruct:aluminum_ore`. Each uses artwork restyled from its Antique texture and cutout rendering,
hardness 3, stone sounds, and stone-pickaxe mining level. Their original common
ore class leaves blast resistance at zero. A valid pickaxe drops the ore block
itself; Fortune and Silk Touch keep that one-block quantity.

The tab also includes `tconstruct:ardite_ore` (Ardite Ore / 阿迪特矿石). Its GIMP
artwork retains Antique's mineral layout over modern netherrack. It keeps
Antique's hardness 10, blast resistance 10, stone sounds, and default cobalt
harvest tier 4, represented by NeoForge's `needs_netherite_tool` tag. Native
netherite pickaxes and assembled cobalt-tier-or-higher pickaxes can harvest it;
diamond and lower tiers cannot. Successful harvesting drops one ore block,
including with Fortune or Silk Touch.

The ores use matching `c:ores/<metal>` block and item tags, plus `c:ores`,
`c:ores_in_ground/stone`, and `c:ore_rates/singular`. They integrate with the
existing Smeltery ore recipes and `smeltery.oreToIngotRatio` setting. The default
ratio 2 produces 288 mB of matching molten metal per ore.

Ardite uses `c:ores/ardite`, `c:ores`, `c:ore_rates/singular`, and
`c:ores_in_ground/netherrack` on blocks and items. Its Smeltery output follows
the same ore-yield setting and produces molten Ardite (288 mB by default).

This block registration provides no world-generation placement or additional
furnace recipes. Packs can place the ore blocks through their own generation
configuration. In-game names support English and Simplified Chinese.
