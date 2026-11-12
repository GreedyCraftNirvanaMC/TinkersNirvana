# Java 附属开发

[首页](../../README.zh-CN.md) · [KubeJS](KUBEJS.md) · [English](../en/TOOL_API.md)

附属使用 Minecraft 1.21.1、NeoForge 和 JDK 21。将本模组 JAR 作为编译及开发运行依赖，例如在附属的 Gradle 工程中使用 `implementation files('libs/TinkersNirvana-1.21.1-1.0.3.jar')`。在附属的 `neoforge.mods.toml` 声明对模组 ID `tconstruct` 的必需依赖。Java 接口位于 `com.gctn.tconstruct.api`，可独立于 KubeJS 使用。

开发工程的依赖示例。

```groovy
dependencies {
    implementation files('libs/TinkersNirvana-1.21.1-1.0.3.jar')
}
```

附属自身的模组声明之外，在 `META-INF/neoforge.mods.toml` 添加依赖块。

```toml
[[dependencies.exampleaddon]]
modId = "tconstruct"
type = "required"
versionRange = "[1.21.1-1.0.3,)"
ordering = "AFTER"
side = "BOTH"
```

## 注册材料与词条

在 `FMLCommonSetupEvent.enqueueWork` 中注册，确保物品与流体已经注册。先注册词条，再安装引用该词条的材料。

```java
package example.addon;

import com.gctn.tconstruct.api.MaterialDefinition;
import com.gctn.tconstruct.api.TraitDefinition;
import com.gctn.tconstruct.tools.traits.TinkerTraits;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("exampleaddon")
public final class ExampleAddon {
    public ExampleAddon(IEventBus bus) {
        bus.addListener(this::setup);
    }

    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TraitDefinition edge = new TraitDefinition("exampleaddon:edge")
                    .displayName(Component.literal("坚固刃口"))
                    .maxLevel(3)
                    .stacking("sum")
                    .appendHandler("modifyStats", ctx -> {
                        ctx.stats.attack += ctx.level * 1.5F;
                    });
            TinkerTraits.registerTrait(edge);

            new MaterialDefinition("exampleaddon:amethyst")
                    .color(0xA77CE3)
                    .craftable(true)
                    .head(1000, 8, 10, 8)
                    .handle(1.1F, 100)
                    .extra(50)
                    .item("minecraft:amethyst_shard", 1)
                    .trait("exampleaddon:edge", 1)
                    .install();
        });
    }
}
```

`MaterialDefinition` 和 `TraitDefinition` 与 KubeJS 共用参数校验和行为分发。完整部位属性、回调字段、等级合并及资源包翻译键见 [KubeJS 文档](KUBEJS.md)。

## 修改与删除

```java
var previous = TinkerTraits.getTrait("tconstruct:fractured");
var replacement = new TraitDefinition(previous.identifier, previous)
        .replaceHandler("modifyStats", ctx -> ctx.stats.attack += 3);
TinkerTraits.replaceTrait(replacement);

TinkerTraits.removeTrait("tconstruct:slimey_blue");

var wood = com.gctn.tconstruct.library.TinkerMaterials.getMaterial("wood");
new MaterialDefinition(wood)
        .removeTrait("tconstruct:ecological")
        .addTrait("exampleaddon:edge", 1)
        .install();
```

将已有定义传给 `TraitDefinition` 构造器可继承原行为。`clearHandler`、`replaceHandler`、`appendHandler` 和 `clearHandlers` 控制对应回调；`clearHandler("rules")` 关闭回调外的内置特殊规则。注册和修改应在启动阶段完成，KubeJS 的修改事件在这些 Java 注册之后执行。

已有工具保存词条 ID 和独立状态，运行时解析当前定义。删除的词条停止生效，其记录留在存档中。定义发生变化后，物品栏中的旧工具会在服务端更新时重算属性。对公开定义字段进行底层直接修改时，调用 `TinkerTraits.definitionsChanged()` 请求重算。

## 自定义 Java 行为

复杂词条可以继承 `AbstractTraits` 或 `LeveledTraits` 并实现 `ITraits` 的回调，再通过 `TinkerTraits.registerTrait` 注册。`LeveledTraits` 默认累加不同初始强度，覆盖 `stacksRepeatedLevels()` 可以让相同强度重复累加。每把工具的计时器等状态应保存在该工具的词条 CompoundTag 中。

`ToolStats.attribute(holder, id, amount, operation, slotGroup)` 添加原生物品属性。`modifyStats` 和修复预览应保持确定性；世界修改使用实际攻击、方块或服务端更新回调。

## 工具升级

`com.gctn.tconstruct.api.ToolUpgradeApi` 提供独立于 KubeJS 的工具升级接口，无需启动注册。经验、升级条件、等级上限和提示由附属控制。工具初始为 0 级；每次成功升级，在材料词条和原有额外槽位之外增加恰好一个强化槽。

```java
import com.gctn.tconstruct.api.ToolUpgradeApi;
import com.gctn.tconstruct.tools.TinkerTools;

// 在逻辑服务端线程执行，先检查附属自己的升级条件。
if (ToolUpgradeApi.tryUpgrade(toolStack)) {
    // 成功后再扣除附属的升级成本。
    int level = ToolUpgradeApi.getLevel(toolStack);
    int freeSlots = ((TinkerTools) toolStack.getItem()).getAvailableModifierSlots(toolStack);
}
```

`isSupported(stack)` 接受非空的 `TinkerTools` 物品，包括附属子类。`getLevel(stack)` 对不支持的物品返回 0。`tryUpgrade(stack)` 原地修改传入的物品；不支持的物品、数量不为 1 的堆叠、达到整数上限或实际总槽位无法恰好增加一个时，返回 false 并保持物品不变。多次调用会连续升级，附属需保证每次升级只调用一次。预览时传入 `stack.copy()`，正式提交时才升级实际物品栏中的工具，并按所属容器接口要求标记物品栏或方块实体已变更。

等级保存在现有材料组件中，随工具存档和复制，修复、更换部件和重算属性时保留；旧工具无需迁移。强化槽数量、强化校验与现有提示立即读取新总数，无需重算属性。接口本身不提供经验获取或等级提示；附属工具子类若自行替换材料数据，应保留原有字段。

## 现有接口

`TinkerContent` 保留材料原料、属性、词条、强化原料、原生属性、斩首掉落和浇铸注册入口。调用 `castingMaterial(material, fluidId)` 将材料绑定到已有流体；服务器启动时会安装对应规则。

自定义强化使用 `ToolModifiers.Definition` 和 `ToolModifiers.register`；强化原料可通过 `TinkerContent.modifierItem`、`modifierTag` 注册。原生晾晒配方使用配方类型 `tconstruct:drying`，包含 `ingredient`、`result`、`time`。其它扩展应复用已有原生配方和组件，保持配方管理器的重载流程。
