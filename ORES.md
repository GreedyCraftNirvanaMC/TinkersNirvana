# Common ores

The Blocks creative tab includes Antique's Copper Ore, Tin Ore, and Aluminum
Ore at `tconstruct:copper_ore`, `tconstruct:tin_ore`, and
`tconstruct:aluminum_ore`. Each uses its original texture and cutout rendering,
hardness 3, stone sounds, and stone-pickaxe mining level. Their original common
ore class leaves blast resistance at zero. A valid pickaxe drops the ore block
itself; Fortune and Silk Touch keep that one-block quantity.

The ores use matching `c:ores/<metal>` block and item tags, plus `c:ores`,
`c:ores_in_ground/stone`, and `c:ore_rates/singular`. They integrate with the
existing Smeltery ore recipes and `smeltery.oreToIngotRatio` setting. The default
ratio 2 produces 288 mB of matching molten metal per ore.

This block registration provides no world-generation placement or additional
furnace recipes. Packs can place the ore blocks through their own generation
configuration. In-game names support English and Simplified Chinese.
