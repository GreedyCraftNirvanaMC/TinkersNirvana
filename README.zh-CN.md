# TinkersNirvana

[English](README.md)

**版本 1.21.1-1.0.0 · Minecraft Java 版 1.21.1 · NeoForge**

TinkersNirvana 将经典的《匠魂怀古》移植到 Minecraft 1.21.1。你可以用不同材质的部件组装工具，借助强化定制工具，并使用冶炼炉、工匠储罐和焦黑熔炉加工材料。移植版包含工坊、史莱姆空岛、实用装置、游戏内宝典《材料与你》，以及重新绘制的方块和物品贴图。

## 安装

1. 使用 Java 21 运行 Minecraft Java 版 1.21.1，并安装 NeoForge 21.1.218 或更高的 21.1 系列版本。
2. 将 `TinkersNirvana-1.21.1-1.0.0.jar` 放入游戏实例的 `mods` 目录。多人游戏中，服务端和各客户端应安装相同版本。

游戏无需安装 Mantle。模组 ID 为 `tconstruct`。

## 从源码构建

使用 JDK 21 执行 `./gradlew build`。Windows 下执行 `.\gradlew.bat build`。构建生成的模组 JAR 位于 `build/libs/`。

## 使用指南

- [工具、装配与强化](TOOLS.md) · [材料与 KubeJS 扩展](TOOL_API.md)
- [冶炼炉](SMELTERY.md) · [工匠储罐](TINKER_TANK.md) · [焦黑熔炉](SEARED_FURNACE.md)
- [游戏内宝典](BOOK.md) · [实用装置](GADGETS.md) · [世界生成](WORLD.md)

## 许可证与署名

项目中采用 MIT 许可的部分适用 [MIT 许可证](LICENSE)，另附[中文译文](LICENSE.zh-CN.md)。源自匠魂怀古的代码和资源沿用 [LGPL-3.0-only](third_party/TinkersAntique-LICENSE.txt)，源自 Mantle 的代码和资源沿用 [Mantle 的 MIT 许可证](third_party/Mantle-LICENSE.txt)。Slime Knights 及其贡献者的署名和源自 Minecraft 的美术资源说明见[第三方声明](third_party/NOTICE.md)。
