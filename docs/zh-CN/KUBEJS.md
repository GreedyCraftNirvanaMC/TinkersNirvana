# KubeJS 材料与词条

[首页](../../README.zh-CN.md) · [Java 附属开发](TOOL_API.md) · [English](../en/KUBEJS.md)

适用于 Minecraft 1.21.1、NeoForge 和 KubeJS 2101.7.2，已验证 build.381。安装 KubeJS 及其声明的依赖后，将以下代码放在 `kubejs/startup_scripts/` 下的 `.js` 文件中。材料和词条使用启动注册，修改脚本后重启客户端和服务器。联机双方应安装一致的启动脚本。

## 注册材料

```js
StartupEvents.registry('tconstruct:material', event => {
    event.create('kubejs:amethyst')
        .color(0xA77CE3)
        .craftable(true)
        .head(1000, 8.0, 10.0, 8)
        .handle(1.1, 100)
        .extra(50)
        .item('minecraft:amethyst_shard', 1)
        .trait('kubejs:reinforced_edge', 1)
})
```

材料与词条定义可以位于不同文件，引用会在注册结束后解析。原料也可以引用同批启动脚本注册的物品。

| 方法 | 参数含义 |
| --- | --- |
| `head(durability, speed, attack, tier)` | 顶端耐久、挖掘速度、攻击伤害、挖掘等级 |
| `handle(multiplier, durability)` | 手柄耐久倍率、耐久加值 |
| `extra(durability)` | 附件耐久加值 |
| `bow(drawSpeed, range, damage)` | 拉弓速度、射程倍率、伤害加值 |
| `bowString(multiplier)` | 弓弦倍率 |
| `shaft(multiplier, bonusAmmo)` | 箭杆弹药倍率、弹药加值 |
| `fletching(accuracy, multiplier)` | 箭羽精度（0–1）、弹药倍率 |
| `craftable(boolean)` | 允许部件加工台制作，默认关闭 |
| `castable(boolean)` | 设置材料可浇铸标记，默认关闭 |
| `castingFluid(id)` | 绑定已注册流体，启用部件熔炼和金属/黏土铸模浇铸 |
| `item(id, value)` | 一个原料物品提供多少份材料 |
| `itemTag(id, value)` | 一个物品标签中的原料提供多少份材料 |
| `representativeItem(id)` | 指定宝典代表物品和名称回退物品 |
| `requireItems(...tags)` | 所有指定物品标签均非空时显示材料 |
| `trait(id, level)` | 绑定默认词条 |
| `partTrait(stat, id, level)` | 绑定指定部位词条 |
| `headTrait/handleTrait/extraTrait/bowTrait(id, level)` | 指定部位绑定的便捷写法 |

`partTrait` 的部位名称为 `Head`、`Handle`、`Extra`、`Bow`、`BowString`、`Shaft`、`Fletching`。显式部位词条列表会覆盖该部位的默认列表。

## 创建词条

```js
StartupEvents.registry('tconstruct:trait', event => {
    event.create('kubejs:reinforced_edge')
        .displayName('坚固刃口')
        .description('提高攻击伤害，减少耐久消耗')
        .color(0xA77CE3)
        .maxLevel(3)
        .priority(100)
        .stacking('sum')
        .modifyStats(ctx => {
            ctx.stats.attack += ctx.level * 1.5
        })
        .onToolDamage(ctx => {
            ctx.amount = Math.max(0, ctx.amount - ctx.level)
        })
        .inventoryTick(ctx => {
            if (ctx.selected) {
                ctx.data.putInt('SelectedTicks', ctx.data.getInt('SelectedTicks') + 1)
            }
        })
})
```

默认最大等级为 1，优先级为 100，等级合并方式为 `max`。优先级高的词条先执行，同优先级按内部 ID 排序。

| `stacking` | 合并方式 |
| --- | --- |
| `max` | 保留最高等级 |
| `sum` | 每个部件贡献都累加 |
| `distinct` | 相同初始强度只贡献一次，不同强度累加 |

等级合并用于装配及部件合并；已有工具保留保存的等级记录。结果限制在 `maxLevel` 内。内置词条修改时默认沿用原合并方式。

## 修改与删除词条

```js
TinkerEvents.modifyTraits(event => {
    event.modify('tconstruct:fractured', trait => {
        trait.color(0xFFAA55)
        trait.replaceHandler('modifyStats', ctx => {
            ctx.stats.attack += 3
        })
        trait.appendHandler('onToolDamage', ctx => {
            ctx.amount = Math.max(0, ctx.amount - 1)
        })
    })

    event.remove('tconstruct:slimey_blue')
})
```

`replaceHandler(name, callback)` 替换一个触发点，`appendHandler(name, callback)` 在已有行为之后追加，`clearHandler(name)` 清除该触发点，`clearHandlers()` 清除所有行为。

部分内置词条还包含回调外的特殊规则，例如 `squeaky` 的精准采集和强化兼容限制、`splitting` 的额外弹体。用 `clearHandler('rules')` 一并关闭这些原生特殊规则。此项不接受追加回调；使用对应生命周期回调实现自己的效果。

