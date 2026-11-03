"""Translate Antique's two village components into native 1.21.1 jigsaw templates.

Layouts: world/village/ComponentToolWorkshop.java and ComponentSmeltery.java.
This writes NBT geometry and loot data; all block textures come from the game/mod.
"""
import gzip
import json
import struct
from dataclasses import dataclass
from pathlib import Path

root = Path(__file__).resolve().parents[1] / "src/main/resources"


@dataclass
class Tag:
    kind: int
    value: object


def typed(value):
    if isinstance(value, Tag):
        return value
    return Tag({int: 3, float: 6, str: 8, list: 9, dict: 10}[type(value)], value)


def payload(value):
    tag = typed(value)
    if tag.kind in [1, 3, 6]:
        return struct.pack({1: ">b", 3: ">i", 6: ">d"}[tag.kind], tag.value)
    if tag.kind == 8:
        data = tag.value.encode("utf-8")
        return struct.pack(">H", len(data)) + data
    if tag.kind == 9:
        rows = [typed(row) for row in tag.value]
        kind = rows[0].kind if rows else 10
        assert all(row.kind == kind for row in rows)
        return struct.pack(">bi", kind, len(rows)) + b"".join(payload(row) for row in rows)
    if tag.kind == 10:
        return b"".join(struct.pack(">b", typed(row).kind) + payload(name) + payload(row) for name, row in tag.value.items()) + b"\0"
    raise ValueError(tag.kind)


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


class Template:
    def __init__(self, size):
        self.size, self.blocks, self.entities = size, {}, []
        self.fill((0, 0, 0), tuple(value - 1 for value in size), "minecraft:air")

    def set(self, x, y, z, name, properties=None, nbt=None):
        state = {"Name": name, **({"Properties": properties} if properties else {})}
        self.blocks[x, y, z] = (state, nbt)

    def fill(self, start, end, name, properties=None):
        for x in range(start[0], end[0] + 1):
            for y in range(start[1], end[1] + 1):
                for z in range(start[2], end[2] + 1):
                    self.set(x, y, z, name, properties)

    def jigsaw(self, x, y, z, biome, villager=False, final_state="minecraft:air"):
        self.set(x, y, z, "minecraft:jigsaw", {"orientation": "up_north" if villager else "north_up"}, {
            "id": "minecraft:jigsaw", "name": "minecraft:bottom" if villager else "minecraft:building_entrance",
            "target": "minecraft:bottom" if villager else "minecraft:building_entrance",
            "pool": f"minecraft:village/{biome}/" + ("villagers" if villager else "streets"),
            "joint": "rollable" if villager else "aligned", "final_state": final_state, "selection_priority": 0, "placement_priority": 0})

    def save(self, name):
        palette, blocks = [], []
        for pos, (state, nbt) in sorted(self.blocks.items(), key=lambda row: (row[0][1], row[0][2], row[0][0])):
            if state not in palette:
                palette.append(state)
            blocks.append({"pos": list(pos), "state": palette.index(state), **({"nbt": nbt} if nbt else {})})
        data = {"DataVersion": 3955, "size": list(self.size), "palette": palette, "blocks": blocks, "entities": self.entities}
        output = root / f"data/tconstruct/structure/village/{name}.nbt"
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(gzip.compress(b"\x0a\0\0" + payload(data), mtime=0))


