# Clear Glass

[README](../../README.md) · [简体中文](../zh-CN/CLEAR_GLASS.md)

Cast 1,000 mB of molten glass in a basin to make Clear Glass. Surround one dye
with eight Clear Glass blocks to craft eight stained clear glass blocks. All
sixteen colors use Antique's connection rules, item names, tint
values, and beacon colors. Adjacent blocks connect only when their type and
color match. The stained variants are in the Smeltery creative tab.

Clear Glass, stained clear glass, and ordinary stained glass melt into 1,000 mB
of molten glass per block. Clear and stained clear glass use the common NeoForge glass and
color tags. They are decorative glass, separate from the seared glass used in
Smeltery and Tinker Tank walls.

By default, recovering clear or stained clear glass requires Silk Touch. Set
`clearGlassSilkTouch = false` under `[smeltery]` in
`config/tconstruct-common.toml` to allow ordinary harvesting. Both choices retain
one-block drops and normal explosion survival behavior. The setting and added
item names support English and Simplified Chinese.

Clear glass, seared glass, and seared tanks transmit skylight, do not conduct
redstone through their bodies, and do not suffocate entities. Full seared walls
block light and conduct redstone. Separate glass blocks retain their complete
borders when placed with commands as well as by players.
