# TinkersNirvana

[简体中文](README.zh-CN.md)

**Version 1.21.1-1.0.0 · Minecraft Java Edition 1.21.1 · NeoForge**

TinkersNirvana brings the classic TinkersAntique experience to Minecraft 1.21.1. Build tools from interchangeable parts, customize them with modifiers, and process materials with the Smeltery, Tinker Tank, and Seared Furnace. The port includes the workshops, slime islands, gadgets, the in-game *Materials and You* guide, and refreshed block and item textures.

## Install

1. Set up Minecraft Java Edition 1.21.1 with Java 21 and NeoForge 21.1.218 or later in the 21.1 series.
2. Put `TinkersNirvana-1.21.1-1.0.0.jar` in the instance's `mods` directory. For multiplayer, install the same version on the server and each client.

Mantle is not required. The mod ID is `tconstruct`.

## Build from source

Run `./gradlew build` with JDK 21. On Windows, run `.\gradlew.bat build`. The mod JAR is written to `build/libs/`.

## Guides

- [Tools, assembly, and modifiers](TOOLS.md) · [Material and KubeJS integration](TOOL_API.md)
- [Smeltery](SMELTERY.md) · [Tinker Tank](TINKER_TANK.md) · [Seared Furnace](SEARED_FURNACE.md)
- [In-game book](BOOK.md) · [Gadgets](GADGETS.md) · [World generation](WORLD.md)

## License and credits

The project's MIT-licensed portions are covered by the [MIT License](LICENSE). TinkersAntique-derived code and assets retain [LGPL-3.0-only](third_party/TinkersAntique-LICENSE.txt). Mantle-derived code and assets retain [Mantle's MIT license](third_party/Mantle-LICENSE.txt). See the [third-party notices](third_party/NOTICE.md) for attribution to Slime Knights and contributors, and for details on Minecraft-derived artwork. A [Chinese license translation](LICENSE.zh-CN.md) is also available.
