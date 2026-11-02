# Smeltery attribution

The Smeltery implementation in `src/main/java/com/gctn/tconstruct/smeltery`
(excluding the pre-existing `Debug.java`) and the accompanying Smeltery assets
are adapted from TinkersAntique 2.13.0.210, by Slime Knights and its contributors.
They are distributed under LGPL-3.0-only. The original license is included in
`TinkersAntique-LICENSE.txt` and packaged in the mod JAR under `META-INF/licenses`.
Existing unrelated project code retains its original license.

Reference project: https://github.com/Elite-Modding-Team/TinkersAntique

Source areas used for this port:

- `smeltery/multiblock/MultiblockCuboid.java`, `MultiblockSmeltery.java`
- `smeltery/tileentity/TileSmeltery.java`, `TileHeatingStructure.java`,
  `TileHeatingStructureFuelTank.java`, `TileFaucet.java`, `TileCasting.java`,
  `TileChannel.java`, and `TileTank.java`
- `library/smeltery/SmelteryTank.java`, `MeltingRecipe.java`, `CastingRecipe.java`
- `smeltery/client/GuiSmeltery.java`, `GuiHeatingStructureFuelTank.java`,
  `client/module/GuiSmelterySideInventory.java`, `tools/common/client/module/GuiSideInventory.java`
- `library/client/texture/CastTexture.java` and `smeltery/client/*Renderer.java`
- Original GUI, block, fluid, material-item, cast, sound, crafting, and language assets

The port adapts these behaviors to modern block entities, data components,
NeoForge capabilities, network payloads, menus, sprite sources, and rendering.