删除后，材料绑定和工具运行时会跳过该词条。旧工具保留原词条数据，重新注册同 ID 后可以恢复。已有工具在服务端物品栏更新时会重算缓存属性。修改最大等级会限制生效等级，保存的原等级仍可供恢复。

## 修改材料

```js
TinkerEvents.modifyMaterials(event => {
    event.modify('tconstruct:wood', material => {
        material.removeTrait('tconstruct:ecological')
        material.addTrait('kubejs:reinforced_edge', 1)
        material.head(100, 3, 1, 1)
    })
})
```

修改器支持材料注册表中的方法，以及 `addTrait`、`removeTrait` 和 `clearTraits`。材料词条绑定变更用于后续装配和部件更换。已有工具仍保留其自身词条记录，定义的修改和删除会作用于这些记录。内置材料可用 `wood` 或 `tconstruct:wood` 查询，存档沿用既有 ID。

## 回调上下文

所有回调都有 `tool` 和数值型 `level`。其余字段按触发点提供；未列出的字段可能为空。回调通过修改上下文字段给出结果，无需返回数值。

| 回调 | 可用字段及结果 |
| --- | --- |
| `modifyModifierSlots` | `originalAmount`、可修改的材料额外强化槽 `amount` |
| `onExperienceDrop` | `entity`、`event`（LivingExperienceDropEvent） |
| `onProjectileHitBlock` | `projectile`、`pos`、`event`（BlockHitResult） |
| `modifyStats` | `stats`，直接修改计算结果 |
| `modifyAttack` | `attacker`、`target`、`originalDamage`、可修改的 `damage` |
| `modifyDurability` | `originalAmount`、可修改的最大耐久 `amount` |
| `onToolDamage/onToolHeal` | `entity`、`originalAmount`、可修改的 `amount` |
| `onRepair` | `amount` 为修复量；操作的是预览工具 |
| `miningSpeed` | `entity`、`event`（BreakSpeed），用 `event.setNewSpeed(...)` 改速度 |
| `beforeBlockBreak` | `entity`、`pos`、`state`、`event`（BreakEvent） |
| `afterBlockBreak` | `world`、`entity`、`pos`、`state`、`effective` |
| `onBlockDrops` | `world`、`pos`、`state`、`event`（BlockDropsEvent） |
| `onDamageTaken` | `entity`、`event`（LivingIncomingDamageEvent） |
| `inventoryTick` | `world`、`entity`、`slot`、`selected`、`data` |
| `afterHit` | `attacker`、`target`，沿用现有命中回调时机 |
| `afterDamage` | `attacker`、`target`、`damage`、`event`（LivingDamageEvent.Post） |
| `onProjectileLaunch/onProjectileTick` | `projectile` |

在 `modifyStats` 中还可用 `ctx.attribute(modifierId, attributeId, amount, operation, slot)` 按字符串 ID 添加原生属性。运算为 `add_value`、`add_multiplied_base`、`add_multiplied_total`，槽位如 `mainhand`、`offhand`、`hand`。

`stats` 包含 `durability`、`harvestLevel`、`miningSpeed`、`attack`、`attackSpeed`、`drawSpeed`、`range`、`projectileDamage`、`accuracy` 等字段。

`modifyStats`、耐久/修复计算和预览可能反复执行；保持确定性，世界交互放在实际事件中。`miningSpeed` 和投射物回调可能在双端执行，投射物上下文提供 `world`；修改世界前检查 `ctx.world.isClientSide`，仅在服务端执行。`inventoryTick` 仅在服务端执行，其 `data` 为当前工具当前词条的独立 CompoundTag，修改后自动写回。其它回调的临时上下文不会自动保存。回调异常会记录词条 ID 和触发点，并停用该回调至重启；可回滚的数值、属性和独立数据会恢复到该回调之前。

## 资源包本地化

资源包使用如下路径。

```text
assets/kubejs/lang/zh_cn.json
assets/kubejs/lang/en_us.json
```

`zh_cn.json` 示例。

```json
{
  "material.kubejs.amethyst.name": "紫水晶",
  "traits.kubejs.reinforced_edge.name": "坚固刃口",
  "traits.kubejs.reinforced_edge.description": "提高攻击伤害，减少耐久消耗"
}
```

自定义 ID 中的冒号和斜线在默认翻译键中替换为点。材料与词条支持 `translationKey(key)`；词条描述另有 `descriptionKey(key)`。既有内置翻译键继续有效。

材料名称依次使用当前语言的材料翻译、`displayName`、`representativeItem` 的物品名、第一个有效 `item` 的物品名、材料 ID。物品名保留为可翻译组件。标签原料需要通过 `representativeItem` 指定名称回退物品。词条名称使用翻译、默认名称、ID；描述使用翻译、默认描述，缺省为空。

## 其它接口

现有 `TinkerContent` Java API 可供旧脚本继续调用，Java 附属开发见 [TOOL_API.md](TOOL_API.md)。晾晒等原生配方可通过 `ServerEvents.recipes` 和数据包添加。当前材料的 `castingFluid` 会根据部件用量生成对应熔炼、金属铸模和黏土铸模浇铸规则。
