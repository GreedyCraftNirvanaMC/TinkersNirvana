# Manual tool-system playtest

[README](../../README.md) · [简体中文](../zh-CN/PLAYTEST_TOOLS.md)

Use `build/libs/TinkersNirvana-1.21.1-1.0.4.jar` with Minecraft 1.21.1 and NeoForge 21.1.218. These scenarios require a game client.

| Scenario | Action | Expected result |
| --- | --- | --- |
| Parts and basic assembly | Craft patterns and a station. Use and sneak-use to select parts, then make a rod, blade, and guard and assemble a broadsword. | The handle is lowest, the guard is in the middle, and the blade is highest. The pattern remains and ingredients are consumed only when taking the correctly named and textured output. |
| Advanced tools | Try the hammer, cleaver, longbow, and bolts at both station types. | Advanced modes are available only at the Tool Forge and accept parts at their illustrated slots. |
| Rejection and cancellation | Insert wrong parts, incompatible mixed materials, or too few ingredients. Change modes or close the screen after previewing. | Invalid inputs produce no result. Cancellation neither consumes ingredients nor duplicates tools. |
| Full batches | Make two shards from a plank. Take the result with left-click, right-click, and Shift-click. Repeat with a full inventory and with room for one item. | Each successful pickup gives the full batch. A full inventory prevents consumption. Excess from a partial fit drops to the player. |
| Modifiers and repair | Repair a damaged tool, apply diamond, redstone, and quartz, then try another upgrade after using all slots. | Stats, durability, progress, and slot counts update. Upgrades without a free slot are rejected. |
| Conflicts and embossing | Combine incompatible Silk Touch and Luck, try embossing twice, and apply a sharpening kit with flint. | Conflicts and repeat embossing are rejected. Sharpening changes the harvest tier. |
| Disassembly and cleanup | Preview disassembly on an undamaged tool, cancel, then repeat and take the first part. Break the station before and after taking a part. | Cancellation returns one original tool. After committing, only parts remain. Breaking the station never drops both the source tool and its preview parts. |
| Harvesting | Test area mining with a hammer or excavator, sneaking for one block, connected logs, both scythes, and shearing sheep. | Only suitable blocks are processed. Mature crops replant from seed, and block entities and unbreakable blocks remain. |
| Melee actions | Test the longsword lunge, rapier backstep, charged frying pan, Battlesign blocking and reflection from front versus rear, and cleaver offhand restrictions. | Motion, knockback, attack speed, direction checks, and durability behave correctly. Broken tools do not block. |
| Material traits | Try wood Ecological, stone traits, lead poison, steel bleeding, cobalt momentum, Manyullyn Insatiable, and sponge Softy. | Effects trigger and cooldowns persist per item. Softy deals no damage and supplies Silk Touch. |
| Ranged tools | Fire vanilla, tipped, and custom arrows. Interrupt a draw, cancel and complete a crossbow reload, relog, and recover a thrown shuriken. | Animations show stages, cancellation consumes no ammunition, loading persists, each shot consumes once, and recovery does not duplicate a stack. |
| Ammunition boundaries | Fire the final shot and recover matching, nonmatching, and split arrows. | Empty ammunition can be repaired. Only matching materials and modifiers refill it. Extra split arrows cannot be recovered. |
| Soulbound and moss | Die in survival. Use moss on a bookshelf below and at five XP levels, store XP, and wait for repairs. | A soulbound tool returns without a ground copy. Moss respects the XP threshold and cost, and repairs occur gradually. |
| Visuals, audio, and sync | Inspect tool layers, damaged states, bow/crossbow states, and Shift/Alt tooltips. Listen to frying pan, Softy, charging, discharge, and reload sounds. Interact on a server. | Textures, materials, text, and sounds are present. Item state and workshop interactions synchronize. |

If a protection or event-cancellation mod is available, reject area harvesting, light-patch placement, and projectile launch. A cancelled native event should prevent the affected block break, light placement, or deduction of ammunition that was never fired.
