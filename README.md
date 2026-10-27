# TinkersNirvana

An early NeoForge port of TinkersAntique for Minecraft 1.21.1. Currently includes
wood/stone pickaxes, three tool parts, a tool station, and a cobalt ore block.
Other listed materials and tool-station modes are still under development.

## Build and install

Use Java 21 and run `gradlew.bat build` (Windows) or `./gradlew build`. Install the
JAR from `build/libs` in a Minecraft 1.21.1 instance with NeoForge 21.1.218.

## Current usage

- Obtain the station and parts from the mod's creative tabs. In pickaxe mode,
  insert a tool rod, pickaxe head, and binding to assemble a pickaxe.
- Hold Shift over a tool for stats, Ctrl for its materials/traits by part, and
  Alt for trait descriptions. Parts also support Alt descriptions.
- In the default station mode, place a damaged pickaxe in the tool input and
  repair ingredients in the other inputs. Only the head material repairs a
  pickaxe. Wood accepts planks, logs, or sticks; stone accepts stone/cobblestone.
  Repairs use the head's material durability, with Cheap's bonus if present.
- In disassembly mode, insert a fully repaired tool in the right slot. Taking
  the tool back cancels the preview; taking the first part commits the disassembly.
- Cobalt ore requires the custom Cobalt harvest tier (Antique's default level 4).
  The currently ported wood/stone heads and vanilla tools cannot harvest it.
  It is available in creative; world generation and higher-tier materials are
  not implemented yet.

Tool stats, traits, and names are stored per stack. Existing tools keep their
saved stats; newly assembled tools use current material definitions. Existing
literal item names created by older versions are not automatically renamed.

See [TRAITS.md](TRAITS.md) for trait extension procedures.
