# 世界生成

[首页](../../README.zh-CN.md) · [English](../en/WORLD.md)

新生成的主世界区块可出现黏液空岛，包含绿色、蓝色或紫色黏液土、彩色草、黏液湖、树木与藤蔓。下界熔岩海可生成岩浆黏液岛。蓝色和紫色史莱姆使用相应岛屿的生成区域；岩浆岛使用岩浆怪。

黏液树苗需种植在黏液土上。骨粉可催生树苗、草与蕨类；剪刀或精准采集可收集树叶。蓝色、紫色藤蔓分为三段，按阶段向下生长。黏液树叶和藤蔓也可作为箭羽、箭杆材料。

村庄的五种原版风格均可生成工具作坊和冶炼建筑，箱内随机战利品在首次打开时生成。建筑使用原版拼图结构池，兼容其它模组向同一结构池添加建筑。

## 配置

配置文件为 `config/tconstruct-common.toml`。世界生成设置作用于以后生成的区块，不会改写已有建筑或地形。

| 配置 | 默认值 | 用途 |
| --- | --- | --- |
| `world.generateSlimeIslands` | `true` | 黏液岛与岩浆岛 |
| `world.slimeIslandRate` / `world.magmaIslandRate` | `730` / `100` | 平均多少区块尝试生成一座岛 |
| `world.islandSlimeSpawnInterval` | `40` | 玩家附近普通黏液岛额外尝试刷新匠魂史莱姆的间隔（游戏刻） |
| `world.islandSlimeCap` | `16` | 每座普通黏液岛自然刷新的匠魂史莱姆数量上限 |
| `world.slimeOutsideIslandSeconds` | `30` | 匠魂史莱姆连续离开空岛多少秒后清除 |
| `world.generateIslandsInSuperflat` | `false` | 超平坦世界中的黏液岛 |
| `world.generateSlimePools` | `false` | 地下黏液池与岩浆池 |
| `world.slimePoolRate` / `world.magmaPoolRate` | `30` | 地下池尝试频率 |
| `world.slimePoolHeightMax` | `64` | 黏液池最高尝试高度 |
| `world.generateVillageStructures` | `true` | 两类村庄建筑 |
| `world.villageWorkshopWeight` / `world.villageSmelteryWeight` | `30` / `9` | 村庄建筑权重 |

黏液岛和黏液池分别提供维度 ID 列表、黑白名单开关及地表维度限制。维度采用资源 ID，空列表有效。矿石分别提供铜、锡、铝、钴和阿迪特的开关与次数；主世界矿石还可调整最低、最高高度。次数为零可关闭对应尝试。

每座新生成的黏液岛会生成 1–3 棵黏液树。蓝色、紫色史莱姆可在液态史莱姆中呼吸和游动，连续离岛达到配置时长后会被清除，不产生掉落、经验或分裂。返回任意普通黏液岛会重置计时；计时随实体保存。

## 数据包扩展

配置特征和放置特征位于 `data/tconstruct/worldgen/configured_feature`、`placed_feature`，群系修饰器位于 `data/tconstruct/neoforge/biome_modifier`。修改这些资源可调整生成阶段、群系与方块。

`tconstruct:slime_tree` 配置包含 `trunk`、`leaves` 方块状态，以及可选的 `vine`、`min_height`、`height_range`、`seek_height`。可复用它生成扩展模组的树木。

村庄建筑模板位于 `data/tconstruct/structure/village/<biome>`，附加结构池和战利品也使用原生数据包格式。宝典扩展见 [BOOK.md](BOOK.md)。
