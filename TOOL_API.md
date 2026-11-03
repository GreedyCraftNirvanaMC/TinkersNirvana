# 工具扩展接口

接口入口为 `com.gctn.tconstruct.api.TinkerContent`。材料、词条和强化应在启动阶段注册，并在客户端与服务端保持一致。修改启动脚本后需要重启；不应在玩家交互或服务器 tick 中修改全局定义。

## KubeJS 材料示例

将以下代码放入 `kubejs/startup_scripts/tinker_materials.js`。`StartupEvents.postInit` 来自所提供的 KubeJS 源码；此示例无需给本模组增加 KubeJS 编译依赖。示例材料使用已有紫水晶物品，因此无需再注册物品。

```js
const TinkerContent = Java.loadClass('com.gctn.tconstruct.api.TinkerContent')

StartupEvents.postInit(event => {
  const amethyst = TinkerContent.registerMaterial('kubejs:amethyst', 0xA77CE3, true, false)
  // 耐久、采掘速度、攻击、Antique 挖掘等级。
  TinkerContent.head(amethyst, 480, 6.5, 4.5, 3)
  TinkerContent.handle(amethyst, 0.9, 40)
  TinkerContent.extra(amethyst, 90)
  TinkerContent.bow(amethyst, 0.8, 1.1, 2)
  TinkerContent.item(amethyst, 'minecraft:amethyst_shard', 1)
  TinkerContent.trait(amethyst, null, 'lightweight', 1)
  TinkerContent.trait(amethyst, 'Head', 'jagged', 1)
})
```

添加 `material.kubejs:amethyst.name` 的资源包翻译即可显示材料名。材料支持 `Head`、`Handle`、`Extra`、`Bow`、`BowString`、`Shaft` 和 `Fletching` 属性；还可通过 `Material.addMaterialStats` 添加自己的属性类型。

`item(material, id, value)` 和 `tag(material, id, value)` 的 value 是一件原料提供的材料份数。一份对应现有冶炼系统的 144 mB。标签在匹配时读取，兼容标签重载；部件本身也可作为材料原料，包覆后的弩箭核心除外。

挖掘等级沿用 `HarvestLevels`：0 木质、1 石质、2 铁质、3 钻石、4 及以上下界合金原版拒绝标签。玩家看到的名称保留 Antique 名称。

弓臂需要 `Bow` 与 `Head`，弩身需要 `Handle` 与 `Extra`。使用弓、弩或弹药特有材料时，提供对应属性后才会生成可用部件。

## 浇筑材料

当流体已经由当前模组或其他模组注册时，可在启动脚本中调用：

```js
TinkerContent.castingMaterial(material, 'kubejs:molten_amethyst')
```

它为该材料的可用部件加入现有金属铸模、一次性黏土铸模和部件重熔配方。流体必须真实存在；本接口不创建流体或额外的金属原料。自定义合金、燃料或更复杂的浇筑可继续使用公开的 `SmelteryRecipes` 配方目录。

## Java 词条

继承 `AbstractTraits` 或 `LeveledTraits`，通过 `TinkerContent.registerTrait` 注册；使用带命名空间的 identifier 避免冲突。可覆盖：

| 回调 | 用途与约束 |
| --- | --- |
| `modifyStats` | 组装/强化重算，修改本次 `ToolStats`；必须确定且无世界副作用 |
| `modifyDurability` | 最终基础耐久调整 |
| `onToolDamage` / `onToolHeal` | 逐步修改耗损或修复量；材料修复时实体为 null |
| `onRepair` | 仅修改预览副本的词条状态 |
| `miningSpeed` | 双端采掘速度，只读工具与世界 |
| `beforeBlockBreak` / `afterBlockBreak` | 玩家破坏前检查、成功后的效果 |
| `modifyAttack` / `afterHit` | 直接工具攻击与带工具数据的投射物攻击 |
| `afterDamage` | 原生伤害结算后，可读取护甲与吸收处理后的实际伤害 |
| `onBlockDrops` | 修改原生掉落事件 |
| `onDamageTaken` | 持有或使用工具时的防御效果 |
| `onProjectileLaunch` / `onProjectileTick` | 原生箭发射参数、自定义工具投射物更新 |
| `inventoryTick` | 服务端独立词条状态更新 |

`ToolTraits.updateTraitData` 会复制并写回指定词条状态，保留其他词条及未知数据。不能把冷却或计数器保存在共享词条定义对象上。词条名与说明使用 `traits.<identifier>.name`、`traits.<identifier>.description`。

单部位词条通过 `trait(material, position, id, level)` 配置。null 为默认列表；多属性部件合并显式部位列表，全部缺失时才回退到默认列表。普通词条取最高等级；`LeveledTraits` 默认按不同初始强度各贡献一次。

## 强化与工具定义

`TinkerContent.registerModifier(new ToolModifiers.Definition(id, maxLevel, unitsPerLevel, ingredient, modifyStats))` 可添加自己的强化。ingredient 是 `Predicate<ItemStack>`，modifyStats 是 `BiConsumer<ToolStats, Integer>`，第二个参数是累计投入数量。预览与提交会重复计算，回调不能扣物品、随机取值或修改世界。

