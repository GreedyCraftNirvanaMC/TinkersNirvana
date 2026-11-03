# Smeltery attribution

The Smeltery implementation in `src/main/java/com/gctn/tconstruct/smeltery`
(excluding the pre-existing `Debug.java`) and the accompanying Smeltery assets
are adapted from TinkersAntique 2.13.0.210, by Slime Knights and its contributors.
They are distributed under LGPL-3.0-only. The original license is included in
`TinkersAntique-LICENSE.txt` and packaged in the mod JAR under `META-INF/licenses`.
Existing unrelated project code retains its original license.

The refreshed metal artwork was edited and exported in GIMP from each item's
corresponding TinkersAntique texture. Original silhouettes, material patterns,
and mineral-cluster layouts retain their attribution above. User-supplied
modern Minecraft iron textures served as visual style references only.
Ore backgrounds use Minecraft 1.21.1 stone and netherrack artwork; those
Minecraft-derived backgrounds remain copyright Mojang AB / Microsoft and
are not covered by the TinkersAntique license.

Reference project: https://github.com/Elite-Modding-Team/TinkersAntique

Source areas used for this port:

- `smeltery/multiblock/MultiblockCuboid.java`, `MultiblockSmeltery.java`
- `smeltery/multiblock/MultiblockTinkerTank.java`, `MultiblockTinker.java`
- `smeltery/tileentity/TileTinkerTank.java`, `TileMultiblock.java`,
  `smeltery/inventory/ContainerTinkerTank.java`, and `smeltery/client/GuiTinkerTank.java`
- `smeltery/multiblock/MultiblockSearedFurnace.java`,
  `smeltery/tileentity/TileSearedFurnace.java`, `TileHeatingStructure.java`,
  `TileHeatingStructureFuelTank.java`, `smeltery/inventory/ContainerSearedFurnace.java`,
  `ContainerSearedFurnaceSideInventory.java`, `smeltery/client/GuiSearedFurnace.java`,
  and `smeltery/client/module/GuiSearedFurnaceSideInventory.java`
- `smeltery/block/BlockSearedSlab.java`, `BlockSearedSlab2.java`, `BlockSearedStairs.java`,
  and the original Tinker Tank controller models, GUI texture, and crafting recipe
- `smeltery/tileentity/TileSmeltery.java`, `TileHeatingStructure.java`,
  `TileHeatingStructureFuelTank.java`, `TileFaucet.java`, `TileCasting.java`,
  `TileChannel.java`, and `TileTank.java`
- `library/smeltery/SmelteryTank.java`, `MeltingRecipe.java`, `CastingRecipe.java`
- `smeltery/client/GuiSmeltery.java`, `GuiHeatingStructureFuelTank.java`,
  `client/module/GuiSmelterySideInventory.java`, `tools/common/client/module/GuiSideInventory.java`
- `library/client/texture/CastTexture.java` and `smeltery/client/*Renderer.java`
- Original GUI, block, fluid, material-item, cast, sound, crafting, and language assets
- `shared/block/BlockClearGlass.java`, `BlockClearStainedGlass.java`,
  the `clearGlassSilkTouch` configuration, and original connected stained-glass assets

The port adapts these behaviors to modern block entities, data components,
NeoForge capabilities, network payloads, menus, sprite sources, and rendering.

The implementation in `src/main/java/com/gctn/tconstruct/gadgets` and its
accompanying gadget assets are also adapted from TinkersAntique
under LGPL-3.0-only. Source areas include `gadgets/TinkerGadgets.java`,
`gadgets/block/BlockBrownstone.java`, `BlockBrownstoneSlab.java`,
`BlockBrownstoneSlab2.java`, `BlockStoneTorch.java`, `BlockStoneLadder.java`,
`BlockWoodenHopper.java`, `gadgets/tileentity/TileWoodenHopper.java`,
`gadgets/WoodenHopperGUIDrawEvent.java`,
`gadgets/block/BlockPunji.java` and its original core/connector models,
`gadgets/block/BlockWoodRail.java`, `BlockWoodRailDropper.java`,
`shared/block/BlockFirewood.java`, `BlockFirewoodSlab.java`, `BlockDecoGround.java`,
`BlockDecoGroundSlab.java`, `BlockMetal.java`, `BlockCommonMetal.java`,
and the firewood/lavawood/mud-brick shape recipes,
and the corresponding original block textures, models, recipes, and translations.

The implementation in `src/main/java/com/gctn/tconstruct/common/slime` and its
slime assets are adapted from TinkersAntique's `shared/TinkerCommons.java`,
`shared/block/BlockSlime.java`, `BlockSlimeCongealed.java`, `ShapedFallbackRecipe`,
`smeltery/TinkerSmeltery.java`, slime block/item textures, models, recipes,
and Chinese/English translations. Food tooltip and always-edible behavior
follow Mantle's `slimeknights.mantle.item.ItemEdible` used by Antique. These ports
are distributed under LGPL-3.0-only with the other adapted content.

The `common/soil` implementation and soil/crystal resources adapt Antique's
`shared/block/BlockSoil.java`, soil and crystal registration in
`shared/TinkerCommons.java`, smelting registration in `tools/TinkerTools.java`,
and the original soil recipes, textures, names, and tooltips under the same license.

Glowball and surface-light implementations adapt `gadgets/item/ItemThrowball.java`,
the glow branch of `gadgets/entity/EntityThrowball.java`,
`shared/block/BlockGlow.java`, `gadgets/client/RenderThrowball.java`, and their
original model, texture, crafting, and translation assets under LGPL-3.0-only.

Common ore additions adapt `shared/block/BlockCommonOre.java`, its registration
in `shared/TinkerCommons.java`, and original copper/tin/aluminum ore textures
and names under LGPL-3.0-only.

Colored slime mobs adapt `world/entity/EntityBlueSlime.java`,
`EntityPurpleSlime.java`, entity/egg registration in `world/TinkerWorld.java`,
`library/client/renderer/RenderTinkerSlime.java`,
`library/client/particle/EntitySlimeFx.java`, the original gray slime texture,
colored entity loot tables, entity-melting configuration, and translations
under LGPL-3.0-only.