themes = {
    "plains": ("oak", "oak_log", "cobblestone", "stone_bricks"),
    "desert": ("jungle", "cut_sandstone", "sandstone", "cut_sandstone"),
    "savanna": ("acacia", "acacia_log", "cobblestone", "stone_bricks"),
    "snowy": ("spruce", "stripped_spruce_log", "stone_bricks", "stone_bricks"),
    "taiga": ("spruce", "spruce_log", "mossy_cobblestone", "mossy_stone_bricks"),
}
for biome, (wood, log, cobble, bricks) in themes.items():
    planks = "minecraft:smooth_sandstone" if biome == "desert" else f"minecraft:{wood}_planks"
    log, cobble, bricks = ["minecraft:" + value for value in [log, cobble, bricks]]
    table = Template((7, 6, 7))
    table.fill((0, 0, 0), (6, 0, 6), cobble)
    table.fill((0, 5, 0), (6, 5, 6), f"minecraft:{wood}_fence")
    table.fill((1, 0, 1), (5, 0, 5), planks)
    table.fill((2, 0, 2), (4, 0, 4), "minecraft:white_wool")
    for x in [0, 6]:
        for z in [0, 6]:
            table.fill((x, 1, z), (x, 4, z), log)
    for y, block in [(1, planks), (3, planks), (4, log)]:
        for side in [0, 6]:
            table.fill((side, y, 1), (side, y, 5), block)
            table.fill((1, y, side), (5, y, side), block)
    table.fill((1, 4, 1), (5, 4, 5), planks)
    for side in [0, 6]:
        for pos in range(1, 6):
            block = planks if pos == 3 else "minecraft:glass_pane"
            table.set(side, 2, pos, block)
            table.set(pos, 2, side, block)
    table.set(2, 2, 0, planks)
    table.set(4, 2, 0, planks)
    for y, half in [(1, "lower"), (2, "upper")]:
        table.set(3, y, 0, f"minecraft:{wood}_door", {"facing": "north", "half": half, "hinge": "left", "open": "false", "powered": "false"})
    for y in range(1, 5):
        table.set(3, y, 5, "minecraft:ladder", {"facing": "north", "waterlogged": "false"})
    for z, kind in [(1, "toolstation"), (2, "pattern_chest"), (3, "part_builder"), (4, "crafting_station"), (5, "stencil_table")]:
        nbt = {"id": "tconstruct:toolstation_blockentity" if kind == "toolstation" else "tconstruct:workshop"}
        if kind in ["pattern_chest", "crafting_station"]:
            nbt["LootTable"] = "tconstruct:chests/workshop_" + ("patterns" if kind == "pattern_chest" else "parts")
        table.set(1, 1, z, "tconstruct:" + kind, {"facing": "east"}, nbt)
    table.set(4, 1, 5, "minecraft:chest", {"facing": "north", "type": "single", "waterlogged": "false"}, {"id": "minecraft:chest", "LootTable": "tconstruct:chests/workshop_parts"})
    table.set(5, 1, 5, "minecraft:piston", {"facing": "north", "extended": "false"})
    table.jigsaw(3, 0, 0, biome, final_state=f"minecraft:{wood}_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]")
    table.jigsaw(3, 0, 3, biome, villager=True, final_state="minecraft:white_wool")
    table.save(f"{biome}/tool_workshop")

    smeltery = Template((9, 4, 7))
    smeltery.fill((1, 0, 0), (7, 0, 6), bricks)
    smeltery.fill((0, 0, 1), (0, 0, 5), bricks)
    smeltery.fill((8, 0, 1), (8, 0, 5), bricks)
    smeltery.fill((2, 0, 1), (6, 2, 5), "tconstruct:seared_bricks")
    smeltery.fill((3, 1, 2), (5, 2, 4), "minecraft:air")
    for x in [1, 7]:
        for z, block in [(2, "casting_table"), (4, "casting_basin")]:
            smeltery.set(x, 1, z, "tconstruct:" + block, nbt={"id": "tconstruct:smeltery_component"})
    smeltery.jigsaw(4, 0, 0, biome, final_state=bricks)
    smeltery.save(f"{biome}/smeltery")
    write(root / f"data/tconstruct/worldgen/template_pool/village/{biome}/antique.json", {"fallback": "minecraft:empty", "elements": [
        {"weight": weight, "element": {"element_type": "minecraft:single_pool_element", "location": f"tconstruct:village/{biome}/{name}", "processors": "minecraft:empty", "projection": "rigid"}}
        for name, weight in [("tool_workshop", 30), ("smeltery", 9)]]})

# Antique's original part types/materials and roll counts. All item components remain translatable.
parts = "pickaxe_head shovel_head axe_head kama_head binding sword_blade wide_guard hand_guard cross_guard knife_blade pan_head sign_head".split()
materials = "wood stone iron flint cactus bone obsidian netherrack slime paper prismarine blueslime".split()
entries = []
for part in parts:
    for material in materials:
        entries.append({"type": "minecraft:item", "name": "tconstruct:parts/" + part, "weight": 15, "functions": [{"function": "minecraft:set_components", "components": {
            "tconstruct:tinker_material": {"Material": material}, "minecraft:item_name": json.dumps({"translate": "item.tconstruct.material_part", "with": [
                {"translate": f"material.{material}.name"}, {"translate": f"item.tconstruct.{part}.name"}]}, ensure_ascii=False)}}]})
write(root / "data/tconstruct/loot_table/chests/workshop_parts.json", {"type": "minecraft:chest", "pools": [{"rolls": {"type": "minecraft:uniform", "min": 3, "max": 27}, "entries": entries}]})
patterns = "tool_rod pickaxe_head binding shovel_head axe_head sword_blade kama_head arrow_head broad_axe_head large_sword_blade hammer_head excavator_head scythe_head large_plate pan_head sign_head knife_blade tough_tool_rod tough_binding wide_guard hand_guard cross_guard bow_limb bow_string arrow_shaft fletching sharpening_kit shard".split()
entries = [{"type": "minecraft:item", "name": "tconstruct:pattern", "weight": 20, "functions": [
    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 3}},
    {"function": "minecraft:set_components", "components": {"tconstruct:tinker_material": {"Pattern": part}}}]} for part in patterns]
write(root / "data/tconstruct/loot_table/chests/workshop_patterns.json", {"type": "minecraft:chest", "pools": [{"rolls": {"type": "minecraft:uniform", "min": 5, "max": 30}, "entries": entries}]})
print("Generated 10 native village templates, 5 addition pools, and 2 loot tables")
