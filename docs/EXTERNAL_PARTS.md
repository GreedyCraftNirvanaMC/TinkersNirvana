# Register an existing addon part

`AntiqueParts.registerExternal` adds an already registered item to the native stencil table, selected-pattern names, part builder and part chest. Call it during common setup after item registration, consistently on both physical sides. It requires a namespaced ID, positive material-unit cost, stat identifier, item supplier, material predicate, independent-stack factory and localized display-name supplier.

```java
AntiqueParts.registerExternal("example:armor_core", 4, "example:core", MY_PART,
    material -> ArmorRegistry.hasCore(material.identifier),
    material -> MyParts.createCore(material),
    () -> Component.translatable("item.example.armor_core"));
```

The predicate is resolved at processing time, so later material edits can change availability without rebuilding the catalog. The factory must return a stack of the registered item; its result is defensively copied. Empty results reject processing. These registrations do not create items or casts and do not replace native tool statistics. Use an addon-specific stat identifier when the addon manages casting itself.

`Definition.displayName()`, `acceptsMaterial(material)` and `external()` expose the same behavior to callers. The existing four-component `Definition` record constructor and all native part IDs remain compatible. Native color and tooltip handlers leave external items to their owning addon. Patterns retain their normal component encoding, with the namespaced external ID in the `Pattern` field.

The Armory integration verifies all seven external armor parts through the actual native part-builder planner, including unchanged preview inputs, exact costs and retained patterns. Native pickaxe-head processing is also checked.
