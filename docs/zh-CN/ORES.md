# 矿石

[首页](../../README.zh-CN.md) · [English](../en/ORES.md)

方块创造栏包含匠魂怀古的铜、锡和铝矿石，对应 `tconstruct:copper_ore`、`tconstruct:tin_ore` 和 `tconstruct:aluminum_ore`。三者均使用重绘贴图和镂空渲染，硬度为 3，使用石质音效，需要石镐或更高等级的镐。原版矿石类的抗爆性为 0。用合适的镐采集时掉落一块矿石，时运或精准采集均不改变数量。

创造栏还有阿迪特矿石 `tconstruct:ardite_ore`。贴图在新版下界岩上保留了匠魂怀古的矿物分布。硬度和抗爆性均为 10，使用石质音效。原版默认采掘等级为钴级 4，在 NeoForge 中通过 `needs_netherite_tool` 标签表示。下界合金镐及钴级以上的组装镐可采集，钻石镐及以下无法采集。成功采集时掉落一块矿石，时运和精准采集同样如此。

常见矿石的方块与物品使用 `c:ores/<metal>`、`c:ores`、`c:ores_in_ground/stone` 和 `c:ore_rates/singular` 标签。阿迪特使用 `c:ores/ardite`、`c:ores`、`c:ores_in_ground/netherrack` 和 `c:ore_rates/singular`。所有矿石均按 `smeltery.oreToIngotRatio` 在冶炼炉中产出对应熔融金属，默认倍率为 2，每块 288 mB。

矿石生成由[世界生成配置](WORLD.md)管理。矿石方块的注册本身不提供额外的普通熔炉配方。数据包可以调整矿石放置，游戏内名称支持简体中文与英文。
