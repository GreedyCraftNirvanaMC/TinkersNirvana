# Java addon API

[README](../../README.md) · [KubeJS](KUBEJS.md) · [简体中文](../zh-CN/TOOL_API.md)

Target Minecraft 1.21.1, NeoForge and JDK 21. Add the mod JAR as a compile and development runtime dependency, for example `implementation files('libs/TinkersNirvana-1.21.1-1.0.1.jar')`. Declare a required dependency on mod ID `tconstruct` in the addon's `neoforge.mods.toml`. The API in `com.gctn.tconstruct.api` works independently of KubeJS.

Development dependency example.

```groovy
dependencies {
    implementation files('libs/TinkersNirvana-1.21.1-1.0.1.jar')
}
```

In addition to the addon's own mod declaration, add this block to `META-INF/neoforge.mods.toml`.

```toml
[[dependencies.exampleaddon]]
modId = "tconstruct"
type = "required"
versionRange = "[1.21.1-1.0.1,)"
ordering = "AFTER"
side = "BOTH"
```

## Register materials and traits

Register through `FMLCommonSetupEvent.enqueueWork`, after items and fluids exist. Register traits before installing materials that reference them.

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
                    .displayName(Component.literal("Reinforced Edge"))
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

`MaterialDefinition` and `TraitDefinition` share validation and dispatch with KubeJS. See [KUBEJS.md](KUBEJS.md) for part-stat arguments, callback contexts, stacking and translation keys.

## Modify and remove content

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

Passing a previous definition to the constructor inherits its behavior. Use `clearHandler`, `replaceHandler`, `appendHandler` or `clearHandlers` to edit hooks. `clearHandler("rules")` disables builtin special rules outside callbacks. Complete registration and edits during startup. KubeJS modification events run after these Java registrations.

Tools save trait IDs and independent state and resolve current definitions at runtime. Removed traits become inactive while their saved records remain. Server inventory ticks rebuild cached tool properties after definition changes. Call `TinkerTraits.definitionsChanged()` when directly editing exposed definition fields.

## Implement custom Java behavior

Extend `AbstractTraits` or `LeveledTraits`, implement the needed `ITraits` callbacks, and register through `TinkerTraits.registerTrait`. Leveled traits add distinct initial strengths by default. Override `stacksRepeatedLevels()` to allow repeated strengths to stack. Store per-tool timers in the tool's trait CompoundTag.

`ToolStats.attribute(holder, id, amount, operation, slotGroup)` adds native item attributes. Keep stat and repair-preview calculations deterministic. Use actual combat, block or server tick hooks for world mutations.

## Existing APIs

`TinkerContent` retains material ingredients, stats, traits, modifier ingredients, native attributes, head drops and casting entry points. `castingMaterial(material, fluidId)` binds an existing fluid; the server installs the derived rules at startup.

Custom modifiers use `ToolModifiers.Definition` and `ToolModifiers.register`. Register their inputs through `TinkerContent.modifierItem` or `modifierTag`. Native drying recipes use type `tconstruct:drying` with `ingredient`, `result` and `time`. Reuse existing native recipe and component workflows for additional integrations.
