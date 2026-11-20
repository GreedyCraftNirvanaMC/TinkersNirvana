# TinkersNirvana

<img src="src/main/resources/tinkersnirvana-logo.png" alt="TinkersNirvana" width="192">

[简体中文](README.zh-CN.md)

**Version 1.21.1-1.0.5 · Minecraft Java Edition 1.21.1 · NeoForge**

TinkersNirvana ports TinkersAntique to Minecraft 1.21.1. Assemble tools from parts made of different materials, customize them with modifiers, and process materials in the Smeltery, Tinker Tank, and Seared Furnace. The port includes workshops, slime islands, gadgets, the in-game *Materials and You* guide, and refreshed block and item textures.

## Upgrading from 1.0.4

Back up your world and replace the old JAR. The mod ID and resource namespace are now `tconsnirvana`. Old item, block, entity and component IDs are resolved when loading saves if no other installed mod owns `tconstruct`. Existing settings are copied to `config/tconsnirvana-common.toml` on the first launch if the new file is absent.

Update custom data packs, resource packs and scripts that refer to the old namespace. Java addons must update their mod dependency to `tconsnirvana`. Java package names and the `TinkerEvents` KubeJS event group are unchanged. See the [changelog](docs/en/CHANGELOG.md) for release details.

## Build from source

Run `./gradlew build` with JDK 21. On Windows, run `.\gradlew.bat build`. The mod JAR is written to `build/libs/`.

## Development and customization

- [KubeJS materials and traits](docs/en/KUBEJS.md)
- [Java addon API](docs/en/TOOL_API.md)
- [Register existing addon parts in native workshops](docs/EXTERNAL_PARTS.md)

## License and credits

The project's MIT-licensed portions are covered by the [MIT License](LICENSE). TinkersAntique-derived code and assets retain [LGPL-3.0-only](third_party/TinkersAntique-LICENSE.txt). Mantle-derived code and assets retain [Mantle's MIT license](third_party/Mantle-LICENSE.txt). See the [third-party notices](third_party/NOTICE.md) for attribution to Slime Knights and contributors, and for details on Minecraft-derived artwork. A [Chinese license translation](LICENSE.zh-CN.md) is also available.
