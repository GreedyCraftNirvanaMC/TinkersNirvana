# 冶炼炉

[首页](../../README.zh-CN.md) · [English](../en/SMELTERY.md)

冶炼炉可在 Minecraft 1.21.1 / NeoForge 的冶炼创造栏找到，沿用匠魂怀古的结构判定、流体用量、加热和浇铸规则。

用完整焦黑砖或焦黑石变体搭建长方形地板，再围起空置内部。内部宽、深各可为 1–9 格。墙体中至少放入一个焦黑储罐、量槽或视窗，以及一个面朝外的冶炼炉控制器。外角、墙下的外框和屋顶可省略。控制器可以高于最低一层墙体，向下最多搜索 64 格。焦黑玻璃、排液口和焦黑梯可用于侧墙，地板必须使用完整焦黑石或砖。

每个内部方块提供一个单物品输入槽和 1,152 mB 共享流体容量。在墙内储罐加入熔岩或炽热血液，右击成形后的控制器。左侧输入栏可滚动，主槽分层显示流体。点击某一层可选为默认排出流体，按住 Shift 悬停显示桶数。槽位状态会显示加热、缺配方、缺燃料、热量不足或容量不足。

在排液口上接浇注口，正下方放浇筑台或铸造盆。右击开始浇铸，再次右击会在当前交易完成后停止。上升沿红石信号也可触发。铸造盆浇铸方块，浇筑台浇铸锭、粒、宝石和工具部件。在对应物品或部件上浇注金、铝黄铜或黄铜可制成可重复使用的铸模，黏土铸模则只能使用一次。浇铸方块接受漏斗输入和成品提取。焦黑储罐破坏后保留内部流体，也可在物品形态存储流体。

导流槽优先向下输送，再向两侧输出分流。点击侧臂切换方向，潜行点击按反方向循环；点击中央开关底部输出。相连的拉杆或按钮也可控制底部闸门。

配置位于 `config/tconstruct-common.toml` 的 `[smeltery]`。

| 键 | 默认值 |
| --- | --- |
| `heatItemsTickrateSmeltery` | `4` |
| `liquidTransferRate` | `6` |
| `oreToIngotRatio` | `2.0` |
| `obsidianAlloy` | `true` |
| `claycasts` | `true` |
| `allowBrickCasting` | `true` |
| `steelAlloy` | `true` |
| `drainGaseousFluids` | `true` |
| `minFluidHeight` | `3` |
| `maxSmelteryItemRenders` | `-1`（不限） |
| `temperatureCelsius` | `true` |

`oredictMeltingIgnore` 支持物品 ID 或 `#物品标签`。从合成原料推导熔化配方时，可忽略列出的原料；默认允许忽略木材、木棍、线、红石和箱子。数据包重载后会重新推导。

集成时在物品和流体注册后初始化 `SmelteryRecipes`，向公开注册表添加熔化、浇铸、燃料、合金或实体熔炼配方。支持通用 `c:ingots`、`c:nuggets`、`c:storage_blocks` 和 `c:ores` 材料标签，浇铸也能识别板和齿轮标签。可在 NeoForge 游戏事件总线上监听 `SmelteryEvents.Melting`、`Casting` 和 `Casted` 来调整结果或拒绝操作。回调在服务端执行，被拒绝的操作保留原料。