`ToolDefinition`、`ToolBuilder.assemble` 与 `AntiqueTool` 可供 Java 扩展共用材料计算、组件、耐久及词条。新物品需在物品注册前由自己的 DeferredRegister 注册；`AntiqueParts.register` 和 `AntiqueTools.register` 同样只允许在注册表关闭前调用。增加完全不同的工具界面仍需扩展自己的菜单；现有工作台的内置工具模式由 `Mode` 定义。

材料和强化状态仍位于 `tconstruct:tinker_material`、`tconstruct:tinker_traits` 组件。属性重算不替换未知组件。修改内置材料只影响新组装/主动重算的工具，不会在登录时重置已有工具。

## 原生属性与强化原料

`ToolStats.attribute(holder, id, amount, operation, slotGroup)` 添加原生物品属性。Java 的参数使用原生 `Holder<Attribute>`、`AttributeModifier.Operation` 和 `EquipmentSlotGroup`。KubeJS 可用字符串形式：

```js
const ToolModifiers = Java.loadClass('com.gctn.tconstruct.tools.ToolModifiers')
const Items = Java.loadClass('net.minecraft.world.item.Items')
StartupEvents.postInit(event => {
  TinkerContent.registerModifier(new ToolModifiers.Definition(
    'kubejs:long_reach', 1, 1,
    stack => stack.is(Items.AMETHYST_SHARD),
    (stats, units) => TinkerContent.attribute(stats, 'kubejs:long_reach',
      'minecraft:player.block_interaction_range', units, 'ADD_VALUE', 'MAINHAND')
  ))
  TinkerContent.modifierItem('haste', 'kubejs:compressed_redstone', 81)
  TinkerContent.modifierTag('sharpness', 'kubejs:quartz_dusts', 1)
})
```

属性 ID 必须已注册；修饰符 ID 应带命名空间且在同一属性、装备栏组中唯一。操作支持 `ADD_VALUE`、`ADD_MULTIPLIED_BASE`、`ADD_MULTIPLIED_TOTAL`。属性随工具组件保存和同步，在工具重算时重新生成，因此移除强化会同时移除它提供的属性。强化材料的 value 是一件原料贡献的累计投入数量，标签支持重载。

## 晾晒与工作台加工

晾晒使用原生配方管理器，数据包和 KubeJS 均可添加：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'tconstruct:drying',
    ingredient: { item: 'minecraft:apple' },
    result: { id: 'kubejs:dried_apple', count: 1 },
    time: 1200
  })
})
```

`time` 是基础游戏刻数，受 `gadgets.dryingTimeMultiplier` 影响。可选的 `leather: true` 使配方遵守皮革晾晒开关。

`com.gctn.tconstruct.tables.recipe.StationProcessingRecipes.register(id, itemPredicate, planner)` 为非工具物品增加装配台加工。输入槽 0 是待加工物品，槽 1–5 是原料。纯 planner 返回 `Plan(result, int[6], byproducts)` 或 null；第一个消耗数必须为 1。预览不得修改输入或世界，提交会重新验证并统一扣除材料、返还原生容器与附产物；槽位放不下的物品掉落在工作台旁。

扩展自行对普通 `Container` 调用 `Plan.consume` 时，应接收返回的溢出物品列表；只有带世界的工作台方块实体会自动掉落溢出物品。

工作台和晾晒架通过 `tconstruct:table_material` 组件保存外观材料。`tconstruct:table` 配方的 `material` 原料指定哪种输入提供外观，可使用标签兼容扩展木材；参照 `data/tconstruct/recipe/workshop` 的原生示例。

## 斩首与实体熔炼

`config/tconstruct-common.toml` 支持以下列表：

```toml
[tools]
mobHeadDrops = ["minecraft:zombie;true;minecraft:zombie_head", "#kubejs:stone_mobs;false;minecraft:cobblestone;3"]
[smeltery]
entityMelting = ["minecraft:iron_golem;true;tconstruct:iron;18", "#kubejs:stone_mobs;false;tconstruct:stone;50"]
```

首项为实体 ID 或 `#实体类型标签`，第二项控制是否包含实体类的子类；标签本身始终动态匹配。斩首的可选第四项是最大数量，随机产出 1 到该数；多个匹配掉落中随机选择一种，玩家头颅保留玩家资料。空列表禁用配置掉落。`TinkerContent.headDrop(entityPredicate, outputFunction)` 可注册额外 Java 规则。

实体熔炼的最后一项为每两点伤害提供的 mB，配置中后匹配的条目优先；空列表和未匹配实体回退到血液。Java 的 `SmelteryRecipes.ENTITY_MELTING` 规则优先于配置，可继续复用现有熔炼接口。旧实体名 `snowman`、`villager_golem` 等支持别名，建议扩展使用完整现代 ID。

世界生成与树木扩展见 [WORLD.md](WORLD.md)，宝典页面与多方块预览扩展见 [BOOK.md](BOOK.md)。
